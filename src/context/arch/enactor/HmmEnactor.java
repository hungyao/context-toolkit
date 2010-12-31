package context.arch.enactor;

import java.util.ArrayList;
import java.util.List;

import be.ac.ulg.montefiore.run.jahmm.Observation;
import be.ac.ulg.montefiore.run.jahmm.ObservationVector;
import context.arch.discoverer.query.AbstractQueryItem;
import context.arch.discoverer.query.HmmWrapper;
import context.arch.intelligibility.hmm.HmmExplainer;
//import context.arch.widget.SequenceWidget;

/**
 * Only one EnactorReference allowed.
 * 
 * @author Brian Y. Lim
 *
 */
public class HmmEnactor<O extends Observation> extends Enactor {

	protected HmmWrapper hmmWrapper;
	
	protected HmmEnactorReference<O> hmmER;

	protected List<String> outcomeValueSequence;
	protected List<String> inputsSequence;
	
	@SuppressWarnings("unchecked")
//	public HMMEnactor(Class<? extends SequenceWidget> inWidgetClass,
	public HmmEnactor(AbstractQueryItem<?,?> inWidgetSubscriptionQuery,
			AbstractQueryItem<?,?> outWidgetSubscriptionQuery, 
			String outcomeName,
			HmmWrapper hmmWrapper,
			String shortId) {
		super(inWidgetSubscriptionQuery, outWidgetSubscriptionQuery, outcomeName, shortId);
		
		this.hmmWrapper = hmmWrapper;

		// set up explainer
		setExplainer(new HmmExplainer((HmmEnactor<ObservationVector>) this));
	
		// set up for enactor reference
		hmmER = new HmmEnactorReference<O>(this);
		setReference(hmmER);
	}

	/**
	 * Refers to HMMSupervised to get the outcome values instead of multiple EnactorReferences, which this does not have.
	 * This returns the possible outcome values for each sequence step.
	 */
	@Override
	public List<String> getOutcomeValues() {
		return hmmWrapper.getOutcomeValues();
	}

	public List<String> getOutcomeValueSequence() {
		if (outcomeValueSequence == null) { return new ArrayList<String>(); }
		// create copy so that the original data doesn't get mutated by some other process
		return new ArrayList<String>(outcomeValueSequence);
	}
	public void setOutcomeValueSequence(List<String> valueSequence) {
		this.outcomeValueSequence = valueSequence;
		new RuntimeException("setOutcomeValueSequence = " + valueSequence);
	}

	public List<String> getInputsSequence() {
		// create copy so that the original data doesn't get mutated by some other process
		return new ArrayList<String>(inputsSequence);
	}
	public void setInputsSequence(List<String> inputsSequence) {
		this.inputsSequence = inputsSequence;
		new RuntimeException("setInputsSequence = " + inputsSequence);
	}
	
	public int getSequenceLength() {
		return hmmWrapper.getSequenceLength();
	}
	
	/**
	 * Really this is now a setReference(EnactorReference er),
	 * since HMMEnactor can only have one EnactorReference.
	 * @param er
	 * @return may be false if exceeded more than one EnactorReference, and would override existing one.
	 * @see #setReference(EnactorReference) which subclasses should use instead
	 */
	@Override
	@Deprecated
	public void addReference(EnactorReference er) {
//		throw new InvalidMethodException("ClassifierEnactor can only take one EnactorReference. Use setEnactorReference(EnactorReference er) instead.");
		// too troublesome to throw exception, though that is good design.
		
		if (enactorReferences.size() >= 1) {			
			enactorReferences.clear(); // remove previous entry
		}
		super.addReference(er);		
	}
	
	@SuppressWarnings("deprecation")
	protected void setReference(HmmEnactorReference<O> er) {
		addReference(er);
	}
	
	public EnactorReference getReference() {
		return enactorReferences.values().iterator().next().get(0); // get the only (first) entry
	}
	
	public HmmWrapper getHMM() {
		return hmmWrapper;
	}
	
	@SuppressWarnings("unchecked")
	public List<O> getObservations() {
		return (List<O>) hmmWrapper.extractObservations(this.getInWidgetState());
	}

}
