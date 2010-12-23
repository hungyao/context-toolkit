package context.arch.service.helper;

import java.util.Hashtable;

/**
 * This class maintains a list of pending service requests, kept by 
 * the service handling the requests.
 *
 * @see context.arch.service.Service
 */
public class PendingOut extends Hashtable<String, ServiceInput> {

	private static final long serialVersionUID = 6988481125316562365L;

	/**
	 * Basic empty constructor
	 */
	public PendingOut() {
		super();
	}

	/**
	 * Adds the given ServiceInput object to the pending container.
	 *
	 * @param input ServiceInput to make pending
	 */
	public void addPending(ServiceInput input) {
		put(input.getUniqueId(), input);
	}

	/**
	 * Determines whether the given ServiceInput object is in the pending container
	 *
	 * @param id Request id to look for
	 * @return whether ServiceInput is pending
	 */
	public boolean isPending(String id) {
		return containsKey(id);
	}

	/**
	 * Removes the given ServiceInput object is in the pending container
	 *
	 * @param id Request id of ServiceInput object to remove
	 */
	public void removePending(String id) {
		remove(id);
	}

	/**
	 * Returns the number of ServiceInput objects pending in the container
	 *
	 * return the number of ServiceInput objects pending in the container
	 */
	public int numPending() {
		return size();
	}

	/**
	 * This method returns the ServiceInput with the given request id
	 * from this list of pending ServiceInput objects.
	 *
	 * @param id Request id
	 * @return ServiceInput pending with the given id
	 */
	public ServiceInput getPending(String id) {
		return (ServiceInput)get(id);
	}

}
