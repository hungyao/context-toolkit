package context.arch.subscriber;

import java.util.concurrent.ConcurrentHashMap;

import context.arch.comm.DataObject;
import context.arch.comm.DataObjects;
import context.arch.comm.language.MessageHandler;
import context.arch.widget.Widget;

/**
 * This class maintains a list of subscribers, allows additions, removals and
 * updates to individual subscribers.
 *
 * Agathe: I have changed Subscribers and Subscriber to allow the addition of
 * the DiscovererSubscriber class. Subscriber and DiscovererSubscriber
 * implement an interface handled by Subscribers.
 *
 * Agathe: modify restart subscription
 * 
 * TODO: consider using MySQL for persistence of subscription instead of a local flat file --Brian
 * TODO: anyway, the storage files are not strict XML because they are delimited with ENTRY_STRING
 *
 * @author Anind, Agathe, Brian Y. Lim
 * @see context.arch.subscriber.Subscriber
 */
public class Subscribers extends ConcurrentHashMap<String, AbstractSubscriber> {

	public static final String WIDGET_SUBSCRIPTIONS_DIR = "widget-subscriptions/";

	private static final long serialVersionUID = 4321311291016308706L;
	
	/** Debug flag */
	public static boolean DEBUG = false;
	/**
	 * Tags written in the log file
	 */
	@SuppressWarnings("unused")
	private final static String ENTRY_STRING = "entry:";

	/*
	 * To be combined like "addSubR:", "updateSubD", etc
	 * See #writeLog
	 */
	private final static String ADD = "add";
	private final static String REMOVE = "remove";
	private final static String UPDATE = "update";
	@SuppressWarnings("unused")
	private final static String SUB_REG = "SubR";
	@SuppressWarnings("unused")
	private final static String SUB_DISCO = "SubD";

	/**
	 * Tag used in messages
	 */
	public static final String SUBSCRIBERS = "subscribers";

	/** */
	@SuppressWarnings("unused")
	private MessageHandler msgHandler;
	/** */
	@SuppressWarnings("unused")
	private String filename;

	/** The id of the component */
	private String baseObjectId;

	/**
	 * Basic constructor that takes an object that implements the MessageHandler
	 * interface and an id to create a logfile name from.
	 */
	public Subscribers(MessageHandler msgHandler, String id) {
		super();
		
		this.msgHandler = msgHandler;
		baseObjectId = id;
		
		// The filename for the log file with directory
		filename = WIDGET_SUBSCRIPTIONS_DIR + id + "-subscription.xml";
		
		restartSubscriptions();
	}

	/**
	 * Adds a subscriber to the subscriber list
	 *
	 * @param sub Subscriber object to add
	 */
	public void add(Subscriber sub) {
		addSubscriber(sub, true);
	}

	public void add(DiscovererSubscriber sub) {
		addSubscriber(sub, true);
	}

	public void addSubscriber(AbstractSubscriber sub, boolean log) {
		if (contains(sub)) { return; } // ignore if already in
		
		// Updates the unique subscription id
		sub.setSubscriptionId(
				sub.getBaseObjectId() + Widget.SPACER + 
				baseObjectId + Widget.SPACER + 
				sub.getSubscriptionCallback() + Widget.SPACER + 
				this.getCounterForUniqueIds());
		
		put(sub.getSubscriptionId(), sub);
		
		writeLog(log, ADD, sub);
	}

	/**
	 * Removes a subscriber from the subscriber list
	 *
	 * @param sub Subscriber object to remove
	 * @return whether the removal was successful or not
	 */
	public synchronized boolean removeSubscriber(AbstractSubscriber sub) {
		return removeSubscriber(sub, true);
	}

	/**
	 * Removes a subscriber from the subscriber list
	 *
	 * @param sub Subscriber object to remove
	 * @param log Whether to log the subscribe or not
	 * @return whether the removal was successful or not
	 */
	public boolean removeSubscriber(AbstractSubscriber sub, boolean log) {
		writeLog(log, REMOVE, sub);
		return super.remove(sub.getSubscriptionId()) != null;
	}

	/** Remove an AbstractSubscriber
	 *
	 * @param subToRemove
	 */
	public boolean remove(AbstractSubscriber subToRemove) {
		String subToRemoveId = subToRemove.getSubscriptionId();
		AbstractSubscriber sub = null;
		
		for (AbstractSubscriber asub : this.values()) {
			if (subToRemoveId.equalsIgnoreCase(asub.getSubscriptionId())) {
				sub = asub;
				break;
			}
		}
		if (sub != null) {
			this.remove(sub);
			return true;
		}
		return false;
		
		// is equalsIgnoreCase really that important? Otherwise, can use the standard remove method
		// super.remove(subToRemove);
	}

	/**
	 * Removes a subscriber from the subscriber list
	 *
	 * @param sub Subscriber object to remove
	 * @return whether the removal was successful or not
	 */
	public synchronized boolean removeSubscriber(String subId) {
		return removeSubscriber(subId, true);
	}

	/**
	 * Removes a subscriber from the subscriber list
	 *
	 * @param sub Subscriber object to remove
	 * @param log Whether to log the subscribe or not
	 * @return whether the removal was successful or not
	 */
	public synchronized boolean removeSubscriber(String subId, boolean log) {
		AbstractSubscriber sub = super.remove(subId);
		writeLog(log, REMOVE, sub);
		return sub != null;
	}

