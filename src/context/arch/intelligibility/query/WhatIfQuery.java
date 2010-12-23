package context.arch.intelligibility.query;

import java.util.Date;

import context.arch.intelligibility.expression.Reason;

/**
 * Query to ask What If the in-widget input conditions (as Reason) were different, what the outcome would be.
 * Anything about the candidate ComponentDescription may be changed.
 * @author Brian Y. Lim
 * @see Reason
 */
public class WhatIfQuery extends Query {

	private static final long serialVersionUID = -1105727007321285362L;
	
	/** Question type to ask If the widget state was different, What would the outcome be */
	public static final String QUESTION_WHAT_IF = "What If";

	private Reason inputs;
	
	public WhatIfQuery(String question, String context, Reason inputs) {
		this(question, context, inputs, System.currentTimeMillis());
	}
	
	/**
	 * Create a WhatIfQuery with timestamp of what time the question is asking about set to current time.
	 * @param question
	 * @param context
	 * @param widgetState
	 * @param timestamp
	 */
	public WhatIfQuery(String question, String context, Reason inputs, long timestamp) {
		super(question, context, timestamp);
		this.inputs = inputs;
	}
	
	public Reason getInputs() {
		return inputs;
	}
	
	public String toString() {
		return "WhatIfQuery(" + question + "," + context + "," + inputs + "@" + new Date(timestamp) + ")";
	}

}
