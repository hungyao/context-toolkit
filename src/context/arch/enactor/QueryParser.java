package context.arch.enactor;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import context.arch.discoverer.ComponentDescription;
import context.arch.discoverer.component.NonConstantAttributeElement;
import context.arch.discoverer.component.NonConstantAttributeNameElement;
import context.arch.discoverer.query.ANDQueryItem;
import context.arch.discoverer.query.AbstractQueryItem;
import context.arch.discoverer.query.BooleanQueryItem;
import context.arch.discoverer.query.ElseQueryItem;
import context.arch.discoverer.query.NOTQueryItem;
import context.arch.discoverer.query.ORQueryItem;
import context.arch.discoverer.query.RuleQueryItem;
import context.arch.discoverer.query.comparison.AttributeComparison;
import context.arch.storage.Attribute;
import context.arch.storage.AttributeNameValue;

/**
 * Parses a bracket-based grammar used in XML files to describe queries.
 * TODO: should use JavaCC or similar parsers to support more intuitive grammars.
 * @author Brian Y. Lim
 *
 */
public class QueryParser {
	
	private Map<String, Comparable<?>> constVars;
	private Map<String, AbstractQueryItem<?, ?>> queries;
	private ComponentDescription widgetStub;
	
	private String fullText;
	
	public QueryParser(String fullText, Map<String, Comparable<?>> constVars, Map<String, AbstractQueryItem<?, ?>> queries, ComponentDescription widgetStub) {
		this.fullText = fullText;
		this.constVars = constVars;
		this.queries = queries;
		this.widgetStub = widgetStub;
	}

	protected AbstractQueryItem<?, ?> parseQuery() {
		return parseQuery(stripBrackets(fullText));
	}
	
	private static String stripBrackets(String text) {
		return text.substring(
					text.indexOf("(") + 1,
					text.lastIndexOf(")")
				).trim();
	}
	
	/**
	 * 
	 * @param text stripped out of brackets
	 * @return
	 */
	protected AbstractQueryItem<?, ?> parseQuery(String text) {
		//System.out.println("text = |" + text + "|");
		
		String op = text.split("\\s")[0]; // get word before white space
		//System.out.println("op = |" + op + "|");
		int opLenth = op.length();
		
		if (isBooleanOp(op)) {
			String[] args = getBooleanArgs(text.substring(opLenth + 1));
			return parseBooleanQuery(op, args);
		}
		else if (isNOTOp(op)) {
			// expect only one argument
			String arg = text.substring(opLenth + 1);
			return parseNOTQuery(op, arg);
		}
		else if (isQueryOp(op)) {
			// expect only one argument
			String arg = text.substring(opLenth + 1); // name of query
			return queries.get(arg);
		}
		else { // isTerminalOp(op)
			// would just have arguments, so can split
			String[] args = text.substring(opLenth + 1).split(" ", 2); // expect only 2 arguments
			return parseAttributeRuleQuery(op, args);
		}
		/*
		 * cannot be a const var, an attribute, or a literal (string or number)
		 * since that would have been processed in terminal op
		 */
		
		// TODO: can't just split by ',' since may be nested!
		
		
	}
	
	/**
	 * 
	 * @param text takes in text between outer (...)
	 * @return
	 */
	protected String[] getBooleanArgs(String text) {
		/*
		 * Assumes each arg has an op
		 */
		int openCount = 0, closeCount = 0;
		int index = 0;
		
		int OPEN_INDEX = -1;
		int CLOSE_INDEX = -1;
		
		List<String> args = new ArrayList<String>();
		
		for (char c : text.toCharArray()) {
			if (c == '(') {
				openCount++;

				// first open
				if (openCount == 1 && closeCount == 0) {
					OPEN_INDEX = index;
				}
			}
			else if (c == ')') {
				closeCount++;

				// balanced, so properly closed
				if (openCount == closeCount) {
					CLOSE_INDEX = index;
					args.add(text.substring(OPEN_INDEX+1, CLOSE_INDEX));
					
					// reset
					openCount = closeCount = 0;
					OPEN_INDEX = -1;
				}
			}
			//System.out.println("c = " + c + ", index = " + index + ", openCount = " + openCount + ", closeCount = " + closeCount + ", OPEN_INDEX = " + OPEN_INDEX + ", CLOSE_INDEX = " + CLOSE_INDEX);
			
			if (openCount > 0) {
			}
			
			// increment
			index++;
		}
		
		return args.toArray(new String[args.size()]);
	}
	
