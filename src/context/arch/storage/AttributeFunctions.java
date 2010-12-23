package context.arch.storage;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import context.arch.comm.DataObject;
import context.arch.comm.DataObjects;

/**
 * This class is a container for a group of related attributes and functions.
 * AttributeFunctions can be added, removed, and found in the container.
 * 
 */
public class AttributeFunctions extends ConcurrentHashMap<String, AttributeFunction<?>> {

	private static final long serialVersionUID = -5077908221541108267L;

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
	public static final String ATTRIBUTE_FUNCTIONS = "attributeFunctions";

	/**
	 * Tag to indicate all attributes are to be used
	 */
//	public static final String ALL = "allAttributes";

	/**
	 * Empty constructor 
	 */
	public AttributeFunctions() {
		super();
	}

	/**
	 * Constructor that takes a DataObject as a parameter.  The DataObject
	 * is expected to contain an <ATTRIBUTEFUNCTIONS> tag.
	 * The constructor stores the encoded data in an AttributeFunctions object.
	 *
	 * @param data DataObject that contains the attribute name (and possibly type and function) info
	 */
	public AttributeFunctions(DataObject data) {
		super();
		DataObject atts = data.getDataObject(ATTRIBUTE_FUNCTIONS);
		if (atts == null) { return; }
		
		for (DataObject d : atts.getChildren()) {
			AttributeFunction<?> af = AttributeFunction.fromDataObject(d);
			put(af.getName(), af);
		}
	}

	/**
	 * Converts to a DataObject.
	 *
	 * @return AttributeFunctions object converted to an <ATTRIBUTE_FUNCTIONS> DataObject
	 */
	public DataObject toDataObject() {
		DataObjects v = new DataObjects();
		for (AttributeFunction<?> af : this.values()) {
			v.add(af.toDataObject());
		}   
		return new DataObject(ATTRIBUTE_FUNCTIONS,v);
	}

//	/**
//	 * This method returns the AttributeFunction with the given name
//	 * from this list of AttributeFunctions.
//	 *
//	 * @param name of the AttributeFunction to return
//	 * @param prefix Structure name to use
//	 * @return AttributeFunction with the given name
//	 */
//	public AttributeFunction<?> getAttributeFunction(String name, String prefix) {
//		prefix = prefix.trim();
//		if ((prefix.length() != 0) && (!(prefix.endsWith(SEPARATOR_STRING)))) {
//			prefix = prefix +SEPARATOR_STRING;
//		}
//		for (int i=0; i<numAttributeFunctions(); i++) {
//			AttributeFunction att = getAttributeFunctionAt(i);
//			if ((prefix+att.getName()).equals(name)) {
//				AttributeFunction attribute = new AttributeFunction(name, att.getSubAttributeFunctions(), att.getType());
//				return attribute;
//			}
//			else if (att.getType().equals(Attribute.STRUCT)) {
//				AttributeFunctions atts = att.getSubAttributeFunctions();
//				att = atts.getAttributeFunction(name,prefix+att.getName());
//				if (att != null) {
//					return att;
//				}
//			}
//		}
//		return null;
//	}

	/**
	 * This method takes a DataObject containing the list of attributes
	 * (names) wanted and it filters all the rest out from this AttributeFunctions
	 * object.
	 *
	 * @param atts AttributeFunctions object containing the attributes to return
	 * @return filtered Attributes object
	 */
	public AttributeFunctions getSubset(Collection<String> names) {
		if (names.isEmpty()) { return this;	}

		AttributeFunctions subset = new AttributeFunctions();
		for (String name : names) {
			subset.put(name, get(name));
		}
		return subset;
	}

}
