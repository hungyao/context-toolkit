package context.arch.intelligibility.expression;

import java.util.Iterator;
import java.util.List;

/**
 * Convenience class to represent a Disjunction of Expressions taking the Disjunctive Normal Form (DNF).
 * Its children must be a {@link Conjunction} of {@link Parameters} (which are terminal literals).
 * @author Brian Y. Lim
 * @see Conjunction
 * @see Parameter
 */
public class DNF extends Disjunction<Reason> {

	private static final long serialVersionUID = -4894904209721540072L;
	
	/** Convenience variable to get a DNF wrapping the Unknown expression. */
	public static final DNF UNKNOWN = new DNF(Unknown.singleton);
	
	public DNF() {
		super();
	}
	
	/**
	 * Create a DNF with reason added. 
	 * This can be used as a convenient way to wrap a reason in a DNF.
	 * @param reason first reason to add
	 */
	public DNF(Reason reason) {
		super();
		add(reason);
	}
	
	/**
	 * Create a DNF with one reason with the literal added.
	 * This can be used as a convenient way to wrap a reason literal in a DNF.
	 * @param literal
	 */
	public DNF(Parameter<?> literal) {
		super();
		add(new Reason(literal));
	}
	
	/**
	 * Convenience method to get the first literal.
	 * This is useful for unwrapping DNF with only one literal value.
	 * Would throw exception if DNF or first Reason is empty. 
	 * @return 
	 */
	public Parameter<?> getFirstLiteral() {
		return get(0).get(0);
	}

	/**
	 * Converts expression fully to Disjunctive Normal Form (DNF).
	 * Flattens to at most 2-3 layers: Disjunction of (Conjunctions or NOT(terminal) or terminal Expressions)
	 * @param expression
	 * @return
	 */
	public static DNF toDNF(Expression expression) {
		Expression collapsed = DNF.toCollapsed(expression);
		Expression deMorganed = Negation.deMorgan(collapsed);
		
		DNF dnf = DNF.deMorganToDNF(deMorganed);		
		return dnf;
	}

	/**
	 * Convenience function to format Expression tree into Disjunctive Normal Form.
	 * It assumes that the expression has been formatted with the de Morgan function, 
	 * such that all NOTs are only at the leaves.
	 * 
	 * @param deMorganed
	 * @return
	 */
	private static DNF deMorganToDNF(Expression deMorganed) {
		DNF traces = new DNF();
		Reason trace = new Reason();
		toDnfRecurse(deMorganed, trace, traces);
		return traces;
	}

	/**
	 * Recursively adds to the Disjunction traces. 
	 * As it traverses the expression tree, it either adds to trace, or creates new traces
	 * @param expression tree to walk
	 * @param trace to add to trace if appending a conjunction
	 * @param traces to add new Disjunctions to
	 */
	private static void toDnfRecurse(Expression expression, Reason trace, DNF traces) {
		if (expression instanceof Disjunction) {
			/*
			 * Create a new trace for each child in the Disjunction, and adds to it
			 */
			Disjunction<?> list = (Disjunction<?>)expression;
			for (Expression childExp : list) {
				Reason childTrace = trace.clone(); // duplicate trace to create variants with OR branches
				toDnfRecurse(childExp, childTrace, traces);
			}
		}
		else if (expression instanceof Conjunction) {
			/*
			 * Append to the Conjunction trace
			 */
			Conjunction<?> list = (Conjunction<?>)expression;
			for (Expression childExp : list) {
				toDnfRecurse(childExp, trace, traces);
			}
		}
		else { // assume terminal leaf; also assumes Negation is terminal
			trace.add((Parameter<?>) expression);
			if (!traces.contains(trace)) { // add only once
				traces.add(trace);
			}
		}
	}
	
	/**
	 * Collapses Conjunctions and Disjunctions if they only have one child.
	 * @param expression
	 * @return
	 */
	@SuppressWarnings({ "unchecked" })
	private static Expression toCollapsed(Expression expression) {
		if (expression instanceof List<?>) {
			List<Expression> list = (List<Expression>)expression;
			
			for (int i = 0; i < list.size(); i++) {
				Expression childExp = list.get(i);
				childExp = toCollapsed(childExp);
				list.set(i, childExp);
			}			

			// purge of empty children
			Iterator<Expression> it = list.iterator();			
			while (it.hasNext()) {
				Expression childExp = it.next();		
				if (childExp == null) { it.remove(); }
			}
			
			if (list.isEmpty()) { return null; }
			else if (list.size() == 1) { return list.get(0); } // even if only 1 child, still return a conjunction
			else { return (Expression)list; }
		}
		else if (expression instanceof Negation) {
			Expression childExp = ((Negation) expression).getExpression(); // unwrap
			childExp = toCollapsed(childExp); // collapse child
			return Negation.negate(childExp); // wrap back
		}
		else {
			return expression;
		}
	}

}
