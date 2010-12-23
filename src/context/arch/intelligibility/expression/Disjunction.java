package context.arch.intelligibility.expression;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * List of expressions as an OR combination.
 * @author Brian Y. Lim
 *
 */
public class Disjunction<E extends Expression> extends ArrayList<E> implements Expression {

	private static final long serialVersionUID = -93022233976523918L;
	
	protected boolean dnf;

	public Disjunction() {
		super();
	}

	public Disjunction(Collection<E> original) {
		super(original);
	}

	public String toString() {
		return "OR" + super.toString();
	}
	
	public boolean isDnf() {
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
	protected static DNF deMorganToDNF(Expression deMorganed) {
		DNF traces = new DNF();
		Reason trace = new Reason();
		toDisjunctionRecurse(deMorganed, trace, traces);
		return traces;
	}

	/**
	 * Recursively adds to the Disjunction traces. 
	 * As it traverses the expression tree, it either adds to trace, or creates new traces
	 * @param expression tree to walk
	 * @param trace to add to trace if appending a conjunction
	 * @param traces to add new Disjunctions to
	 */
	protected static void toDisjunctionRecurse(Expression expression, Reason trace, DNF traces) {
		if (expression instanceof Disjunction) {
			/*
			 * Create a new trace for each child in the Disjunction, and adds to it
			 */
			Disjunction<?> list = (Disjunction<?>)expression;
			for (Expression childExp : list) {
				Reason childTrace = trace.clone(); // duplicate trace to create variants with OR branches
				toDisjunctionRecurse(childExp, childTrace, traces);
			}
		}
		else if (expression instanceof Conjunction) {
			/*
			 * Append to the Conjunction trace
			 */
			Conjunction<?> list = (Conjunction<?>)expression;
			for (Expression childExp : list) {
				toDisjunctionRecurse(childExp, trace, traces);
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
	public static Expression toCollapsed(Expression expression) {
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

	/**
	 * Converts expression fully to DNF.
	 * Note that resulting traces do not have disjunctions. All disjunctions (OR) have been propagated to the top and are separated.
	 * Flattens to at most 2-3 layers: Disjunction of (Conjunctions or NOT(terminal) or terminal Expressions)
	 * @param expression
	 * @return
	 */
	public static DNF toDNF(Expression expression) {
		Expression collapsed = Disjunction.toCollapsed(expression);
		Expression deMorganed = Negation.deMorgan(collapsed);
		
		DNF dnf = Disjunction.deMorganToDNF(deMorganed);		
		return dnf;
	}

	/**
	 * TODO: currently the same as for Conjunction; not sure if that is ok, or this should just be invalid
	 */
	@Override
	public boolean isSatisfiedBy(Expression other) {
		if (other instanceof Conjunction) {
			// need all of other's children to satisfy this
			for (Expression otherChild : (Conjunction<?>)other) {
				// any failure would fail all
				if (!this.isSatisfiedBy(otherChild)) {
					return false;
				}
			}
			return true;
		}
		
		else if (other instanceof Disjunction) {
			// any of other's children satisfies => overall satisfies
			for (Expression otherChild : (Disjunction<?>)other) {
				// any success would succeed overall
				if (this.isSatisfiedBy(otherChild)) {
					return true;
				}
			}
			return false;
		}
		
		else { // assume terminal literal
			// scan through children
			for (Expression child : this) {
				// if any child is satisfied by other, then other fits this trace
				if (child.isSatisfiedBy(other)) {
					return true;
				}
			}
			return false;
		}		
	}

}
