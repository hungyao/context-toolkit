package context.arch.comm.protocol;

/**
 * This class implements the InvalidProtocolException.  This exception is
 * thrown if the protocol handler class selected can not be instantiated.
 *
 * @see context.arch.comm.CommunicationsObject#start()
 * @see context.arch.comm.protocol.ProtocolException
 */
public class InvalidProtocolException extends Exception {

	private static final long serialVersionUID = 3607040531528207299L;
	
	private String message = "";

	/** 
	 * Basic constructor for InvalidProtocolException with no message
	 */
	public InvalidProtocolException() { 
		super();
	}

	/** 
	 * Constructor for InvalidProtocolException with error message
	 *
	 * @param message Error message
	 */
	public InvalidProtocolException(String message) { 
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

