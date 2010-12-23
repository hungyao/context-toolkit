package context.arch.intelligibility.hmm;

import java.util.List;

import be.ac.ulg.montefiore.run.jahmm.Hmm;
import be.ac.ulg.montefiore.run.jahmm.ObservationVector;
import be.ac.ulg.montefiore.run.jahmm.io.OpdfVector;
import context.arch.discoverer.query.HmmWrapper;
import context.arch.enactor.HmmEnactor;
import context.arch.intelligibility.Explainer;
import context.arch.intelligibility.expression.DNF;
import context.arch.intelligibility.expression.Parameter;
import context.arch.intelligibility.expression.Reason;
import context.arch.intelligibility.query.Query;
import context.arch.widget.SequenceWidget;

public class HmmExplainer extends Explainer {

	protected Hmm<ObservationVector> hmm;
	protected HmmWrapper hmmWrapper;
	protected HmmEnactor<ObservationVector> hmmEnactor;
	
	/*
	 * The following are HMM parameters copied from the model
	 */
	protected double[] pi; // to model probabilities of states at t=1
	protected double[][] a; // to model A matrix: state transition probabilities
	protected double[][] b; // to model B matrix: emission probabilities from states to observations
	protected double[][] b_naive; // simplified/modified emission probabilities; see comments at top of class
	protected String[] OUTPUT_NAMES; // names of states
	protected String[] INPUT_NAMES; // names of each feature input
	
	public HmmExplainer(HmmEnactor<ObservationVector> enactor) {
		super(enactor);
		this.hmmEnactor = enactor; // save casted form
		this.hmmWrapper = enactor.getHMM();
		this.hmm = hmmWrapper.getHmm();

		portParamsToMatrices();
		
		N = hmm.nbStates();
		T = hmmWrapper.getSequenceLength();
		n = hmmWrapper.getInputNames().length;
		N_pow_T = Math.pow(N, T);
		
		F = new double[n];
	}

	public static final int F_VERSION_FULL = 0; // show evidence due to observations over time and features
	public static final int F_VERSION_BY_FEATURE = 1; // show evidence due to observations summed over time; get to distinguish by features only
	public static final int F_VERSION_BY_TIME = 2; // show evidence due to observations summed over features; get to distinguish by time only
	
	protected int F_VERSION = F_VERSION_FULL;
	
	public int getFeatureVersion() {
		return F_VERSION;
	}	
	public void setFeatureVersion(int F_VERSION) {
		this.F_VERSION = F_VERSION;
	}
	
	/* =======================================================================================================================================
	 * Whole bunch of code to port from Jahmm objects to arrays (which are probably more efficient)
	 */
	
	/**
	 * Port parameters of Hmm to matrices.
	 */
	protected void portParamsToMatrices() {
		int NUM_STATES = hmm.nbStates();
		
		pi = new double[NUM_STATES];
		for (int i = 0; i < NUM_STATES; i++) {
			pi[i] = hmm.getPi(i);
		}
		
		a = new double[NUM_STATES][NUM_STATES];
		for (int i = 0; i < NUM_STATES; i++) {
			for (int j = 0; j < NUM_STATES; j++) {
				a[i][j] = hmm.getAij(i, j);
			}			
		}

		int VECTOR_DIM = ((OpdfVector)hmm.getOpdf(0)).dimension(); // TODO: should do some checking
		int VECTOR_PERMS = (int)Math.pow(2, VECTOR_DIM); // TODO: warning: magic number representing NUM_OBSERVATION_VALS

		b = new double[NUM_STATES][VECTOR_PERMS];
		b_naive = new double[NUM_STATES][VECTOR_DIM];
		
		for (int i = 0; i < NUM_STATES; i++) {
			for (int k = 0; k < VECTOR_PERMS; k++) {
				b[i][k] = ((OpdfVector)hmm.getOpdf(i)).probability(k);
			}
			
			/*
			 * Derive naive emission matrix
			 */
			for (int f = 0; f < VECTOR_DIM; f++) {
				b_naive[i][f] = 0;		
				
				for (int k = 0; k < VECTOR_PERMS; k++) {
					if (((1 << f) & k) > 0) { // bit mask of whether k has bit value = 1 in element f
						b_naive[i][f] += b[i][k]; // then add it
					}
				}
			}
		}
		
		OUTPUT_NAMES = hmmWrapper.getOutputNames();
		INPUT_NAMES = hmmWrapper.getInputNames();
	}
	
