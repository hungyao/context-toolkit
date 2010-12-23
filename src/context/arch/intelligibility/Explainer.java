package context.arch.intelligibility;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import context.arch.discoverer.ComponentDescription;
import context.arch.discoverer.query.AbstractQueryItem;
import context.arch.discoverer.query.HmmQueryItem;
import context.arch.enactor.Enactor;
import context.arch.enactor.EnactorReference;
import context.arch.enactor.HmmEnactorReference;
import context.arch.intelligibility.expression.DNF;
import context.arch.intelligibility.expression.Parameter;
import context.arch.intelligibility.expression.Reason;
import context.arch.intelligibility.query.AltQuery;
import context.arch.intelligibility.query.Query;
import context.arch.intelligibility.query.WhatIfQuery;
import context.arch.intelligibility.reducers.Reducer;
import context.arch.storage.Attributes;
import context.arch.widget.Widget;

/**
 * The Explainer class generates explanations of various question types from a Query.
 * This provides default implementation for model-independent explanations (e.g. What, When, Inputs, Outputs), 
 * and descriptive explanations (through DescriptiveExplanationDelegate).
 * This needs to be subclassed to implement model-dependent explanations (e.g. Why, Why Not, How To, Certainty).
 * @author Brian Y. Lim
 * @see Query
 * @see DescriptiveExplainerDelegate
 */
public abstract class Explainer {

	/**
	 * The enactor that this explainer is explaining.
	 */
	protected Enactor enactor;
	
	/** 
	 * Set to static so that it is a global variable.
	 * Defaults as DescriptiveExplainerDelegate, but can be replaced.
	 */
	protected DescriptiveExplainerDelegate descExplainer;
	
	/**
	 * 
	 * @param enactor that this explainer is explaining
	 */
	public Explainer(Enactor enactor) {
		this.enactor = enactor;
	}
	
	public DescriptiveExplainerDelegate getDescriptionExplainer() {
		// lazy loading
		if (descExplainer == null) {
			descExplainer = new DescriptiveExplainerDelegate();
		}
		return descExplainer;
	}
	public void setDescriptionExplainer(DescriptiveExplainerDelegate descExplainer) {
		this.descExplainer = descExplainer;
	}

	/**
	 * Convenience method to apply a reduction to the explanation while getting it.
	 * @param query
	 * @param widgetState
	 * @param reducer
	 * @return
	 */
	public Explanation getExplanation(Query query, Reducer reducer) {
		Explanation explanation = getExplanation(query);
		explanation = reducer.apply(explanation);
		return explanation;
	}
	
	/**
	 * This is the main method to be used to get Explanations from explainers
	 * by supplying a Query.
	 * It should be extended by subclasses to support more types of question.
	 * @param query containing the question about a context regarding a certain time
	 * @return the generated Explanation corresponding to the Query
	 */
	public Explanation getExplanation(Query query) {	
		if (query == null) {
			return new Explanation(query, DNF.UNKNOWN);	
		}
		
		String question = query.getQuestion();
		String context = query.getContext();
//		System.out.println("Explainer.getExplanation question = " + question);
		
		/*
		 * Model-independent explanations
		 */
		
		if (question == null) {
			return new Explanation(query, DNF.UNKNOWN);		
		}
		
		if (question.equals(Query.QUESTION_WHAT)) { // output value
			return new Explanation(query, 
					new DNF(getWhatExplanation(context)));
		}
		else if (question.equals(Query.QUESTION_WHEN)) {
			return new Explanation(query, 
					new DNF(getWhenExplanation()));
		}
		else if (question.equals(WhatIfQuery.QUESTION_WHAT_IF)) {
			return new Explanation(query, 
					new DNF(getWhatIfExplanation(((WhatIfQuery)query).getInputs())));
		}
		else if (question.equals(Query.QUESTION_INPUTS)) { // for convenience, return names and values of inputs
			return new Explanation(query, 
					new DNF(getInputsExplanation()));
		}
		else if (question.equals(Query.QUESTION_OUTPUTS)) {
			return new Explanation(query, 
					getOutputsExplanation());
		}
		else if (question.equals(Query.QUESTION_CERTAINTY)) {
			return new Explanation(query, 
					getCertaintyExplanation());
		}
		
		/*
		 * Descriptive explanations
		 */
		
		else if (question.equals(Query.QUESTION_DEFINITION)) {
			return new Explanation(query, 
					new DNF(getDefinitionExplanation(context))
					);
		}
		else if (question.equals(Query.QUESTION_RATIONALE)) {
			return new Explanation(query, 
					new DNF(descExplainer.getRationaleExplanation(context)));
		}
		else if (question.equals(Query.QUESTION_PRETTY_NAME)) {
			return new Explanation(query, 
					new DNF(descExplainer.getPrettyNameExplanation(context)));
		}
		else if (question.equals(Query.QUESTION_UNIT)) {
			return new Explanation(query, 
					new DNF(descExplainer.getUnitExplanation(context)));
		}
		
		/*
		 * Model-dependent explanations
		 */
		
		if (question.equals(Query.QUESTION_WHY)) {
			return new Explanation(query, 
					getWhyExplanation());
		}
		else if (question.equals(AltQuery.QUESTION_WHY_NOT)) {
			String altOutcomeValue = ((AltQuery)query).getAltOutcomeValue();
			return new Explanation(query, 
					getWhyNotExplanation(altOutcomeValue));
		}
		else if (question.equals(AltQuery.QUESTION_HOW_TO)) {
			String altOutcomeValue = ((AltQuery)query).getAltOutcomeValue();
			return new Explanation(query, 
					getHowToExplanation(altOutcomeValue));
		}
//		else if (question.equals(Query.QUESTION_CONTROL)) {
//			// TODO maybe this should be implemented at the subclass level, which is application domain dependent?
//			// or can just use the EnactorParameters framework
//		}
		
		/*
		 * Explanation unknown
		 */		
		return new Explanation(query, DNF.UNKNOWN);
	}
	
