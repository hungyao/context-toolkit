package context.arch.discoverer.query.comparison;

//import context.arch.comm.DataObject;
//import context.arch.discoverer.ComponentDescription;
//import context.arch.discoverer.querySystem.ClassifierWrapper;
//import edu.cmu.intelligibility.hmm.HMMSupervised;

/**
 * Wrapper class of HMM classifier to minimize redundant calls to classify by multiple
 * EnactorReferences of the same Enactor.
 * 
 * EnactorReferences of the same Enactor should use the same instance of this class,
 * rather than create multiple instances.
 * 
 * TODO
 * Maybe this class just knows how to wrap a classification in a comparison
 * It is just a process facilitator, and not a data/model holder.
 * 
 * @author Brian Y. Lim
 *
 */
public class HmmComparison
{
//	
//	public static final String NAME = "HMMComparison";
//	
//	protected String lastOutcomeValue;
//	
//	public HMMComparison() {
//		super(NAME);
//	}
//	
//	public String getLastOutcomeValue() {
//		return lastOutcomeValue;
//	}
//
//	/**
//	 * As long as outcomeValue is not null, this means that a classification happened.
//	 * Actually, as long as classification is valid, it will always return a non-null value.
//	 * We just pass that along to the EnactorReference's ruleSatisfied, and
//	 * leave it to that to deal with the consequence of the specific value.
//	 * @param classifier
//	 * @param widgetState
//	 * @return null if classification process failed (e.g. some null value in an attribute)
//	 */
//	@Override
//	public Boolean compare(HMMSupervised hmm, ComponentDescription widgetState) {
//		lastOutcomeValue = classifier.classify(widgetState);
//		
//		// lastOutcomeValue != null; // return result, not whether it was null
//		if (lastOutcomeValue == null) { return null; }
//		else { return true; }
//	}
//
//	public static HMMComparison fromDataObject(DataObject data) {		
//		// TODO: does this even get called, or would it be expected in AbstractComparison?
//		return null;
//	}

}
