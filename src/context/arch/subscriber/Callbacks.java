package context.arch.subscriber;

import java.util.concurrent.ConcurrentHashMap;

import context.arch.comm.DataObject;
import context.arch.comm.DataObjects;
import context.arch.storage.Attributes;

/**
 * This class is a container for a group of callbacks.
 * Callbacks can be added, removed, and found in the container.
 */
public class Callbacks extends ConcurrentHashMap<String, Callback> {

	private static final long serialVersionUID = -8878636503751586244L;
	
	/**
	 * Tag for a widget's callbacks
	 */
	public static final String CALLBACKS = "callbacks";

	/**
	 * Empty constructor 
	 */
	public Callbacks() {
		super();
	}

	/**
	 * Constructor that takes a DataObject as a parameter.  The DataObject
	 * must contain the tag <CALLBACKS>.  It stores the encoded data.
	 *
	 * @param data DataObject that contains the callback info
	 */
	public Callbacks(DataObject data) {
		super();
		DataObject calls = data.getDataObject(CALLBACKS);
		DataObjects v = calls.getChildren();
		for (int i=0; i<v.size(); i++) {
			addCallback(new Callback((DataObject)v.elementAt(i)));
		}
	}

	/**
	 * Converts to a DataObject.
	 *
	 * @return Callbacks object converted to an <CALLBACKS> DataObject
	 */
	public DataObject toDataObject() {
		DataObjects v = new DataObjects();
		for (Callback c : values()) {
			v.add(c.toDataObject());
		}
		return new DataObject(CALLBACKS,v);
	}

	/**
	 * Adds the given Callback object to the container.
	 *
	 * @param callback Callback to add
	 */
	public void addCallback(Callback callback) {
		put(callback.getName(), callback);
	}

	/**
	 * Adds the given callback name and attributes to the container.  
	 *
	 * @param name Name of the callback to add
	 * @param attributes Attributes of the callback being added
	 */
	public void addCallback(String name, Attributes attributes) {
		put(name, new Callback(name, attributes));
	}

	/**
	 * Adds the given Callbacks object to the container.
	 *
	 * @param callbacks Callbacks to add
	 */
	public void addCallbacks(Callbacks callbacks) {
		putAll(callbacks);
	}

}
