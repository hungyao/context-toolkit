package context.arch.discoverer.query;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import be.ac.ulg.montefiore.run.jahmm.Hmm;
import be.ac.ulg.montefiore.run.jahmm.ObservationVector;
import be.ac.ulg.montefiore.run.jahmm.io.FileFormatException;
import be.ac.ulg.montefiore.run.jahmm.io.HmmReader;
import be.ac.ulg.montefiore.run.jahmm.io.OpdfVectorReader;

import context.arch.comm.DataObject;
import context.arch.comm.DataObjects;
import context.arch.discoverer.ComponentDescription;
import context.arch.intelligibility.hmm.HmmSupervisedLearner;
import context.arch.widget.SequenceWidget;

import weka.classifiers.Classifier;
import weka.core.Instances;

public abstract class HmmWrapper {

	public static final String HMM_WRAPPER = "HMM_WRAPPER";
	public static final String HMM_MODEL = "HMM_MODEL";
	public static final String HEADER_FILE_NAME = "HEADER_FILE_NAME";
	public static final String SEQUENCE_LENGTH = "SEQUENCE_LENGTH";

	public static final int CACHE_LIMIT = 10;
//	private LinkedHashMap<Instance, String> instanceClassifications = new BoundedSizeMap<Instance, String>(CACHE_LIMIT);
	
//	private List<String> outcomeValues = new ArrayList<String>();

	protected Hmm<ObservationVector> hmm;

	protected int numObservationValues;

	protected int sequenceLength;
	
	/*
	 * These are used to map numbers in the text format to names
	 */
	protected List<String> OUTPUT_NAMES; // names of states
	protected List<String> INPUT_NAMES; // names of each feature input
	private String headerFileName;
	private String hmmModelFileName;
	
	public HmmWrapper(String headerFileName,
			String observationSequencesFileName, String stateSequencesFileName, 
			int sequenceLength) {
		loadHeaderInfo(headerFileName);

		// learn model from source dataset
		HmmSupervisedLearner learner = new HmmSupervisedLearner(OUTPUT_NAMES.size(), INPUT_NAMES.size(), numObservationValues);		
		this.hmm = learner.learn(
				new File(observationSequencesFileName), 
				new File(stateSequencesFileName));
		
		this.sequenceLength = sequenceLength;
	}
	
