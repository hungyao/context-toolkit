package context.arch.intelligibility.query;

import java.io.Serializable;
import java.util.Date;

/**
 * Encapsulation for a query that may be put into an Explainer to ask for an explanation.
 * It consists of a question type, context (Widget Attribute name) to ask about, and timestamp.
 * Base question types are specified in this class (e.g.: {@link #QUESTION_WHAT }, {@link #QUESTION_WHY }, {@link #QUESTION_CERTAINTY }),
 * but there is no guarantee that all Explainers know how to process them, and other question types may be defined for other Explainers to support.
 * 
 * @author Brian Y. Lim
 *
 */
public class Query implements Serializable {
	
	private static final long serialVersionUID = -5844063265287509377L;

	/** Question type to ask What the value of the context is */
	public static final String QUESTION_WHAT = "What";
	/** Question type to ask how Certain the application was about its behavior or decision */
	public static final String QUESTION_CERTAINTY = "Certainty";
	/** Question type to ask about what Input contexts are used to make decisions (and possibly also what their values are) */
	public static final String QUESTION_INPUTS = "Inputs";

	/** Question type to ask When the context value had last changed */
	public static final String QUESTION_WHEN = "When";
	/** Question type to ask What the value of the context was at a specified timestamp */
	public static final String QUESTION_WHAT_AT_TIME = "What At Time";

	/** Question type to ask Why the model decided on the current context value */
	public static final String QUESTION_WHY = "Why";

//	/** Question type to ask for (possibly descriptive) user instructions on how to change the context value */
//	public static final String QUESTION_CONTROL = "Control";

	/** Question type to ask about what possible Output values the context can take */
	public static final String QUESTION_OUTPUTS = "Outputs";

	/** Descriptive Question type to ask for the definition of what the context means */
	public static final String QUESTION_DEFINITION = "Definition";
	/** Descriptive Question type to ask for the rationale or implication of the context taking various meanings */
	public static final String QUESTION_RATIONALE = "Rationale";	
	/** Descriptive Question type to ask for a user-friendly, human-readable name of the context */
	public static final String QUESTION_PRETTY_NAME = "Pretty Name";	
	/** Descriptive Question type to ask for the unit of measurement of the context (e.g. kg, cm, lb) */
	public static final String QUESTION_UNIT = "Unit";	

	/** Question type to specify a non-query; for invalid cases */
	public static final String QUESTION_NONE = "None";

	/**
	 * Stores the question type: why, why not, certainty, etc.
	 * Subclasses may specify new question types that other components know how to handle.
	 */
	protected String question;
	/**
	 * Stores the context (usually the name of widget attribute) being asked about
	 */
	protected String context;
	/**
	 * Stores the timestamp of what time the question is asking about
	 */
	protected long timestamp;
	
	/**
	 * Create a Query with timestamp of what time the question is asking about set to current time.
	 * @param question question type, e.g.: {@link #QUESTION_WHAT }, {@link #QUESTION_WHY }, {@link #QUESTION_CERTAINTY }
	 * @param context to ask about
	 */
	public Query(String question, String context) {
		this(question, context, System.currentTimeMillis());
	}
	
	/**
	 * Create a Query asking a question about a context at timestamp. 
	 * @param question question type, e.g.: {@link #QUESTION_WHAT }, {@link #QUESTION_WHY }, {@link #QUESTION_CERTAINTY }
	 * @param context to ask about
	 * @param timestamp of what time the question is asking about
	 */
	public Query(String question, String context, long timestamp) {
		this.question = question;
		this.context = context;
		this.timestamp = timestamp;
	}
	
	/**
	 * @return the context the query is asking about
	 */
	public String getContext() {
		return context;
	}

	/**
	 * @return the question to ask
	 */
	public String getQuestion() {
		return question;
	}

	/**
	 * @return the timestamp of what time the question is asking about
	 */
	public long getTimestamp() {
		return timestamp;
	}
	
	@Override
	public String toString() {
		return "Query(" + question + "," + context + "@" + new Date(timestamp) + ")";
	}

}
