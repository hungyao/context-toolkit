package context.arch.service.helper;

import context.arch.comm.DataObject;
import context.arch.comm.DataObjects;
import context.arch.storage.Attributes;
import context.arch.util.Constants;

/**
 * This class implements a service input object used for executing a service.
 */
public class ServiceInput {

	/**
	 * Tag for a service input
	 */
	public static final String SERVICE_INPUT = "serviceInput";

	/**
	 * Tag for a service id
	 */
	public static final String SERVICE_ID = "serviceId";

	/**
	 * Tag for a service name
	 */
	public static final String SERVICE_NAME = ServiceDescription.SERVICE_NAME;

	/**
	 * Tag for a function name
	 */
	public static final String FUNCTION_NAME = FunctionDescription.FUNCTION_NAME;

	/**
	 * Tag for a service request tag
	 */
	public static final String REQUEST_TAG = "requestTag";

	private String serviceId;
	private String service;
	private String function;
	private Attributes input;
	private String host;
	private String port;
	private String id;
	private String requestTag;

	/**
	 * Basic constructor that creates a serviceInput object for a synchronous services.
	 * It contains the name of the service and function to execute and the input to 
	 * the service.
	 *
	 * @param service Name of the service to execute
	 * @param function Name of the service function to execute
	 * @input containint AttributeNameValues as input to the service
	 */
	public ServiceInput(String service, String function, Attributes input) {
		this.service = service;
		this.function = function;
		this.input = input;
	}

	/**
	 * Basic constructor that creates a serviceInput object for an asynchronous services.
	 * It contains the name of the service and function to execute and the input to 
	 * the service, just like the one for the synchronous service.  But, it also
	 * includes the requester's port, hostname, id, a unique tag to identify
	 * the request and the id of the component offering the service.
	 *
	 * @param serviceId Id of the component offering the service
	 * @param service Name of the service to execute
	 * @param function Name of the service function to execute
	 * @input AttributeNameValues object containing the input to the service
	 * @param host Hostname of the component requesting the service
	 * @param port Port of the component requesting the service
	 * @input id Id of the component requesting the service
	 * @input requestId Unique id for identifying the request
	 */
	public ServiceInput(String serviceId,String service, String function, Attributes input, String host, 
			int port, String id, String requestTag) {
		this(serviceId,service,function,input,host,String.valueOf(port),id,requestTag);
	}

	/**
	 * Basic constructor that creates a serviceInput object for an asynchronous services.
	 * It contains the name of the service and function to execute and the input to 
	 * the service, just like the one for the synchronous service.  But, it also
	 * includes the requester's port, hostname, id, a unique tag to identify
	 * the request and the id of the component offering the service.
	 *
	 * @param serviceId Id of the component offering the service
	 * @param service Name of the service to execute
	 * @param function Name of the service function to execute
	 * @input AttributeNameValues object containing the input to the service
	 * @param host Hostname of the component requesting the service
	 * @param port Port of the component requesting the service
	 * @input id Id of the component requesting the service
	 * @input requestId Unique id for identifying the request
	 */
	public ServiceInput(String serviceId,String service, String function, Attributes input, String host, 
			String port, String id, String requestTag) {
		this.serviceId = serviceId;
		this.service = service;
		this.function = function;
		this.input = input;
		this.host = host;
		this.port = port;
		this.id = id;
		this.requestTag = requestTag;
	}

	/**
	 * Basic constructor that creates a serviceInput object from a DataObject.
	 * The DataObject must contain the <SERVICE_INPUT> tag.
	 */
	public ServiceInput(DataObject data) {
		DataObject serv = data.getDataObject(SERVICE_INPUT);
		DataObject sidObj = serv.getDataObject(SERVICE_ID);
		if (sidObj != null) {
			serviceId = sidObj.getValue();
		}
		DataObject nameObj = serv.getDataObject(SERVICE_NAME);
		if (nameObj != null) {
			service = nameObj.getValue();
		}
		DataObject functionObj = serv.getDataObject(FUNCTION_NAME);
		if (functionObj != null) {
			function = functionObj.getValue();
		}
		input = Attributes.fromDataObject(serv);
		DataObject hostObj = serv.getDataObject(Constants.HOSTNAME);
		if (hostObj != null) {
			host = hostObj.getValue();
		}
		DataObject portObj = serv.getDataObject(Constants.PORT);
		if (portObj != null) {
			port = portObj.getValue();
		}
		DataObject idObj = serv.getDataObject(Constants.ID);
		if (idObj != null) {
			id = idObj.getValue();
		}
		DataObject requestTagObj = serv.getDataObject(REQUEST_TAG);
		if (requestTagObj != null) {
			requestTag = requestTagObj.getValue();
		}
	}

