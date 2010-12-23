package context.arch.widget;

import java.io.FileReader;
import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Enumeration;

import context.arch.storage.Attribute;
import context.arch.storage.Attributes;

import weka.core.Instance;
import weka.core.Instances;

/**
 * Extends Widget to provide capabilities for reading non-constant Attributes from a WEKA .arff file.
 * It assumes that the last feature is the class feature and does NOT add it.
 * 
 * @author Brian Y. Lim
 *
 */
public class ClassifierWidget extends Widget {

	/**
	 * Names of features from the WEKA .arff file
	 */
	protected String[] FEATURES;

	/**
	 * 
	 * @param id
	 * @param widgetClassName
	 * @param arffFilename file name of .arff file containing instances header
	 */
	@SuppressWarnings("unchecked")
	public ClassifierWidget(String id, String widgetClassName, String arffFilename) {
		super(id, widgetClassName);
		
		/*
		 * Populate feature names from Weka ARFF file
		 */
		try {
			Instances dataset = new Instances(new FileReader(arffFilename));
			Enumeration<weka.core.Attribute> attrs = dataset.enumerateAttributes();
			FEATURES = new String[dataset.numAttributes() - 1]; // skip last attribute which is the class attribute
						
			for (int i = 0; i < FEATURES.length; i++) {
				weka.core.Attribute attr = attrs.nextElement();
				String FEATURE = attr.name();
				
				FEATURES[i] = FEATURE; // store into array
				
				// add to non-constant attributes
				addAttribute(Attribute.instance(
						FEATURE, 
						wekaTypeToClass(attr.type()))); 
			}
		} catch (Exception e) { e.printStackTrace(); }
	}
	
	/**
	 * Utility method to convert a type from WEKA format to java class.
	 * @param type
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Object & Comparable<? super T>> Class<T> wekaTypeToClass(int type) {
		switch (type) {
		case weka.core.Attribute.NUMERIC:
			return (Class<T>) Double.class;
		case weka.core.Attribute.NOMINAL:
			return (Class<T>) String.class; // TODO: would there be a better substitute class?
		case weka.core.Attribute.STRING:
			return (Class<T>) String.class;
		case weka.core.Attribute.DATE:
			return (Class<T>) Date.class;
		default:
			return (Class<T>) String.class;
		}
	}
	
	/**
	 * Date format to parse date format used by WEKA: yyyy-MM-dd'T'HH:mm:ss
	 */
	public static final DateFormat wekaDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"); 
	
	/**
	 * Utility method to extract value of a weka.core.Attribute from an Instance and cast to the appropriate object class.
	 * @param instance
	 * @param attribute
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Comparable<? super T>> T getValue(Instance instance, weka.core.Attribute attribute) {
		int type = attribute.type();
		
		switch (type) {
		case weka.core.Attribute.NUMERIC:
			return (T) new Double(instance.value(attribute));
		case weka.core.Attribute.NOMINAL:
			return (T) instance.stringValue(attribute);
		case weka.core.Attribute.STRING:
			return (T) instance.stringValue(attribute);
		case weka.core.Attribute.DATE:
			String strValue = instance.stringValue(attribute);
			try {
				return (T) wekaDateFormat.parse(strValue);
			} catch (ParseException e) {
				e.printStackTrace();
				return null; // this should cause a very nasty error to help spot the bug
			}
		default:
			return null;
		}
	}
	
	/**
	 * Convenience method to extract Attributes from a WEKA instance.
	 * It does not include the last weka attribute that is assumed to be the class attribute.
	 * @param instance
	 * @return
	 */
	public static Attributes instanceToAttributes(Instance instance) {
		Attributes atts = new Attributes();
		
		for (int a = 0; a < instance.numAttributes() - 1; a++) { // skip last attribute
			weka.core.Attribute attribute = instance.attribute(a);
			atts.addAttribute(
					attribute.name(), 
					ClassifierWidget.getValue(instance, attribute));
		}
		
		return atts;
	}
	
	/*
	 * Get input feature names 
	 */
	public String[] getFeatureNames() {
		return FEATURES;
	}

}
