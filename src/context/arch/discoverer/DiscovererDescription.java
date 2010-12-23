/*
 * DiscovererDescription.java
 *
 * Created on 7 avril 2001, 17:35
 */

package context.arch.discoverer;

import context.arch.comm.DataObject;
import context.arch.util.Error;

/**
 * This class keeps the information describing a discoverer
 *
 * @author  Agathe
 * @see context.arch.discoverer.Discoverer
 */
public class DiscovererDescription {

	public boolean available;
	private boolean waiting = false;
	// Id of the discoverer
	private String name;

	// Number of the discoverer port
	private int port = -1;

	// May be a hostname or a IP address
	private String hostname;


	/** 
	 * Generic constructor for DiscovererDescription that creates a new description 
	 * with a name, port and hostname
	 *
	 * @param dName The discoverer name
	 * @param dHostname The discoverer hostname
	 * @param dPort The discoverer port
	 * @see context.arch.discoverer.Discoverer
	 */
	public DiscovererDescription(String dName, String dHostname, int dPort) {
		name = dName;
		hostname = dHostname;
		port = dPort;
		available = true;
	}

	/** 
	 * Constructor for DiscovererDescription that takes no parameters
	 */
	public DiscovererDescription() {
		available = false;
	}

	/**
	 * Sets the discoverer name
	 * 
	 * @param discovererName The discoverer name
	 * @see context.arch.discoverer.Discoverer
	 */
	public void setName(String discovererName){
		name = discovererName;
		if (this.port != -1 || this.hostname!= null) {
			available = true;
		}
	}

	/**
	 * Set the discoverer name contained in a DataObject
	 *
	 * @param data The DataObject containing the name
	 * @return Error An error code
	 * @see context.arch.discoverer.Discoverer
	 * @see context.arch.comm.DataObject
	 */
	public Error setName(DataObject data) {
		Error err = new Error();
		if (data != null) {
			DataObject doName = data.getDataObject(Discoverer.DISCOVERER_ID);
			if (doName != null){
//				Vector vTemp = doName.getValue();
				String name = doName.getValue();
//				if ( ! vTemp.isEmpty()){
				if (name != null){
//					setName((String) vTemp.firstElement());
					setName(name);
					err.setError(Error.NO_ERROR);
					return err;
				}
			}
		}
		err.setError(Error.INVALID_DATA_ERROR);
		return err;
	}

	/**
	 * Sets the discoverer hostname
	 * 
	 * @param discovererHostname The discoverer hostname
	 * @see context.arch.discoverer.Discoverer
	 */
	public void setHostname(String discovererHostname){
		hostname = discovererHostname;
	}

	/**
	 * Set the discoverer hostname contained in a DataObject
	 *
	 * @param data The DataObject containing the hostname
	 * @return Error An error code
	 * @see context.arch.discoverer.Discoverer
	 * @see context.arch.comm.DataObject
	 */
	public Error setHostname (DataObject data){
		Error err = new Error();
		if (data != null) {
			DataObject doHost = data.getDataObject(Discoverer.HOSTNAME);
			if (doHost != null){
				setHostname(doHost.getValue());
				err.setError(Error.NO_ERROR);
				return err;
			}
		}
		err.setError(Error.INVALID_DATA_ERROR);
		return err;
	}

	/**
	 * Sets the discoverer port
	 * 
	 * @param discovererNPort The discoverer port
	 * @see context.arch.discoverer.Discoverer
	 */
	public void setPort(int discovererPort){
		port = discovererPort;
	}

	/**
	 * Set the discoverer port contained in a DataObject
	 *
	 * @param data The DataObject containing the port
	 * @return Error An error code
	 * @see context.arch.discoverer.Discoverer
	 * @see context.arch.comm.DataObject
	 */
	public Error setPort (DataObject data){
		Error err = new Error();
		if (data != null) {
			DataObject doPort = data.getDataObject(Discoverer.PORT);
			if (doPort != null){
				setPort(new Integer(doPort.getValue()).intValue() );
				err.setError(Error.NO_ERROR);
				return err;
			}
		}
		err.setError(Error.INVALID_DATA_ERROR);
		return err;
	}

	/**
	 * Returns the discoverer name
	 * 
	 * @return String The discoverer name
	 * @see context.arch.discoverer.Discoverer
	 */
	public String getName(){
		return name;
	}

	/**
	 * Returns the discoverer hostname
	 * 
	 * @return String The discoverer hostname
	 * @see context.arch.discoverer.Discoverer
	 */
	public String getHostname(){
		return hostname;
	}

	/**
	 * Sets the discoverer port
	 * 
	 * @return int The discoverer port
	 * @see context.arch.discoverer.Discoverer
	 */
	public int getPort(){
		return port;
	}

	/**
	 * Returns a printable version of the discoverer description
	 * 
	 * @return String The string version of the discoverer description
	 * @see context.arch.discoverer.Discoverer
	 */
	public String toString(){
		return "Discoverer : Id=" + name + " - Host=" + hostname + " - port=" + port;
	}

	/**
	 * Sets the discoverer description contained in a DataObject
	 * 
	 * @param data The DataObject containing the description
	 * @return Error The error code
	 * @see context.arch.discoverer.Discoverer
	 */
	public synchronized Error setDescription (DataObject data){
		Error err;
		Error error = new Error();

		error.setError(Error.NO_ERROR);

		err = setName(data);
		if ( ! err.getError().equals(Error.NO_ERROR) )
			error.setError(err.getError());

		err = setHostname(data);
		if ( ! err.getError().equals(Error.NO_ERROR) )
			error.setError(err.getError());

		err = setPort(data);
		if ( ! err.getError().equals(Error.NO_ERROR) )
			error.setError(err.getError());
		available = true;
		if (waiting){
			notifyAll();
		}

		return error;
	}

	public synchronized void waitAvailable(long time) {
		if (!available) {
			try {
				waiting = true;
				wait(time);
				waiting = false;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}


}//class end
