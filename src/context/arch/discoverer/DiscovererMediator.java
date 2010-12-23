/*
 * DiscovererMediator.java
 *
 * Created on July 2, 2001, 4:00 PM
 */

package context.arch.discoverer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import context.arch.BaseObject;
import context.arch.comm.DataObject;
import context.arch.comm.DataObjects;
import context.arch.comm.RequestObject;
import context.arch.comm.clients.IndependentCommunication;
import context.arch.comm.language.MessageHandler;
import context.arch.discoverer.component.dataModel.AbstractDataModel;
import context.arch.discoverer.component.dataModel.DiscovererDataModel;
import context.arch.discoverer.lease.Lease;
import context.arch.discoverer.lease.LeasesKeeper;
import context.arch.discoverer.query.AbstractQueryItem;
import context.arch.subscriber.AbstractSubscriber;
import context.arch.subscriber.Subscribers;
import context.arch.util.Error;

/**
 * This mediator allows to handle the dialog between the discoverer and 
 * the DiscovererDataModel, the LeasesKeeper.
 * It is able to store components into the database, to update and remove them.
 * It handles queries and subscription queries.
 * It asks the discoverer to check the components' liveliness
 *
 * @author  Agathe
 */
public class DiscovererMediator {
	
	private static final Logger LOGGER = Logger.getLogger(DiscovererMediator.class.getName());
	static {LOGGER.setLevel(Level.WARNING);} // this should be set in a configuration file

	/**
	 * The Discoverer object the DiscovererMediator works for
	 */
	private Discoverer discoverer;

	/**
	 * The message handler (the Discoverer object) used to decode and encode 
	 * DataObject
	 */
	@SuppressWarnings("unused")
	private MessageHandler mh;

	/**
	 * The database object where the DiscovererMediator stores the index tables
	 */
	private AbstractDataModel dataModel;

	/**
	 * The object containing a timer that triggers the check of components' 
	 * liveliness
	 */
	private LeasesKeeper leasesKeeper;

	/**
	 * Boolean to keep a logfile
	 */
	public boolean useLogFile = false;

	/**
	 * Tags used for the log file
	 */
	private final static String ENTRY_STRING = "entry:";
	private final static String ADD_COMP = "addComp:";
	private final static String REMOVE_COMP = "removeComp:";


	/** 
	 * The default name for the Discoverer database log file
	 */
	@SuppressWarnings("unused")
	private String filename = "discoverer-database.xml";

	/**
	 * Creates new DiscovererMediator 
	 *
	 * @param discoverer The discoverer object
	 * @param keppLogFile If true, the log file is updated
	 */
	public DiscovererMediator(Discoverer discoverer, boolean keepLogFile) {
		dataModel = new DiscovererDataModel();
		this.discoverer = discoverer;
		mh = (MessageHandler) discoverer;
		leasesKeeper = new LeasesKeeper(this);
		useLogFile = keepLogFile;
		filename = Subscribers.WIDGET_SUBSCRIPTIONS_DIR + this.discoverer.getId () + "-database.xml";

		setRegisteredComponents ();

	}

	/**
	 * This method allows to register a ComponentDescription object
	 *
	 * @param comp the ComponentDescription object
	 * @param lease The lease specified for this ComponentDescripion object
	 * @return Error
	 */
	public Error add(ComponentDescription comp, Lease lease) {
		Error error = new Error(Error.NO_ERROR);
		
		// If it already exists : removes it
		ComponentDescription old = dataModel.remove(comp.id);
		if (old != null) {
			if (useLogFile) {
				removeFromLog(old);
			}
			leasesKeeper.removeLease(comp.id);
		}
		
		// Now adds it
		dataModel.add(comp);
		if (useLogFile) {
			addToLog(comp);
		}

		// Registers the lease
		lease.setComponentIndex(comp.id);
		leasesKeeper.addLease(lease);

		return error;
	}

	/**
	 * Updates a lease for a registered component
	 *
	 * @param compId The id of the component
	 * @param lease The new Lease object
	 * @return Error
	 */
	public Error updateLease(String compId, Lease lease){
		Error err = new Error();
		lease.setComponentIndex(compId);
		// Updates the lease
		if (leasesKeeper.renewLease(lease)) {
			err.setError (Error.NO_ERROR);
		}
		else {
			err.setError (Lease.LEASE_ERROR);
		}
		return err;
	}

