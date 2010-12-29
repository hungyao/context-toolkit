package context.arch.discoverer.query;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import context.arch.comm.DataObject;
import context.arch.comm.DataObjects;
import context.arch.discoverer.ComponentDescription;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Adapter class to wrap a Weka Classifier to work with the Context Toolkit.
 * This class deals with formatting the classifier and sending/retrieving its information from the Discoverer network.
 * Note that this supports only classification (to nominal strings), not regression.
 * 
 * @author Brian Y. Lim
 *
 */
public class ClassifierWrapper {

	public static final String CLASSIFIER_WRAPPER = "CLASSIFIER_WRAPPER";
	public static final String CLASSIFIER = "CLASSIFIER";
	public static final String DATASET_HEADER = "DATASET_HEADER";

	public static final int CACHE_LIMIT = 10;
	private LinkedHashMap<Instance, String> instanceClassifications = new LRUCache<Instance, String>(CACHE_LIMIT);
	
	private List<String> outcomeValues = new ArrayList<String>();

	protected Classifier classifier;
	protected Instances header;
	protected Attribute classAttribute;
	protected int NUM_ATTRIBUTES;
	
	private String classifierFileName;
	private String headerFileName;
	
	@SuppressWarnings("unchecked")
	public ClassifierWrapper(String classifierFileName, String headerFileName) {
		this.classifierFileName = classifierFileName;
		this.headerFileName = headerFileName;
		
		// extract classifier from serialized file
		this.classifier = loadClassifier(classifierFileName);

		// extract Instances dataset header from serialized file
		this.header = loadDataset(headerFileName);
		
		// extract outcome values
		this.classAttribute = header.classAttribute();
		Enumeration<String> values = classAttribute.enumerateValues();
		while (values.hasMoreElements()) {
			outcomeValues.add(values.nextElement());
		}
		
		NUM_ATTRIBUTES = header.numAttributes();
	}
	
	/**
	 * 
	 * @return name of .model file containing the WEKA classifier model
	 */
	public String getClassifierFileName() {
		return classifierFileName;
	}
	
	/**
	 * 
	 * @return name of .arff file that contains the header information of WEKA attributes for the dataset
	 */
	public String getHeaderFileName() {
		return headerFileName;
	}
	
	/**
	 * 
	 * @return the WEKA classifier
	 */
	public Classifier getClassifier() {
		return classifier;
	}
	
	/**
	 * 
	 * @return a (possibly empty) dataset containing header information of WEKA attributes
	 */
	public Instances getHeader() {
		return header;
	}
	
	/**
	 * 
	 * @return number of possible states (classes) for the outcome.
	 */
	public int numOutcomeValues() {
		return outcomeValues.size();
	}
	
	/**
	 * 
	 * @param index
	 * @return
	 */
	public String getOutcomeValue(int index) {
		return outcomeValues.get(index);
	}
	
	public List<String> getOutcomeValues() {
		return Collections.unmodifiableList(outcomeValues);
	}
	
	public String getClassAttributeName() {
		return classAttribute.name();
	}
	
