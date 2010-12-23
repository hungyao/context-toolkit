package context.arch.widget;

import context.arch.storage.Attribute;
import context.arch.storage.AttributeNameValue;
import context.arch.storage.Attributes;

/**
 * Widget that repeats attributes for multiple times depending on sequence length.
 * @author Brian Y. Lim
 *
 */
public abstract class SequenceWidget extends Widget {

	public static final String SEQUENCE_LENGTH = "SEQUENCE_LENGTH"; // tag
	protected int sequenceLength;
	
	public static final String SEQUENCE_MARKER = "__T";

	public SequenceWidget(int port, String id, String widgetClassName, int sequenceLength) {
		super(port, id, widgetClassName);
		this.sequenceLength = sequenceLength;
	}

	public SequenceWidget(int port, String id, String widgetClassName, boolean storageFlag, int sequenceLength) {
		super(port, id, widgetClassName, storageFlag);
		this.sequenceLength = sequenceLength;
	}

	public SequenceWidget(String id, String widgetClassName, int sequenceLength) {
		super(id, widgetClassName);
		this.sequenceLength = sequenceLength;
	}

	public SequenceWidget(String clientClass, String serverClass,
			int serverPort, String encoderClass, String decoderClass,
			boolean storageFlag, String id, String widgetClassName, int sequenceLength) {
		super(clientClass, serverClass, serverPort, encoderClass, decoderClass,
				storageFlag, id, widgetClassName);
		this.sequenceLength = sequenceLength;
	}

	public SequenceWidget(String clientClass, String serverClass,
			int serverPort, String encoderClass, String decoderClass,
			String storageClass, String id, String widgetClassName, int sequenceLength) {
		super(clientClass, serverClass, serverPort, encoderClass, decoderClass,
				storageClass, id, widgetClassName);
		this.sequenceLength = sequenceLength;
	}
	
	public int getSequenceLength() {
		return sequenceLength;
	}
	
	/**
	 * Overridden to replicate attributes to sequence
	 */
	@Override
	protected void initFull() {		
		init();
		
		/*
		 * Attributes added in init(), so replicate them
		 */
		// already includes w/o prepended time step, to represent freshest data, so that attribute naming can match
		nonConstantAttributes.putAll(replicateToSequence(nonConstantAttributes, sequenceLength));
		
		// don't replicate constant attributes

		/*
		 * Add other non-repetitive attributes after replication
		 */
		addAttribute(Attribute.instance(TIMESTAMP, Long.class));
		
		// add sequence length as constant attribute
		addAttribute(AttributeNameValue.instance(SEQUENCE_LENGTH, sequenceLength), true);
		
		setCallbacks(initCallbacks());
//		setServices(initServices());
		setSubscribers();
		getNewOffset();
	}
	
	/**
	 * Prepends sequence index to the names of each attribute.
	 * This creates new attributes because it's part of a replication process,
	 * so there needs to be seperate objects.
	 * @param atts
	 * @return
	 */
	protected static Attributes prependSequenceIndex(Attributes atts, int index) {
		Attributes atts2 = new Attributes();
		for (Attribute<?> att : atts.values()) {
			Attribute<?> att2; // new version with prepended name
			if (att instanceof AttributeNameValue<?>) {
				att2 = att.cloneWithNewName(
						getTPrepend(index) + att.getName() // NAME -> __T#_NAME
						);
			}
			else {
				att2 = att.cloneWithNewName(
						getTPrepend(index) + att.getName() // NAME -> __T#_NAME
						);
			}
			atts2.add(att2);
		}
		return atts2;
	}
	
	public static String getTPrepend(int index) {
		return SEQUENCE_MARKER + index + '_';
	}
	
	/**
	 * Replicates attributes to sequence length.
	 * @param atts
	 * @param sequenceLength
	 * @return
	 */
	protected static Attributes replicateToSequence(Attributes atts, int sequenceLength) {
		Attributes sequenceAtts = new Attributes();
		for (int i = 0; i < sequenceLength; i++) {
			sequenceAtts.putAll(prependSequenceIndex(atts, i));
		}		
		return sequenceAtts;
	}
	
