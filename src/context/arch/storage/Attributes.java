package context.arch.storage;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import context.arch.comm.DataObject;
import context.arch.comm.DataObjects;

/**
 * This class is a container for a group of related attributes.
 * Attributes can be added, removed, and found in the container.
 * key = attribute name, value = attribute
 * 
 * TODO: make type safe by either supporting Attribute or AttributeNameValue, but not both in a single Vector?
 */
public class Attributes extends ConcurrentHashMap<String, Attribute<?>> {

	private static final long serialVersionUID = 5240091799907839312L;

	/**
	 * Connector for nested attributes
	 */
	public static final char SEPARATOR = '.';

	/**
	 * Connector for nested attributes - String
	 */
	public static final String SEPARATOR_STRING = new Character(SEPARATOR).toString();

	/**
	 * Tag for attributes
	 */
	public static final String ATTRIBUTES = "attributes";

	public Attributes() {
		super();
	}
	
	public Attributes(Attributes attributes) {
		super(attributes);
	}
	
	public Attributes(Collection<? extends Attribute<?>> attributes) {
		super();
		for (Attribute<?> att : attributes) {
			add(att);
		}
	}

	/**
	 * Constructor that takes a DataObject as a parameter.  The DataObject
	 * is expected to contain an <ATTRIBUTES> tag.
	 * The constructor stores the encoded data in an Attributes object.
	 *
	 * @param data DataObject that contains the attribute name (and possibly type) info
	 */
	public static Attributes fromDataObject(DataObject data) {
		data = data.getDataObject(ATTRIBUTES);
		if (data == null) { return null; }

		Attributes atts = new Attributes();

//		System.out.println("Attributes.fromDataObject atts : " + atts);
		
		for (DataObject dobj : data.getChildren()) {
			String dobjName = dobj.getName();
			
//			System.out.println("dobjName = " + dobjName + " : value = " + dobj.getValue());
				
			if (dobjName.equals(Attribute.ATTRIBUTE)) {
				Attribute<?> att = Attribute.fromDataObject(dobj);
//				System.out.println("Attributes.fromDataObject (Attribute)att : " + att);
				atts.add(att);
			} 
			else if (dobjName.equals(AttributeNameValue.ATTRIBUTE_NAME_VALUE)) {
				AttributeNameValue<?> att = AttributeNameValue.fromDataObject(dobj);
//				System.out.println("Attributes.fromDataObject (AttributeNameValue)att : " + att);
				atts.add(att);
			}
		}
		
		return atts;
	}

	/**
	 * Converts to a DataObject.
	 *
	 * @return Attributes object converted to an <ATTRIBUTES> DataObject
	 */
	public DataObject toDataObject() {
		DataObjects v = new DataObjects();
		for (Attribute<?> att : this.values()) {
//			System.out.println("Attributes.toDataObject att : " + att);
			v.add(att.toDataObject());
		}   
		return new DataObject(ATTRIBUTES, v);
	}
	
	/**
	 * Add an attribute, but may replace an old one if there existed one with the same name.
	 * @param the attribute to add
	 * @return the replaced attribute
	 */
	public Attribute<?> add(Attribute<?> att) {
		return super.put(att.getName(), att);
	}
	
	/**
	 * Convenience method to add an Attribute.
	 * @param <T>
	 * @param name
	 * @param type
	 * @return the replaced attribute if an old one existed
	 */
	public <T extends Comparable<? super T>> Attribute<?> addAttribute(String name, Class<T> type) {
		return add(new Attribute<T>(name, type));
	}
	
	/**
	 * Convenience method to add an AttributeNameValue; type is implicit in the class of T 
	 * @param <T> the type for value
	 * @param name
	 * @param value
	 * @return the replaced attribute if an old one existed
	 */
	public <T extends Comparable<? super T>> Attribute<?> addAttribute(String name, T value) {
		return add(new AttributeNameValue<T>(name, value));
	}
	
	/**
	 * Checks whether this contains an Attribute with a specific name.
	 * @param attributeName the name to find an Attribute with
	 * @return true if there exists an Attribute with name.equals(attributeName)
	 */
	public boolean containsName(String attributeName) {
		return this.containsKey(attributeName);
	}

	/**
	 * This method takes an Attributes containing the list of attributes
	 * (names) wanted and it filters all the rest out from this Attributes
	 * object.
	 *
	 * @param atts Attributes object containing the attributes to return
	 * @return filtered Attributes object
	 */
	public Attributes getSubset(Attributes filterAtts) {
		if (filterAtts.isEmpty()) {
			return this;
		}

		Attributes subset = new Attributes();
		for (Attribute<?> filterAtt : filterAtts.values()) {
			//System.out.println("filterAtt = " + filterAtt);
			Attribute<?> localAtt = get(filterAtt.getName());
			if (localAtt != null) {
				// if subAtt is an AttributeNameValue, filter with the value as well
				if (filterAtt instanceof AttributeNameValue<?>) {
					if (localAtt instanceof AttributeNameValue<?>) {
						Object filterValue = ((AttributeNameValue<?>)filterAtt).getValue();
						Object localValue = ((AttributeNameValue<?>)localAtt).getValue();

//						System.out.println("filterValue = " + filterValue + " | " + filterAtt);
//						System.out.println("localValue = " + localValue + " | " + localAtt);
						if (localValue != null && localValue.equals(filterValue)) {
							subset.add(localAtt);
						}
					}
				} else {
					subset.add(localAtt);
				}
			}
		}
//		System.out.println();
		return subset;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Comparable<? super T>> T getAttributeValue(String attName) {
		Attribute<?> att = super.get(attName);
		if (att instanceof AttributeNameValue<?>) {
			return ((AttributeNameValue<T>) att).getValue();
		}
		else {
			return null;
		}
	}
	
	/**
	 * Compare this Attributes with another.
	 * It only checks for each Attribute in this (i.e. via their name), and compares with the value in other.
	 * This is an asymmetric comparison; be careful the proper direction to use it.
	 * @param other
	 * @return Attributes containing any Attribute in other that is different in value in this; the value assigned is that belonging to the original collection in this.
	 */
	public <T extends Comparable<? super T>> Attributes compare(Attributes other) {
		Attributes diff = new Attributes();
		
		/*
		 * Iterate through this 
		 */
		for (Attribute<?> att : this.values()) {
			String name = att.getName();
			T val = this.getAttributeValue(name);
			
			// if different value, then add
			T otherVal = other.getAttributeValue(name);
			if (val != null && !val.equals(otherVal)) {
				diff.add(AttributeNameValue.instance(name, val));
			}
		}
		
		return diff;
	}

}
