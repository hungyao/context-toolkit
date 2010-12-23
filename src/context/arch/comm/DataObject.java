package context.arch.comm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class stores the data used for sending messages between components.
 * 
 * Dissociated children from value; they are now separate entities and concepts.
 * 
 * TODO: consider using XStream for data binding and serialization.
 * 
 * @author Anind K. Dey
 * @author Brian Y. Lim
 */
public class DataObject {

	private String name;
	private String value;
	private DataObjects children = new DataObjects();
	
	/* these two fields seem to be used for some internal chaining mechanism
	 * @see addElement, closeElement
	 * TODO: so where does this chaining matter? Maybe SAX_XMLDecoder
	 */
	private DataObject currentObject;
	private DataObject parent;

	/**
	 * Debug flag. Set to true to see debug messages.
	 */
	public static boolean DEBUG = false;

	/** 
	 * Basic constructor.  Sets up necessary internal variables.
	 */
	public DataObject() { 
		currentObject = this;
		currentObject.parent = null;
	}

	/**
	 * Constructor that sets the name of the DataObject element
	 *
	 * @param name Name of the DataObject element
	 */
	public DataObject(String name) {
		this();
		this.name = name;
	}

	/**
	 * Constructor that sets the name of the DataObject element and a single
	 * value for the element
	 *
	 * @param name Name of the DataObject element
	 * @param value Value of the DataObject element
	 */
	public DataObject(String name, String value) {
		this(name);
		if (name != null) {
			this.value = value;
		}
	}

	/**
	 * Constructor that sets the name of the DataObject element and a vector
	 * of values for the element
	 *
	 * @param name Name of the DataObject element
	 * @param children Vector of values for the DataObject element
	 */
	public DataObject(String name, DataObjects children) {
		this(name);
		this.value = null;
		this.children = children;	
		children.setParent(this);
	}
	
	public DataObject(String name, String value, DataObjects children) {
		this(name);
		this.value = value;
		this.children = children;	
		children.setParent(this);
	}

	/**
	 * Returns the name of the DataObject element
	 *
	 * @return Name of the DataObject element
	 */	
	public String getName() {
		return name;
	}

	/**
	 * Returns the values for the DataObject element
	 * @return value in string form; may need to be reconstituted with T.valueOf(String), where T is the type class
	 */	
	public String getValue() {
		return value;
	}

	/**
	 * Sets the name of the DataObject element
	 * @param name of the DataObject element
	 */	
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Counts the children of the current DataObject
	 *
	 * @return int the number of children
	 */	
	protected int countChildren() {
		return children.size();
	}

	/**
	 * Note similarity to getDataObject, but maybe this doesn't do a recursive search
	 * TODO: so why would this be called instead of getDataObject?? --Brian
	 */
	public DataObject getChild(String name) {
		for (DataObject child : children) {
			if (name.equals(child.getName())) {
				return child;
			}
		}
		return null;
	}

	/**
	 * Returns the children of the current DataObject
	 *
	 * @return Vector a vector containing the children
	 */	
	public DataObjects getChildren() {
		return children;
	}

	/**
	 * Returns the DataObject element/sub-element with the specified name.
	 * Could return itself or any recursively selected child.
	 * If multiple children have the same name, then only the first child is returned.
	 * @see getDataObjects to retrieve multiple children with name.
	 *
	 * @param name Name of the element to return
	 * @return DataObject with the specified name or null, if not found
	 * 
	 */ 
	public DataObject getDataObject(String name) {
		// object's name matches, so return it
		if (this.name.equals(name)) { return this; }
		
		// not found, so recursively search among children
		for (DataObject child : children) { // iterate through each child
			DataObject result = child.getDataObject(name);
			if (result != null) { return result; }
		}		
		
		// didn't find anything
		return null;
	}

	/**
	 * Returns all (possibly multiple) the DataObject elements/sub-elements with the specified name.
	 * Could include itself or any recursively selected child.
	 *
	 * @param name Name of the element to return
	 * @return DataObject with the specified name or null, if not found
	 * 
	 */ 
	public List<DataObject> getDataObjects(String name) {
		List<DataObject> result = new ArrayList<DataObject>();
		
		// object's name matches, so add it
		if (this.name.equals(name)) { result.add(this); }
		
		// so recursively search among children
		for (DataObject child : children) { // iterate through each child to add more
			// note that child may add an empty list
			result.addAll(child.getDataObjects(name));
		}		
		
		return result;
	}