	/* ----------------------------------------------------------------------------------------
	 * Model-Independent explanations
	 * ---------------------------------------------------------------------------------------- */

	/**
	 * Get the current value of an attribute. This is normally the outcome value (i.e. of
	 * the output attribute), but may also refer to an input attribute, depending on the name
	 * of the context.
	 * @param <T> the type of the value of the context
	 * @param context to retrieve the value of, normally the outcome name (i.e. {@link Enactor#getOutcomeName()}, 
	 * but may also be an input attribute name.
	 * @return Parameter<T> where name is context, and value is of the context
	 */
	@SuppressWarnings("unchecked")
	public <T extends Comparable<? super T>> Parameter<T> getWhatExplanation(String context) {
		T value;
//		System.out.println("context = " + context);
//		System.out.println("enactor.containsOutAttribute(context) = " + enactor.containsOutAttribute(context));
//		if (enactor.containsOutAttribute(context)) { // asking What about output
		if (enactor.getOutcomeName().equals(context)) {
			value = (T) enactor.getOutcomeValue();
		}
		else { // asking What about input
			value = enactor.getInWidgetState().getAttributeValue(context);
		}
		
		Parameter<T> p = Parameter.instance(context, value);
		return p;
	}
	
	/**
	 * Get the timestamp of when the outcome value of the enactor was changed.
	 * @return Parameter<Date> where name is {@link Widget.TIMESTAMP}, and value is a java.util.Date
	 */
	protected Parameter<Date> getWhenExplanation() {
		long timestamp = enactor.getInWidgetState().getAttributeValue(Widget.TIMESTAMP);
		Date date = new Date(timestamp);
		Parameter<Date> exp = Parameter.instance(Widget.TIMESTAMP, date);
		return exp;
	}
	
	/**
	 * Get the outcome value if an alternative set of input values were provided instead of the
	 * current input values.
	 * @param <T> the type of the outcome value
	 * @param altInputs conjunction of alternative input values to ask about
	 * @return Parameter<T> where name is the outcome name, and value is the resulting outcome value.
	 */
	@SuppressWarnings("unchecked")
	public <T extends Comparable<? super T>> Parameter<T> getWhatIfExplanation(Reason altInputs) {
		// check which enactorRef has its query satisfied by the widgetState
		String outcomeName = enactor.getOutcomeName();
		String outcomeValue = null;
		
		// TODO replace by directly crunching through enactor? But need to be non-mutative
		
		// create modified widget stub from altInputs
		ComponentDescription altWidgetState = enactor.getInWidgetState().clone();
		altWidgetState.addNonConstantAttributes(altInputs.toAttributes()); // to replace some attributes
		
		for (EnactorReference enactorRef : enactor.getReferences()) {
			AbstractQueryItem<?,?> query = enactorRef.getConditionQuery();	
			Boolean queryResult = query.match(altWidgetState);
			
//			System.out.println("getWhatIfExplanation queryResult = " + queryResult);
//			System.out.println("getWhatIfExplanation enactorRef.getOutcomeValue() = " + enactorRef.getOutcomeValue());
//			System.out.println("getWhatIfExplanation query = " + query);
			
			if (queryResult != null && queryResult) {
				// TODO: this is a non-scalable way of handling the special case for sequences
				if (enactorRef instanceof HmmEnactorReference) {
					outcomeValue = ((HmmQueryItem)query).getLastOutcomeValueSequence().toString();
				}
				else { // Rules or Classifier
					outcomeValue = enactorRef.getOutcomeValue(); // this only works for Rule ERs, not classifier, which need to set value
				}
				
				break;
			}
		}
		
		if (outcomeValue == null) { return null; } // no rule satisfied by state, so no valid reaction
		
		// return output of selected enactorRef
		Parameter<T> p = Parameter.instance(outcomeName, (T) outcomeValue);
		return p;
	}
	