	/**
	 * Take a query and returns the corresponding (multiple) components
	 *
	 * @param query 
	 * @return DataObject Contains the component descriptions of the component 
	 * matching the query
	 */
	public DataObject search(AbstractQueryItem<?,?> query) {
		Collection<ComponentDescription> components = findComponents(query);

		DataObjects content = new DataObjects();
		DataObject result = null;

		for (ComponentDescription comp : components) {
			DataObject compDataObj = comp.getBasicDataObject();
			DataObject newContent = new DataObject(Discoverer.DISCOVERER_QUERY_REPLY_CONTENT, compDataObj.getChildren());
			
			LOGGER.info("\n\n a content = " + newContent);
			content.addElement(newContent);
		}
		
		result = new DataObject(Discoverer.DISCOVERER_QUERY_REPLY_CONTENTS, content);
		return result;
	}

	/**
	 * Gets an AbstractQueryObject and returns a array with for each index
	 * of registered components a value yes/no to indicate if this component
	 * fits the query or not
	 *
	 * TODO : complete the search to remove the widget included in server: USEFUL?? --who?
	 *
	 * @param query The abstract query object
	 * @return 
	 */
	public Collection<ComponentDescription> findComponents(AbstractQueryItem<?,?> query) {
		// Result from the data Model
//		System.out.println("DiscovererMediator.findComponents query: " + query);
//		System.out.println("DiscovererMediator.findComponents dataModel: " + dataModel);
		Collection<ComponentDescription> components = query.search(dataModel); // TODO should not delegate to an external class that should not need to know about internal formats

		// Do some complementary process: for example, is a server has subscribed to a widget.
		return components;
	}

	/**
	 * Returns a printable version of this object: the database content, the
	 * leases.
	 *
	 * @return String
	 */
	public String toString(){
		StringBuffer sb = new StringBuffer("DiscovererMediator");
		sb.append (dataModel.toString ());
		sb.append ("\n\n");
		sb.append (leasesKeeper.toString ());
		return sb.toString ();
	}

	/**
	 * Removes a component from the discoverer base
	 *
	 * @param compId The ComponentDescription's id
	 * @return Error
	 */
	public Error remove(String compId){
		Error error = new Error(Error.NO_ERROR);
		
		ComponentDescription removed = dataModel.remove(compId); // remove it
		
		// Update the log file
		if (removed != null && useLogFile){
			removeFromLog(removed);
		}
		
		// Remove the lease
		leasesKeeper.removeLease(compId);
		
		// Remove from the subscriber
		for (AbstractSubscriber sub : discoverer.subscribers.values()) {
			String subId = sub.getSubscriptionId();
			if (subId.startsWith(compId)
//					&& sub instanceof DiscovererSubscriber // I don't think this is required --Brian
					)
				discoverer.subscribers.removeSubscriber(sub, true);
		}
		if (removed == null) {
			error.setError (Error.INVALID_REQUEST_ERROR);
		}
		
		return error;
	}

	/**
	 * Update the description of a component and its lease
	 *
	 * @param component The component to update
	 * @param lease The new Lease
	 * @return Error 
	 */
	public Error update(ComponentDescription component, Lease lease){
		Error error = new Error(Error.NO_ERROR);
		String index = dataModel.update(component);
		// Update the log file
		if (useLogFile){
			removeFromLog (component);
			addToLog (component);
		}
		if (index == null)
			error.setError (Error.ERROR_CODE);
		if (lease != null){
			updateLease(index, lease);
		}
		return error;
	}

	/**
	 * Returns the ComponentDescription of a registered component
	 *
	 * @param stringOrIndex The component index or id
	 * @return ComponentDescription
	 */
	public ComponentDescription getComponentDescription(String componentIndex){
		return dataModel.getComponent(componentIndex);
	}

