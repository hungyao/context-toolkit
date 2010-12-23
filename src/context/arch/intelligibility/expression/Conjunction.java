package context.arch.intelligibility.expression;

import java.util.ArrayList;
import java.util.Collection;

/**
 * List of expressions as an AND combination.
 * @author Brian Y. Lim
 *
 */
public class Conjunction<E extends Expression> extends ArrayList<E> implements Expression, Cloneable {
	
	private static final long serialVersionUID = 98079915437383558L;

	public Conjunction() {
		super();
	}
	
	public Conjunction(Collection<E> original) {
		super(original);
	}
	
	public Conjunction(E ... original) {
		super();
		for (E child : original) {
			add(child);
		}
	}
	
	/**
	 * Convenience method to search for expression that has the name and return its value
	 * @param name
	 * @return null if no expression with such name exists
	 */
	public Object getValue(String name) {
		Parameter<?> child = this.getChild(name);
		if (child != null) {
			return child.getValue();
		}
		else {
			return null;
		}
	}

	/**
	 * Convenience method to search for expression that has the name and return it
	 * @param name
	 * @return
	 */
	public Parameter<?> getChild(String name) {
		for (E exp : this) {
			if (exp instanceof Parameter<?>) {
				Parameter<?> param = (Parameter<?>)exp;
				if (param.getName().equals(name)) {
					return param;
				}
			}
		}
		return null;
	}
	
	public String toString() {
		return "AND" + super.toString();
	}
	
	/**
	 * If the child expression is a Parameter that has the same name as an existing child, then it would be merged with that.
	 * If Comparison, then the range of the original would be set (and possibly tightened).
	 * If Parameter, then there actually is a contradiction, and would be ignored.
	 * @param child
	 */
	@SuppressWarnings("unchecked")
	public void addOrMerge(E child) {
		if (child instanceof Parameter<?>) {
			String name = ((Parameter<?>) child).getName();
			Parameter<?> origChild = this.getChild(name);
			
			if (origChild != null) {
				// comparison, so we adjust range
				if (origChild instanceof Comparison<?>) {
					/*
					 * create a new copy of child
					 * don't use original reference, since that would affect other Conjunctions that also reference it
					 */
					Comparison<?> newChild = ((Comparison<?>) origChild).clone();
					newChild.setRange((Comparison<?>) child);
					
					// replace original with new child; retains position
					super.set(super.indexOf(origChild), (E) newChild);					
					return;
				}

				// parameter
				else {
					// actually, this should not happen, as it would lead to a contradiction
					new RuntimeException("Parameter reassignment: name=" + name + ", orig=" + origChild.getValue() + ", to=" + ((Parameter<?>) child).getValue()).printStackTrace();
					// end up not adding too
				}
			}
		}

		// just add normally
		super.add(child);
		
	}

	@Override
	public boolean isSatisfiedBy(Expression other) {
		if (other instanceof Conjunction<?>) {
			// need all of other's children to satisfy this
			for (Expression otherChild : (Conjunction<?>)other) {
				// ignore if this does not contain otherChild
				if (otherChild instanceof Parameter<?> &&
						this.getChild(((Parameter<?>) otherChild).getName()) == null) {
					continue;
				}
				
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
