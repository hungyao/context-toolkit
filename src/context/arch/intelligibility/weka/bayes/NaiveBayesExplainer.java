package context.arch.intelligibility.weka.bayes;

import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.bayes.NaiveBayesAdapter;
import weka.core.Attribute;
import weka.core.Instance;
import weka.estimators.Estimator;
import weka.estimators.NormalEstimator;
import context.arch.discoverer.query.ClassifierWrapper;
import context.arch.enactor.Enactor;
import context.arch.intelligibility.Explanation;
import context.arch.intelligibility.expression.DNF;
import context.arch.intelligibility.expression.Parameter;
import context.arch.intelligibility.expression.Reason;
import context.arch.intelligibility.expression.Unknown;
import context.arch.intelligibility.query.AltQuery;
import context.arch.intelligibility.query.Query;
import context.arch.intelligibility.weka.WekaExplainer;

/**
 * Explainer for the WEKA Naive Bayes classifier.
 * 
 * @author Brian Y. Lim
 *
 */
public class NaiveBayesExplainer extends WekaExplainer<NaiveBayes> {

	public static final String QUESTION_WHY_LESS = "Why Less";
	public static final String QUESTION_HOW_TO_IF = "How To If";
	public static final String QUESTION_HOW_TO_INPUTS = "How To Inputs";
	
	protected NaiveBayesAdapter nbAdapter;
	
	public NaiveBayesExplainer(Enactor enactor, ClassifierWrapper classifierWrapper) throws Exception {
		super(enactor, classifierWrapper);
		
		nbAdapter = new NaiveBayesAdapter(classifier);
		m_Distributions = nbAdapter.getMDistributions();
		m_ClassDistribution = nbAdapter.getMClassDistribution();
	}
	
	/**
	 * Overridden to add more explanation types.
	 */
	@Override
	public Explanation getExplanation(Query query) {
		String question = query.getQuestion();

		if (question == null) {
			return new Explanation(query, 
					new DNF(Unknown.singleton));		
		}
		
		if (question.equals(QUESTION_WHY_LESS)) {
			return new Explanation(query, 
					new DNF(getWhyLessExplanation()));
		}
		else if (question.equals(QUESTION_HOW_TO_IF)) {
			String altOutcomeValue = ((AltQuery)query).getAltOutcomeValue();
			return new Explanation(query, 
					getHowToIfExplanations(altOutcomeValue));
		}
		else if (question.equals(QUESTION_HOW_TO_INPUTS)) {
			String altOutcomeValue = ((AltQuery)query).getAltOutcomeValue();
			return new Explanation(query, 
					new DNF(getHowToInputsExplanation(altOutcomeValue)));			
		}
		else {
			return super.getExplanation(query);
		}
	}
	
