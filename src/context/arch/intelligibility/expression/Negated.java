package context.arch.intelligibility.expression;

/**
 * This is meant to be a terminal Negation, i.e. its child is a terminal literal.
 * Contrast this with Negation.
 * @author Brian Y. Lim
 *
 * @param <T>
 * @see Negation
 */
public class Negated<T extends Comparable<? super T>> extends Parameter<T> {

	private static final long serialVersionUID = 172294501664047155L;
	
	public static final String NEG_PREFIX = "not ";
	
	protected Parameter<T> child;

	public Negated(Parameter<T> child) {
		super(child.name, child.value);
		this.child = child;
	}
	
	public Parameter<T> getChildExpression() {
		return child;
	}
	
	/**
	 * Convenience method to negate comparison, and deals with double negative by dropping both negations.
	 * @param comparison
	 * @return
	 */
	public static <T extends Comparable<? super T>> Parameter<T> negate(Parameter<T> expression) {
		if (expression instanceof Negated<?>) {
			return ((Negated<T>)expression).getChildExpression();
		}
		else {
			return new Negated<T>(expression);
		}
	}
	
	@Override
	public String toString() {
		return "NOT(" + child + ")";
	}

	/**
	 * Would be the negation of isSatisfiedBy of the childExpression
	 */
	@Override
	public boolean isSatisfiedBy(Expression other) {
		return !child.isSatisfiedBy(other);
	}

}
