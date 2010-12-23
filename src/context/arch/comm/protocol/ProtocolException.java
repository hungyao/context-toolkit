package context.arch.comm.protocol;

/**
 * This class implements the ProtocolException.  This exception is
 * thrown if the protocol handler class generates an error while
 * adding or stripping away the protocol.
 *
 * @see context.arch.comm.protocol.InvalidProtocolException
 */
public class ProtocolException extends Exception {

	private static final long serialVersionUID = 4651643561666814730L;
	
	private String message = "";

	/** 
	 * Basic constructor for ProtocolException with no message
	 */
	public ProtocolException() { 
		super();
	}

	/** 
	 * Constructor for ProtocolException with error message
	 *
	 * @param message Error message
	 */
	public ProtocolException(String message) { 
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

