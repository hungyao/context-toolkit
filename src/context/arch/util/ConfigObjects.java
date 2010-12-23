package context.arch.util;

import context.arch.comm.DataObject;

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;

/**
 * This class maintains a list of configuration objects.
 *
 * @see context.arch.util.ConfigObject
 */
public class ConfigObjects extends Hashtable<String, ConfigObject> {

	private static final long serialVersionUID = -530097038134911072L;

	/**
	 * Tag for component id
	 */
	public static final String ID = "ID";

	/**
	 * Tag for component hostname
	 */
	public static final String HOSTNAME = "HOST";

	/**
	 * Tag for component port
	 */
	public static final String PORT = "PORT";

	/**
	 * Tag for component type
	 */
	public static final String TYPE = "TYPE";

	/**
	 * Basic empty constructor
	 */
	public ConfigObjects() {
		super();
	}

	/**
	 * Constructor that creates a ConfigObjects object from a 
	 * DataObject.
	 *
	 * @param data DataObject containing configuration information
	 */
	public ConfigObjects(DataObject data) {
		super();
		Vector<DataObject> v = data.getChildren();
		for (int i=0; i<v.size(); i++) {
			DataObject d = (DataObject)v.elementAt(i);
			String id = (String)d.getDataObjectFirstValue(ID);
			String host = (String)d.getDataObjectFirstValue(HOSTNAME);
			String port = (String)d.getDataObjectFirstValue(PORT);
			String type = (String)d.getDataObjectFirstValue(TYPE);
			addConfigObject(new ConfigObject(id,host,port,type));
		}
	}

	/**
	 * Adds a configuration object to the list
	 * 
	 * @param obj Configuration object to add
	 */
	public synchronized void addConfigObject(ConfigObject obj) {
		put(obj.getId(), obj);
	}

	/**
	 * Retrieves a configuation object from the component list
	 * 
	 * @param id Id to use to retrieve the configuration object
	 * @return ConfigObject object with the corresponding id
	 */
	public synchronized ConfigObject getConfigObject(String id) {
		return (ConfigObject)get(id);
	}

	/**
	 * Returns an enumeration of the configuration objects
	 *
	 * @return Enumeration of the configuration objects
	 */
	public synchronized Enumeration<ConfigObject> getEnumeration() {
		return elements();
	}

	/**
	 * Returns a list of all the configuration objects with
	 * the given type
	 *
	 * @param type Type of configuration object to match
	 * @return ConfigObjects object containing objects with the given type
	 */
	public ConfigObjects getObjectsType(String type) {
		ConfigObjects objects = new ConfigObjects();
		for (Enumeration<ConfigObject> e = elements(); e.hasMoreElements();) {
			ConfigObject co = (ConfigObject) e.nextElement();
			if (type.equals(co.getType())) {
				objects.addConfigObject(co);
			}
		}
		return objects;
	}
}
