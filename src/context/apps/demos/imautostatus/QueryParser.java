package context.apps.demos.imautostatus;

import context.arch.intelligibility.query.AltQuery;
import context.arch.intelligibility.query.Query;

public class QueryParser {
	
	public static Query getQuery(String question, String arguments) {
		if (question == null) {
			return null; // empty
		}
		
		String altValue = "-1";
		if (arguments != null) {
			if (arguments.equalsIgnoreCase("within")) {
				altValue = "0";
			}
			else {
				altValue = "1";
			}
		}

		if (question.equals(Query.QUESTION_INPUTS.replace(" ", "").toLowerCase())) {
			return new Query(Query.QUESTION_INPUTS, null, System.currentTimeMillis());
		}
		else if (question.equals(Query.QUESTION_OUTPUTS.replace(" ", "").toLowerCase())) {
			return new Query(Query.QUESTION_OUTPUTS, null, System.currentTimeMillis());
		}
//		else if (question.equals(Query.QUESTION_WHAT)) {
//			return renderWhat(expression);
//		}
//		else if (question.equals(Query.QUESTION_WHEN)) {
//			return renderWhen(expression);
//		}
//		else if (question.equals(AltQuery.QUESTION_WHEN_LAST)) {
//			return renderWhenLast(expression);
//		}
		else if (question.equals(Query.QUESTION_CERTAINTY.replace(" ", "").toLowerCase())) {
			return new Query(Query.QUESTION_CERTAINTY, null, System.currentTimeMillis());
		}
		else if (question.equals(Query.QUESTION_WHY.replace(" ", "").toLowerCase())) {
			return new Query(Query.QUESTION_WHY, null, System.currentTimeMillis());
		}
//		else if (question.equals(WhatIfQuery.QUESTION_WHAT_IF.replace(" ", "").toLowerCase())) {
//			return renderWhatIf(expression);
//		}
		else if (question.equals(AltQuery.QUESTION_WHY_NOT.replace(" ", "").toLowerCase())) {
			return new AltQuery(AltQuery.QUESTION_WHY_NOT, null, altValue, System.currentTimeMillis());
		}
		else if (question.equals(AltQuery.QUESTION_HOW_TO.replace(" ", "").toLowerCase())) {
			return new AltQuery(AltQuery.QUESTION_HOW_TO, null, altValue, System.currentTimeMillis());
		}
//		else {
//			return null;
//		}
		
		return null;
	}

}
