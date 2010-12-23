/*
 * LeaseWatcher.java
 *
 * Created on May 25, 2001, 2:03 PM
 */

package context.arch.discoverer.lease;

import javax.swing.Timer;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Vector;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is used to watch for all context components leases
 * each xx time, as defined by Lease.TIME_SLOT_MILLIS, and then send
 * to the LeasesKeeper object an
 * Enumeration of all component index whose lease expires.
 *
 * @author  Agathe
 */
public class LeasesWatcher {
	
	private static final Logger LOGGER = Logger.getLogger(LeasesWatcher.class.getName());
	static {LOGGER.setLevel(Level.WARNING);} // this should be set in a configuration file

	/**
	 * The timer object that waits for Lease.TIME_SLOT_MILLIS minutes to start
	 * the leases watching
	 *
	 * @see context.arch.discoverer.lease.Lease#TIME_SLOT_MILLIS
	 */
	private Timer timer;

	/**
	 * The LeasesKeeper object
	 */
	protected LeasesKeeper keeper;

	/**
	 * The vector containing the lease objects
	 */
	protected Vector<Lease> leases;

	/**
	 * Creates new LeasesWatcher
	 *
	 * @param discoverer The discoverer object
	 */
	public LeasesWatcher(LeasesKeeper leasesKeeper) {
		keeper = leasesKeeper;

		int delay = 2 * (int) Lease.TIME_SLOT_MILLIS;
		// Creates a new xx minutes Timer
		timer = new Timer(delay, new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				watchLeases();
			}
		});
		timer.start();
		LOGGER.info("LeasesWatcher <init> The timer ("+ timer.getDelay()+")has started... at " + Calendar.getInstance().getTime());
	}

	/**
	 * Stops the timer when this object is destroyed
	 */
	protected void finalize(){
		timer.stop();
	}


	/**
	 *
	 */
	protected void watchLeases() {
		LOGGER.info("\n\n-----LeasesWatcher <watchLeases> The timer has expired time= " + Calendar.getInstance().getTime());
		if (leases != null) {
//			// Take the current date
//			Calendar currentDate = Calendar.getInstance();
			// The result vector that will contain  index of the component to which
			// the discoverer has to send a lease end notification message
			ArrayList<String> leasesToEnd = new ArrayList<String>();

			// Checks all leases, if the lease expires, add the context component index to the vector
			for (Lease lease : leases) {
				//Tests if the lease corresponds to an existing component description
				if (keeper.existingComponentDescription(lease.getComponentIndex())) {
					// this commented code is the original: we don't check the components before the end of the lease
					/*Calendar date = l.getEndDate();
			          if (date.before (currentDate)){
			            // Adds the index
			            result.add (l.getComponentIndex());
			          }*/
//					Calendar date = lease.getEndDate();
					// Adds the index
					leasesToEnd.add(lease.getComponentIndex());
				}
			}
			if (!leasesToEnd.isEmpty()) {
				// Sends the Enumeration of leases terms to the LeasesKeeper
				keeper.leaseEndNotificationTo(leasesToEnd);
			}
		}
	}

	/**
	 * This method allows to copy the current leases into the object the
	 * LeasesWatcher will watch.
	 *
	 * @param currentLeases The vector object containing the leases
	 */
	synchronized protected void putLeases(Hashtable<String, Lease> currentLeases) {
		LOGGER.info("LeasesWatcher <putLeases> " + currentLeases);
		leases = null;
		// Does a copy of the object
		if (currentLeases != null) {
			leases = new Vector<Lease>(currentLeases.values());
		}
	}

}// class end
