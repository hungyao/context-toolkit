package context.arch.intelligibility.hmm;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import be.ac.ulg.montefiore.run.jahmm.Hmm;
import be.ac.ulg.montefiore.run.jahmm.ObservationInteger;
import be.ac.ulg.montefiore.run.jahmm.ObservationVector;
import be.ac.ulg.montefiore.run.jahmm.io.FileFormatException;
import be.ac.ulg.montefiore.run.jahmm.io.HmmWriter;
import be.ac.ulg.montefiore.run.jahmm.io.ObservationIntegerReader;
import be.ac.ulg.montefiore.run.jahmm.io.ObservationSequencesReader;
import be.ac.ulg.montefiore.run.jahmm.io.ObservationVectorReader;
import be.ac.ulg.montefiore.run.jahmm.io.OpdfVector;
import be.ac.ulg.montefiore.run.jahmm.io.OpdfVectorWriter;

/**
 * To learn a HMM from supervised data. This class was written since JAHMM only had classes to do unsupervised learning.
 * Currently only limited to ObservationVector as observation.
 * @author Brian Y. Lim
 *
 */
public class HmmSupervisedLearner {

	protected List<ObservationVector> trainingObservations;
	protected List<ObservationInteger> trainingStates;

	/** number of hidden states */
	protected int NUM_STATES;
	/** number of observations in a sequence, T (i.e. length of sequence) for the training data */
	protected int NUM_OBSERVATIONS;
	/** number of sensors per observation; number of dimensions in observation */
	protected int NUM_OBSERVATION_DIM; 
	/** number of values an observation sensor can take; 2=binary for Kasteren dataset */
	protected int NUM_OBSERVATION_VALS;
	/** number of permutations of observation vectors; should be 2^NUM_OBSERVATIONS_DIM for binary sensors; NUM_OBSERVATION_VALS^NUM_OBSERVATIONS_DIM in general */
	protected int NUM_OBSERVATION_PERMS;

	/*
	 * The following fields are counts to help calculate the HMM probability parameters.
	 * Measured from training set.
	 */
	/** from i to j; should be [N_STATES][N_STATES] square matrix; to calculate A parameter */
	protected int[][] N_STATE_TO_STATE; 
	/** number of times state[j] occurred in training set */
	protected int[]	N_STATE; 
	/** sequence length, T, used when doing inferencing */
	//	protected int SEQUENCE_LENGTH;
	/** number of times state[j] leads to observation[k]; where k is the integer form of the double[] binary */
	protected int[][] N_STATE_TO_OBS; // [NUM_STATE][NUM_OBSERVATION_VALS]
	/** number of times state[j] leads to attribute[r]; dimension_r=1; assumes independence across attributes */
	protected int[][] N_STATE_TO_ATTR; // [NUM_STATE][NUM_OBSERVATION_DIM]

	/*
	 * The following are HMM parameters
	 */
	protected double[] pi; // to model probabilities of states at t=1
	protected double[][] a; // to model A matrix: state transition probabilities
	//	protected Map<DoubleArrayWrapper, Double>[] b; // one map for each state; using a map instead of an array so that I don't have to worry about array maintenance/logistics
	protected double[][] b; // to model B matrix: emission probabilities from states to observations
	protected double[][] b_naive; // simplified/modified emission probabilities; see comments at top of class

	public HmmSupervisedLearner(int numStates, int obsDimension, int numObservationValues) {
		//		N = 
		NUM_STATES = numStates;
		NUM_OBSERVATION_DIM = obsDimension;
		NUM_OBSERVATION_VALS = numObservationValues; // 2; specified in dataset
	}

	public Hmm<ObservationVector> learn(List<ObservationVector> trainingObservations, List<ObservationInteger> trainingStates) {
		this.trainingObservations = trainingObservations;
		this.trainingStates = trainingStates;

		initCounters();
		generateCountsFromTraining();
		calculateHmmParams();

		Hmm<ObservationVector> hmm = new Hmm<ObservationVector>(pi, a, 
				generateOpdfVectors());
		return hmm;
	}

	public Hmm<ObservationVector> learn(File observationSequencesFile, File stateSequencesFile) {
		List<ObservationVector> trainingObservations = readObservationsSequencesFromFile(observationSequencesFile, NUM_OBSERVATION_DIM);
		List<ObservationInteger> trainingStates = readStateSequencesFromFile(stateSequencesFile);
		return learn(trainingObservations, trainingStates);
	}

	protected List<OpdfVector> generateOpdfVectors() {
		List<OpdfVector> opdfVectors = new ArrayList<OpdfVector>();

		for (int state = 0; state < NUM_STATES; state++) {
			OpdfVector v = new OpdfVector(b[state]);
			opdfVectors.add(v);
		}

		return opdfVectors;
	}

