package context.arch.service;

import context.arch.comm.DataObject;
import context.arch.comm.CommunicationsHandler;
import context.arch.storage.Attributes;
import context.arch.service.helper.FunctionDescription;
import context.arch.service.helper.ServiceInput;
import context.arch.service.helper.FunctionDescriptions;
import context.arch.handler.AsyncServiceHandler;
import context.arch.MethodException;
import context.arch.InvalidMethodException;

/**
 * This class implements a generic inherited service.  A service is inherited
 * by a server from a widget that it uses.  It allows the server to act as
 * a proxy to widget services.
 *
 * @see context.arch.service.Service
 * @see context.arch.handler.AsyncServiceHandler
 */
public class InheritedService extends Service implements AsyncServiceHandler {

	private String serviceHost;
	private String servicePort;
	private String serviceId;
	@SuppressWarnings("unused")
	private String serverName;
	private String serviceName;

	/**
	 * Basic constructor that creates an inherited service object.
	 *
	 * @param comm Object that implements the CommunicationsHandler interface
	 * @param serverName Name of the server offering the inherited service
	 * @param serviceName Name of the service being inherited
	 * @param descriptions FunctionDescriptions for the inherited service
	 * @param serviceHost Hostname of the component offering the original service
	 * @param servicePort Port of the component offering the original service
	 * @param serviceId Id of the component offering the original service
	 */
	public InheritedService(CommunicationsHandler comm, String serverName, String serviceName, FunctionDescriptions descriptions, 
			String serviceHost, String servicePort, String serviceId) {
		super(comm,serverName+serviceName,descriptions);
		this.serviceHost = serviceHost;
		this.servicePort = servicePort;
		this.serviceId = serviceId;
		this.serviceName = serviceName;
		this.serverName = serverName;
	}

	/**
	 * Basic constructor that creates an inherited service object.
	 *
	 * @param comm Object that implements the CommunicationsHandler interface
	 * @param serverName Name of the server offering the inherited service
	 * @param serviceName Name of the service being inherited
	 * @param descriptions FunctionDescriptions for the inherited service
	 * @param serviceHost Hostname of the component offering the original service
	 * @param servicePort Port of the component offering the original service
	 * @param serviceId Id of the component offering the original service
	 */
	public InheritedService(CommunicationsHandler comm, String serverName, String serviceName, FunctionDescriptions descriptions, 
			String serviceHost, int servicePort, String serviceId) {
		super(comm,serverName+serviceName,descriptions);
		this.serviceHost = serviceHost;
		this.servicePort = Integer.toString(servicePort);
		this.serviceId = serviceId;
		this.serviceName = serviceName;
		this.serverName = serverName;
	}

	/**
	 * This method is called when a component requests that this inherited service
	 * be executed.  It checks to see whether the service is synchronous or 
	 * asynchronous and then calls the inherited service.  If the service is
	 * asynchronous, it adds the request to its pending queue.
	 *
	 * @param serviceInput Object containing all information required to execute the service
	 * @return result of the service request
	 * @see CommunicationsHandler#executeSynchronousWidgetService(String,int,String,String,String,AttributeNameValues)
	 * @see CommunicationsHandler#executeAsynchronousWidgetService(AsyncServiceHandler,String,int,String,String,String,AttributeNameValues,String)
	 * @see context.arch.service.helper.PendingOut#addPending(ServiceInput)
	 */
	public DataObject execute(ServiceInput serviceInput) {
		String timing = getFunctionDescriptions().getFunctionDescription(serviceInput.getFunctionName()).getSynchronicity();
		if (timing.equals(FunctionDescription.FUNCTION_SYNC)) {
			// replace name; not sure why --Brian
			serviceInput.setServiceName(serviceName);
			
			return comm.executeSynchronousWidgetService(serviceHost,Integer.parseInt(servicePort),serviceId,
					//serviceName,serviceInput.getFunctionName(),serviceInput.getInput()
					serviceInput);
		}
		else if (timing.equals(FunctionDescription.FUNCTION_ASYNC)) {
			pending.addPending(serviceInput);
			return comm.executeAsynchronousWidgetService(this,serviceHost,Integer.parseInt(servicePort),serviceId,
					serviceName,serviceInput.getFunctionName(),serviceInput.getInput(),
					serviceInput.getUniqueId());
		}
		return new DataObject();
	}

	/**
	 * This method is called when this inherited asynchronous service returns 
	 * the result.  It collects the returned data, determines which requester
	 * the data should go to, removes the request from the pending queue and
	 * sends the data to the requester.
	 *
	 * @param requestTag Unique id to identify the request
	 * @param data DataObject containing the service results
	 * @return Always returns null
	 *
	 * @exception MethodException thrown if the asynchronous service result can't 
                be handled successfully
	 * @exception InvalidMethodException thrown if the asynchronous service result
	 *            was not expected and can't be routed correctly
	 * @see Service#sendServiceResult(ServiceInput,AttributeNameValues)
	 * @see context.arch.service.helper.PendingOut#removePending(String)
	 */ 
	public DataObject asynchronousServiceHandle(String requestTag, DataObject data) throws MethodException, InvalidMethodException {
		Attributes atts = Attributes.fromDataObject(data);
		ServiceInput si = pending.getPending(requestTag);
		pending.removePending(requestTag);
		@SuppressWarnings("unused")
		DataObject data2 = sendServiceResult(si,atts);
		return null;
	}

}