	/**
	 * TODO: this is currently a hack; make more 'native'
	 * Convert from list to 2d array
	 * @param o_naive_l
	 * @return
	 */
	protected double[][] portToObservationsArray(List<ObservationVector> o_naive_l) {
		if (o_naive_l.isEmpty()) { return new double[0][0]; }
		if (!(o_naive_l.get(0) instanceof ObservationVector)) { throw new RuntimeException("Only works with ObservationVector for now"); }
		 
		double[][] o_naive = new double[o_naive_l.size()][((ObservationVector)o_naive_l.get(0)).dimension()];
		for (int t = 0; t < o_naive.length; t++) {
			for (int r = 0; r < o_naive[t].length; r++) {
				o_naive[t][r] = ((ObservationVector)o_naive_l.get(t)).value(r);
			}
		}
		return o_naive;
	}
	
	/* ======================================================================================================================================= */
	
	/**
	 * g(...) = ...
	 * @param o_naive observation sequence
	 * @param x state sequence (actual, or target/desired)
	 * @param F_VERSION
	 * @return
	 */
	protected Reason getWhyExplanation(List<ObservationVector> o_naive_l, int[] x, int F_VERSION) {
		Reason reason = new Reason();
		
		// TODO: make more 'native'
		double[][] o_naive = portToObservationsArray(o_naive_l);

		// total sum of evidence
		double totalEvidence = 0; // sum through method
		int numComponents = 0;
		
		// prior probability
		double priorEvidence = getPriorEvidence(x[0], T);
		//String prior_name = "t" + 0 + "(" + OUTPUT_NAMES[x[0]] + ")"; // e.g. t0(bed)
		String prior_name = SequenceWidget.getTPrepend(0); // "__T0_"
		reason.add(Parameter.instance(prior_name, priorEvidence));
		totalEvidence += priorEvidence;
		numComponents++;
		
		// transition probabilities
		for (int t = 1; t < T; t++) { // t=1 to T; skip first
			double transitionEvidence = getTransitionEvidence(x[t-1], x[t]);
			//String name = "t" + (t) + "((" + OUTPUT_NAMES[x[t-1]] + ")_to_(" + OUTPUT_NAMES[x[t]] + "))"; // e.g. t1((bed)_to_(breakfast))
			String name = SequenceWidget.getTPrepend(t); // "__T#_"
			reason.add(Parameter.instance(name, transitionEvidence));
			totalEvidence += transitionEvidence;
			numComponents++;
		}
		
		/*
		 * emission probabilities: 3 versions
		 */
		if (F_VERSION == F_VERSION_BY_FEATURE) { // get to distinguish by features, r
			for (int r = 0; r < n; r++) {
				double featureEvidence = 0;
				String name = "(" + INPUT_NAMES[r] + "="; // e.g. (microwave=1,0,0,...)
				for (int t = 0; t < T; t++) {
					featureEvidence += getFeatureEvidence(x[t], o_naive[t], r);
					name += (int)o_naive[t][r] + (t < T-1 ? "," : ")");
				}
				reason.add(Parameter.instance(name, featureEvidence));
				totalEvidence += featureEvidence;
				numComponents++;
			}			
		}
		else if (F_VERSION == F_VERSION_BY_TIME) { // get to distinguish by time, t
			for (int t = 0; t < T; t++) {
				double featureEvidence = 0;
				String name = "t" + t + "("; // e.g. t1(microwave=1,toilet=0,...)
				for (int r = 0; r < n; r++) {
					featureEvidence += getFeatureEvidence(x[t], o_naive[t], r);
					name += INPUT_NAMES[r] + "=" + o_naive[t][r] + (t < T-1 ? "," : ")");
				}
				reason.add(Parameter.instance(name, featureEvidence));
				totalEvidence += featureEvidence;
				numComponents++;
			}			
		}
		else { // F_VERSION == F_VERSION_FULL
			for (int t = 0; t < T; t++) {
				for (int r = 0; r < n; r++) {
					double featureEvidence = getFeatureEvidence(x[t], o_naive[t], r);
//					String name = "t"+t+"(" + INPUT_NAMES[r] + "=" + o_naive[t][r] + ")"; // e.g. t1(microwave=1)
					String name = SequenceWidget.getTPrepend(t) + INPUT_NAMES[r]; // "__T#_Name"
					reason.add(Parameter.instance(name, featureEvidence));
					totalEvidence += featureEvidence;
					numComponents++;
				}			
			}
		}

		// average evidence; use average instead of total, to normalize the "lengths" for visualization
//		int n = NUM_OBSERVATION_DIM;
//		double avgEvidence = totalEvidence / (1 + (T-1) + T*n); // obsolete
		double avgEvidence = totalEvidence / numComponents;
		reason.add(0, Parameter.instance("average", avgEvidence)); // add to front
		
		return reason;
	}
	
