package context.arch.subscriber;

import context.arch.storage.Attributes;
import context.arch.comm.DataObject;
import context.arch.comm.DataObjects;

/**
 * This class is a container for a callback and associated attributes.
 */
public class Callback {

	/**
	 * Tag for the name of the callback
	 */
	public static final String CALLBACK_NAME = "callbackName";

	/**
	 * Tag for a callback
	 */
	public static final String CALLBACK = "callback";

	private String name;
	private Attributes attributes;

	/**
	 * Empty constructor
	 */
	public Callback() {
	}

	/**
	 * Constructor that takes a name and attributes object
	 *
	 * @param name Name of callback to store
	 * @param atts Attributes of the callback
	 */
	public Callback(String name, Attributes atts) {
		this.name = name;
		this.attributes = atts;
	}

	/**
	 * Constructor that takes a DataObject holding the callback info.
	 * The expected tag of the DataObject is <CALLBACK>
	 *
	 * @param data DataObject containing the callback info
	 */
	public Callback(DataObject data) {
		DataObject nameObj = data.getDataObject(CALLBACK_NAME);
		this.name = nameObj.getValue();
		this.attributes = Attributes.fromDataObject(data);
	}

	/** 
	 * This method converts the Callback object to a DataObject
	 *
	 * @return Callback object converted to a <CALLBACK> DataObject
	 */
	public DataObject toDataObject() {
		DataObjects v = new DataObjects();
		v.addElement(new DataObject(CALLBACK_NAME, name));
		v.addElement(attributes.toDataObject());
		return new DataObject(CALLBACK, v);
	}

	/**
	 * Sets the name of a callback
	 *
	 * @param name Name of the callback to store
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Sets the attributes for the callback
	 *
	 * @param atts Attributes of the callback
	 */
	public void setAttributes(Attributes atts) {
		this.attributes = atts;
	}

	/**
	 * Returns the name of the callback
	 *
	 * @return name of the callback
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the attributes for the callback
	 *
	 * @return attributes for the callback
	 */
	public Attributes getAttributes() {
		return attributes;
	}

	/**
	 * A printable version of this class.
	 * 
	 * @return String version of this class
	 */
	public String toString() {
		return new String("name=" + getName() + ",attributes=" + attributes);
	}
}
