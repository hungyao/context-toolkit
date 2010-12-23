package context.arch.intelligibility.rules;

import java.util.HashMap;

import context.arch.discoverer.component.AbstractElement;
import context.arch.discoverer.component.AttributeElement;
import context.arch.discoverer.component.CallbackElement;
import context.arch.discoverer.component.ClassnameElement;
import context.arch.discoverer.component.HostnameElement;
import context.arch.discoverer.component.IdElement;
import context.arch.discoverer.component.NonConstantAttributeElement;
import context.arch.discoverer.component.PortElement;
import context.arch.discoverer.component.ServiceElement;
import context.arch.discoverer.component.SubscriberElement;
import context.arch.discoverer.component.TypeElement;
import context.arch.discoverer.query.ANDQueryItem;
import context.arch.discoverer.query.AbstractQueryItem;
import context.arch.discoverer.query.ElseQueryItem;
import context.arch.discoverer.query.NOTQueryItem;
import context.arch.discoverer.query.ORQueryItem;
import context.arch.discoverer.query.RuleQueryItem;
import context.arch.discoverer.query.comparison.AbstractComparison;
import context.arch.discoverer.query.comparison.AttributeComparison;
import context.arch.enactor.Enactor;
import context.arch.enactor.EnactorXmlParser;
import context.arch.intelligibility.expression.Comparison;
import context.arch.intelligibility.expression.Conjunction;
import context.arch.intelligibility.expression.DNF;
import context.arch.intelligibility.expression.Disjunction;
import context.arch.intelligibility.expression.Expression;
import context.arch.intelligibility.expression.Negation;
import context.arch.intelligibility.expression.Parameter;
import context.arch.storage.AttributeNameValue;
import context.arch.widget.WidgetXmlParser;

/**
 * Parses the AbstractQueryItem abstract syntax tree (AST) into the Expression tree, and into DNF form.
 * @author Brian Y. Lim
 *
 */
public class QueryItemParser {
	
	/**
	 * Converts  a query into an Expression equivalent, and also in DNF form.
	 * Rules can have multiple traces, but all have the same output.
	 * Traces are a disjunctions of trace conjunctions.
	 * Each trace is a conjunction of Comparisons
	 * @param query
	 */
	public static DNF parse(AbstractQueryItem<?,?> query) {
		Expression ruleTree = parseRecurse(query);	
		DNF dnf = Disjunction.toDNF(ruleTree);
		return dnf;
	}
	
	/**
	 * Converts a query into an Expression equivalent. Would do this recursively if query consists of boolean/list queries with children.
	 * 
	 * Subclasses should override and extend this method to be able to attach expressions for newly defined AbstractQueryItems.
	 * 
	 * @param query
	 * @param trace one trace per leaf, create new traces by duplicating from parent
	 * @param traces
	 */
	private static Expression parseRecurse(AbstractQueryItem<?,?> query) {		
		/*
		 * Atomic rule queries
		 */
		if (query instanceof RuleQueryItem<?,?>) {
			return parse((RuleQueryItem<?,?>) query);
		}
		
		/*
		 * Boolean queries
		 */
		else if (query instanceof ANDQueryItem) {
			return parseRecurse((ANDQueryItem) query);
		}
		else if (query instanceof ORQueryItem) {
			return parseRecurse((ORQueryItem) query);
		}
		else if (query instanceof NOTQueryItem) {
			return parseRecurse((NOTQueryItem) query);
		}
		else if (query instanceof ElseQueryItem) {
			return parseRecurse((ElseQueryItem) query);
		}
		
		else {
			return null; // TODO: we get null, because we are using custom QueryItems and custom Comparisons
		}
	}
	
