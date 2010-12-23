package context.arch.discoverer.query.comparison;

import context.arch.comm.DataObject;
import context.arch.discoverer.ComponentDescription;
import context.arch.discoverer.query.ClassifierWrapper;

/**
 * <p>
 * Wrapper class of Weka classifier to minimize redundant calls to classify by multiple
 * EnactorReferences of the same Enactor.
 * </p>
 * <p>
 * EnactorReferences of the same Enactor should use the same instance of this class,
 * rather than create multiple instances.
 * </p>
 * 
 * @author Brian Y. Lim
 *
 */
public class ClassifierComparison extends AbstractComparison<ClassifierWrapper, ComponentDescription> {
	
	public static final String NAME = "ClassifierComparison";
	
	protected String lastOutcomeValue;
	
	public ClassifierComparison() {
		super(NAME,
				ClassifierWrapper.class, ComponentDescription.class);
	}
	
	public String getLastOutcomeValue() {
		return lastOutcomeValue;
	}

	/**
	 * As long as outcomeValue is not null, this means that a classification happened.
	 * Actually, as long as classification is valid, it will always return a non-null value.
	 * We just pass that along to the EnactorReference's ruleSatisfied, and
	 * leave it to that to deal with the consequence of the specific value.
	 * @param classifier
	 * @param widgetState
	 * @return null if classification process failed (e.g. some null value in an attribute)
	 */
	@Override
	public Boolean compare(ClassifierWrapper classifier, ComponentDescription widgetState) {
		lastOutcomeValue = classifier.classify(widgetState);
		
		// lastOutcomeValue != null; // return result, not whether it was null
		if (lastOutcomeValue == null) { return null; }
		else { return true; }
	}

	@SuppressWarnings("unchecked")
	public static ClassifierComparison fromDataObject(DataObject data) {		
		// TODO: does this even get called, or would it be expected in AbstractComparison?
		return null;
	}

}
