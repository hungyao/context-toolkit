/*
 * LeasesKeeper.java
 *
 * Created on May 30, 2001, 10:53 AM
 */

package context.arch.discoverer.lease;

import context.arch.discoverer.DiscovererMediator;

import java.util.Hashtable;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A component registers to the discoverer for a given period of time that is
 * defined with a lease.
 * That allows the discovery system to make sure a component is still available.
 * This class keeps all information about the context components leases that
 * are registered.
 * It contains a LeasesWatcher object that watches the end of leases each 
 * xx minutes as defined in the Lease class by
 * the constant Lease.TIME_SLOT_MILLIS.
 * If the LeaseKeeper detects the end of a lease, it sends it to the discoverer
 * that will send a checking message to the component. 
 *
 * @author  Agathe
 * @see context.arch.discoverer.lease.LeasesWatcher
 * @see context.arch.discoverer.Discoverer
 */
public class LeasesKeeper {
	
	private static final Logger LOGGER = Logger.getLogger(LeasesKeeper.class.getName());
	static {LOGGER.setLevel(Level.WARNING);} // this should be set in a configuration file

	/**
	 * The discoverer object
	 */
	protected DiscovererMediator mediator;

	/**
	 * All leases, associates the component (Integer)index -> Lease object
	 */
	protected Hashtable<String, Lease> leases;

	/**
	 * The watcher that triggers the leases examination
	 */
	protected LeasesWatcher watcher;

	/** 
	 * Creates new LeasesKeeper
	 *
	 * TO COMPLETE (if the discoverer restarts from a log file)
	 *
	 * @param discoverer The Discoverer object
	 */
	public LeasesKeeper (DiscovererMediator mediator) {
		if (mediator != null){
			this.mediator = mediator;
		}
		watcher = new LeasesWatcher(this);
		leases = new Hashtable<String, Lease>();
	}

	/**
	 * Adds a Lease object and update the LeaseWatcher object
	 *
	 * @param lease The Lease object
	 */
	public void addLease(Lease lease){
		lease.setStartDate(); 
		leases.put(lease.getComponentIndex(), lease);
		watcher.putLeases(leases);
	}

	/**
	 * This method allows to send a list of leases that should end to the 
	 * discoverer.
	 *
	 * @param listOfLeaseEnd The leases to send to the discoverer
	 */
	public void leaseEndNotificationTo(ArrayList<String> listOfLeaseEnd){
		LOGGER.info("LeasesKeeper <leaseEndNotification> - lease=" + listOfLeaseEnd);
		mediator.sendLeaseEndNotificationTo(listOfLeaseEnd);
		LOGGER.info("has sent it to the disco");
	}

	/**
	 * This method allows to remove a Lease corresponding to the index
	 * 
	 * @param indexToRemove The index of the ComponentDescription for which we
	 * want to remove the lease
	 * @return Lease The removed Lease object
	 */
	public synchronized Lease removeLease(String indexToRemove) {
		LOGGER.info("LeaseKeeper <removeLease>");
		Lease l = null;
		if ((l = leases.remove(indexToRemove)) != null) {
			LOGGER.info("LeasesKeeper <removeLease> new leases ="+leases);
			watcher.putLeases(leases);
		}
		return l;
	}

	/**
	 * This method tests if an index of a ComponentDescription exists
	 *
	 * @param componentIndex The Integer to test
	 * @return boolean True if this object contains componentIndex
	 */
	public synchronized boolean contains(String componentIndex) {
		return leases.keySet().contains(componentIndex);
	}

	/**
	 * Returns a printable version of this object
	 *
	 * @return String
	 */
	public String toString(){
		String s = "Leases :";
		for (String index : leases.keySet()) {
			s += "\nIndex=" + index + " => " + leases.get(index);			
		}
		return s;
	}

	/**
	 * This method allows to renew an exiting lease
	 *
	 * @param index The index of the lease
	 * @param renewal The new lease
	 * @return boolean True if the lease has been updated for the given index
	 */
	public synchronized boolean renewLease(Lease renewal) {
		String index = renewal.getComponentIndex();
		if (index == null) { return false; }
		
		Object o = leases.remove(index);
		
		if (o != null) {
			renewal.setStartDate ();
			leases.put (renewal.getComponentIndex (), renewal);
			watcher.putLeases (leases);
			return true;
		}
		// Return false cause lease not found
		return false;  
	}

	/**
	 * Tests if an index corresponds to an existing ComponentDescription in the
	 * discoverer
	 *
	 * @param componentIndex The index of the object to test
	 * @return boolean True if there is an existing component description with
	 * this index in the discoverer
	 */
	public boolean existingComponentDescription(String componentIndex) {
		return mediator.exists(componentIndex);
	}

} // class end
