package context.arch.intelligibility.expression;

import java.util.ArrayList;
import java.util.Collection;

/**
 * List of expressions as an OR combination.
 * @author Brian Y. Lim
 *
 */
public class Disjunction<E extends Expression> extends ArrayList<E> implements Expression {

	private static final long serialVersionUID = -93022233976523918L;

	public Disjunction() {
		super();
	}

	public Disjunction(Collection<E> original) {
		super(original);
	}

	public String toString() {
		return "OR" + super.toString();
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