	/**
	 * Get a conjunction of input values.
	 * @return conjunction of {@link Parameter}s
	 */
	public Reason getInputsExplanation() {
		ComponentDescription widgetState = enactor.getInWidgetState();
		
		if (widgetState == null) { return new Reason(); } // empty

		/*
		 * TODO: reconsider risk of this is that attributes not used in rules also gets returned
		 * Alternative of reading off from rules is that it is not generalizable to the whole application
		 * i.e. different rules may consider a subset of all inputs
		 */
		// not matching constant attributes, as they should only be used for subscription
		Attributes attributes = widgetState.getNonConstantAttributes();
//		System.out.println("getInputsExplanation.attributes = " + attributes);
		
		return Reason.fromAttributes(attributes);
	}

	/**
	 * Get a {@link DNF} of all the possible output values.
	 * @return Disjunction of {@link Reasons}, where each Reason is of size one containing a {@link Parameter}
	 * for an outcome value, and all their names are the outcome name.
	 */
	public DNF getOutputsExplanation() {
		DNF outputs = new DNF();
		String outcomeName = enactor.getOutcomeName();
		for (String outcomeValue : enactor.getOutcomeValues()) {
			outputs.add(new Reason(Parameter.instance(outcomeName, outcomeValue)));
		}
		return outputs;		
	}
	
	/**
	 * Returns the descriptive definition of the context with name = attributeName.
	 * By default, this uses a DescriptionExplainerDelegate, but may be overridden.
	 * @param attributeName of the context to get its definition
	 * @return Parameter<String> where name is attributeName and value is the definition.
	 */
	public Parameter<String> getDefinitionExplanation(String attributeName) {
		return descExplainer.getDefinitionExplanation(attributeName);
	}
		
	/* ----------------------------------------------------------------------------------------
	 * Model-dependent explanations
	 * ---------------------------------------------------------------------------------------- */
	
	/**
	 * Get why the enactor's model decided on its outcome value. This may be a reason trace, a probabilistic explanation, etc.
	 * @return DNF with one or more explanations.
	 */
	public abstract DNF getWhyExplanation();
	
	/**
	 * Get why the enactor's model did <b>not</b> decide on an alternative outcome value. This may be a reason trace, a probabilistic explanation, etc.
	 * @param altOutcomeValue the alternative outcome value to ask about
	 * @return DNF with one or more explanations.
	 */
	public abstract DNF getWhyNotExplanation(String altOutcomeValue);

	/**
	 * Get how the enactor's model would decide on a candidate outcome value. This may be a reason trace, a probabilistic explanation, etc.
	 * @param altOutcomeValue the candidate outcome value to ask about
	 * @return DNF with one or more explanations.
	 */
	public abstract DNF getHowToExplanation(String altOutcomeValue);

	/**
	 * Get an explanation about how certain the enactor's model is of its decision.
	 * @return DNF with one or more explanations. The explanation may also be very simple with just one literal wrapped in a DNF.
	 */
	public abstract DNF getCertaintyExplanation();
	
	/* ----------------------------------------------------------------------------------------
	 * Convenience methods
	 * ---------------------------------------------------------------------------------------- */
	
	/**
	 * Convenience method to extract the names of inputs from an inputs explanation.
	 * @param
	 * @return
	 */
	public static List<String> inputsToLabels(Reason inputs) {
		// doesn't matter if Conjunction or Disjunction, both are List<Expression>
		List<String> list = new ArrayList<String>();		
		for (Parameter<?> literal : inputs) {
			String label = literal.getName();
			list.add(label);
		}
		return list;
	}	
	
	/**
	 * Convenience method to unwrap the output explanation in DNF form into a list of string values.
	 * @param outputs output values explanation
	 * @return 
	 */
	public static List<String> outputsToLabels(DNF outputs) {
		// doesn't matter if Conjunction or Disjunction, both are List<Expression>
		List<String> list = new ArrayList<String>();		
		for (Reason reason : outputs) {
			String label = reason.get(0).getValue().toString();
			list.add(label);
		}
		return list;
	}

}
