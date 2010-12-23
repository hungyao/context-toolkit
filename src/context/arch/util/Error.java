package context.arch.util;

import context.arch.comm.DataObject;

/**
 * This class is a container for an error
 */
public class Error {

	/**
	 * Tag for the name of the callback
	 */
	public static final String ERROR_CODE = "errorCode";

	/**
	 * Tag for no errors
	 */
	public static final String NO_ERROR = "noError"; // TODO need to distinguish this with null: are they logically equivalent?

	/**
	 * Error tag for no results
	 */
	public static final String EMPTY_RESULT_ERROR = "emptyResultError";

	/**
	 * Error tag for invalid request
	 */
	public static final String INVALID_REQUEST_ERROR = "invalidRequestError";

	/**
	 * Tag if the given doesn't match the receiving component's id
	 */
	public static final String INVALID_ID_ERROR = "invalidIDError";

	/**
	 * Tag if a parameter to a method is missing
	 */
	public static final String MISSING_PARAMETER_ERROR = "missingParameterError";

	/**
	 * Tag if the subscriber in the message is unknown
	 */
	public static final String UNKNOWN_SUBSCRIBER_ERROR = "unknownSubscriberError";

	/**
	 * Tag if the callback is not known in the receiving component
	 */
	public static final String UNKNOWN_CALLBACK_ERROR = "unknownCallbackError";

	/**
	 * Tag if the method is not known in the receiving component
	 */
	public static final String UNKNOWN_METHOD_ERROR = "unknownMethodError";

	/**
	 * Tag if the data being requested is not valid
	 */
	public static final String INVALID_DATA_ERROR = "invalidDataError";

	/**
	 * Tag if the data being requested can not be fully returned
	 */
	public static final String INCOMPLETE_DATA_ERROR = "incompleteDataError";

	/**
	 * Tag if an attribute being requested is not valid
	 */
	public static final String INVALID_ATTRIBUTE_ERROR = "invalidAttributeError";

	/**
	 * Tag if a callback being subscribed to is not valid
	 */
	public static final String INVALID_CALLBACK_ERROR = "invalidCallbackError";

	/**
	 * Tag if a service requested does not exist
	 */
	public static final String UNKNOWN_SERVICE_ERROR = "unknownServiceError";

	/**
	 * Tag if a service function requested does not exist
	 */
	public static final String UNKNOWN_FUNCTION_ERROR = "unknownFunctionError";

	/**
	 * Tag if a service request id is unknown
	 */
	public static final String INVALID_REQUEST_ID_ERROR = "invalidRequestIdError";

	/**
	 * Tag if a service timing is incorrect (synchronous vs asynchronous
	 */
	public static final String INVALID_TIMING_ERROR = "invalidTimingError";

	/**
	 * Tag if a request is a duplicate
	 */
	public static final String DUPLICATE_ERROR = "duplicateError";

	/**
	 * Tag if a request is for an invalid subscription
	 */
	public static final String UNKNOWN_SUBSCRIPTION_ERROR = "unknownSubscriptionError";

	/**
	 * Tag used when IOException occurs
	 */
	public static final String IO_ERROR = "ioError";

	private String error;

	/**
	 * Empty constructor
	 */
	public Error() {
	}

	/**
	 * Constructor that takes an error string
	 *
	 * @param err String that defines the error
	 */
	public Error(String err) {
		error = err;
	}

	/**
	 * Constructor that takes a DataObject holding the error info
	 * The DataObject is expected to contain the <ERROR_CODE> tag.
	 *
	 * @param data DataObject containing the error info
	 */
	public Error(DataObject data) {
		DataObject err = data.getDataObject(ERROR_CODE);
		error = err.getValue();
	}

	/** 
	 * This method converts the Error object to a DataObject
	 *
	 * @return Error object converted to an <ERROR_CODE> DataObject
	 */
	public DataObject toDataObject() {
		return new DataObject(ERROR_CODE,error);
	}

	/**
	 * Sets the error string
	 *
	 * @param err String containing the error
	 * @return return itself updated, for chaining.
	 */
	public Error setError(String err) {
		error = err;
		return this;
	}

	/**
	 * Returns the error string
	 *
	 * @return error string
	 */
	public String getError() {
		return error;
	}

	/**
	 * Returns a printable version of this class
	 *
	 * @return printable version of this class
	 */
	public String toString() {
		return getError();
	}

}
