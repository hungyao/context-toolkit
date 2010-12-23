package context.arch.intelligibility.rules;

import java.util.HashMap;
import java.util.Map;

import context.arch.enactor.Enactor;
import context.arch.enactor.EnactorReference;
import context.arch.intelligibility.Explainer;
import context.arch.intelligibility.StaticExplainerDelegate;
import context.arch.intelligibility.expression.DNF;
import context.arch.intelligibility.expression.Unknown;

/**
 * Gives explanations from a rules model. Takes the Enactor-EnactorReference-QueryItem structure.
 * One Enactor per application.
 * Multiple EnactorReferences per Enactor: supports multiple Outputs, one per EnactorReference, limit to not more than one EnactorReference per output
 * One AbstractQueryItem per EnactorReference: supports one rule per EnactorReference 
 * @author Brian Y. Lim
 *
 */
public class RulesExplainer extends Explainer {
	
	private StaticExplainerDelegate delegate;
	
	public RulesExplainer(Enactor enactor) {
		super(enactor);
		
		/*
		 * Create delegate to pre-generate explanations.
		 */
		delegate = new StaticExplainerDelegate(enactor) {
			@Override
			protected Map<String, DNF> initReasonsDNF() {
				Map<String, DNF> valueTraces = new HashMap<String, DNF>();
				
				for (EnactorReference ref : enactor.getReferences()) {
					valueTraces.put(
							ref.getOutcomeValue(), 
							// parse query from each enactor reference
							QueryItemParser.parse(ref.getConditionQuery()));
				}
				
				return valueTraces;
			}
		};
	}
	
	/**
	 * Certainty is currently not defined for the Enactor rules framework.
	 * Or it could be considered absolutely certain.
	 * Should be overridden by subclass if there is a semantic uncertainty.
	 * @return {@link Unknown#singleton }
	 */
	@Override
	public DNF getCertaintyExplanation() {
		return DNF.UNKNOWN;
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
