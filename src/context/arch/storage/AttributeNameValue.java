package context.arch.storage;

import context.arch.comm.DataObject;
import context.arch.comm.DataObjects;
import context.arch.discoverer.Discoverer;
import context.arch.discoverer.component.AbstractElement;

/**
 * This class is a container for an attribute name, value and type.
 * TODO: how to handle characteristics such as: possible nominal values, min/max range? 
 */
public class AttributeNameValue<T extends Comparable<? super T>> extends Attribute<T> {

	protected T value;

	/**
	 * Tag for an attribute name/value pair
	 */
	public static final String ATTRIBUTE_NAME_VALUE = "attributeNameValue";

	/** Tag for an attribute name */
	public static final String ATTRIBUTE_NAME = "attributeName";
	/** Tag for an attribute value */
	public static final String ATTRIBUTE_VALUE = "attributeValue";

	protected long lastModifiedTimestamp;
	/** ID of the component that last modified it */
	protected String lastModifiedById;
	protected String lastModifiedByHostName;
	protected int lastModifiedByPort;

	/** Tag for an attribute lastModifiedTimestamp */
	public static final String ATTRIBUTE_LAST_MODIFIED_TIMESTAMP = "attributeLastModifiedTimestamp";
	/** Tag for an attribute lastModifiedById, 
	 * which is the ID of the component that last modified it */
	public static final String ATTRIBUTE_LAST_MODIFIED_BY_ID = "attributeLastModifiedById";
	public static final String ATTRIBUTE_LAST_MODIFIED_BY_HOSTNAME = "attributeLastModifiedByHostName";
	public static final String ATTRIBUTE_LAST_MODIFIED_BY_PORT = "attributeLastModifiedByPort";

	/**
	 * Constructor that takes only a name
	 *
	 * @param name Name of attribute to store
	 */
	public AttributeNameValue(String name, Class<T> type) {
		super(name, type);
	}

	@SuppressWarnings("unchecked")
	public AttributeNameValue(String name, T value) {
		this(name, (Class<T>)value.getClass(), value);
	}

	/**
	 * Constructor that takes a name, value and type
	 *
	 * @param name Name of attribute to store
	 * @param value Value of attribute to store
	 * @param type Datatype of attribute to store
	 */
	public AttributeNameValue(String name, Class<T> type, T value) {
		super(name, type);
		this.value = value;
	}
	
	public AttributeNameValue(String name, Class<T> type, T value, Attributes subAttributes) {
		this(name, type, value);
		this.subAttributes = subAttributes;
	}
	
	/**
	 * Allows to dynamically (at runtime) instantiate a AttributeNameValue with the
	 * corresponding generic type.
	 * @param <T>
	 * @param name
	 * @param value
	 * @return
	 */
	public static <T extends Comparable<? super T>> AttributeNameValue<T> instance(String name, T value) {
		return new AttributeNameValue<T>(name, value);
	}

	public static <T extends Comparable<? super T>> AttributeNameValue<T> instance(String name, Class<T> type) {
		return new AttributeNameValue<T>(name, type);
	}

	/**
	 * Use this to create a AttributeNameValue where the type of value is determined dynamically at runtime.
	 * @param <T>
	 * @param name
	 * @param type
	 * @param value
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Comparable<? super T>> AttributeNameValue<T> instance(String name, Class<T> type, Comparable<? super T> value) {
		return new AttributeNameValue<T>(name, type, (T) value);
	}

	@SuppressWarnings("unchecked")
	public static <T extends Comparable<? super T>> AttributeNameValue<T> instance(String name, Class<T> type, Comparable<? super T> value, Attributes subAttrs) {
		return new AttributeNameValue<T>(name, type, (T) value, subAttrs);
	}

	/**
	 * Constructor that takes a DataObject as input.  The DataObject
	 * must have <ATTRIBUTE_NAME_VALUE> as its top-level tag
	 *
	 * @param attribute DataObject containing the attribute info
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Comparable<? super T>> AttributeNameValue<T> fromDataObject(DataObject data) {
		String name = data.getDataObject(ATTRIBUTE_NAME_VALUE).getValue();
		String tClassName = data.getDataObject(ATTRIBUTE_TYPE).getValue();
		try {
			return fromDataObject(data, name, (Class<T>) Class.forName(tClassName));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	protected static <T extends Comparable<? super T>> AttributeNameValue<T> fromDataObject(DataObject data, String name, Class<T> type) {
		try {
			Attributes subAttrs = Attributes.fromDataObject(data);
			
			// invoke .valueOf(String) method to parse value from string
			String strValue = data.getDataObject(ATTRIBUTE_VALUE).getValue();
			if (strValue == null || strValue.equals("null")) { // e.g. because value not set
				return null;
			}
			T value = valueOf(type, strValue);
			
//			return (AttributeNameValue<T>) AttributeNameValue.class
//															 .getConstructor(String.class, Class.class, Object.class, Attributes.class)
//											    			 .newInstance(name, type, value, subAttrs);
			return AttributeNameValue.instance(name, type, value, subAttrs);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Convenience method to use reflection to call the valueOf static method of the class of type
	 * to get a corresponding object representation from String strValue.
	 * @param type
	 * @param strValue
	 * @return Object but actually instantiated to its respective type
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Comparable<? super T>> T valueOf(Class<T> type, String strValue) {
		T value = null;
		if (type.equals(String.class)) { // special case that String does not have a valueOf(String) method
			value = (T) strValue;
		}
		else {
			try {
				value = (T) type.getMethod(Attribute.METHOD_VALUE_OF, String.class)
								.invoke(null, strValue);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return value;
	}
	
	public T valueOf(String strValue) {
		return valueOf(this.getType(), strValue);
	}

	/**
	 * Converts this object to a DataObject.
	 *
	 * @return AttributeNameValue object converted to an <ATTRIBUTE_NAME_VALUE> DataObject
	 */
	public DataObject toDataObject() {
		DataObject dobj = super.toDataObject();
		
		// change name value for data object
		dobj.setName(ATTRIBUTE_NAME_VALUE);
		
		/*
		 * Extend data object from Attribute
		 */
		DataObjects children = dobj.getChildren();
		
		children.add(new DataObject(ATTRIBUTE_VALUE, String.valueOf(getValue())));

		children.add(new DataObject(ATTRIBUTE_LAST_MODIFIED_TIMESTAMP, String.valueOf(lastModifiedTimestamp)));
		children.add(new DataObject(ATTRIBUTE_LAST_MODIFIED_BY_ID, lastModifiedById));
		children.add(new DataObject(ATTRIBUTE_LAST_MODIFIED_BY_HOSTNAME, lastModifiedByHostName));
		children.add(new DataObject(ATTRIBUTE_LAST_MODIFIED_BY_PORT, String.valueOf(lastModifiedByPort)));
		
		return dobj;
	}  

