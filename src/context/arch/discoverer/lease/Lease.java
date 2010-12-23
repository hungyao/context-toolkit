/*
 * Lease.java
 *
 * Created on May 24, 2001, 3:29 PM
 */

package context.arch.discoverer.lease;

import context.arch.comm.DataObject;
import context.arch.comm.DataObjects;

import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class keeps information about the lease of a context component when it
 * registers to the discoverer.
 * A lease is defined by the component for a given duration defined as un number
 * of timeslots.
 *
 * @author Agathe
 * @author Brian Y. Lim
 * @see context.arch.discoverer.lease.CTKCalendar
 */
public class Lease {
	
	private static final Logger LOGGER = Logger.getLogger(Lease.class.getName());
	static {LOGGER.setLevel(Level.WARNING);} // this should be set in a configuration file

	/**
	 * This constant is the default duration for a component when it does not
	 * specify a lease.
	 * 72 corresponds to 12 hours lease
	 */
	public static final int DEFAULT_DURATION = 1;//3;//72;

	/**
	 * This constant defines the smallest time slot. A lease is defined as a 
	 * multiple of this time slot.
	 * This corresponds to 10 minutes.
	 */
	public static final int TIME_SLOT_MILLIS = 60000; //600000;

	/**
	 * This tag is used to encapsulate a lease dataobject
	 */
	public static final String LEASE = "lease";

	/**
	 * This tag is used by the discoverer to inform the context component of
	 * the end of its lease.
	 * Then the component may reply with a lease renewal or accepts the term and
	 * returns a LEASE_END.
	 */
	public static final String LEASE_END_NOTIFICATION = "leaseEndNotification";

	/**
	 * This tag is used by the context component to extend its lease
	 */
	public static final String LEASE_RENEWAL = "leaseRenewal";

	/**
	 * This tag is used by the context component to stop its lease
	 */
	public static final String LEASE_END = "leaseEnd";

	/**
	 * This tag is used to specify the lease number of time slots
	 */
	public static final String TIMESLOTS = "timeslots";

	/**
	 * This tag is used to specify an error
	 */
	public static final String LEASE_ERROR = "leaseError";

	/**
	 * The date of first registration of the context component
	 */
	protected Calendar start;

	/**
	 * The date of unregistration of the context component
	 */
	protected Calendar end;

	/**
	 * The number of lease renewal the context component has done
	 */
	protected int nbRenewal = -1;

	/**
	 * The index of the component owning this lease
	 */
	protected String componentIndex;

	/**
	 * This number specify the duration of the lease as a number of timeslots
	 */
	protected int nbTimeSlots = -1;

	/** 
	 * Creates new Lease with the default duration
	 */
	public Lease () {
		this (DEFAULT_DURATION);
	}

	/**
	 * Creates new Lease with the specified number of timeslots
	 *
	 * @param long numberOfTimeslots The number of timeslots
	 */
	public Lease(int numberOfTimeSlots){
		if (numberOfTimeSlots > 0 ) {
			this.nbTimeSlots = numberOfTimeSlots;
		}
		else {
			this.nbTimeSlots = DEFAULT_DURATION;
		}
	}

	/**
	 * This method returns the DataObject version of the Lease object
	 *
	 * @return DataObject The data object version
	 */
	public DataObject toDataObject (){
		DataObject result;
		DataObjects v = new DataObjects();

		if (nbTimeSlots > 0){
			v.addElement (new DataObject(Lease.TIMESLOTS, String.valueOf (nbTimeSlots)));
		}
		else {
			v.add (new DataObject(Lease.TIMESLOTS, String.valueOf (Lease.DEFAULT_DURATION)));
		}
		result = new DataObject (Lease.LEASE, v);
		return result;
	}

	/**
	 * Returns a printable version of a Lease object 
	 *
	 * @return String The printable version of the object
	 */
	public String toString(){
		StringBuffer s = new StringBuffer();
		s.append ("nbTimeslots = " + nbTimeSlots + " = " + this.toMinutes() + "minutes"); 
		if (start != null){
			s.append (" - start = " + start.getTime());
		}
		if (end != null){
			s.append (" - end = " + end.getTime());
		}

		return s.toString ();
	}

	/**
	 * This method returns the duration of the lease in minutes
	 *
	 * @return long The minutes number of the lease
	 */
	public long toMinutes(){
		return ( (nbTimeSlots * Lease.TIME_SLOT_MILLIS) / 1000l) / 60l;
	}

	/**
	 * Set the component index information
	 *
	 * @param compId The index of the component description owning the lease
	 */
	public void setComponentIndex(String compId){
		if (compId != null) {
			componentIndex = compId;
		}
	}

	/**
	 * Returns the component index information
	 *
	 * @return Integer The index of the component description owning the lease
	 */
	public String getComponentIndex() {
		return componentIndex;
	}

	/**
	 * This method allows to put the current date/time as the start date of the
	 * lease.
	 */
	public void setStartDate() {
		this.setStartDate (Calendar.getInstance ());
	}

	/**
	 * This method allows to set the start date of the
	 * lease and then update the end date
	 *
	 * @param date The Calendar object
	 */
	public void setStartDate(Calendar date){
		start = date;
		nbRenewal = 0;
		int millis = this.nbTimeSlots * Lease.TIME_SLOT_MILLIS;
		end = (Calendar) start.clone ();
		end.add(Calendar.MILLISECOND, millis);
		LOGGER.info("Lease nbTimeSlots = " + this.nbTimeSlots);
		LOGGER.info("Lease - start date=" + start.getTime () + "\n - end=" + end.getTime ());
	}

	/**
	 *
	 */
	public Calendar getStartDate(){
		return start;
	}

	/**
	 *
	 */
	public Calendar getEndDate(){
		return end;
	}

	/**
	 * This method allows to convert a DataObject to a Lease object
	 *
	 * @param dataObject The dataObject containing lease information
	 * @return Lease The lease object
	 */
	public static Lease dataObjectToLease(DataObject dataObject){
		Lease result;
		if (dataObject != null) {
			DataObject leaseInfo = dataObject.getDataObject (Lease.LEASE);
			if (leaseInfo != null) {
				DataObject doSlots = leaseInfo.getDataObject (Lease.TIMESLOTS);
				if (doSlots != null){
					String slots = doSlots.getValue();
					if (slots != null){
						try {
							int nbSlots = Integer.parseInt (slots);
							result = new Lease(nbSlots);
							return result;
						}
						catch(NumberFormatException nfe){
							System.out.println("Lease dataObjectToLease - error : " + nfe);
						}
					}
				}
			}
		}
		result = new Lease();
		return result;
	}

}//end of class