	protected boolean isBooleanOp(String op) {
		return op.equals("OR") ||
			   op.equals("AND") ||
			   op.equals("ELSE");
	}
	
	protected boolean isNOTOp(String op) {
		return op.equals("NOT");
	}
	
	protected boolean isQueryOp(String op) {
		return op.equals("QUERY");
	}
	
	protected boolean isTerminalOp(String op) {
		return true; // TODO delete this useless test
	}
	
	protected BooleanQueryItem parseBooleanQuery(String op, String[] args) {
		/*
		 * Boolean queries
		 */
		if (op.equals("OR")) {
			ORQueryItem query = new ORQueryItem();
			for (String arg : args) {
				query.add(parseQuery(arg));
			}
			return query;
		}
		else if (op.equals("AND")) {
			ANDQueryItem query = new ANDQueryItem();
			for (String arg : args) {
				query.add(parseQuery(arg));
			}
			return query;
		}
		else if (op.equals("ELSE")) {
			ElseQueryItem query = new ElseQueryItem();
			for (String arg : args) {
				query.add(parseQuery(arg));
			}
			return query;
		}
		
		return null;
	}

	protected NOTQueryItem parseNOTQuery(String op, String arg) {
		NOTQueryItem query = new NOTQueryItem(
				parseQuery(stripBrackets(arg)));
		return query;
	}

	/**
	 * ... 
	 * Assume RuleQueryItem regarding a non-constant attribute, i.e. with
	 * NonConstantAttributeElement, and a corresponding AbstractComparison
	 * @param op
	 * @param args {attributeName, comparisonValue}. comparisonValue may be null, or missing, then RuleQueryItem would just track for changes
	 */
	@SuppressWarnings("unchecked")
	protected <T extends Comparable<? super T>> RuleQueryItem<?, ?> parseAttributeRuleQuery(String op, String[] args) {
		String attName = args[0].trim();
		String comparisonValueStr = args.length == 2 ? args[1].trim() : null;
		
		T comparisonValue;

		AttributeComparison comparison = null;

		// assume non-constant attribute
		Attribute<?> att = widgetStub.getNonConstantAttribute(attName);
		if (att == null) {
			throw new RuntimeException("Attribute " + attName + " not found among non-constant attributes");
		}
		
		if (comparisonValueStr != null && comparisonValueStr.length() > 0) {
			// comparison to a constant variable
			if (constVars.containsKey(comparisonValueStr)) { // refers to const var name
				// retrieve var value
				comparisonValue = (T) constVars.get(comparisonValueStr);
			}
	
			// comparison to a String literal
			else if (att.isType(String.class)) {
				try {
					comparisonValue = (T) URLDecoder.decode(comparisonValueStr, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
					comparisonValue = (T) "";
				}
			}	
			
			/*
			 * TODO: Need to properly support querying about other ComponentDescription elements too, but in another method
			 */
			
			// comparison to a Comparable that needs to be parsed
			else {
				// cast comparison value to the type of the att
				comparisonValue = AttributeNameValue.valueOf((Class<T>) att.getType(), comparisonValueStr);
			}

			/*
			 * Iterate through possibilities of AttributeComparison
			 */
			if (op.equals("EQUAL")) { comparison = new AttributeComparison(AttributeComparison.Comparison.EQUAL); }
			else if (op.equals("DIFFERENT")) { comparison = new AttributeComparison(AttributeComparison.Comparison.DIFFERENT); }
			else if (op.equals("GREATER")) { comparison = new AttributeComparison(AttributeComparison.Comparison.GREATER); }
			else if (op.equals("GREATER_EQUAL")) { comparison = new AttributeComparison(AttributeComparison.Comparison.GREATER_EQUAL); }
			else if (op.equals("LESS")) { comparison = new AttributeComparison(AttributeComparison.Comparison.LESS); }
			else if (op.equals("LESS_EQUAL")) { comparison = new AttributeComparison(AttributeComparison.Comparison.LESS_EQUAL); }
			
			if (comparison == null) { // not yet matched
				// TODO: more comparison types? extensibility?
				new RuntimeException("op: " + op).printStackTrace();
			}

			RuleQueryItem<?, ?> query = RuleQueryItem.instance(
					new NonConstantAttributeElement(AttributeNameValue.instance(attName, comparisonValue)),
					comparison
			);
			return query;
		}
		
		// no comparison value
		else {
			// would just check if attribute with name changes
			RuleQueryItem<?, ?> query = RuleQueryItem.instance(
					new NonConstantAttributeNameElement(attName)
			);
			return query;
		}
	}

}