	/**
	 * Why Reason: this classValue has the highest probability among others. Their values are...
	 * Returns conjunction of parameter(classValueName, probValue)
	 */
	public Reason getWhyLessExplanation() {
		Instance instance = classifierWrapper.extractInstance(enactor.getInWidgetState());		
		
		try {
			Reason probs = new Reason();
			double[] distroForInstance = classifier.distributionForInstance(instance);

			for (int i = 0; i < distroForInstance.length; i++) {
				String name = classAttribute.value(i);
				Parameter<?> p = Parameter.instance(name, distroForInstance[i]);
				probs.add(p);
			}

			return probs;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}

	/**
	 * Why Reason: gives "weights of evidence"
	 * Conjunction of total evidence for instance: 
	 * 	g(x,c) = h(c) + sum_f{f(x_f,c)} 
	 * 
	 * where f(x_f,c) is function getEvidenceAttribute, and
	 * h(c) is function getEvidencePrior
	 * 
	 * Assume that the class value has been classified and set into the instance
	 */
	@Override
	public DNF getWhyExplanation() {
		Instance instance = classifierWrapper.extractInstance(enactor.getInWidgetState());		
		String classValue = enactor.getOutcomeValue(); //instance.classAttribute().value((int)instance.classValue());
		
		DNF whyTraces = new DNF();
		whyTraces.add(getWhyExplanation(instance, classValue));
		return whyTraces;
	}

	/**
	 * Gives weight of evidence for a specific classValue, regardless whether that was the classified one.
	 * Can be used to obtain the evidence for Why Not, to supply to a UI displaying fully the evidence for Why and Why Not.
	 * Does not enforce further interpretations or processing.
	 * @param instance
	 * @param classValue
	 * @return
	 */
	protected Reason getWhyExplanation(Instance instance, String classValue) {
		Reason list = new Reason();

		// total sum of evidence
		double totalEvidence = 0; // sum through method
		
		// calculate and add evidence due to prior
		double priorEvidence = getEvidencePrior(classValue);
		list.add(Parameter.instance(LIKELIHOOD, priorEvidence));
		totalEvidence += priorEvidence;
		
		// calculate and add evidences due to each attribute/feature
		for (int f = 0; f < instance.numAttributes(); f++) {
			if (f == instance.classIndex()) { continue; } // skip class attribute
			String attrName = instance.attribute(f).name();
			double attrEvidence = getEvidenceAttribute(classValue, f, instance);
			list.add(Parameter.instance(attrName, attrEvidence));
			totalEvidence += attrEvidence;
		}

		// average evidence; use average instead of total, to normalize the "lengths" for visualization
		double avgEvidence = totalEvidence / (1 + instance.numAttributes());
		list.add(0, Parameter.instance(AVERAGE, avgEvidence)); // add to front
		
		return list;
	}

	/**
	 * Gets Why explanation for current/actual classValue, and Why explanation for desired whyNotClassValue. 
	 * Compares the two sets of evidences by taking a delta/diff and returning the deltas in a conjunction.
	 * 
	 * If delta > 0, then evidence shows f(x_f) was too high to get whyNotClassValue
	 * 
	 * Note that this is equivalent to just treating the problem as 2-class with values whyClassValue and whyNotClassValue.
	 * One can see this is true because after taking a delta, all evidence from other classes are canceled out, and what is left is due to the two.
	 * While this is not as computationally efficient, it is equivalent.
	 * 
	 * @param altOutcomeValue to ask about
	 * @return only one reason (i.e. Disjunction only has one child).
	 */
	@Override
	public DNF getWhyNotExplanation(String altOutcomeValue) {
		Instance instance = classifierWrapper.extractInstance(enactor.getInWidgetState());		
		String whyNotClassValue = altOutcomeValue;
		
		Reason reason = new Reason();
		String whyClassValue = enactor.getOutcomeValue();

		// total sum of evidence
		double dTotalEvidence = 0; // sum through method
		
		// calculate and add evidence due to prior
		double whyPriorEvidence = getEvidencePrior(whyClassValue);
		double whyNotPriorEvidence = getEvidencePrior(whyNotClassValue);
		double dPriorEvidence = whyNotPriorEvidence - whyPriorEvidence; // delta = target - actual				
		dPriorEvidence = -dPriorEvidence; // flip to retain the same directional sense as for Why
		reason.add(Parameter.instance(LIKELIHOOD, dPriorEvidence));
		dTotalEvidence += dPriorEvidence;
		
		/*
		 * Get conjunctions for Why and Why Not.
		 * Both should be of the same size with the same attribute order.
		 * Iterate through and process deltas
		 */
		for (int f = 0; f < instance.numAttributes(); f++) {
			if (f == instance.classIndex()) { continue; } // skip class attribute
			String attrName = instance.attribute(f).name();
			
			double whyAttrEvidence = getEvidenceAttribute(whyClassValue, f, instance);
			double whyNotAttrEvidence = getEvidenceAttribute(whyNotClassValue, f, instance);
			/*
			 * Why not whyNotClassValue? Because attrVal was too high (delta>0) and caused whyClassValue instead of whyNotClassValue.
			 * The aforementioned is true when whyAttrEvidence > whyNotAttrEvidence.
			 */
			double dAttrEvidence = whyNotAttrEvidence - whyAttrEvidence; // delta = target - actual						
			dAttrEvidence = -dAttrEvidence; // flip to retain the same directional sense as for Why
			
//			System.out.println(whyNotAttrEvidence + " - " + whyAttrEvidence + " = " + dAttrEvidence);
			
			reason.add(Parameter.instance(attrName, dAttrEvidence));
			dTotalEvidence += dAttrEvidence;
		}

		// average evidence; use average instead of total, to normalize the "lengths" for visualization
		double dAvgEvidence = dTotalEvidence / (1 + instance.numAttributes());
		reason.add(0, Parameter.instance(AVERAGE, dAvgEvidence)); // add to front

		DNF reasons = new DNF();
		reasons.add(reason);
		return reasons;
	}

	/**
	 * This is similar to the "Ranks of Evidence" explanation capability of ExplainD [Poulin et al. 2004].
	 * This returns a conjunction of evidence of all attributes at various values.
	 * For nominal attributes, there would be one evidence per attribute value.
	 * For numeric attributes, there would be one evidence for each attribute, with the attribute value normalized to unity (one).
	 * 
	 * Users can see which weights are more important, and mentally add them up to see if the threshold is exceeded.
	 * Ultimately, this How To explanation would be more useful in combination with the What If explanation, 
	 * so that the user can explore a suitable solution, given that this How To explanation is only a *hint*, and not a solution.  
	 * 
	 * @param classValue to ask about
	 * @return only one reason (i.e. Disjunction only has one child).
	 */
	@Override
	public DNF getHowToExplanation(String classValue) {
		Reason reason = new Reason();

		// total sum of evidence
		double totalEvidence = 0; // sum through method
		
		// calculate and add evidence due to prior
		double priorEvidence = getEvidencePrior(classValue);
		reason.add(Parameter.instance(LIKELIHOOD, priorEvidence));
		totalEvidence += priorEvidence;
		
		// calculate and add evidences due to each attribute/feature value
		for (int f = 0; f < header.numAttributes(); f++) {
			if (f == header.classIndex()) { continue; } // skip class attribute
			Attribute attr = header.attribute(f);
			String attrName = attr.name();
			
			if (attr.type() == Attribute.NUMERIC) {
				double attrEvidence = getEvidenceAttributeNumericValue(classValue, f);
				reason.add(Parameter.instance(attrName, attrEvidence));
				totalEvidence += attrEvidence;
			}
			else if (attr.type() == Attribute.NOMINAL) {
				// iterate through nominal values and add
				double attrEvidence = 0;
				for (int v = 0; v < attr.numValues(); v++) {
					attrEvidence = getEvidenceAttributeNominalValue(classValue, f, v);
					attrName += "_" + attr.value(v); // enhance name
					reason.add(Parameter.instance(attrName, attrEvidence));
//					totalEvidence += attrEvidence; // TODO: not quite right here! Need to select only one
				}
				totalEvidence += attrEvidence; // TODO: at the moment, just take the last one as selected
			}
		}

		// average evidence; use average instead of total, to normalize the "lengths" for visualization
		double avgEvidence = totalEvidence / (1 + header.numAttributes());
		reason.add(0, Parameter.instance(AVERAGE, avgEvidence)); // add to front

		DNF reasons = new DNF();
		reasons.add(reason);
		return reasons;
	}

	/**
	 * Returns a conjunction of mean feature values for predicting the class value.
	 * Since Naive Bayes considers numeric attributes as Gaussian distributions, this essentially shows the "average" case of features to produce the class value.
	 * @param classValue
	 * @return
	 */
	public Reason getHowToInputsExplanation(String classValue) {
		Reason list = new Reason();

		// calculate and add mean values due of each attribute/feature value given the classValue
		for (int f = 0; f < header.numAttributes(); f++) {
			if (f == header.classIndex()) { continue; } // skip class attribute
			Attribute attr = header.attribute(f);
			String attrName = attr.name();
			
			if (attr.type() == Attribute.NUMERIC) {
				double attrMean = getMeanAttributeNumericValue(classValue, f);
				list.add(Parameter.instance(attrName, attrMean));
			}
			// TODO: figure out how to obtain most likely nominal value
//			else if (attr.type() == Attribute.NOMINAL) {
//				// iterate through nominal values and add
//				double attrEvidence = 0;
//				for (int v = 0; v < attr.numValues(); v++) {
//					attrEvidence = getEvidenceAttributeNominalValue(classValue, f, v);
//					attrName += "_" + attr.value(v); // enhance name
//					list.add(Parameter.instance(attrName, new Value(attrEvidence, Value.NUMERIC_TYPE)));
//				}
//			}
		}
		
		return list;
	}

	public DNF getHowToIfExplanations(String altOutputValue) {
		Instance instance = classifierWrapper.extractInstance(enactor.getInWidgetState());		
		
		Attribute attrToCalc = null;
		for (int i = 0; i < instance.numAttributes(); i++) {
			if (instance.isMissing(i)) { 
				attrToCalc = instance.attribute(i);
				break;
			}
		}
		if (attrToCalc == null) { return null; } // none missing, so nothing to determine for how-to
		
		/*
		 * TODO
		 * Calculate the cumulative weights from priors and all attributes except the selected one.
		 * If attribute is numeric,
		 * 	If threshold (g>0) is not yet reached, then find lower bound of x_f with f(x_f) to pass threshold
		 * 	If threshold already passed, and if f(x_f) is negative, then find upper bound
		 * 	There may not be a solution if x_f is bounded
		 * If attribute is nominal,
		 * 	Trial each nominal value to see which passes the threshold
		 */
		
		// TODO Auto-generated method stub
		return null;
	}
	
	/* ================================================================================
	 * Internal methods to calculate evidences due to naive Bayes model
	 * ================================================================================ */
	
	/**
	 * By [attrIndex][classIndex]
	 * Posterior probabilities
	 */
	protected Estimator[][] m_Distributions;
	/**
	 * By classIndex
	 * Prior probabilities of class values
	 */
	protected Estimator m_ClassDistribution;
	
	/**
	 * Evidence as described in ExplainD [Poulin et al. 2006]. For Naive Bayes, this binarizes the class values into one-against-all, and
	 * calculates the log-odds ratio as a discriminant:
	 * 	g(x) = log(P(c|x)/P(!c|x))
	 * 	g(x) > 1 when c is more probable, and <=1 otherwise.
	 * 
	 * For a multi-class output, output is c_i when 
	 * 	argmax(P(c|x)) = P(c_i|x)
	 * This means for any j and k != i,
	 * 	P(c_i|x) > P(c_j|x), and P(c_i|x) > P(c_k|x)
	 * Using the transitive property of inequalities, these can be multiplied together to give 
	 * 	[P(c_i|x)]^2 > P(c_j|x) + P(c_k|x)
	 * 
	 * This allows us to generalize f to:
	 * 	g(x) = log([P(c_i|x)]^(N-1) / product{P(c_j|x)}), where j != i, N is the number of class values
	 * 
	 * g(x) for a multi-class classifier is actually a function of the inputs x, and the class value being considered c. So we rewrite as f(x,c)
	 * g(x,c) tells us the evidence of 
	 * 
	 * Now, we seek to find the evidence that each attribute has to "vote" for class value c.
	 * 	g(x) = log(P(c|x)/P(!c|x))
	 * 	     = log(P(c)/P(!c)) * log(product{P(x_i|c)/P(x_i|!c)})
	 *       = log(P(c)/P(!c)) + sum{log(P(x_i|c)/P(x_i|!c))}
	 * 	g(x,c) = log([P(c_i|x)]^(N-1) / product{P(c_j|x)})
	 *         = log( [P(c_i)*product{P(x_f|c_i)/P(x)}]^(N-1) / product{P(c_j)*product{P(x_f|c_j)/P(x)}} }, then we can cancel the P(x)'s
	 *         = log( [P(c_i)*product{P(x_f|c_i)}]^(N-1) / product{P(c_j)*product{P(x_f|c_j)}} }
	 *         = (N-1)*log(P(c_i)*product{P(x_f|c_i)}) - sum_j{log(P(c_j)*product{P(x_f|c_j)})}, where sum_j is sum by j, it sums over N-1
	 *         = sum_j{log(P(c_i)/P(c_j))} + sum_f{sum_j{log(P(x_f|c_i)/P(x_f|c_j))}}, note that i is independent of j, so sum_j over a constant is just a multiplication by (N-1); sum_f is sum over attributes (features)
	 * 
	 * We can see that the left term on the RHS is independent of x, and consists of prior probabilities. We label this as:
	 * 	h(c) = sum_j{log(P(c_i)/P(c_j))}
	 * 
	 * The right term on the RHS depends on x and its features, so that is the evidence:
	 * 	f(x,c) = sum_f{sum_j{P(x_f|c_i)/P(x_f|c_j)}}
	 * Each feature, x_f, has evidence:
	 * 	f(x_f,c) = sum_j{log(P(x_f|c_i)/P(x_f|c_j))}
	 * 			 = (N-1)*log(P(x_f|c_i)) - log(product_j{P(x_f|c_j)}) // computational form
	 * 
	 * Total evidence:
	 * 	g(x) = h(c) + sum_f{f(x_f,c)} 
	 * 
	 * The more positive the evidence, the larger the evidence that this feature "votes" for the classValue.
	 * Negative evidence votes against.
	 * 
	 * @param classValue c_i
	 * @param attributeIndex index of x_f
	 * @param instance
	 * @return f(x_f,c)
	 */
	protected double getEvidenceAttribute(String classValue, int attributeIndex, Instance instance) {
		return getEvidenceAttributeValue(classValue, attributeIndex, instance.value(attributeIndex));
	}

	/**
	 * To get instance independent evidence due to the attribute taking a certain numeric value.
	 * Numeric value calibrated to mean. No point testing 1 standard deviation away.
	 * @param classValue
	 * @param attributeIndex
	 * @return
	 */
	protected double getEvidenceAttributeNumericValue(String classValue, int attributeIndex) {
		int classValueIndex = header.classAttribute().indexOfValue(classValue);		
		NormalEstimator estimator = (NormalEstimator)m_Distributions[attributeIndex][classValueIndex];
		
		double mean = estimator.getMean();
		double value = mean;				
//		System.out.println("mean(" + header.attribute(attributeIndex).name() + ") = " + value);
		
		double evidence = getEvidenceAttributeValue(classValue, attributeIndex, value);
		return evidence;
	}

	/**
	 * To get instance independent mean value due to the attribute taking a certain numeric value.
	 * Used in How To What explanation.
	 * @param classValue
	 * @param attributeIndex
	 * @return
	 */
	protected double getMeanAttributeNumericValue(String classValue, int attributeIndex) {
		int classValueIndex = header.classAttribute().indexOfValue(classValue);		
		NormalEstimator estimator = (NormalEstimator)m_Distributions[attributeIndex][classValueIndex];
		
		double mean = estimator.getMean();
		return mean;
	}

	/**
	 * To get instance independent evidence due to the attribute taking a certain nominal value.
	 * @param classValue
	 * @param attributeIndex
	 * @param valueIndex
	 * @return
	 */
	protected double getEvidenceAttributeNominalValue(String classValue, int attributeIndex, int valueIndex) {
		double value = valueIndex; // index of nominal value would map straight to the weka double index
		return getEvidenceAttributeValue(classValue, attributeIndex, value);
	}

	/** constant to take a very tiny value for Laplace correction, when p=0 */
	public static final double EPSILON = 1e-10; // keep too small, then log's may become too big
	
	public static final String AVERAGE = "Average";
	public static final String LIKELIHOOD = "Likelihood";
	
	/**
	 * Get the evidence due to a specific attribute value.
	 * @param classValue
	 * @param attributeIndex
	 * @param valueIndex uses the Weka internal double format
	 * @return
	 */
	protected double getEvidenceAttributeValue(String classValue, int attributeIndex, double value) {
		int classValueIndex = header.classAttribute().indexOfValue(classValue);
		int N = header.numClasses();
		
		double[] prob_xf_c = new double[N];
		double product_p_xf_cj = 1; // product{P(x_f|c_j)}
		for (int j = 0; j < prob_xf_c.length; j++) {
			prob_xf_c[j] = m_Distributions[attributeIndex][j].getProbability(value);
			// note that prob_xf_c[j] where j=classValueIndex is P(x_f|c_i)
			
			/*
			 * May need Laplace smoothing because some may be 0: p = 1/N(c_j)
			 * However, this would still lead to a small p, and cause some log to have a very large magnitude.
			 * Nevertheless, these extremities would probably cancel out on average; just that specific values may not have valid values.
			 */
			if (prob_xf_c[j] == 0) {
				prob_xf_c[j] = EPSILON;
				          
//				String name = trainingSet.attribute(attributeIndex).name();
//				double mean = ((NormalEstimator)m_Distributions[attributeIndex][j]).getMean();
//				double sd = ((NormalEstimator)m_Distributions[attributeIndex][j]).getStdDev();
//				System.out.println("\t\tprob_xf_c[" + name + ",(" + classValue + ")] = " + 0);
//				System.out.println("\t\t mean = " + mean);
//				System.out.println("\t\t sd = " + sd);
			}
						
			if (j != classValueIndex) { // exclude class of interest				
				product_p_xf_cj *= prob_xf_c[j];
			}
		}
		
		double evidence = (N-1)*Math.log(prob_xf_c[classValueIndex]) - Math.log(product_p_xf_cj);
//		System.out.println("\t prob_xf_c[classValueIndex]: " + prob_xf_c[classValueIndex]);
//		System.out.println("\t product_p_xf_cj: " + product_p_xf_cj);
//		System.out.println("\t Math.log(prob_xf_c[classValueIndex]): " + Math.log(prob_xf_c[classValueIndex]));
//		System.out.println("\t evidence: " + evidence);		
		return evidence;
	}
	
	/**
	 * Get the evidence due to the prior probability.
	 * 
	 * We can see that the classification decision is also affected by the prior probabilities, so the left term on the RHS
	 * 	h(c) = sum_j{log(P(c_i)/P(c_j))}
	 * 		 = (N-1)*log(P(c_i) - log(product_j{P(c_j)}) // computational form
	 * 
	 * This indicates whether, in general, the data is predisposed (via prior probabilities) to "voting" for this class.
	 * The more positive the evidence, the larger the evidence that this class value is predisposed to be voted.
	 * Negative evidence votes against.
	 * 
	 * @param classValue
	 * @return
	 */
	protected double getEvidencePrior(String classValue) {
		int classValueIndex = header.classAttribute().indexOfValue(classValue);
		int N = header.numClasses();

		double[] prob_c = new double[N]; // P(c_j)
		double product_p_cj = 1; // product{P(c_j)}
		for (int j = 0; j < prob_c.length; j++) {
			prob_c[j] = m_ClassDistribution.getProbability(j);
			
			if (j != classValueIndex) { // exclude class of interest
				product_p_cj *= prob_c[j];
			}
		}
		
		double evidence = (N-1)*Math.log(prob_c[classValueIndex]) - Math.log(product_p_cj);
		return evidence;
	}

}
