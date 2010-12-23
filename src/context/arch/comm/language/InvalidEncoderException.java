package context.arch.comm.language;

/**
 * This class implements the InvalidEncoderException.  It is thrown if an 
 * EncoderInterface object can not be created.
 *
 * @see context.arch.comm.language.InvalidDecoderException
 */
public class InvalidEncoderException extends Exception {
	
	private static final long serialVersionUID = -1233694206268019324L;
	
	private String message = "";

	/** 
	 * Basic constructor for InvalidEncoderException with no message
	 */
	public InvalidEncoderException() { 
		super();
	}

	/** 
	 * Constructor for InvalidEncoderException with error message
	 *
	 * @param message Error message
	 */
	public InvalidEncoderException(String message) { 
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