	@SuppressWarnings("unchecked")
	private static <T extends Comparable<? super T>> Parameter<T> parse(RuleQueryItem<?,?> query) {
		RuleQueryItem<?,?> qi = (RuleQueryItem<?,?>)query;
		AbstractComparison<?,?> comp = qi.getComparison();
		AbstractElement<?,?,?> elementToMatch = qi.getElementToMatch();

		//String relationship = comp.getComparisonName();
		
		/*
		 * Match element about Attribute
		 */
		if (elementToMatch instanceof AttributeElement) { 
			AttributeNameValue<T> att = (AttributeNameValue<T>) ((AttributeElement)elementToMatch).getValue();
			String attName = att.getName();
			T value = att.getValue();
			
			/*
			 * Would only compare attribute values as Comparable (EQUAL, GREATER, etc)
			 * So can map directly to context.arch.intelligibility.expression.Comparison
			 */
			if (comp instanceof AttributeComparison) {
				// convert comparison
				AttributeComparison attrComp = (AttributeComparison) comp;
				Comparison.Relation relation;
				switch (attrComp.getComparison()) {
				case EQUAL: relation = Comparison.Relation.EQUALS; break;
				case DIFFERENT: relation = Comparison.Relation.NOT_EQUALS; break;
				case GREATER: relation = Comparison.Relation.GREATER_THAN; break;
				case GREATER_EQUAL: relation = Comparison.Relation.GREATER_THAN_OR_EQUAL; break;
				case LESS: relation = Comparison.Relation.LESS_THAN; break;
				case LESS_EQUAL: relation = Comparison.Relation.LESS_THAN_OR_EQUAL; break;
				default: relation = Comparison.Relation.NO_RELATION; break;
				}
				
				if (att.isNumeric()) {
					return Comparison.instance(attName, value, relation);
				}
				else {
					return Parameter.instance(attName, value);
				}
			}
									
			else {
				throw new RuntimeException("some other form of comparison for Attribute"); // TODO support some other form of comparison for Attribute
			}
		}
		
		/*
		 * For matching other elements of ComponentDescription.
		 * Assumes that it is just an equality match.
		 * TODO: generalize for other types of comparison
		 */
		String paramName = elementToMatch.getElementName();
		T paramValue = (T) elementToMatch.getValue();
		
		if (	elementToMatch instanceof IdElement ||
				elementToMatch instanceof HostnameElement ||
				elementToMatch instanceof PortElement ||
				elementToMatch instanceof ClassnameElement ||
				elementToMatch instanceof TypeElement ||
				elementToMatch instanceof CallbackElement ||
				elementToMatch instanceof ServiceElement ||
				elementToMatch instanceof SubscriberElement
		) {
			return Parameter.instance(paramName, paramValue);
		}

		return null;
	}
	
	private static Conjunction<Expression> parseRecurse(ANDQueryItem query) {
		Conjunction<Expression> list = new Conjunction<Expression>();
		for (AbstractQueryItem<?,?> child : query.getChildren()) {
			list.add(parseRecurse(child));
		}
		return list;
	}
	
	private static Disjunction<Expression> parseRecurse(ORQueryItem query) {
		Disjunction<Expression> list = new Disjunction<Expression>();
		for (AbstractQueryItem<?,?> child : query.getChildren()) {
			list.add(parseRecurse(child));
		}
		return list;
	}
	
	private static Expression parseRecurse(NOTQueryItem query) {
		Expression child = parseRecurse(query.getChild());
		return Negation.negate(child);
	}
	
	private static Expression parseRecurse(ElseQueryItem query) {
		Disjunction<Expression> list = new Disjunction<Expression>();
		for (AbstractQueryItem<?,?> child : query.getChildren()) {
			list.add(parseRecurse(child));
		}
		Expression ret = Negation.negate(list);
		return ret;
	}
	
	@SuppressWarnings("serial")
	public static void main(String[] args) {
		AbstractQueryItem<?,?> query = WidgetXmlParser.getWidgetSubscriptionQuery(
				"demos/room-rules/room-widget.xml", 
				"Living Room", // widgetId
				new HashMap<String, Comparable<?>>() {{
					put("room", "Living Room");
				}});
		query = RuleQueryItem.instance(
				new NonConstantAttributeElement(AttributeNameValue.instance("greater_test", 10)),
				AttributeComparison.GREATER
		);
		Enactor enactor = EnactorXmlParser.getEnactor(
				"demos/room-rules/room-enactor.xml", 
				"Living Room Ceiling", 
				new HashMap<String, Comparable<?>>() {{
					put("room", "Living Room");
				}}, 
				new HashMap<String, Comparable<?>>() {{
					put("lamp", "Ceiling");
				}});
//		query = enactor.getReference("Off").getConditionQuery();
//		Disjunction dnf = QueryItemParser.parse(query);
//		System.out.println("dnf = " + dnf);
		query = enactor.getReference("On").getConditionQuery();
		DNF dnf = QueryItemParser.parse(query);
		System.out.println("dnf = " + dnf);
	}

}
