/*
 * Created on Feb 18, 2004
 *
 * $Id: LoggingException.java,v 1.1 2004/02/18 23:06:21 squiddity Exp $
 */
package context.arch.logging;

/**
 * 
 * 
 * @author alann
 */
public class LoggingException extends Exception {

	private static final long serialVersionUID = 5483263418329310890L;

	/**
	 * 
	 */
	public LoggingException() {
		super();
	}

	/**
	 * @param message
	 */
	public LoggingException(String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public LoggingException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public LoggingException(Throwable cause) {
		super(cause);
	}

}