	/**
	 * 
	 * @param instance
	 * @return null if classification failed or was invalid (e.g. null values in attributes)
	 */
	protected String classify(Instance instance) {
		// return cached result if recently classified
		if (instanceClassifications.containsKey(instance)) {
			return instanceClassifications.get(instance);
		}
		
		try {
			double value = classifier.classifyInstance(instance);
			
			// save label back into instance
			// TODO: not guaranteed to always be stored in other circumstances
			instance.setValue(classAttribute, value);
			
			// for debugging
//			double[] distroForInstance = classifier.distributionForInstance(instance);				
//			System.out.println("ClassifierWrapper.classify distroForInstance");			
//			for (int i = 0; i < distroForInstance.length; i++) {
//				System.out.println("\t " + classAttribute.value(i) + ": " + distroForInstance[i]);
//			}
			
			String strValue = classAttribute.value((int)value);
			
			// cache result
			instanceClassifications.put(instance, strValue);
			
			return strValue;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Checks if widget state can be extracted as an appropriate Instance,
	 * since other widgets are also queried. 
	 * If this fails, then classification would fail and return null.
	 * @param widgetState
	 * @return
	 */
	protected boolean isInstanceExtractable(ComponentDescription widgetState) {
		return true; // TODO: not sure if this needs to be checked
	}

	/**
	 * Assumes that widgetState is validated to extract instance
	 * @param widgetState
	 * @return
	 */
	public String classify(ComponentDescription widgetState) {
		Instance instance = extractInstance(widgetState);
		if (instance == null) { return null; }
		
		String outcomeValue = classify(instance);
		
		// store value back into widgetState
		widgetState.getNonConstantAttributes().addAttribute(classAttribute.name(), outcomeValue);
//		System.out.println("ClassifierWrapper.classifiy stored: " + String.valueOf(Enactor.getAtt(classAttribute.name(), widgetState.getNonConstantAttributes())));
		
		return outcomeValue;
	}
	
	/**
	 * Calls distributionForInstance of the Instance after extracing it from ComponentDescription
	 * @param widgetState
	 * @return
	 */
	public double[] distributionForInstance(ComponentDescription widgetState) {
		Instance instance = extractInstance(widgetState);
		try {
			return classifier.distributionForInstance(instance);
			// TODO: utilize caching!
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Extracts a weka Instance from the attributes of ComponentDescription widget state
	 * @param widgetState of a widget from which to extract instance
	 * @return
	 */
	public Instance extractInstance(ComponentDescription widgetState) {	
		if (!isInstanceExtractable(widgetState)) { return null; }
		
		Instance instance = new DenseInstance(NUM_ATTRIBUTES);
		
		for (int i = 0; i < NUM_ATTRIBUTES; i++) {
			weka.core.Attribute attr = header.attribute(i);

			// add attribute value, depending on type
			if (attr.isNumeric()) {
				double attrVal = widgetState.getAttributeValue(attr.name());
				instance.setValue(attr, attrVal);				
			}
			else { // nominal or string
//				System.out.println("extractInstance attr.name() = " + attr.name());
//				System.out.println("extractInstance widgetState = " + widgetState);
				String attrVal = widgetState.getAttributeValue(attr.name());
				
//				System.out.println("attrVal = " + attrVal);
//				System.out.println("(attrVal != null) = " + (attrVal != null));
//				System.out.println("(!attrVal.equals(\"null\")) = " + (!attrVal.equals("null")));
				
				if (attrVal != null && !attrVal.equals("null")) {
					if (attr.isNumeric()) {
						instance.setValue(attr, Double.parseDouble(attrVal));
					}
					else if (attr.isNominal()) {
						instance.setValue(attr, attrVal);
					}
				}
			}
		}
		
		// set dataset
		instance.setDataset(header);
		
		return instance;
	}
	
	/**
	 * Convert to DataObject
	 * @return
	 */
	public DataObject toDataObject() {
		DataObjects v = new DataObjects();
		v.add(new DataObject(CLASSIFIER, classifierFileName));
		v.add(new DataObject(DATASET_HEADER, headerFileName));
		return new DataObject(CLASSIFIER_WRAPPER, v);
	}
	
	public static ClassifierWrapper fromDataObject(DataObject data) {
		@SuppressWarnings("unused")
		String classifierFileName = data.getDataObject(CLASSIFIER).getValue();
		@SuppressWarnings("unused")
		String headerFileName = data.getDataObject(DATASET_HEADER).getValue();
		
		return null; // TODO: this is an abstract class, so it cannot instantiate...need a factory
		// but maybe it never gets called too
	}
	
	/**
	 * Extracts header in an empty Instances dataset from an .arff file.
	 * It assumes that the last attribute is the class attribute
	 * @param datasetFileName
	 * @return
	 */
	public static Instances loadDataset(String datasetFileName) {
		try {
			Reader arffReader = new FileReader(datasetFileName);
			Instances header = new Instances(arffReader);
			header.setClassIndex(header.numAttributes()-1); // last attribute is class
			return header;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Loads a serialized WEKA classifier model from a .model file
	 * @param classifierFileName
	 * @return
	 */
	public static Classifier loadClassifier(String classifierFileName) {
		ObjectInputStream ois = null;
		try {
			
			ois = new ObjectInputStream(new FileInputStream(classifierFileName));
			Classifier classifier = (Classifier)ois.readObject();
			return classifier;
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				if (ois != null) { ois.close(); }
			} catch (IOException e) {}
		}
		return null;
	}

	/**
	 * Use LRU (Least Recently Used cache; instead of FIFO) map storage of classification result of instances.
	 * This is to minimize redundant classifications of recently seen instances.
	 * Internally manages the limiting of the size.
	 * See: http://www.java-alg.info/O.Reilly-Java.Generics.and.Collections/0596527756/javagenerics-CHP-16-SECT-2.html
	 */
	public static class LRUCache<K, V> extends LinkedHashMap<K, V> {
		
		private static final long serialVersionUID = 3752030986272893668L;
		
		private int maxEntries;
		
		public LRUCache(int maxEntries) {			
			super(maxEntries, // set initial capacity to max
					1,        // don't need to increase size, so just use unity load factor
					true);    // order the map by access, instead of insertion
			this.maxEntries = maxEntries;
		}
		
		@Override
		protected boolean removeEldestEntry(Map.Entry<K,V> eldest) {
			return size() > maxEntries;
		}
		
	}

}
