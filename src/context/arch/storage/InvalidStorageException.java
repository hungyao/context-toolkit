package context.arch.storage;

/**
 * This class implements the InvalidStorageException.  It is thrown if a
 * Storage object can not be created.
 *
 * @see context.arch.storage.Storage
 */
public class InvalidStorageException extends Exception {

	private static final long serialVersionUID = -5846866877835074289L;

	private String message = "";

	/** 
	 * Basic constructor for InvalidStorageException with no message
	 */
	public InvalidStorageException() { 
		super();
	}

	/** 
	 * Constructor for InvalidStorageException with error message
	 *
	 * @param message Error message
	 */
	public InvalidStorageException(String message) { 
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

