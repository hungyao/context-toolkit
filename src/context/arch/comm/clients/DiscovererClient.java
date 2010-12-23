/*
 * DiscovererClient.java
 *
 * Created on May 16, 2001, 11:26 AM
 */

package context.arch.comm.clients;

import context.arch.BaseObject;
import context.arch.comm.DataObject;
import context.arch.discoverer.Discoverer;

/**
 * This class is used by a context component to handle a communication with
 * the discoverer while continuing a communication with another context component.
 *
 * @author  Agathe
 * @see context.arch.comm.clients.IndependentClient
 * @see context.arch.discoverer.Discoverer
 * @see context.arch.BaseObject
 */

public class DiscovererClient extends IndependentClient {

	/** Debug flag */
	public static boolean DEBUG = false;
	/**
	 * The method
	 */
	protected String method;

	/**
	 * The data to send
	 */
	protected DataObject data;

	/**
	 * Other parameters
	 */
	protected String otherParameter;

	/**
	 * The generic DiscovererClient constructor
	 *
	 * @param baseObject The base object owning this DiscovererClient object
	 * @param methodToUse The method type of the data object to send to the discoverer
	 * @param dataToSend The data to send
	 * @param conditionParameter Other parameters
	 */
	public DiscovererClient (BaseObject baseObject,String methodToUse,DataObject dataToSend,String conditionParameter) {
		super(baseObject);
		method = methodToUse;
		data = dataToSend;
		otherParameter = conditionParameter;
	}

	/**
	 * The DiscovererClient constructor
	 *
	 * @param baseObject The base object owning this DiscovererClient object
	 * @param methodToUse The method type of the data object to send to the discoverer
	 * @param dataToSend The data to send
	 */
	public DiscovererClient (BaseObject baseObject,String method,DataObject data) {
		this (baseObject, method, data, null);
	}

	/**
	 * This class overrides the handleCommunication() and process the communication
	 */
	public void handleCommunication(){
		if (method.equalsIgnoreCase(Discoverer.DISCOVERER_UPDATE)) {
			if (otherParameter != null) {
				debugprintln("DiscovererClient handle comm otherparameter");
				parent.discovererUpdate();
			} 
			else {
				debugprintln("DiscovererClient handle comm");
				parent.discovererUpdate();
			}
		}
	}

	/** Print a message if the DEBUG mode is active
	 *
	 * @param s Any object, even null
	 */
	public void debugprintln(Object s){
		if (DEBUG) {
			System.out.println("" + s);
			System.out.flush();
		}
	}

}
