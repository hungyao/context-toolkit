package context.arch.service;

import context.arch.comm.DataObject;
import context.arch.service.helper.ServiceDescriptions;

import java.util.concurrent.ConcurrentHashMap;

/**
 * This class maintains a list of services.
 *
 * @see context.arch.service.Service
 */
public class Services extends ConcurrentHashMap<String, Service> {

	private static final long serialVersionUID = 245824478880470506L;

	/**
	 * Basic empty constructor
	 */
	public Services() {
		super();
	}

	public Services(Services original) {
		super(original);
	}	

	/**
	 * Adds the given Service object to the container.
	 *
	 * @param service Service to add
	 */
	public void add(Service service) {
		put(service.getName(), service);
	}

	/**
	 * Determines whether the given Service object is in the container
	 *
	 * @param service Name of the service to check
	 * @return whether Service is in the container
	 */
	public boolean hasService(String service) {
		return containsKey(service);
	}

	/**
	 * Returns the number of Services in the container
	 *
	 * return the number of Services in the container
	 */
	public int numServices() {
		return size();
	}

	/**
	 * This method returns the Service with the given name
	 * from this list of Services.
	 *
	 * @param name of the Service to return
	 * @return Service with the given name
	 */
	public Service getService(String name) {
		return (Service)get(name);
	}

	/**
	 * Creates a ServiceDescriptions object and returns the DataObject
	 * version of it
	 *
	 * @return Services object converted to an <SERVICES> DataObject
	 */
	public DataObject toDataObject() {
		ServiceDescriptions descriptions = new ServiceDescriptions();
		for (Service service : values()) {
			descriptions.addServiceDescription(service.getServiceDescription());
		}
		return descriptions.toDataObject();
	}    
}