	public HmmWrapper(String headerFileName, String hmmModelFileName, 
			int sequenceLength) {
		loadHeaderInfo(headerFileName);
		
		// extract classifier from serialized file
		try {
			this.hmm = HmmReader.read(
					new FileReader("demos/home-hmm/kasteren-jahmm.model"), 
					new OpdfVectorReader());
		} catch (FileFormatException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		this.headerFileName = headerFileName;
		this.hmmModelFileName = hmmModelFileName;
		this.sequenceLength = sequenceLength;
	}
	
	public void loadHeaderInfo(String fileName) {
		INPUT_NAMES = new ArrayList<String>();
		OUTPUT_NAMES = new ArrayList<String>();
		
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(fileName));

			numObservationValues = Integer.parseInt(properties.getProperty("numObservationValues"));
			
			String[] inputNames =  properties.getProperty("observationVector").split(",");
			for (String name : inputNames) {
				INPUT_NAMES.add(name.trim());
			}

			String[] outputNames =  properties.getProperty("states").split(",");
			for (String name : outputNames) {
				OUTPUT_NAMES.add(name.trim());
			}
			
		} catch (IOException e) {
		}
	}
	
	public String[] getInputNames() {
		return INPUT_NAMES.toArray(new String[INPUT_NAMES.size()]);
	}
	public String[] getOutputNames() {
		return OUTPUT_NAMES.toArray(new String[OUTPUT_NAMES.size()]);
	}
	
	public int getSequenceLength() {
		return sequenceLength;
	}
	
	public Hmm<ObservationVector> getHmm() {
		return hmm;
	}
	
	public int numOutcomeValues() {
//		return outcomeValues.size();
		return OUTPUT_NAMES.size();
	}
	
	public String getOutcomeValue(int index) {
//		return outcomeValues.get(index);
		return OUTPUT_NAMES.get(index);
	}
	
	public List<String> getOutcomeValues() {
//		return Collections.unmodifiableList(outcomeValues);
		return Collections.unmodifiableList(OUTPUT_NAMES);
	}
	
	/**
	 * 
	 * @param instance
	 * @return null if classification failed or was invalid (e.g. null values in attributes)
	 */
	protected String[] classify(List<ObservationVector> obs) {
		// TODO: caching?

		int[] x = hmm.mostLikelyStateSequence(obs);
		
		String[] stateSeqs = new String[x.length];
		for (int t = 0; t < x.length; t++) {
			stateSeqs[t] = OUTPUT_NAMES.get(x[t]); // assign numeric values to output names
		}

		System.out.println("classify obs = " + obs);
		System.out.println("classify x = " + HmmSupervisedLearner.toIntArrayString(x));
		
		return stateSeqs;
	}
	
	/**
	 * 
	 * @param stateSeq
	 * @return first probability is actually the prior for the first state; the others are transition probabilities of matrix A
	 */
	public double[] getTransitionProbabilities(List<String> stateSeq) {
		double[] probs = new double[stateSeq.size()];
		
		probs[0] = hmm.getPi(OUTPUT_NAMES.indexOf(stateSeq.get(0)));
		
		for (int t = 1; t < probs.length; t++) {
			probs[t] = hmm.getAij(
					OUTPUT_NAMES.indexOf(stateSeq.get(t-1)), 
					OUTPUT_NAMES.indexOf(stateSeq.get(t)));
		}
		
		return probs;
	}

	/**
	 * 
	 * @param stateSeq
	 * @return first probability is actually the prior for the first state; the others are transition probabilities of matrix A
	 */
	public double[] getTransitionProbabilities(int[] x) {
		double[] probs = new double[x.length];
		
		probs[0] = hmm.getPi(x[0]);
		
		for (int t = 1; t < probs.length - 1; t++) {
			probs[t] = hmm.getAij(x[t-1], x[t]);
		}
		
		return probs;
	}

	/**
	 * Checks if widget state can be extracted as an appropriate Instance,
	 * since other widgets are also queried. 
	 * If this fails, then classification would fail and return null.
	 * @param widgetState
	 * @return
	 */
	protected abstract boolean isInstanceExtractable(ComponentDescription widgetState);

	/**
	 * Assumes that widgetState is validated to extract instance
	 * @param widgetState
	 * @return
	 */
	public List<String> classify(ComponentDescription widgetState) {
		List<ObservationVector> obs = extractObservations(widgetState);
		if (obs == null) { return null; }
		
		String[] outcomeSequence = classify(obs);
		
		// store value back into widgetState
		// TODO: stuff multiple values
//		Enactor.setAttValue(classAttribute.name(), outcomeValue, widgetState.getNonConstantAttributes());
//		System.out.println("ClassifierWrapper.classifiy stored: " + String.valueOf(Enactor.getAtt(classAttribute.name(), widgetState.getNonConstantAttributes())));
		
		return Arrays.asList(outcomeSequence);
	}
	
	protected double[] distributionForInstance(List<ObservationVector> obs) {
		// TODO: caching?
		
		int NUM_STATES = hmm.nbStates();
		double[] probs = new double[NUM_STATES];

		// get most likely state sequence
		int[] x = hmm.mostLikelyStateSequence(obs);
		
		// then permute last state
		int last_t = x.length - 1;
		for (int i = 0; i < NUM_STATES; i++) {
			x[last_t] = i;
			probs[i] = hmm.probability(obs, x);
		}
		
		return probs;
	}
	
	/**
	 * Applicable only to getting distribution for different final states; the (earlier) rest of the sequence is fixed. 
	 * @param widgetState
	 * @return
	 */
	public double[] distributionForInstance(ComponentDescription widgetState) {
		List<ObservationVector> obs = extractObservations(widgetState);
		return distributionForInstance(obs);
	}

	public List<ObservationVector> extractObservations(ComponentDescription widgetState) {
		if (widgetState == null) { return null; }
		if (!isInstanceExtractable(widgetState)) { return null; }
		
		/*
		 * Need to iterate for time steps
		 */
		
		List<ObservationVector> observations = new ArrayList<ObservationVector>();
		
		// grab input values for each time stamp
		for (int t = 0; t < sequenceLength; t++) {
			String seqIndexMarker = SequenceWidget.getTPrepend(t); // prepend marker
			
			double[] inputValues = new double[INPUT_NAMES.size()];

//			System.out.println("extractObservations widgetState = " + widgetState);
			
			// iterate inputs names; note order is important
			for (int i = 0; i < INPUT_NAMES.size(); i++) {
				String attrName = seqIndexMarker + INPUT_NAMES.get(i);
				Object value = widgetState.getAttributeValue(attrName);
				if (value == null) { // value may be invalid if sequence not fully populated or ready yet
					continue; 
//					return null;
				}
				inputValues[i] = (Integer) value;
			}

			// set observation vector for this time stamp
			ObservationVector o = new ObservationVector(inputValues);
			observations.add(o);
		}
		
		return observations;
	}
	
	public DataObject toDataObject() {
		DataObjects v = new DataObjects();
		v.add(new DataObject(HMM_MODEL, hmmModelFileName));
		v.add(new DataObject(HEADER_FILE_NAME, headerFileName));
		v.add(new DataObject(SEQUENCE_LENGTH, ""+sequenceLength));
		return new DataObject(HMM_WRAPPER, v);
	}
	
	public static HmmWrapper fromDataObject(DataObject data) {
		@SuppressWarnings("unused")
		String hmmModelFileName = data.getDataObject(HMM_WRAPPER).getValue();
		@SuppressWarnings("unused")
		String headerFileName = data.getDataObject(HEADER_FILE_NAME).getValue();
		
		return null; // TODO: this is an abstract class, so it cannot instantiate...need a factory
		// but maybe it never gets called too
	}
	
	public static Instances loadHeader(String headerFileName) {
		try {
			Reader arffReader = new FileReader(headerFileName);
			Instances header = new Instances(arffReader);
			header.setClassIndex(header.numAttributes()-1); // last attribute is class
			return header;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
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
	public static class BoundedSizeMap<K, V> extends LinkedHashMap<K, V> {
		
		private static final long serialVersionUID = 3752030986272893668L;
		
		private int maxEntries;
		
		public BoundedSizeMap(int maxEntries) {			
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