	/**
	 * This method allows to send a lease end notification to each
	 * component whose lease ends. The reply received by the component
	 * either renews the lease or confirms it.
	 * The list of components is sent back to the discoverer that take
	 * care of sending a message to them
	 *
	 * @listOfComponents The components index
	 */
	public void sendLeaseEndNotificationTo(ArrayList<String> listOfComponents) {
		discoverer.sendLeaseEndNotificationTo(listOfComponents);
	}

	/**
	 * Return true if the component is registered by the discoverer
	 *
	 * @param componentIndex The component index or id
	 * @return boolean
	 */
	public boolean exists(String componentIndex) {
		return dataModel.getComponent(componentIndex) != null;
	}

	/**
	 * Adds a component description to the log file
	 *
	 * @param comp The component description
	 */
	public synchronized void addToLog(ComponentDescription comp) {
		writeLog(ENTRY_STRING+ADD_COMP, comp);
	}

	/**
	 * Writes something to the log file
	 * 
	 * TODO: currently disabled till a more stable log format replaces the flat file storage
	 *
	 * @param header The header to write
	 * @param comp The component to write
	 */
	private void writeLog(String header, ComponentDescription comp) {
////		synchronized (this) {	
//			BufferedWriter writer = null;
//			try {
//				writer = new BufferedWriter(FileUtil.getWriter(filename, true));
//				String out = new String(header+mh.encodeData(comp.toDataObject())+"\n");
//				writer.write(out,0,out.length());
//				writer.flush();
//			} catch (IOException ioe) {
//				System.out.println("DiscovererDataModel writeLog() IO: "  + ioe + ", filename=" + filename);
//				ioe.printStackTrace(System.err);
//			} catch (EncodeException ee) {
//				System.out.println("DiscovererDataModel writeLog() Encode: "+ee);
//			} catch (InvalidEncoderException iee) {
//				System.out.println("DiscovererDataModel writeLog() InvalidEncoder: "+iee);
//			} finally {
//				FileUtil.closeWriter(writer);
//			}
////		}
	}

	/**
	 * Adds the information that a component is removed from the database into
	 * the log file
	 *
	 * @param comp The component description to remove
	 */
	public synchronized void removeFromLog(ComponentDescription comp){
		removeFromLog(ENTRY_STRING+REMOVE_COMP, comp);
	}

	/**
	 * Adds the remove information into the log file
	 * TODO: currently disabled till a more stable log format replaces the flat file storage
	 *
	 * @param header The header information
	 * @param comp The component description removed
	 */
	public void removeFromLog(String header, ComponentDescription comp){
////		synchronized (this) {
//			BufferedWriter writer = null;
//			try {
//				writer = new BufferedWriter(FileUtil.getWriter(filename, true));
//				LOGGER.info("\n\nDiscoDataModel <removeFromLog> comp " + comp);
//				LOGGER.info("\nto dataObj " + comp.toDataObject ());
//				String out = new String(header+mh.encodeData(comp.toDataObject())+"\n");
//				writer.write(out,0,out.length());
//				writer.flush();
//			} catch (IOException ioe) {
//				System.out.println("DiscovererDataModel removeFromLog() IO: "+ioe);
//			} catch (EncodeException ee) {
//				System.out.println("DiscovererDataModel removeFromLog() Encode: "+ee);
//			} catch (InvalidEncoderException iee) {
//				System.out.println("DiscovererDataModel removeFromLog() InvalidEncoder: "+iee);
//			} finally {
//				FileUtil.closeWriter(writer);
//			}
////		}
	}

	/**
	 * Retrieve the information from the log file, check the liveliness of the
	 * components and put them back into the database
	 */
	private void setRegisteredComponents(){
		// Get the list of component to restart
		HashMap<String, ComponentDescription> uncheckedComps = restartRegistrations();
		if (uncheckedComps != null){
			// Check them to be sure they are still alive
//			int i = 0;
			// Uses an independent communication to send a PING
			for (ComponentDescription comp : uncheckedComps.values()) {
				IndependentCommunication indComm = new IndependentCommunication(
						new RequestObject(null,null, 
								comp.hostname,comp.port,comp.id), true);
				indComm.setObjectToStore (comp);
				indComm.setSenderClassId (Discoverer.DISCOVERER+Discoverer.REGISTERER+BaseObject.PING);
				discoverer.pingComponent(indComm);
//				i++;
			}
		}
	}