	protected static void stepBackNames(Attributes atts) {
		/*
		 * create a new collection so that it does not get
		 * corrupted with removals of atts with the same names 
		 */		
		Attributes newAtts = new Attributes();
		
		for (Attribute<?> att : atts.values()) {			
			String name = att.getName();
			int markerPos = name.indexOf(SEQUENCE_MARKER);
			
			// SEQUENCE_MARKER not found => not a sequence feature (e.g. timestamp)
			if (markerPos == -1) { continue; } // ignore
			
			int step = -1;
			try {
				String substr = name.substring(markerPos + SEQUENCE_MARKER.length()); // skip past "__T"
				int splitIndex = substr.indexOf("_");
				
				String origName = substr.substring(splitIndex + 1);
				substr = substr.substring(0, splitIndex); // stop before next "_"
				step = Integer.parseInt(substr);
				
				// the new step: 0 if it should be removed, -1 if it didn't have a step count; otherwise the new step index
				if (step == 0) { continue; } // just ignore
				
				// replace with new name where step count is decremented
				String newName = getTPrepend(--step) + origName;
				att = att.cloneWithNewName(newName);
//				System.out.println("name = " + name + ", newName = " + newName + ", att = " + att);
//				System.out.println("atts = " + atts);
//				System.out.println("att.class = " + att.getClass());
				
				/*
				 * Add to attributes if not stepped out of bounds
				 */
				newAtts.add(att);
				
			} catch (NumberFormatException e) { 
				// this should not happen, but may if there is a bug
				e.printStackTrace(); 
			}
		}
		
		/*
		 * Clear original attributes and add new
		 */
		//atts.clear(); // don't clear the original since some attributes are needed to check canHandle (e.g. w/o sequence steps); just replace
		atts.putAll(newAtts);
	}

	/**
	 * Overridden to take attributes data as an incremental input to the widget. It then removes the oldest sequence step of data.
	 * Effectively doing a FIFO operation.
	 */
	@Override
	protected void notify(String event, Attributes attrs) {
//		System.out.println("SequenceWidget.notify attrs 1 = " + attrs); 
		if (attrs == null) { return; }
		
		/*
		 * Shift all previous sequence steps one step back, deleting oldest step
		 */
		stepBackNames(nonConstantAttributes);
//		System.out.println("SequenceWidget.notify nonConstantAttributes = " + nonConstantAttributes);
		
		/*
		 * Set attrs for the newest sequence step
		 */
		attrs = prependSequenceIndex(attrs, sequenceLength - 1);
//		System.out.println("SequenceWidget.notify attrs 2 = " + attrs); 
		
		/*
		 * Add the new attrs
		 */
		nonConstantAttributes.putAll(attrs);
//		System.out.println("notify.nonConstantAttributes = " + nonConstantAttributes);
		super.notify(event, nonConstantAttributes);
	}
	
	/*
	 * Should just use a normal widget with no memory of history
	 */
//	public abstract static class SequenceWidgetData extends WidgetData {
//		
//		protected int sequenceLength;
//
//		public SequenceWidgetData(long timestamp, int sequenceLength) {
//			super(timestamp);
//			this.sequenceLength = sequenceLength;
//		}
//
//		@Override
//		@Deprecated
//		public Attributes toAttributes() {
//			Attributes atts = super.toAttributes();
//
//			// add this parameter as attribute
//			atts.addAttributeNameValue(SEQUENCE_LENGTH, sequenceLength, Attribute.INT);
//
//			for (int t = 0; t < sequenceLength; t++) {
//				String seqIndexMarker = getSequenceIndexMarker(t);
//				Attributes seqAtts = toSequenceAttributes(t, seqIndexMarker);
//				atts.addAll(seqAtts);
//			}
//			
//			return atts;
//		}
//
//		/**
//		 * To be used by subclasses instead of toAttributes()
//		 * @param t time step
//		 * @param seqIndexMarker to prepend attribute name with
//		 * @return
//		 */
//		public abstract Attributes toSequenceAttributes(int t, String seqIndexMarker);
//		
//	}

}
