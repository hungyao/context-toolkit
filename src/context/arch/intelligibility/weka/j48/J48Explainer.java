package context.arch.intelligibility.weka.j48;

import java.util.HashMap;
import java.util.Map;

import weka.classifiers.trees.J48;
import context.arch.discoverer.query.ClassifierWrapper;
import context.arch.enactor.Enactor;
import context.arch.intelligibility.StaticExplainerDelegate;
import context.arch.intelligibility.expression.DNF;
import context.arch.intelligibility.weka.WekaExplainer;

/**
 * Explainer for the WEKA J48 decision tree classifier.
 * 
 * TODO support dynamic changes to classifier
 * @author Brian Y. Lim
 *
 */
public class J48Explainer extends WekaExplainer<J48> {
	
	private StaticExplainerDelegate delegate;
	
	public J48Explainer(Enactor enactor, ClassifierWrapper classifierWrapper) throws Exception {
		super(enactor, classifierWrapper);
		
		/*
		 * Create delegate to pre-generate explanations.
		 */
		delegate = new StaticExplainerDelegate(enactor) {
			@Override
			protected Map<String, DNF> initReasonsDNF() {
				try {
					return J48Parser.parse(classifier, header); // start parsing tree
				} catch (Exception e) {
					e.printStackTrace();
					return new HashMap<String, DNF>();
				}
			}
		};
	}

	@Override
	public DNF getWhyExplanation() {
		return delegate.getWhyExplanation();
	}

	@Override
	public DNF getWhyNotExplanation(String altOutcomeValue) {
		return delegate.getWhyNotExplanations(altOutcomeValue);
	}

	@Override
	public DNF getHowToExplanation(String altOutcomeValue) {
		return delegate.getHowToExplanations(altOutcomeValue);
	}

}