	/**
	 * Overrides the method that handle independent Reply. If the independent
	 * communication has been sent by this class, this class handles it. Otherwise
	 * the super class handleIndependeReply is called.
	 *
	 * Catches:
	 * - Discoverer+Registerer+Ping message
	 *
	 * TO DO: restore the lease?? not useful because the next time the discoverer
	 * will check the component, the component will send its lease
	 *
	 */
	public void handleIndependentReply(IndependentCommunication independentCommunication){
		LOGGER.info("\nThe discovererMediator gets the reply from the the element ");
		if (independentCommunication != null) {
			independentCommunication.decodeReply (discoverer);
			DataObject replyContent = independentCommunication.getDecodedReply ();
			LOGGER.info("\nDiscovererMediator <handleIndependentReply> Reply=" + replyContent + " - exceptions " + independentCommunication.getExceptions ());

			// For RESTART REGISTRATION
			if (independentCommunication.getSenderClassId ().equals (Discoverer.DISCOVERER+Discoverer.REGISTERER+BaseObject.PING)){
				if ( ! independentCommunication.getExceptions ().isEmpty () 
						|| replyContent == null){
					LOGGER.info("DiscovererMediator <handleIndependentReply> comp does not answer " + ((ComponentDescription)(independentCommunication.getObjectToStore ())).id);
				}
				// Adds the comp into the database if the comp is alive
				else {
					LOGGER.info("DiscovererMediator <handleIndependentReply> add the comp" );
					ComponentDescription comp =  (ComponentDescription) independentCommunication.getObjectToStore ();
					this.add(comp, new Lease());
				}
			}
		}
	}

	/**
	 * Retrieve a list of component to restart from the log file
	 * TODO: currently disabled till a more stable log format replaces the flat file storage
	 *
	 * @return Object contains a HashMap object
	 */
	private HashMap<String, ComponentDescription> restartRegistrations() {
//		String log = FileUtil.read(filename);
//		int index = log.indexOf(ENTRY_STRING);
		HashMap<String, ComponentDescription> hash = new HashMap<String, ComponentDescription>();
//
//		while (index != -1) {
//			String entry1 = null; // contains the command
//			int index2 = log.indexOf(ENTRY_STRING,index+1);
//			if (index2 == -1) {
//				entry1 = log.substring(index+ENTRY_STRING.length());
//			}
//			else {
//				entry1 = log.substring(index+ENTRY_STRING.length(),index2);
//			}
//			try { // Test the message code : ADD, REMOVE and creates a new Component
//				// object based on the log file
//				if (entry1.indexOf(ADD_COMP) != -1) {
//					index = entry1.indexOf(">");
//					String entry = entry1.substring(index+1);
//					ComponentDescription comp = ComponentDescription.fromDataObject (mh.decodeData(new StringReader(entry)));
//					if (hash.containsKey (comp.id)){
//						hash.remove (comp.id);
//					}
//					hash.put (comp.id, comp); 
//				}
//				else if (entry1.indexOf(REMOVE_COMP) != -1) {
//					index = entry1.indexOf(">");
//					String entry = entry1.substring(index+1);
//					ComponentDescription comp = ComponentDescription.fromDataObject (mh.decodeData(new StringReader(entry)));
//					hash.remove (comp.id); 
//				}
//			} catch (DecodeException de) {
//				System.out.println("DiscovererDataModel Decode: "+de);
//			} catch (InvalidDecoderException ide) {
//				System.out.println("DiscovererDataModel InvalidDecoder: "+ide);
//			}
//			index = index2;
//		}
//		FileWriter fw = null;
//		try {
////			synchronized (this) {			
//				// Clear the content of the file
//				fw = FileUtil.getWriter(filename, false);
//				fw.flush ();
//				fw.close ();
////			}
//		} catch (IOException ioe) {
//			System.out.println("DiscovererDataModel writeLog() IO: "+ioe);
//		} finally {
//			FileUtil.closeWriter(fw);
//		}
		return hash;
	}

}//class end