	/**
	 * delta_g = g(xTarget) - g(xActual)
	 * @param o_naive
	 * @param xActual
	 * @param xTarget
	 * @param F_VERSION
	 * @return
	 */
	protected Reason getWhyNotExplanation(List<ObservationVector> o_naive_l, int[] xActual, int[] xTarget, int F_VERSION) {
		Reason list = new Reason();
		
		// TODO: make more 'native'
		double[][] o_naive = portToObservationsArray(o_naive_l);

		// total sum of evidence
		double dTotalEvidence = 0; // sum through method
		int numComponents = 0;
		
		// calculate and add evidence due to prior
		double whyPriorEvidence = getPriorEvidence(xActual[0], T);
		double whyNotPriorEvidence = getPriorEvidence(xTarget[0], T);
		double dPriorEvidence = whyNotPriorEvidence - whyPriorEvidence; // delta = target - actual
//		String prior_name = "t" + 0 + "(" + OUTPUT_NAMES[xTarget[0]] + " vs. " + OUTPUT_NAMES[xActual[0]] + ")"; // e.g. t0(target vs. actual)
		String prior_name = SequenceWidget.getTPrepend(0); // "__T0_"
		list.add(Parameter.instance(prior_name, dPriorEvidence));
		dTotalEvidence += dPriorEvidence;
		numComponents++;
		
		// transition probabilities
		for (int t = 1; t < T; t++) { // t=1 to T; skip first
			double whyTransitionEvidence = getTransitionEvidence(xActual[t-1], xActual[t]);
			double whyNotTransitionEvidence = getTransitionEvidence(xTarget[t-1], xTarget[t]);
//			System.out.println("whyNotTransitionEvidence = " +
//					"getTransitionEvidence(xTarget["+(t-1)+"], xTarget["+t+"]) = " +
//							"getTransitionEvidence("+xTarget[t-1]+", "+xTarget[t]+") = " + 
//							getTransitionEvidence(xTarget[t-1], xTarget[t]));
			double dTransitionEvidence = whyNotTransitionEvidence - whyTransitionEvidence; // delta = target - actual
//			String name = "t" + (t) + "(((" + OUTPUT_NAMES[xTarget[t-1]] + ")_to_(" + OUTPUT_NAMES[xTarget[t]] + ")) vs. " +
//									   "((" + OUTPUT_NAMES[xActual[t-1]] + ")_to_(" + OUTPUT_NAMES[xActual[t]] + ")))"; // e.g. t1(((bed)_to_(breakfast)) vs. ((bed)_to_(breakfast)))
			String name = SequenceWidget.getTPrepend(t); // "__T#_"
			list.add(Parameter.instance(name, dTransitionEvidence));
			dTotalEvidence += dTransitionEvidence;
			numComponents++;
		}
		
		/*
		 * emission probabilities: 3 versions
		 */
		if (F_VERSION == F_VERSION_BY_FEATURE) { // get to distinguish by features, r
			for (int r = 0; r < n; r++) {
				double whyFeatureEvidence = 0, whyNotFeatureEvidence = 0;
				String name = "(" + INPUT_NAMES[r] + "="; // e.g. (microwave=1,0,0,...)
				for (int t = 0; t < T; t++) {
					whyFeatureEvidence += getFeatureEvidence(xActual[t], o_naive[t], r);
					whyNotFeatureEvidence += getFeatureEvidence(xTarget[t], o_naive[t], r);
					name += (int)o_naive[t][r] + (t < T-1 ? "," : ")");
				}
				double dFeatureEvidence = whyNotFeatureEvidence - whyFeatureEvidence; // delta = target - actual
				list.add(Parameter.instance(name, dFeatureEvidence));
				dTotalEvidence += dFeatureEvidence;
				numComponents++;
			}			
		}
		else if (F_VERSION == F_VERSION_BY_TIME) { // get to distinguish by time, t
			for (int t = 0; t < T; t++) {
				double whyFeatureEvidence = 0, whyNotFeatureEvidence = 0;
				String name = "t" + t + "("; // e.g. t1(microwave=1,toilet=0,...)
				for (int r = 0; r < n; r++) {
					whyFeatureEvidence += getFeatureEvidence(xActual[t], o_naive[t], r);
					whyNotFeatureEvidence += getFeatureEvidence(xTarget[t], o_naive[t], r);
					name += INPUT_NAMES[r] + "=" + o_naive[t][r] + (t < T-1 ? "," : ")");
				}
				double dFeatureEvidence = whyNotFeatureEvidence - whyFeatureEvidence; // delta = target - actual
				list.add(Parameter.instance(name, dFeatureEvidence));
				dTotalEvidence += dFeatureEvidence;
				numComponents++;
			}			
		}
		else { // F_VERSION == F_VERSION_FULL
			for (int t = 0; t < T; t++) {
				for (int r = 0; r < n; r++) {
					double whyFeatureEvidence = getFeatureEvidence(xActual[t], o_naive[t], r);
					double whyNotFeatureEvidence = getFeatureEvidence(xTarget[t], o_naive[t], r);
//					String name = "t"+t+"(" + INPUT_NAMES[r] + "=" + o_naive[t][r] + ")"; // e.g. t1(microwave=1)
					String name = SequenceWidget.getTPrepend(t) + INPUT_NAMES[r]; // "__T#_Name"
					double dFeatureEvidence = whyNotFeatureEvidence - whyFeatureEvidence; // delta = target - actual
					list.add(Parameter.instance(name, dFeatureEvidence));
					dTotalEvidence += dFeatureEvidence;
					numComponents++;
				}			
			}
		}

		// average evidence; use average instead of total, to normalize the "lengths" for visualization
//		int n = NUM_OBSERVATION_DIM;
		double avgEvidence = dTotalEvidence / numComponents;
		list.add(0, Parameter.instance("average", avgEvidence)); // add to front
		
		return list;
	}
	
