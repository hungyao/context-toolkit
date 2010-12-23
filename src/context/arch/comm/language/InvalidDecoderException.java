package context.arch.comm.language;

/**
 * This class implements the InvalidDecoderException.  It is thrown if a
 * DecoderInterface object can not be created.
 *
 * @see context.arch.comm.language.InvalidEncoderException
 */
public class InvalidDecoderException extends Exception {

	private static final long serialVersionUID = 6343298976947760980L;
	
	private String message = "";

	/** 
	 * Basic constructor for InvalidDecoderException with no message
	 */
	public InvalidDecoderException() { 
		super();
	}

	/** 
	 * Constructor for InvalidDecoderException with error message
	 *
	 * @param message Error message
	 */
	public InvalidDecoderException(String message) { 
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

