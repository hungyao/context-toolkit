package context.arch.intelligibility.expression;

/**
 * Represents that the explanation is unknown.
 * @author Brian Y. Lim
 *
 */
public class Unknown extends Parameter<String> {
	
	private static final long serialVersionUID = -2286119092901229869L;
	
	public static final Unknown singleton = new Unknown();
	
	private Unknown() {
		super("Unknown", null);
	}

	/**
	 * Will always return false
	 */
	@Override
	public boolean isSatisfiedBy(Expression other) {
		return false;
	}

}
