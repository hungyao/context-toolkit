package context.arch;

/**
 * This class implements the InvalidMethodException.  It is thrown when
 * a handler for a specified RPC can't be found.
 *
 * @see MethodException
 */
public class InvalidMethodException extends Exception {

	private static final long serialVersionUID = -4550839488849129624L;

	private String message = "";
	/** 
	 * Basic constructor for InvalidMethodException with no message
	 */
	public InvalidMethodException() { 
		super();
	}

	/** 
	 * Constructor for InvalidMethodException with error message
	 *
	 * @param s Error message
	 */
	public InvalidMethodException(String message) { 
		super(message);
		this.message = message;
	}

	/**
	 * Returns the error message
	 */
	public String getMessage() {
		return message;
	}
}

