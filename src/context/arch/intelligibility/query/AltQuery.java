package context.arch.intelligibility.query;

import java.util.Date;

/**
 * Extends Query to support asking questions about an alternative outcome value.
 * E.g. asking Why an alternative value was Not chosen.
 * 
 * @author Brian Y. Lim
 *
 */
public class AltQuery extends Query {
	
	private static final long serialVersionUID = -2053952021155159327L;
	
	/** Question type to ask Why an alternative outcome value was Not chosen */
	public static final String QUESTION_WHY_NOT = "Why Not";
	/** Question type to ask How To get the alternative outcome value selected (i.e. what are the satisfying input conditions) */
	public static final String QUESTION_HOW_TO = "How To";
	
	/** Question type to ask When was the last time that the context was a certain value */
	public static final String QUESTION_WHEN_LAST = "When Last";
	
	/**
	 * The alternative outcome value to ask in relation to.
	 */
	protected String altOutcomeValue;

	/**
	 * Create an AltQuery with timestamp of what time the question is asking about set to current time.
	 * @param question
	 * @param context
	 * @param altOutcomeValue
	 */
	public AltQuery(String question, String context, String altOutcomeValue) {
		this(question, context, altOutcomeValue, System.currentTimeMillis());
	}
	
	public AltQuery(String question, String context, String altOutcomeValue, long timestamp) {
		super(question, context, timestamp);
		this.altOutcomeValue = altOutcomeValue;
	}
	
	public String getAltOutcomeValue() {
		return altOutcomeValue;
	}
	
	public String toString() {
		return "AltQuery(" + question + "," + context + "," + altOutcomeValue + "@" + new Date(timestamp) + ")";
	}

}
