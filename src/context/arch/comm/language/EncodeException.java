package context.arch.comm.language;

/**
 * This class implements the EncodeException.  It is thrown if a parser
 * can not encode a given message
 *
 * @see context.arch.comm.language.ParserObject#encodeData(DataObject)
 * @see context.arch.comm.language.EncoderInterface#encodeData(DataObject)
 * @see context.arch.comm.language.DecodeException
 */
public class EncodeException extends Exception {

	private static final long serialVersionUID = 422304453143994489L;

	private String message = "";

	/** 
	 * Basic constructor for EncodeException with no message
	 */
	public EncodeException() { 
		super();
	}

	/** 
	 * Constructor for EncodeException with error message
	 *
	 * @param message Error message
	 */
	public EncodeException(String message) { 
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

