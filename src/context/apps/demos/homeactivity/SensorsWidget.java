package context.apps.demos.homeactivity;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import context.arch.storage.Attribute;
import context.arch.storage.Attributes;
import context.arch.widget.SequenceWidget;

public final class SensorsWidget extends SequenceWidget {
	
	public static final String CLASSNAME = SensorsWidget.class.getName();

	public static String[] FEATURES;
	static {
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream("demos/home-hmm/kasteren-jahmm-header.properties"));			
			Integer.parseInt(properties.getProperty("numObservationValues")); // skip line
			
			String[] inputNames =  properties.getProperty("observationVector").split(",");
			FEATURES = new String[inputNames.length];
			for (int i = 0; i < inputNames.length; i++) {
				FEATURES[i] = inputNames[i].trim();
			}			
		} catch (IOException e) {
		}
	}
		
	public SensorsWidget() {
		super(CLASSNAME, CLASSNAME, HomeModel.SEQUENCE_LENGTH);
		super.start(true);
	}

	@Override
	protected void init() {
		//atts.addAttribute(SEQUENCE_LENGTH, Attribute.INT); // init-ed as a constant attribute

		// non-constant attributes
		for (String FEATURE : FEATURES) {
			addAttribute(Attribute.instance(FEATURE, Integer.class));
		}
	}
	
	public static class SensorsData extends WidgetData {
		
		public final int[] featureValues = new int[FEATURES.length];

		public SensorsData(long timestamp) {
			super(SensorsWidget.class.getName(), timestamp);
		}

		@Override
		public Attributes toAttributes() {
			Attributes atts = new Attributes();

			for (int i = 0; i < FEATURES.length; i++) {
				atts.addAttribute(FEATURES[i], featureValues[i]);
			}
			
			return atts;
		}
		
		/**
		 * Similar to #toAttributes but prepends names with T marker
		 * @param t
		 * @return
		 */
		public Attributes toAttributes(int t) {
			Attributes atts = new Attributes();

			for (int i = 0; i < FEATURES.length; i++) {
				atts.addAttribute(SequenceWidget.getTPrepend(t) + FEATURES[i], featureValues[i]);
			}
			
			return atts;
		}
		
	}

}