	/**
	 * Returns the Nth DataObject element/sub-element with the specified name
	 * NB: we assume the current DataObject has 1 level of children. Thus,
	 * getNthDataObject (x, 1) is not equivalent to getDataObject (x)
	 * I'll fix this later. --DS
	 * 
	 * Returns the Nth DataObject element/sub-element with the specified name
	 * NB: Solved the problem with the previous method which considered only 1 level of children
	 * ~~Kanupriya
	 *
	 * @param name Name of the element to return
	 * @return DataObject with the specified name or null, if not found
	 */ 
	public DataObject getNthDataObject(String name, int n) {
		DataObject result = null;
		int count = 0;

		if (this.name.equals(name)) {
			result = this;
			count++;
			if (count == n) {
				return result;
			}
		}

		for (DataObject object : children) {
			result = object.getDataObject(name);
			if (result != null) {
				count++;
				if (count == n) {
					return result;
				}
			}
		}
		return null;
	}

	/**
	 * This method looks for an element in this DataObject.
	 * Why does this method exist? No external class uses this! --Brian
	 *
	 * @param name Name of an element
	 * @return boolean true if there is an element by that name, else false
	 */
	protected boolean elementExists(String name) {
//		int n = this.countChildren();
		boolean result = false;

		for (DataObject currentChild : this.getChildren()) {
			String currentName = currentChild.getName();

			if (currentName.equals(name)) {
				// sounds like we've found it
				result = true;
			}
			if (result == false) { // carry on searching the children
				if (currentChild.countChildren() > 0) {
					result = currentChild.elementExists(name);
				}
				if (result) { // did we find it?
					break;	// then stop here
				}
			}
		}
		return result;
	}

	/**
	 * This method adds an element and list of attributes to this DataObject.
	 * So far this was only used by SAX_XMLDecoder
	 *
	 * @param name Name of an element
	 * @param atts Map list of attributes; actually not being used anymore (deprecated)
	 */
	public void addElement(String name, Map<String, String> atts) {
		currentObject.name = name;
		
		DataObject newObject = new DataObject();
		newObject.parent = currentObject; // new object is a child of the current
		if (currentObject.parent != null) {
//			currentObject.parent.getValue().addElement(currentObject);
			currentObject.parent.children.add(currentObject);
		}
		currentObject = newObject;
	}

	/**
	 * This method closes the currently open/added element in order to do some 
	 * internal housecleaning.
	 *
	 * @param name Name of the element being closed 
	 */
	public void closeElement(String name) {
		DataObject newObject = new DataObject();
		if (currentObject.parent != null) {
			newObject.parent = currentObject.parent.parent;
		}
		else {
			newObject.parent = null;
		}
		currentObject = newObject;
	}

	/**
	 * Adds a value to the current element.
	 * Not really adding to this, per se, but setting the value of an internally shifted currentObject's parent.
	 * TODO: should really be refactored more --Brian
	 *
	 * @param value Value being added to the current element
	 */
	public void addValue(String value) {
//		currentObject.parent.getValue().addElement(value);
		
		currentObject.parent.value = value; // assume this only happens once for each currentObject
		
		/*
		 * I'm not sure if this gets called more than once per DataObject.
		 * It's only called by SAX_XMLDecoder.character.
		 * I'll assume it's called only once, since elsewhere in the toolkit, only one value is possible.
		 * Warn me and kill the process disgracefully if otherwise.
		 * WORKS well w/o this condition failing so far
		 * --Brian
		 */
//		currentObject.parent.numInvocations_addValue++;
//		if (currentObject.parent.numInvocations_addValue > 1) {
//			System.err.println("DataObject " + currentObject.parent + "(" + currentObject.parent.hashCode() + ").addValue(value) called more than once");
//			System.exit(-1); // quit
//		}
	} 
//	private int numInvocations_addValue = 0;

	/**
	 * Returns the first value of the DataObject element/sub-element with 
	 * the specified name, if it exists.  Returns null otherwise.
	 *
	 * @param string Name of the element to return
	 * @return 1st value of the DataObject with the specified name or null, if not found
	 */ 
	public Object getDataObjectFirstValue(String string) {
		DataObject dobj = getDataObject(string); // get the data object, then return its first element of its vector
		if (dobj != null) {
			return dobj.value;
		}
		return null;
	}

	/**
	 * This method creates a string version of the recursive DataObject
	 *
	 * @return String version (printable) version of the DataObject
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("\n[name="+getName());

		// print value
		sb.append(", value=" + value);
		
		// print children
		int i = 0;
		for (DataObject child : children) {
			sb.append(", " + name + "-child(" + i++ + ")=");
			sb.append(child.toString());

		}
		sb.append ("]");
		return sb.toString();
	}
	
	/**
	 * Convenience method to check whether an object is of a certain class type.
	 * Ordinarily, we could just use "o instanceof SomeClass",
	 * but since we are potentially operating in a distributed environment with multiple runtimes,
	 * each runtime would not recognize the same class loaded in a different runtime.
	 * So we test by class name instead. 
	 * 
	 * TODO: Not sure if this class is the best place to put this static method.
	 * 
	 * @param o
	 * @param someClass
	 * @return
	 */
	public static boolean instanceOf(Object o, Class<?> someClass) {
		// alternative: o instanceof someClass.class;
		return o.getClass().getName().equals(someClass.getName());
	}

}
