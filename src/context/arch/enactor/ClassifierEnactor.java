package context.arch.enactor;

import java.util.Collection;

import context.arch.discoverer.query.AbstractQueryItem;
import context.arch.discoverer.query.ClassifierWrapper;
import context.arch.intelligibility.weka.WekaExplainer;

/**
 * Enactor that uses a WEKA classifier to model reasoning.
 * Only one EnactorReference allowed that is automatically set up in the constructor.
 * 
 * @author Brian Y. Lim
 *
 */
public class ClassifierEnactor extends Enactor {
	
	protected ClassifierWrapper classifier;
	
	protected ClassifierEnactorReference classifierER; 
	
	/**
	 * Creates an enactor with the reference already set up
	 * @param inWidgetSubscriptionQuery
	 * @param outWidgetSubscriptionQuery
	 * @param outcomeName
	 * @param classifier
	 * @param shortId
	 */
	public ClassifierEnactor(AbstractQueryItem<?,?> inWidgetSubscriptionQuery, AbstractQueryItem<?,?> outWidgetSubscriptionQuery, 
			String outcomeName, ClassifierWrapper classifier, String shortId) {
		super(inWidgetSubscriptionQuery, outWidgetSubscriptionQuery, outcomeName, shortId);
		this.classifier = classifier;
	
		// set up for enactor reference, so subclass does not have to
		classifierER = new ClassifierEnactorReference(this);
		setReference(classifierER);
		
		// set up explainer
		try {
			setExplainer(WekaExplainer.instance(this, classifier));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Refers to ClassifierWrapper to get the outcome values instead of multiple EnactorReferences, which this does not have
	 */
	@Override
	public Collection<String> getOutcomeValues() {
		return classifier.getOutcomeValues();
	}
	
	/**
	 * Really this is now a setReference(EnactorReference er),
	 * since ClassifierEnactor can only have one EnactorReference.
	 * @param er
	 * @return may be false if exceeded more than one EnactorReference, and would override existing one.
	 * @see #setReference(EnactorReference) which subclasses should use instead
	 */
	@Override
	@Deprecated
	public void addReference(String outcomeValue, EnactorReference er) {
//		throw new InvalidMethodException("ClassifierEnactor can only take one EnactorReference. Use setEnactorReference(EnactorReference er) instead.");
		// too troublesome to throw exception, though that is good design.
		
		if (enactorReferences.size() >= 1) {			
			enactorReferences.clear(); // remove previous entry
		}
		super.addReference(outcomeValue, er);		
	}
	
	protected void setReference(ClassifierEnactorReference er) {
		addReference("singleton", er);
	}
	
	public EnactorReference getReference() {
		return enactorReferences.values().iterator().next().get(0); // get the only (first) entry
	}
	
	public ClassifierWrapper getClassifier() {
		return classifier;
	}

}