	/**
	 * Sets the value of an attribute
	 *
	 * @param value of the attribute to store
	 * @param true if value is the correct type of this Attribute; false otherwise and does not set if failed
	 */
	@SuppressWarnings("unchecked")
	public boolean setValue(Object value) {
		if (type.isAssignableFrom(value.getClass())) {
			this.value = (T) value;
			return true;
		}
		return false;
	}
	
	/**
	 * Copy value from source attribute if their types match.
	 * @param source
	 * @return true if the copy was successful.
	 */
	@SuppressWarnings("unchecked")
	public boolean copyValue(AttributeNameValue<?> source) {
		if (source.getType().equals(type)) {
			setValue((T)source.getValue());
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * Returns the value of the stored attribute
	 *
	 * @return value of the stored attribute
	 */
	public T getValue() {
		return value;
	}
	
	public long getLastModifiedTimestamp() {
		return lastModifiedTimestamp;
	}
	public String getLastModifiedById() {
		return lastModifiedById;
	}
	public String getLastModifiedByHostName() {
		return lastModifiedByHostName;
	}
	public int getLastModifiedByPort() {
		return lastModifiedByPort;
	}
	
	public void setLastModified(long timestamp, String byId, String byHostName, int byPort) {
		this.lastModifiedTimestamp = timestamp;
		this.lastModifiedById = byId;
		this.lastModifiedByHostName = byHostName;
		this.lastModifiedByPort = byPort;
	}

	/**
	 * A printable version of this class.
	 *
	 * @return String version of this class
	 */
	public String toString() {
//		return new String("[name="+getName()+",value="+getValue()+",type="+getType()+"]");
//		return "\n\t(" + getType() + ", by=" + getLastModifiedById() + ", at=" + getLastModifiedTimestamp() + ")\t" + getName() + " = " + getValue();
		return super.toString() +
			   ",value=" + getValue() +
			   ",by=" + getLastModifiedById() +
			   ",time=" + getLastModifiedTimestamp();
	}
	
	@Override
	public AttributeNameValue<T> clone() {
		return new AttributeNameValue<T>(name, type, value, subAttributes);
	}

	@Override
	public AttributeNameValue<T> cloneWithNewName(String name) {
		return new AttributeNameValue<T>(name, type, value, subAttributes);		
	}
	
	public AttributeNameValue<T> cloneWithNewValue(Object value) {
		AttributeNameValue<T> att = new AttributeNameValue<T>(name, type, null, subAttributes);
		att.setValue(value);
		return att;
	}

	/**
	 * Convert to value codex representation that is used by the Discoverer component model, for querying.
	 * Format: name+type+value, where '+' would be the URL encoded form of space ' '.
	 * @see #fromValueCodex(String)
	 * @see AbstractElement#fromDataObject(DataObject)
	 * @return
	 */
	@Override
	public String toValueCodex() {
		return super.toValueCodex() + Discoverer.FIELD_SEPARATOR + value;
	}
	
	/**
	 * Create a AttributeNameValue (shallow with no sub-attributes) from the value codex representation
	 * that is used by the Discoverer component model, for querying.
	 * Format: name+type+value, where '+' would be the URL encoded form of space ' '.
	 * @param valueCodex
	 * @return
	 * @see #toValueCodex()
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Comparable<? super T>> AttributeNameValue<T> fromValueCodex(String valueCodex) {
		String[] args = valueCodex.split("\\+"); // "name+type+value" -> {name, type, value}
		String name = args[0];
		String typeClassname = args[1];
		String strValue = args[2]; 
		
		try {
			Class<T> type = (Class<T>) Class.forName(typeClassname);
			T value = valueOf(type, strValue);
			return AttributeNameValue.instance(name, type, value);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Compares the values of this AttributeNameValue and other.
	 * @param other
	 * @return null if this and other are not compatible (i.e. different names or types); otherwise return value.compareTo(other.value)
	 */
	@SuppressWarnings("unchecked")
	public Integer compareToValue(AttributeNameValue<?> other) {
		// check if names and types are the same, otherwise, invalid
		if (!this.name.equals(other.name) ||
			!this.type.equals(other.type)
		) {
			return null;
		}
		
		T otherValue = (T) other.value;
		return this.value.compareTo(otherValue);
	}
	
}
