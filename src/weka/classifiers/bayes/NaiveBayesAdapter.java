package weka.classifiers.bayes;

import weka.estimators.Estimator;

/**
 * Trick class to get access to protected methods and fields of NaiveBayes, to get under its hood.
 * This hack would fail if ever the WEKA jar is signed, and requires all classes in the same package to be in the same directory or jar too.
 * @author Brian Y. Lim
 *
 */
public class NaiveBayesAdapter {
	
	protected NaiveBayes classifier;
	
	public NaiveBayesAdapter(NaiveBayes classifier) {
		this.classifier = classifier;
	}
	
	public NaiveBayes getClassifier() {
		return classifier;
	}
	
	/**
	 * The attribute estimators.
	 * @return [attrIndex][classIndex]
	 */
	public Estimator[][] getMDistributions() {
		return classifier.m_Distributions;
	}
	
	/**
	 * The class estimator.
	 * @return [classIndex]
	 */
	public Estimator getMClassDistribution() {
		return classifier.m_ClassDistribution;
	}

}
