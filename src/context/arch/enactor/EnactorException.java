package context.arch.enactor;

/**
 * General Exception for Enactor package.
 * TODO: this doesn't add anything to the default Exception, so either add more info, or delete it --Brian
 * 
 * @author alann
 */
public class EnactorException extends Exception {

	private static final long serialVersionUID = -4446126290415171210L;


	public EnactorException() {}

	public EnactorException(String message) {
		super(message);
	}


	public EnactorException(String message, Throwable t) {
		super(message, t);
	}
}
