package context.arch.enactor;

import java.util.List;

import be.ac.ulg.montefiore.run.jahmm.Observation;
//import be.ac.ulg.montefiore.run.jahmm.ObservationVector;
import context.arch.discoverer.ComponentDescription;
import context.arch.discoverer.query.HmmQueryItem;
import context.arch.discoverer.query.HmmWrapper;

/**
 * An EnactorReference that uses a Hidden Markov Model (HMM) for inference.
 * Similar to {@link ClassifierEnactorReference}, but also deals with handling sequential output.
 * 
 * @author Brian Y. Lim
 *
 */
public class HmmEnactorReference<O extends Observation> extends EnactorReference {

	protected HmmWrapper hmmWrapper;
	protected HmmEnactor<O> hmmEnactor;
	
	public HmmEnactorReference(HmmEnactor<O> enactor) {
		super(enactor, 
				new HmmQueryItem(enactor.getHMM()), 
				"HmmOutcomeValue"); // not associated with any single outcomeValue
		
		this.hmmEnactor = enactor;
		this.hmmWrapper = enactor.getHMM();
	}
	
	/**
	 * Since this is tied to multiple outcomeValues,
	 * need to extract current value from the EnactorComponentInfo
	 * that was just stored during the comparison.
	 */
	@Override
	public void evaluateComponent(EnactorComponentInfo eci) {
		ComponentDescription widgetState = eci.getCurrentState();
		enactor.setInWidgetState(widgetState);
		
		List<String> valueSequence = ((HmmQueryItem)conditionQuery).getLastOutcomeValueSequence();
		hmmEnactor.setOutcomeValueSequence(valueSequence);
		
		outcomeValue = valueSequence.get(valueSequence.size() - 1); // just the last value of the sequence
		enactor.setOutcomeValue(outcomeValue); 
		this.setOutcomeValue(outcomeValue);
		
		conditionSatisfied(eci); // subclass can do whatever
		
		enactor.fireComponentEvaluated(eci); // notify listeners after reacting		
	}

}
