// Routines used for testing assertions. 
// (c) 1998 McGraw-Hill

package context.arch.util;

public class Assert
{
	private Assert()
	// pre: An Assert cannot be constructed.
	{
		Assert.fail("Attempt to construct an Assert!?");
	}

	static public void pre(boolean test, String message)
	// pre: result of precondition test.
	// post: does nothing if test true, otherwise abort w/message
	{
		if (test == false) throw new FailedPrecondition(message);
	}

	static public void post(boolean test, String message)
	// pre: result of postcondition test.
	// post: does nothing if test true, otherwise abort w/message
	{
		if (test == false) {
			throw new FailedPostcondition(message);
		}
	}

	static public void condition(boolean test)
	{
		condition(test, "No message");
	}

	static public void condition(boolean test, String message)
	// pre: result of general condition test.
	// post: does nothing if test true, otherwise abort w/message
	{
		if (test == false) throw new FailedAssertion(message);
	}

	static public void fail(String message)
	// post: throws error with message
	{
		throw new FailedAssertion(message);
	}
}

class FailedAssertion extends java.lang.Error
{
	private static final long serialVersionUID = 7894457794424843204L;

	public FailedAssertion(String reason)
	// post: constructs a new failed assertion error
	{
		super("\nAssertion that failed: " + reason);
	}
}

class FailedPrecondition extends FailedAssertion
{
	private static final long serialVersionUID = 5599144215721271264L;

	public FailedPrecondition(String reason)
	// post: constructs a new failed precondition
	{
		super("\nA precondition: " + reason);
	}
}

class FailedPostcondition extends FailedAssertion
{
	private static final long serialVersionUID = -2032607093049686385L;

	public FailedPostcondition(String reason)
	// post: constructs a new failed postcondition
	{
		super("\nA postcondition: " + reason);
	}
}

