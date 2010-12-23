package context.arch.intelligibility.expression;

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

}
