package context.arch.comm.language;

/**
 * This class implements the DecodeException.  It is thrown if a parser
 * can not decode a given message
 *
 * @see context.arch.comm.language.ParserObject#decodeData(Reader)
 * @see context.arch.comm.language.DecoderInterface#decodeData(Reader)
 * @see context.arch.comm.language.EncodeException
 */
public class DecodeException extends Exception {

	private static final long serialVersionUID = 1622924277664397866L;

	private String message = "";

	/** 
	 * Basic constructor for DecodeException with no message
	 */
	public DecodeException() { 
		super();
	}

	/** 
	 * Constructor for DecodeException with error message
	 *
	 * @param message Error message
	 */
	public DecodeException(String message) { 
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

