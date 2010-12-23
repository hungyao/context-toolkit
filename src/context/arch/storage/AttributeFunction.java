package context.arch.storage;

import context.arch.comm.DataObject;
import context.arch.comm.DataObjects;

//import context.arch.comm.DataObject;
//import context.arch.comm.DataObjects;
//
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;

/**
 * This class is a container for an attribute name, a function, subAttributes (used for
 * structures - STRUCT) and type.
 * 
 * An AttributeFunction object is basically the same as an Attribute object, except that it has a function parameter. 
 * The function parameter allows a component to be more specific about the context it wants to retrieve. 
 * Example functions are MAX, MIN, COUNT, AVERAGE, SUM, etc. When a component wants to know when the last time anyone 
 * entered a room after 5 pm, it sets up an AttributeFunction object as follows: name is TIMESTAMP, datatype is long 
 * (timestamps are longs), and function as MAX. It would also set a Condition object for TIMESTAMP > 5 pm. 
 * 
 * @author Anind K. Dey
 * deprecated? Superseded by Enactors framework; or still used by Retrieval system
 */
public class AttributeFunction<T extends Comparable<? super T>> extends Attribute<T> {

	protected AttributeFunctions afs;
	protected String function;

	/**
	 * Tag for an attribute function object
	 */
	public static final String ATTRIBUTE_FUNCTION = "attributeFunction";

	/**
	 * Tag for an attribute function
	 */
	public static final String FUNCTION = "function";

	/**
	 * Tag for an attribute
	 */
	public static final String ATTRIBUTE = "attribute";

	/**
	 * Tag for default function - none
	 */
	public static final String FUNCTION_NONE = "none";

	/**
	 * Tag for MAX function
	 */
	public static final String FUNCTION_MAX = "max";

	/**
	 * Tag for MIN function
	 */
	public static final String FUNCTION_MIN = "min";

	/**
	 * Tag for COUNT function
	 */
	public static final String FUNCTION_COUNT = "count";

	/**
	 * Tag for AVG function
	 */
	public static final String FUNCTION_AVG = "avg";

	/**
	 * Tag for SUM function
	 */
	public static final String FUNCTION_SUM = "sum";

	/**
	 * Constructor that takes only a name
	 *
	 * @param name Name of attribute to store
	 */
	public AttributeFunction(String name) {
		super(name, null);
		afs = null;
		function = FUNCTION_NONE;
	}

	/**
	 * Constructor that takes only a name and a function
	 *
	 * @param name Name of attribute to store
	 * @param function Function to execute on attribute
	 */
	public AttributeFunction(String name, String function) {
		this(name);
		afs = null;
		this.function = function;
	}

	/**
	 * Constructor that takes a name, value, and type
	 *
	 * @param name Name of attribute to store
	 * @param afs subAttributes of this attribute
	 */
	public AttributeFunction(String name, AttributeFunctions afs) {
		this(name);
		this.afs = afs;
		function = FUNCTION_NONE;
	}

	/**
	 * Constructor that takes a name, value, and type
	 *
	 * @param name Name of attribute to store
	 * @param afs subAttributes of this attribute
	 * @param function Function to execute on attribute
	 */
	public AttributeFunction(String name, AttributeFunctions afs, String function) {
		this(name, afs);
		this.function = function;
	}

	/**
	 * Constructor that takes a DataObject as input.  The DataObject
	 * must have <ATTRIBUTE_FUNCTION> as its top-level tag
	 *
	 * @param attribute DataObject containing the attribute info
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Comparable<? super T>> AttributeFunction<T> fromDataObject(DataObject data) {
		String name = data.getDataObject(ATTRIBUTE_FUNCTION).getValue();
		String typeClassName = data.getDataObject(ATTRIBUTE_TYPE).getValue();
		try {
			return fromDataObject(data, name, (Class<T>) Class.forName(typeClassName));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	protected static <T extends Comparable<? super T>> AttributeFunction<T> fromDataObject(DataObject data, String name, Class<T> type) {
		try {
			Attributes subAttrs = Attributes.fromDataObject(data);
			String function = data.getDataObject(FUNCTION).getValue();
			return (AttributeFunction<T>) AttributeFunction.class
										   .getConstructor(type)
										   .newInstance(name, subAttrs, function);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Converts this object to a DataObject.
	 * 
	 * @return Attribute object converted to an <ATTRIBUTE_FUNCTION> DataObject
	 */
	public DataObject toDataObject() {
		DataObjects children = new DataObjects();
		children.add(new DataObject(ATTRIBUTE_TYPE, type.getName()));

		children.add(new DataObject(FUNCTION, function));
		
		if (subAttributes != null) { // has sub-attributes
			children.add(subAttributes.toDataObject());
		}
		
		DataObject dobj = new DataObject(ATTRIBUTE_FUNCTION, name, children);
		return dobj;
	}

	/**
	 * Sets the subAttributes of this attribute 
	 *
	 * @param afs subAttributes of the attribute to store
	 */
	public void setSubAttributes(AttributeFunctions afs) {
		this.afs = afs;
	}

	/**
	 * Sets the function of an attribute 
	 *
	 * @param function Function to act on the attribute
	 */
	public void setFunction(String function) {
		this.function = function;
	}

	/**
	 * Returns the subAttributes of the stored attribute
	 *
	 * @return subAttributes of the stored attribute
	 */
	public AttributeFunctions getSubAttributeFunctions() {
		return afs;
	}

	/**
	 * Returns the function of an attribute 
	 *
	 * @return function to act on the attribute
	 */
	public String getFunction() {
		return function;
	}

	/**
	 * A printable version of this class.
	 * 
	 * @return String version of this class
	 */
	public String toString() {
		return new String("[name="+getName()+", atts="+getSubAttributes()+",type="+getType()+",function="+getFunction()+"]");
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof AttributeFunction<?>)) { return false; }
		
		AttributeFunction<?> other = (AttributeFunction<?>) o;
		if (	!this.afs.equals(other.afs) ||
				!this.function.equals(other.function)) {
			return false;
		}
		
		// remaining check the same as for Attribute superclass
		return super.equals(o);		
	}
	
}
