package context.arch.service;

import java.io.IOException;

import context.arch.comm.CommunicationsHandler;
import context.arch.comm.DataObject;
import context.arch.comm.DataObjects;
import context.arch.comm.language.DecodeException;
import context.arch.comm.language.EncodeException;
import context.arch.comm.language.InvalidDecoderException;
import context.arch.comm.language.InvalidEncoderException;
import context.arch.comm.protocol.InvalidProtocolException;
import context.arch.comm.protocol.ProtocolException;
import context.arch.service.helper.FunctionDescription;
import context.arch.service.helper.FunctionDescriptions;
import context.arch.service.helper.PendingOut;
import context.arch.service.helper.ServiceDescription;
import context.arch.service.helper.ServiceInput;
import context.arch.storage.Attributes;
import context.arch.util.Constants;

/**
 * This class implements a service object.
 * A Service represents ...?
 *
 * @see context.arch.service.Services
 */
public abstract class Service {

	/**
	 * Tag for a service request
	 */
	public static final String SERVICE_REQUEST = "serviceRequest";

	/**
	 * Tag to indicate message is a service reply
	 */
	public static final String SERVICE_REQUEST_REPLY = "serviceRequestReply";

	/**
	 * Tag for a service result
	 */
	public static final String SERVICE_RESULT = "serviceResult";

	/**
	 * Tag for a service result reply
	 */
	public static final String SERVICE_RESULT_REPLY = "serviceResultReply";

	/**
	 * Tag to indicate service function is SYNCHRONOUS
	 * @deprecated use {@link FunctionDescription#FUNCTION_SYNC} instead
	 */
	public static final String SYNCHRONOUS = FunctionDescription.FUNCTION_SYNC;//"synchronous";

	/**
	 * Tag to indicate service function is ASYNCHRONOUS
	 * @deprecated use {@link FunctionDescription#FUNCTION_ASYNC} instead
	 */
	public static final String ASYNCHRONOUS = FunctionDescription.FUNCTION_ASYNC;//"asynchronous";

	/**
	 * Tag to indicate the status of a service request
	 */
	public static final String STATUS = "status";

	/**
	 * Tag to indicate the status of a service request is executing
	 */
	public static final String EXECUTING = "executing";

	/**
	 * Tag to indicate the status of a service request is executed
	 */
	public static final String EXECUTED = "executed";

	/**
	 * Tag to indicate the status of a service request is failed
	 */
	public static final String FAILED = "failed";


	private String name;
	private FunctionDescriptions functions;
	protected CommunicationsHandler comm;
	protected PendingOut pending;

	/**
	 * Basic constructor that creates a service object.  It creates a
	 * pending queue to keep track of requests in the case that this
	 * service is an asynchronous service.
	 *
	 * @param comm Object that implements the CommunicationsHandler interface
	 * @see CommunicationsHandler
	 * @see PendingOut
	 */
	public Service(CommunicationsHandler comm) {
		this.comm = comm;
		pending = new PendingOut();
	}

	/**
	 * Basic constructor that creates a service object.  It creates a
	 * pending queue to keep track of requests in the case that this
	 * service is an asynchronous service.  It sets the service name and
	 * descriptions of the service functions.
	 *
	 * @param comm Object that implements the CommunicationsHandler interface
	 * @see CommunicationsHandler
	 * @see PendingOut
	 */
	public Service(CommunicationsHandler comm, String name, FunctionDescriptions functions) {
		this(comm);
		this.name = name;
		this.functions = functions;
	}

	/**
	 * This abstract method implements what should occur when the service is
	 * executed.
	 * 
	 * @param serviceInput Object that contains all the information necessary to execute the service.
	 * @return Result of the service request
	 */
	public abstract DataObject execute(ServiceInput serviceInput);

	/**
	 * This method is called to send a the results of an asynchronous service execution
	 * to a requesting component.
	 *
	 * @param input Object containing information on the requesting component
	 * @param atts AttributeNameValues containing the service results
	 * @return result of sending the service results
	 * @see CommunicationsHandler#userRequest(DataObject,String,String,int)
	 */
	protected DataObject sendServiceResult(ServiceInput input, Attributes atts) {
		DataObjects v = new DataObjects();
		v.addElement(new DataObject(Constants.ID,input.getId()));
		input.setInput(null);
		v.addElement(input.toDataObject());
		v.addElement(atts.toDataObject());
		DataObject result = new DataObject(SERVICE_RESULT,v);
		try {
			return comm.userRequest(result, SERVICE_RESULT, input.getHostname(), Integer.parseInt(input.getPort()));
		} catch (DecodeException de) {
			System.out.println("Service sendServiceResult() Decode: "+de);
		} catch (EncodeException ee) {
			System.out.println("Service sendServiceResult() Encode: "+ee);
		} catch (InvalidDecoderException ide) {
			System.out.println("Service sendServiceResult() InvalidDecoder: "+ide);
		} catch (InvalidEncoderException iee) {
			System.out.println("Service sendServiceResult() InvalidEncoder: "+iee);
		} catch (InvalidProtocolException ipe) {
			System.out.println("Service sendServiceResult() InvalidProtocol: "+ipe);
		} catch (ProtocolException pe) {
			System.out.println("Service sendServiceResult() Protocol: "+pe);
		} catch (IOException io) {
			System.out.println("Service sendServiceResult() IOException: "+io);
		}
		return null;
	}

	/**
	 * Sets the service name
	 *
	 * @param name Name of the service
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the service name
	 *
	 * @return name of the service
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the descriptions of the service functions
	 *
	 * @param functions Descriptions of the service functions
	 */
	public void setFunctionDescriptions(FunctionDescriptions functions) {
		this.functions = functions;
	}

	/**
	 * Returns the descriptions of the service functions
	 *
	 * @return descriptions of the service functions
	 */
	public FunctionDescriptions getFunctionDescriptions() {
		return functions;
	}

	/**
	 * Returns the ServiceDescription object 
	 *
	 * @return ServiceDescription object 
	 */
	public ServiceDescription getServiceDescription() {
		return new ServiceDescription(name,functions);
	}

	/**
	 * Creates a ServiceDescription object and returns the DataObject
	 * version of it
	 *
	 * @return Service object converted to an <SERVICE> DataObject
	 */
	public DataObject toDataObject() {
		return getServiceDescription().toDataObject();
	}

}