	/**
	 * This method converts the service input info to a DataObject
	 *
	 * @return ServiceInput object converted to a <SERVICE_INPUT> DataObject
	 */
	public DataObject toDataObject() {
		DataObjects v = new DataObjects();
		if (service != null) {
			v.addElement(new DataObject(SERVICE_NAME,service));
		}
		if (function != null) {
			v.addElement(new DataObject(FUNCTION_NAME,function));
		}
		if (input != null) {
			v.addElement(input.toDataObject());
		}
		if (serviceId != null) {
			v.addElement(new DataObject(SERVICE_ID,serviceId));
		}
		if (host != null) {
			v.addElement(new DataObject(Constants.HOSTNAME,host));
		}
		if (port != null) {
			v.addElement(new DataObject(Constants.PORT,port));
		}
		if (id != null) {
			v.addElement(new DataObject(Constants.ID,id));
		}
		if (requestTag != null) {
			v.addElement(new DataObject(REQUEST_TAG,requestTag));
		}
		return new DataObject(SERVICE_INPUT, v);
	}

	/**
	 * Sets the id of the component offering the service.
	 * 
	 * @param serviceId Id of the component offering the service
	 */
	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

	/**
	 * Returns the id of the component offering the service.
	 * 
	 * @return id of the component offering the service
	 */
	public String getServiceId() {
		return serviceId;
	}

	/**
	 * Sets the requested service name.
	 * 
	 * @param serviceName Name of the requested service
	 */
	public void setServiceName(String service) {
		this.service = service;
	}

	/**
	 * Returns the requested service name.
	 * 
	 * @return name of the requested service
	 */
	public String getServiceName() {
		return service;
	}

	/**
	 * Sets the requested service function name.
	 * 
	 * @param functionName Name of the requested service function
	 */
	public void setFunctionName(String function) {
		this.function = function;
	}

	/**
	 * Returns the requested service function name.
	 * 
	 * @return name of the requested service function
	 */
	public String getFunctionName() {
		return function;
	}

	/**
	 * Sets the input to the service.
	 * 
	 * @param input Attributes for the service to operate with
	 */
	public void setInput(Attributes input) {
		this.input = input;
	}

	/**
	 * Returns the input to the service.
	 * 
	 * @return input to the service
	 */
	public Attributes getInput() {
		return input;
	}

	/**
	 * Sets the requester's hostname.
	 * 
	 * @param host Hostname of the requester
	 */
	public void setHostname(String host) {
		this.host = host;
	}

	/**
	 * Returns the requester's hostname.
	 * 
	 * @return hostname of the requester
	 */
	public String getHostname() {
		return host;
	}

	/**
	 * Sets the requester's port.
	 * 
	 * @param port Port of the requester
	 */
	public void setPort(String port) {
		this.port = port;
	}

	/**
	 * Returns the requester's port.
	 * 
	 * @return Port of the requester
	 */
	public String getPort() {
		return port;
	}

	/**
	 * Sets the requester's id.
	 * 
	 * @param id Id of the requester
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Returns the requester's id.
	 * 
	 * @return id of the requester
	 */
	public String getId() {
		return id;
	}

	/**
	 * Sets the requester's request tag.
	 * 
	 * @param requestTag Tag for the requester's request
	 */
	public void setRequestTag(String requestTag) {
		this.requestTag = requestTag;
	}

	/**
	 * Returns the requester's requesting tag
	 * 
	 * @return the requester's requesting tag
	 */
	public String getRequestTag() {
		return requestTag;
	}

	/**
	 * Returns the requester's unique id.
	 * 
	 * @return the requester's unique id
	 */
	public String getUniqueId() {
		return requestTag+host+port+id;
	}

	/**
	 * This method returns a printable version of this class
	 *
	 * @return printable version of this class
	 */
	public String toString() {
		return new String("service="+service+",function="+function+",input="+input+
				",host="+host+",port="+port+",id="+id+",requestTag="+requestTag);
	}

}