	int N; // number of states
	int T; // sequence length
	int n; // number of features
	/** Commonly used factor, so store it; N^T */
	double N_pow_T;
	
	/** Is a constant that only needs to be set once */
	protected double H = Double.NaN; 
	
	/**
	 * @param x0
	 * @return
	 */
	protected double getPriorEvidence(int x0, int T) {
		if (Double.isNaN(H)) { // not yet set
			// H = [sum(j=1..N){log(pi[j])}]^T
			H = 1;
			for (double p : pi) { H += Math.log(p); }
			H = Math.pow(H, T);
		}
		
		// h = (N^T)*log(pi[x0]) - H
		double evidence = N_pow_T * Math.log(pi[x0]) - H;
//		System.out.println("N_pow_T: " + N_pow_T);
//		System.out.println("H: " + H);
//		System.out.println("N_pow_T * Math.log(pi[x0]): " + N_pow_T * Math.log(pi[x0]));		
		return evidence;
	}

	/** Is a constant that only needs to be set once */
	protected double U = Double.NaN; 
	/**
	 * 
	 * @param x1 from this state; x[t-1]
	 * @param x2 to this state; x[t]
	 * @return
	 */
	protected double getTransitionEvidence(int x1, int x2) {
		// U = [sum(j1=1..N,j2=1..N){log(a[j1][j2])}]^(T-2)
		if (Double.isNaN(U)) {
			U = 1;
			for (int j1 = 0; j1 < N; j1++) {
				for (int j2 = 0; j2 < N; j2++) {
					U += Math.log(a[j1][j2]); 
				}				
			}
			U = Math.pow(U, T-2);
		}
		
		// u = (N^T)*log(a[x1][x2]) - U
		double evidence = N_pow_T * Math.log(a[x1][x2]) - U;
//		System.out.println("U: " + U);
//		System.out.println("Math.log(a[x1][x2]): " + Math.log(a[x1][x2]));
		return evidence;
	}

