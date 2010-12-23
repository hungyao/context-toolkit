package context.arch.intelligibility.presenters;

import context.arch.enactor.Enactor;
import context.arch.intelligibility.DescriptiveExplainerDelegate;
import context.arch.intelligibility.Explainer;
import context.arch.intelligibility.Explanation;

/**
 * Manages the presentation of Explanation data structures to suit the application needs.
 * This needs to be subclassed to provide the appropriate rendering.
 * @author Brian Y. Lim
 *
 * @param <P> the class of the object that is rendered (e.g. string, JPanel, JSONObject)
 */
public abstract class Presenter<P> {
	
	/*
	 * TODO: not sure whether to decouple Enactor and Explainer from Presenter
	 * so that the Presenter can render explanations from any (and multiple) enactor(s)
	 */
	
	/**
	 * The enactor associated with the Presenter.
	 * The presenter can use the enactor to extract more information subsidiary to the explanation.
	 */
	protected Enactor enactor;

	/**
	 * The explainer associated with the Presenter.
	 * The presenter can use the explainer to extract more explanations to present a compound explanation:
	 * e.g. Certainty along with What.
	 */
	protected Explainer explainer;
	/**
	 * Descriptive explainer associated with the explainer.
	 * This is a convenience reference.
	 */
	protected DescriptiveExplainerDelegate descExplainer;
	
	/**
	 * 
	 * @param enactor to associate with the Presenter
	 */
	public Presenter(Enactor enactor) {
		this.enactor = enactor;
		this.explainer = enactor.getExplainer();
		this.descExplainer = explainer.getDescriptionExplainer();
	}
	
	/**
	 * Main method to use to 
	 * @param explanation
	 * @return
	 */
	public abstract P render(Explanation explanation);

}
