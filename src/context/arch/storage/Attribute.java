package context.arch.storage;

import context.arch.comm.DataObject;
import context.arch.comm.DataObjects;
import context.arch.discoverer.Discoverer;
import context.arch.discoverer.component.AbstractElement;

/**
 * This class is a container for an attribute name, subAttributes (used for
 * structures - STRUCT) and type. Implements Comparable; for proper operation
 * Attributes must compare by name.
 * 
 * @param T represents the class type of the attribute. It may be a data primitive (actually, the autoboxed equivalent wrapper class) 
 * such as double, int, boolean, or Objects such as String. However, to support convertion to and from string formats so that it can
 * be transported through Sockets, T needs to properly implement the instance method <code>toString()</code>, and support static 
 * method <code>valueOf(String)</code>, which returns and instance of T.  
 */
public class Attribute<T extends Comparable<? super T>> {

	/** Needs to be a proper name with no white spaces or fancy characters like Java variable names. */
	protected String name;
	protected Class<T> type;
	
	/**
	 * An attribute may have nested sub-attributes to support hierarchical relations.
	 * E.g. 
	 * TODO May also be useful to represent supplementary information like min/max or possible nominal values.
	 */
	protected Attributes subAttributes;

	/** Tag for an attribute */
	public static final String ATTRIBUTE = "attribute";

	/** Tag for an attribute data type */
	public static final String ATTRIBUTE_TYPE = "attributeType";
	/** Tag for sub-attributes; actually, not normally used */
	public static final String SUB_ATTRIBUTE = Attributes.ATTRIBUTES;

	public static final String METHOD_VALUE_OF = "valueOf";
	public static final String METHOD_TO_STRING = "toString";
	
	/** Tag for default attribute type */
	public static final Class<String> DEFAULT_TYPE = String.class;

	/**
	 * Constructor that takes a name, value, and type
	 * 
	 * @param name of attribute to store
	 * @param subAttributes of this attribute
	 */
	public Attribute(String name, Class<T> type, Attributes subAttributes) {
		this.name = name;
		this.type = type;
		this.subAttributes = subAttributes;
	}

	public Attribute(String name, Class<T> type) {
		this(name, type, null);
	}
	
	/**
	 * Creates an instance of Attribute.
	 * This should be used in place of the constructor when wanting to dynamically instantiate an
	 * Attribute at runtime, with the type not known at design-time.
	 * @param <T>
	 * @param name
	 * @param type
	 * @return
	 */
	public static <T extends Comparable<? super T>> Attribute<T> instance(String name, Class<T> type) {
		return new Attribute<T>(name, type);
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends Comparable<? super T>> Attribute<T> fromDataObject(DataObject data) {
		String name = data.getDataObject(ATTRIBUTE).getValue();
		String typeClassName = data.getDataObject(ATTRIBUTE_TYPE).getValue();
		try {
			return fromDataObject(data, name, (Class<T>) Class.forName(typeClassName));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	protected static <T extends Comparable<? super T>> Attribute<T> fromDataObject(DataObject data, String name, Class<T> type) {
		try {
			Attributes subAttrs = Attributes.fromDataObject(data);
			return (Attribute<T>) Attribute.class
										   .getConstructor(String.class, Class.class, Attributes.class)
										   .newInstance(name, type, subAttrs);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Converts this object to a DataObject.
	 * 
	 * @return Attribute object converted to an <ATTRIBUTE> DataObject
	 */
	public DataObject toDataObject() {
		DataObjects children = new DataObjects();
		children.add(new DataObject(ATTRIBUTE_TYPE, type.getName()));
		
		if (subAttributes != null) { // has sub-attributes
			children.add(subAttributes.toDataObject());
		}
		
		DataObject dobj = new DataObject(ATTRIBUTE, name, children);
		return dobj;
	}

	// TODO: should we allow attributes to be non-primitive serializable
	// objects? This helps support multivariate or complex structures
	// or support that via AbstractDescriptionElements?
	// we may want to support enums too; basically an attribute should be able
	// to be any nominal concept

	/**
	 * Returns the name of the attribute
	 * 
	 * @return name of the stored attribute
	 */
	public String getName() {
		return name;
	}
	
	// don't allow name to be re-set as it can mess up other Attributes maps that point to the same object, but have the wrong name keys
//	/**
//	 * Sets the name of the attribute.
//	 * Use this sparingly because the name should strictly be an immutable property.
//	 * However, this is changed frequently by SequenceWidget.
//	 * @param name
//	 * @see SequenceWidget
//	 */
//	public void setName(String name) {
//		this.name = name;
//	}

	/**
	 * Returns the subAttributes of the stored attribute
	 * 
	 * @return subAttributes of the stored attribute
	 */
	public Attributes getSubAttributes() {
		return subAttributes;
	}
	
	public boolean hasSubAttributes() {
		return subAttributes != null;
	}
	
	public void setSubAttributes(Attributes atts) {
		this.subAttributes = atts;
	}

	/**
	 * Returns the datatype of the attribute
	 * 
	 * @return name of the attribute
	 */
	public Class<T> getType() {
		return type;
	}
	
	public boolean isType(Class<?> type) {
		return getType().equals(type);
	}
	
	/**
	 * Checks whether the type is numeric: int, float, double, short, long, etc.
	 * @return
	 */
	public boolean isNumeric() {
		return Number.class.isAssignableFrom(type);
	}

	/**
	 * A printable version of this class.
	 * 
	 * @return String version of this class
	 */
	public String toString() {
		return "name=" + getName() + 
		  	  ",type=" + getType() +
			  ",subAtts=" + getSubAttributes();
	}

	/**
	 * Attributes can be ordered by name
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Attribute<?> other) {
		return this.name.compareTo(other.name);
	}
	
	@Override
	public boolean equals(Object other) {
		if (other == null || !(other instanceof Attribute<?>)) { return false; }
		
		Attribute<?> otherAtt = (Attribute<?>) other;
		if (	!this.name.equals(otherAtt.name) &&
				!this.type.equals(otherAtt.type)) {
			return false;
		}
		
		/*
		 * subAttributes
		 */
		if (this.subAttributes == null) {
			if (otherAtt.subAttributes == null) { return true; }
			else { return false; }
		}
		else {
			return this.subAttributes.equals(otherAtt.subAttributes);
		}
	}
	
	@Override
	public Attribute<T> clone() {
		return new Attribute<T>(name, type, subAttributes);
	}
	
	/**
	 * Convenience method to clone this attribute, and change its name
	 * @param name
	 * @return
	 */
	public Attribute<T> cloneWithNewName(String name) {
		return new Attribute<T>(name, type, subAttributes);		
	}
	
	/**
	 * Convert to value codex representation that is used by the Discoverer component model, for querying.
	 * Format: name+type, where '+' would be the URL encoded form of space ' '.
	 * @see #fromValueCodex(String)
	 * @see AbstractElement#fromDataObject(DataObject)
	 * @return
	 */
	public String toValueCodex() {
		return name + Discoverer.FIELD_SEPARATOR + type.getName();
	}
	
	/**
	 * Create a Attribute (shallow with no sub-attributes) from the value codex representation
	 * that is used by the Discoverer component model, for querying.
	 * Format: name+value, where '+' would be the URL encoded form of space ' '.
	 * @param valueCodex
	 * @return
	 * @see #toValueCodex()
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Comparable<? super T>> Attribute<T> fromValueCodex(String valueCodex) {
		String[] args = valueCodex.split("\\+"); // "name+type" -> {name, type}
		
		try {
			Class<T> type = (Class<T>) Class.forName(args[1]);
			return Attribute.instance(args[0], type);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
	
}
