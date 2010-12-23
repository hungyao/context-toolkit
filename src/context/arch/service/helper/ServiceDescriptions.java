package context.arch.service.helper;

import context.arch.comm.DataObject;
import context.arch.comm.DataObjects;

import java.util.Vector;

/**
 * This class maintains a list of service descriptions.
 *
 * @see context.arch.service.Service
 */
public class ServiceDescriptions extends Vector<ServiceDescription> {

	private static final long serialVersionUID = -443788880568797984L;
	
	/**
	 * Tag for a widget's services
	 */
	public static final String SERVICES = "services";

	/**
	 * Basic empty constructor
	 */
	public ServiceDescriptions() {
		super();
	}

	/**
	 * Constructor that takes a DataObject as a parameter.  The DataObject
	 * must contain the tag <SERVICES>.  It stores the encoded data.
	 *
	 * @param data DataObject that contains the service info
	 */
	public ServiceDescriptions(DataObject data) {
		super();
		DataObject services = data.getDataObject(SERVICES);
		DataObjects v = services.getChildren();
		for (int i=0; i<v.size(); i++) {
			addServiceDescription(new ServiceDescription((DataObject)v.elementAt(i)));
		}
	}

	/**
	 * Converts to a DataObject.
	 *
	 * @return Services object converted to an <Services> DataObject
	 */
	public DataObject toDataObject() {
		DataObjects v = new DataObjects();
		for (int i=0; i<numServiceDescriptions(); i++) {
			v.addElement(getServiceDescriptionAt(i).toDataObject());
		}   
		return new DataObject(SERVICES,v);
	}

	/**
	 * Adds the given ServiceDescription object to the container.
	 *
	 * @param service ServiceDescription to add
	 */
	public void addServiceDescription(ServiceDescription service) {
		addElement(service);
	}

	/**
	 * Adds the given service description to the container.
	 *
	 * @param name Name of the service to add
	 * @param descriptions Descriptions of the service being added
	 */
	public void addServiceDescription(String name, FunctionDescriptions descriptions) {
		addElement(new ServiceDescription(name,descriptions));
	}

	/**
	 * Adds the given ServiceDescriptions object to the container.
	 *
	 * @param services ServiceDescriptions to add
	 */
	public void addServiceDescriptions(ServiceDescriptions services) {
		for (int i=0; i<services.numServiceDescriptions(); i++) {
			addServiceDescription(services.getServiceDescriptionAt(i));
		}
	}

	/**
	 * Returns the ServiceDescription object at the given index
	 *
	 * @param index Index into the container
	 * @return ServiceDescription at the specified index
	 */
	public ServiceDescription getServiceDescriptionAt(int index) {
		return (ServiceDescription)elementAt(index);
	}

	/**
	 * Determines whether the given ServiceDescription object is in the container
	 *
	 * @param call ServiceDescription to check
	 * @return whether ServiceDescription is in the container
	 */
	public boolean hasServiceDescription(ServiceDescription service) {
		return contains(service);
	}

	/**
	 * Determines whether the given service description is in the container.
	 *
	 * @param name Name of the service to check
	 * @param descriptions Descriptions of the service to check
	 * @return whether the given service description is in the container
	 */
	public boolean hasServiceDescription(String name, FunctionDescriptions descriptions) {
		return contains(new ServiceDescription(name,descriptions));
	}

	/**
	 * Determines whether a service with the given name is in the container
	 *
	 * @param name Name of the service to look for
	 * @return whether a service with the given name is in the container
	 */
	public boolean hasServiceDescription(String name) {
		for (int i=0; i<numServiceDescriptions(); i++) {
			if (getServiceDescriptionAt(i).getName().equals(name)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the index at which the ServiceDescription object occurs
	 *
	 * @param service ServiceDescription to look for
	 * @return index of the specified ServiceDescription
	 */
	public int indexOfServiceDescription(ServiceDescription service) {
		return indexOf(service);
	}

	/**
	 * Returns the index at which the given service description occurs
	 *
	 * @param name Name of the service to look for
	 * @param descriptions Descriptions of the service to look for
	 */
	public int indexOfServiceDescription(String name, FunctionDescriptions descriptions) {
		return indexOf(new ServiceDescription(name,descriptions));
	}

	/**
	 * Returns the number of ServiceDescriptions in the container
	 *
	 * return the number of ServiceDescriptions in the container
	 */
	public int numServiceDescriptions() {
		return size();
	}

	/**
	 * This method returns the ServiceDescription with the given name
	 * from this list of ServiceDescriptions.
	 *
	 * @param name of the Service to return
	 * @return ServiceDescription with the given name
	 */
	public ServiceDescription getServiceDescription(String name) {
		for (int i=0; i<numServiceDescriptions(); i++) {
			ServiceDescription service = getServiceDescriptionAt(i);
			if (service.getName().equals(name)) {
				return service;
			}
		}
		return null;
	}

}

