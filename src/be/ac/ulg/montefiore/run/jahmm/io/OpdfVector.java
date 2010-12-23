package be.ac.ulg.montefiore.run.jahmm.io;

import java.text.NumberFormat;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import be.ac.ulg.montefiore.run.jahmm.ObservationVector;
import be.ac.ulg.montefiore.run.jahmm.Opdf;

/**
 * TODO: now only supports binary state for each vector element.
 * 
 * @author Brian Y. Lim
 *
 */
public class OpdfVector implements Opdf<ObservationVector> {
	
	private static final long serialVersionUID = -6795564904406631043L;
	
	protected Map<Integer, Double> probs; // where Integer represents the integer form of the vector value in ObservationVector

	protected int NUM_OBSERVATION_DIM;

	private int NUM_VALUES;

	// TODO: now only supports binary state for each vector element.
	public static int NUM_OBSERVATION_VALS = 2;
	
	/**
	 * This is effectively converting from the b emission probability matrix from SupervisedLearner
	 * TODO: Need to support other methods
	 * @param b_i b[i] where i is a state
	 * @param state which output state this emission PDF is for
	 */
	public OpdfVector(double[] b_i) {
		probs = new LinkedHashMap<Integer, Double>();
		
		NUM_VALUES = b_i.length;
		NUM_OBSERVATION_DIM = (int)(Math.log(NUM_VALUES) / Math.log(NUM_OBSERVATION_VALS));

		for (int j = 0; j < NUM_VALUES; j++) {
//			double[] obsVal = toVector(j, NUM_OBSERVATION_VALS, NUM_OBSERVATION_DIM);
			double prob = b_i[j];			
			probs.put(j, prob);
		}
	}
	
	/**
	 * Number of permutations that can be formed from binary values of each element of the vector
	 * @return
	 */
	public int nbValues() {
		return NUM_VALUES;
	}
	
	public int dimension() {
		return NUM_OBSERVATION_DIM;
	}
	
	private OpdfVector() { // for cloning
	}
	
	/**
	 * Takes a number i and converts to n-ary form to populate cells of an array
	 * E.g. for n=2 and len=5: i=2 gives [0,0,0,1,0], i=7 gives [0,0,1,1,1]
	 * 
	 * @param number to convert
	 * @param n the base of the number space
	 * @param dim length of vector; number of dimensions of vector
	 * @return
	 */
	public static double[] toVector(int number, int n, int dim) {
		double[] vector = new double[dim];
		for (int j = 0; j < dim; j++) {
			vector[j] = number % n;
			number = number/n; // whittle down
		}
		return vector;
	}	
	public static int getIntegerEquivalent(double[] vector, int n) {
		int value = 0;
		for (int j = 0; j < vector.length; j++) { // TODO: change?
//		for (int j = vector.length-1; j >= 0; j--) { // reverse order of bits
			value += vector[j] * Math.pow(n, j);
		}
		return value;
	}
	
	@Override
	public OpdfVector clone() {
		OpdfVector clone = new OpdfVector();
		clone.probs = new LinkedHashMap<Integer, Double>(this.probs);
		return clone;
	}

	@Override
	public void fit(ObservationVector... o) {
		// TODO Auto-generated method stub		
	}

	@Override
	public void fit(Collection<? extends ObservationVector> o) {
		// TODO Auto-generated method stub		
	}

	@Override
	public void fit(ObservationVector[] o, double[] weights) {
		// TODO Auto-generated method stub		
	}

	@Override
	public void fit(Collection<? extends ObservationVector> o, double[] weights) {
		// TODO Auto-generated method stub		
	}

	@Override
	public ObservationVector generate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double probability(ObservationVector obs) {
		int val = getIntegerEquivalent(obs.values(), NUM_OBSERVATION_VALS);
		return probs.get(val);
	}
	
	public static double EPSILON = 1e-10;

	/**
	 * 
	 * @param k integer equivalent of values vector
	 * @return
	 */
	public double probability(int k) {
		Double prob = probs.get(k);
		
		if (prob == null) { // means vector value was never encountered before, and so the count is theoretically 0
			return EPSILON; // using Laplace smoothing to avoid multiplication by zero
		}
		else {
			return prob;
		}
	}

	@Override
	public String toString(NumberFormat nf) {
		// TODO Auto-generated method stub
		return null;
	}

}