	/**
	 * Updates a subscriber in the subscriber list.  The subscriber name is
	 * retrieved from the subscriber object and the old subscriber entry with
	 * this name is replaced by the given one.
	 *
	 * @param sub Subscriber object to update
	 */
	public synchronized void updateSubscriber(AbstractSubscriber sub) {
		updateSubscriber(sub,true);
	}

	/**
	 * Updates a subscriber in the subscriber list.  The subscriber name is
	 * retrieved from the subscriber object and the old subscriber entry with
	 * this name is replaced by the given one.
	 *
	 * @param sub Subscriber object to update
	 * @param log Whether to log the subscribe or not
	 */
	public synchronized void updateSubscriber(AbstractSubscriber sub, boolean log) {
		put(sub.getSubscriptionId(), sub);
		writeLog(log, UPDATE, sub);
	}

	/**
	 * Converts to a DataObject.
	 *
	 * @return
	 */
	public DataObject toDataObject() {
		DataObjects v = new DataObjects();
		for (AbstractSubscriber s : this.values()) {
			v.add(s.toDataObject());
		}
		return new DataObject(SUBSCRIBERS,v);
	}

	/** 
	 * Return the number to use
	 * @return
	 */
	private long getCounterForUniqueIds() {
//		return counterForUniqueIds++; // this would not have been unique if the Subscribers was reset
		return System.currentTimeMillis();
	}

	/**
	 * This method reads in the subscription log, restarts all the subscriptions
	 * that were valid at the time of this object being shut down and writes
	 * out the valid subscriptions to the log.  It deletes the old log and
	 * creates a new one, so that it can clear out entries in the log for
	 * corresponding unsubscribes and subscribes.
	 * 
	 * TODO: prone to corruption during prototyping; currently disabled
	 */
	private void restartSubscriptions() {
//		String log = FileUtil.read(filename);
//		
//		/*
//		 * Format of log:
//		 * Text delimited by ENTRY_STRING ,
//		 * followed by a command (e.g. ADD_SUB_REG, REMOVE_SUB_REG),
//		 * followed by XML containing info about a subscriber
//		 */
//		String[] entries = log.split(ENTRY_STRING);
//		
//		for (String entry : entries) {			
//			try { // Test the message code : ADD, REMOVE, UPDATE and creates a Subscriber
//
//				int index = entry.indexOf(">"); // to skip "<?xml version="1.0"?>"
//				String data = entry.substring(index + 1).trim();
//				if (data.length() == 0) { continue; }
//				
//				DataObject decodedData = msgHandler.decodeData(new StringReader(data));
//								
//				// object based on the log file
//				if (entry.startsWith(ADD + SUB_REG)) {
//					addSubscriber(new Subscriber(decodedData), false);
//				}
//				else if (entry.startsWith(REMOVE + SUB_REG)) {
//					removeSubscriber(new Subscriber(decodedData), false);
//				}
//				else if (entry.startsWith(UPDATE + SUB_REG)) {
//					updateSubscriber(new Subscriber(decodedData), false);
//				}
//				
//				// Discoverer subscribers
//				else if (entry.startsWith(ADD + SUB_DISCO)) {
//					addSubscriber(new DiscovererSubscriber(decodedData), false);
//				}
//				else if (entry.startsWith(REMOVE + SUB_DISCO)) {
//					removeSubscriber(new DiscovererSubscriber(decodedData), false);
//				}
//				else if (entry.startsWith(UPDATE + SUB_DISCO)) {
//					updateSubscriber(new DiscovererSubscriber(decodedData), false);
//				}
//				
//			} catch (DecodeException de) {
//				de.printStackTrace();
//			} catch (InvalidDecoderException ide) {
//				ide.printStackTrace();
//			}
//		}
//
//		// why are we writing to the log when we have just read from it w/o making changes? --Brian
//		for (AbstractSubscriber sub : this.values()) {
//			writeLog(true, ADD, sub);
//		}
	}

	/**
	 * This private method writes an entry to the logfile.
	 * TODO: prone to corruption during prototyping; currently disabled
	 *
	 * @param header Header of the entry to append to the logfile
	 * @param sub Subscriber information to put in the entry
	 */
	private void writeLog(boolean log, String action, AbstractSubscriber sub) {
		if (!log) { return; }
		
//		String header = ENTRY_STRING + action +
//		(sub instanceof DiscovererSubscriber ? 
//				SUB_DISCO : // discoverer subscriber
//				SUB_REG) +  // widget subscriber
//		":";
//		
//		PrintWriter out = null;
//		try {
//			out = new PrintWriter(FileUtil.getWriter(filename, true));
//			out.println(header + msgHandler.encodeData(sub.toDataObject()));
//			out.flush();
//		} catch (Exception e) {
//			e.printStackTrace();
//		} finally {
//			FileUtil.closeWriter(out);
//		}
	}

	/** Set the BaseObject id used to attribute the unique id for subscribers
	 * @param id
	 */
	public void setBaseObjectId(String id){
		this.baseObjectId = id;
	}

}
