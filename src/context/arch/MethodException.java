package context.arch;

/**
 * This class implements the MethodException.  It is thrown when
 * a specified RPC has an error during execution.
 *
 * @see InvalidMethodException

 */
public class MethodException extends Exception {

	private static final long serialVersionUID = -1605794778818865484L;

	private String message = "";

	/** 
	 * Basic constructor for MethodException with no message
	 */
	public MethodException() { 
		super();
	}

	/** 
	 * Constructor for MethodException with error message
	 *
	 * @param message Error message
	 */
	public MethodException(String message) { 
		super(message);
		this.message = message;
	}

	/**
	 * Returns the error message
	 *
	 * @return the error message
	 */
	public String getMessage() {
		return message;
	}
}