	public void printPi() {
		System.out.println("Pi = [");
		for (int i = 0; i < pi.length; i++) {
			System.out.println(pi[i] + "\t");
		}
		System.out.println("]");
	}

	public void printA() {
		System.out.println("A = [");
		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < a[i].length; j++) {
				System.out.print(a[i][j] + "\t");
			}
			System.out.println();
		}
		System.out.println("]");
	}

	/**
	 * Use this sparingly, as it takes up a lot of space in the console and may overflow such that previous output is not viewable.
	 * TODO: want to have one B per attribute for leave-one-attribute explanation strategy
	 */
	public void printB() {
		System.out.println("B = [");
		for (int j = 0; j < b.length; j++) {
			//			for (int k = 0; k < b[j].size(); k++) {
			for (int k = 0; k < NUM_OBSERVATION_PERMS; k++) {
				//				double[] observationVal = generateIntegerVector(k, NUM_OBSERVATION_VALS, NUM_OBSERVATION_DIM);
				//				DoubleArrayWrapper observationValWrapper = new DoubleArrayWrapper(observationVal);
				System.out.print(b[j][k] + "\t");
			}
			System.out.println();
		}
		System.out.println("]");
	}

	public void printB_naive() {
		System.out.println("B_naive = [");
		for (int j = 0; j < b_naive.length; j++) {
			for (int f = 0; f < NUM_OBSERVATION_DIM; f++) {
				System.out.print(b_naive[j][f] + "\t");
			}
			System.out.println();
		}
		System.out.println("]");
	}

	protected void initCounters() {
		NUM_OBSERVATIONS = trainingObservations.size();

		//		T = SEQUENCE_LENGTH = 5;
		//		N_pow_T = Math.pow(N, T);
		//		n = NUM_OBSERVATION_DIM;

		N_STATE_TO_STATE = new int[NUM_STATES][NUM_STATES];
		N_STATE = new int[NUM_STATES];
		NUM_OBSERVATION_PERMS = (int)Math.pow(2, NUM_OBSERVATION_DIM);
		N_STATE_TO_OBS = new int[NUM_STATES][NUM_OBSERVATION_PERMS];
		N_STATE_TO_ATTR = new int[NUM_STATES][NUM_OBSERVATION_DIM];
	}

	/**
	 * Prepares the counts from training data before calculating the HMM parameters
	 */
	public void generateCountsFromTraining() {
		// iterate through observations together with states
		int prevState = -1;
		for (int obs = 0; obs < NUM_OBSERVATIONS; obs++) {
			// count states and state transitions
			ObservationInteger stateObj = trainingStates.get(obs); // state(t)
			int state = stateObj.value;
			if (obs == 0) {
				prevState = state;
			}
			N_STATE_TO_STATE[prevState][state]++; // increment count for transition from prevState to state
			N_STATE[state]++; // increment count for this state; do for all t

			// count emissions of state to observation
			ObservationVector observation = trainingObservations.get(obs);
			double[] observationVal = observation.values(); // vector of form e.g.: [0 0 1 1 0 1 0 ...]

			//			DoubleArrayWrapper observationValWrapper = new DoubleArrayWrapper(observationVal);
			//			Map<DoubleArrayWrapper, Integer> n_state_to_obs =  N_STATE_TO_OBS[state];
			//			Integer origCount = n_state_to_obs.get(observationValWrapper);
			//			if (origCount == null) { origCount = 0; }
			//			n_state_to_obs.put(observationValWrapper, origCount + 1); // increment emission count for this observation vector


			N_STATE_TO_OBS[state][OpdfVector.getIntegerEquivalent(observationVal, NUM_OBSERVATION_VALS)]++; // increment emission count for this observation
			// iterate through features to see which is activated
			for (int f = 0; f < observationVal.length; f++) {
				if (observationVal[f] == 1) {
					N_STATE_TO_ATTR[state][f]++;
				}
			}			

			// update prevState to current state
			prevState = state; 				
		}
	}

	/**
	 * Calculate the HMM parameters: Pi, A, B
	 */
	public void calculateHmmParams() {
		// calculate Pi, the probabilities for states at t=1
		pi = new double[NUM_STATES];
		for (int i = 0; i < NUM_STATES; i++) {
			int count = N_STATE[i];	
			count = count == 0 ? 1 : count; // Laplace smoothing to prevent log(0)
			pi[i] = ((double)count) / NUM_OBSERVATIONS;
		}

		// calculate A, the state transition probabilities
		a = new double[NUM_STATES][NUM_STATES];
		for (int i = 0; i < NUM_STATES; i++) {			
			for (int j = 0; j < NUM_STATES; j++) {
				int count = N_STATE_TO_STATE[i][j];	
				count = count == 0 ? 1 : count; // Laplace smoothing to prevent log(0)
				a[i][j] = ((double)count) / N_STATE[i];
			}
		}

		// calculate B, the emission probabilities of a state resulting in an observation
		//		b = new Map[NUM_STATES];
		b = new double[NUM_STATES][NUM_OBSERVATION_PERMS];
		b_naive = new double[NUM_STATES][NUM_OBSERVATION_DIM];

		for (int j = 0; j < NUM_STATES; j++) {
			//			b[j] = new HashMap<DoubleArrayWrapper, Double>();
			//			Map<DoubleArrayWrapper, Integer> n_state_to_obs =  N_STATE_TO_OBS[j];

			// calculate across all observation permutations
			for (int k = 0; k < NUM_OBSERVATION_PERMS; k++) {
				//				double[] observationVal = generateIntegerVector(k, NUM_OBSERVATION_VALS, NUM_OBSERVATION_DIM);
				//				DoubleArrayWrapper observationValWrapper = new DoubleArrayWrapper(observationVal);
				//				Integer count = n_state_to_obs.get(observationValWrapper);
				//				if (count == null) { count = 0; }

				double count = N_STATE_TO_OBS[j][k];		
				count = count == 0 ? 1e-15 : count; // Laplace smoothing to prevent log(0)
				double prob = count / N_STATE[j];

				//				b[j].put(observationValWrapper, prob);
				b[j][k] = prob;

				//				System.out.println(observationValWrapper + "\t b(" + j + "," + k + ") = " + prob);
			}

			// calculate across features
			for (int f = 0; f < NUM_OBSERVATION_DIM; f++) {
				int count = N_STATE_TO_ATTR[j][f];
				count = count == 0 ? 1 : count; // Laplace smoothing to prevent log(0)
				double prob = ((double)count) / N_STATE[j];
				b_naive[j][f] = prob;
			}
		}
	}

	public static String toDoubleArrayString(double[] vector) {
		String ret = "[";
		for (double el : vector) {
			ret += el + ", ";
		}
		ret = ret.substring(0, ret.length()-2); // truncate off last ", "
		ret += "]";
		return ret;
	}

	public static String toIntArrayString(int[] vector) {
		String ret = "[";
		for (int el : vector) {
			ret += el + ", ";
		}
		ret = ret.substring(0, ret.length()-2); // truncate off last ", "
		ret += "]";
		return ret;
	}

	/**
	 * Ordinarily, the result is a number of sequences of a number of observations.
	 * However, for home activity recognition, we take a long contiguious sequence and just use a sliding window of fixed length to get the sequences
	 */
	public static List<ObservationVector> readObservationsSequencesFromFile(File f, int dimension) {
		try {
			Reader reader = new FileReader(f);
			List<List<ObservationVector>> v = ObservationSequencesReader.readSequences(new ObservationVectorReader(dimension), reader); // TODO get rid of magic number
			reader.close();			
			return v.get(0);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (FileFormatException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Ordinarily, the result is a number of sequences of a number of observations.
	 * However, for home activity recognition, we take a long contiguious sequence and just use a sliding window of fixed length to get the sequences
	 */
	public static List<ObservationInteger> readStateSequencesFromFile(File f) {
		try {
			Reader reader = new FileReader(f);
			List<List<ObservationInteger>> s = ObservationSequencesReader.readSequences(new ObservationIntegerReader(), reader);
			reader.close();			
			return s.get(0);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (FileFormatException e) {
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * For testing, loading and saving
	 */
	public static void main(String[] args) {
		// learning HMM
		HmmSupervisedLearner learner = new HmmSupervisedLearner(8, 14, 2);		
		Hmm<ObservationVector> hmm = learner.learn(
				new File("demos/home-hmm/kasteren-jahmm-observations.seq"), 
				new File("demos/home-hmm/kasteren-jahmm-states.seq"));

		try {
			/*
			 * Save HMM to file
			 * Quite slow!
			 */
			HmmWriter.write(
					new FileWriter("demos/home-hmm/kasteren-jahmm.model"), 
					new OpdfVectorWriter(), 
					hmm);

			/*
			 * Load HMM from file
			 * Also slow...but since this is just an init step, it may be ok
			 * ~6.4s
			 */
//			long start = System.currentTimeMillis();
//			Hmm<ObservationVector> hmm = HmmReader.read(
//					new FileReader("demos/home-hmm/kasteren-jahmm.model"), 
//					new OpdfVectorReader());
//			long end = System.currentTimeMillis();
//
//			System.out.println("hmm = " + hmm.getPi(0));
//			System.out.println("duration = " + (float)(end-start)/1000);
		} catch (IOException e) {
			e.printStackTrace();
//		} catch (FileFormatException e) {
//			e.printStackTrace();
		}
	}

}