	/** Is a constant that only needs to be set once */
	protected double[] F; 
	/**
	 * 
	 * @param xt state at time t
	 * @param ot observation vector at time t
	 * @param r index of feature of observation we care about here
	 * @return
	 */
	protected double getFeatureEvidence(int xt, double[] ot, int r) {
		if (F[r] == 0) { // not yet set
			double Fr = 1;
			// F = n^(T-1) * [sum(j=1..N){log(b_naive[j][r])}]^T
			for (int j = 0; j < N; j++) { // iterate states 
				double p;
				if (ot[r] == 1) { p = b_naive[j][r]; }
				else { p = 1 - b_naive[j][r]; } // probability of not
				
//				System.out.println("b_naive["+j+"]["+r+"] = " + b_naive[j][r]);
				
				if (p > 0) {
					Fr += Math.log(p);
				}
				else {
					/*
					 * because p <= 0, probably due to some addition error arising from 
					 * counting too many zeros even with Laplace smoothing
					 */
					Fr += 0;
				}
//				System.out.println("F"+r+" = " + Fr + ", j = " + j + ", p = " + p + ", N = " + N); // why is p negative? b_naive > 1
			}
			Fr = Math.pow(Fr, T);
			Fr *= Math.pow(n, T-1);			
			
			F[r] = Fr;			
//			System.out.println("F["+r+"] = " + F[r]);
		}

		// f = (N^T)*log(b_naive[i][r]) - F
		double evidence = N_pow_T * Math.log(b_naive[xt][r]) - F[r];
//		System.out.println("F: " + F);
//		System.out.println("Math.log(b_naive[i][r]): " + Math.log(b_naive[xt][r]));
		return evidence;
	}
	
	// TODO increase efficiency by using caching

	@SuppressWarnings("serial")
	@Override
	public DNF getWhyExplanation() {
		final List<ObservationVector> o = hmmEnactor.getObservations();
		final int[] x = hmm.mostLikelyStateSequence(o);
		return new DNF() {{ add(getWhyExplanation(o, x, F_VERSION)); }};
	}
	
	/**
	 * 
	 * @param altOutcomeValue; assumes format: "# # #..." up to sequence length
	 * @return
	 */
	protected int[] parseOutcomeValueSequence(String altOutcomeValue) {
		String[] altOutcomeValueSequence = altOutcomeValue.split(" ");
		int[] xTarget = new int[altOutcomeValueSequence.length];
		for (int i = 0; i < xTarget.length; i++) {
			try {
				xTarget[i] = Integer.parseInt(altOutcomeValueSequence[i]);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}		
		return xTarget;
	}

	@Override
	public DNF getWhyNotExplanation(String altOutcomeValue) {
		return getWhyNotExplanations(parseOutcomeValueSequence(altOutcomeValue));
	}
	
	public DNF getWhyNotExplanations(int[] xTarget) {
		List<ObservationVector> o = hmmEnactor.getObservations();
		int[] x = hmm.mostLikelyStateSequence(o);
		Reason conj = getWhyNotExplanation(o, x, xTarget, F_VERSION);
		
		DNF dnf = new DNF();
		dnf.add(conj);
		return dnf;
	}

	@Override
	public DNF getHowToExplanation(String altOutcomeValue) {
		return getHowToExplanations(parseOutcomeValueSequence(altOutcomeValue));
	}
	
	public DNF getHowToExplanations(int[] xTarget) {
		List<ObservationVector> o = hmmEnactor.getObservations();
		Reason conj = getWhyExplanation(o, xTarget, F_VERSION);
		
		DNF dnf = new DNF();
		dnf.add(conj);
		return dnf;
	}

	@Override
	public DNF getCertaintyExplanation() {
		List<ObservationVector> o = hmmEnactor.getObservations();
		int[] x = hmm.mostLikelyStateSequence(o);
		double certainty = hmm.probability(o, x);
		return new DNF(Parameter.instance(Query.QUESTION_CERTAINTY, certainty));
	}
	
	/* ================================================================================
	 * Internal methods to calculate evidences due to the HMM
	 * ================================================================================ */

}
