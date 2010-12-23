package context.arch.service.helper;

import context.arch.comm.DataObject;
import context.arch.comm.DataObjects;
import context.arch.storage.Attributes;

import java.util.Vector;

/**
 * This class maintains a list of function descriptions.
 * Essentially this is a collection of multiple functions.
 *
 * @see context.arch.service.helper.FunctionDescription
 */
public class FunctionDescriptions extends Vector<FunctionDescription> {

	private static final long serialVersionUID = 2539446224680881013L;
	
	/**
	 * Tag for a widget's service functions
	 */
	public static final String FUNCTIONS = "functions";

	/**
	 * Basic empty constructor
	 */
	public FunctionDescriptions() {
		super();
	}

	/**
	 * Constructor that takes a DataObject as a parameter.  The DataObject
	 * must contain the tag <FUNCTIONS>.  It stores the encoded data.
	 *
	 * @param data DataObject that contains the function descriptions info
	 */
	public FunctionDescriptions(DataObject data) {
		super();
		DataObject functions = data.getDataObject(FUNCTIONS);
		for (DataObject child : functions.getChildren()) {
			addFunctionDescription(new FunctionDescription(child));
		}
	}

	/**
	 * Converts to a DataObject.
	 *
	 * @return Services object converted to an <FUNCTIONS> DataObject
	 */
	public DataObject toDataObject() {
		DataObjects v = new DataObjects();
		for (int i=0; i<numFunctionDescriptions(); i++) {
			v.addElement(getFunctionDescriptionAt(i).toDataObject());
		}   
		return new DataObject(FUNCTIONS,v);
	}

	/**
	 * Adds the given FunctionDescription object to the container.
	 *
	 * @param function FunctionDescription to add
	 */
	public void addFunctionDescription(FunctionDescription function) {
		add(function);
	}

	/**
	 * Adds the given function name, description and timing to the container.
	 *
	 * @param name Name of the function to add
	 * @param description Descripion of the function being added
	 * @param timing Timing of the function being added
	 */
	public void addFunctionDescription(String name, String description, String timing) {
		addFunctionDescription(name,description,new Attributes(),timing);
	}

	/**
	 * Adds the given function name, description and timing to the container.
	 *
	 * @param name Name of the function to add
	 * @param description Descripion of the function being added
	 * @param atts Attributes of the function being added
	 * @param timing Timing of the function being added
	 */
	public void addFunctionDescription(String name, String description, Attributes atts, String timing) {
		addElement(new FunctionDescription(name,description,atts,timing));
	}

	/**
	 * Adds the given FunctionDescriptions object to the container.
	 *
	 * @param functions FunctionDescriptions to add
	 */
	public void addFunctionDescriptions(FunctionDescriptions functions) {
		for (int i=0; i<functions.numFunctionDescriptions(); i++) {
			addFunctionDescription(functions.getFunctionDescriptionAt(i));
		}
	}

	/**
	 * Returns the FunctionDescription object at the given index
	 *
	 * @param index Index into the container
	 * @return FunctionDescription at the specified index
	 */
	public FunctionDescription getFunctionDescriptionAt(int index) {
		return (FunctionDescription)elementAt(index);
	}

	/**
	 * Determines whether the given FunctionDescription object is in the container
	 *
	 * @param function FunctionDescription to check
	 * @return whether function is in the container
	 */
	public boolean hasFunctionDescription(FunctionDescription function) {
		return contains(function);
	}

	/**
	 * Determines whether the given function name and description are in the container.
	 *
	 * @param name Name of the function to check
	 * @param description Description of the function to check
	 * @param timing Description of the function timing
	 * @return whether the given function name and description are in the container
	 */
	public boolean hasFunctionDescription(String name, String description, String timing) {
		return contains(new FunctionDescription(name,description,timing));
	}

	/**
	 * Determines whether a function with the given name is in the container
	 *
	 * @param name Name of the function to look for
	 * @return whether a function with the given name is in the container
	 */
	public boolean hasFunctionDescription(String name) {
		for (int i=0; i<numFunctionDescriptions(); i++) {
			if (getFunctionDescriptionAt(i).getName().equals(name)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the index at which the FunctionDescription object occurs
	 *
	 * @param function FunctionDescription to look for
	 * @return index of the specified FunctionDescription
	 */
	public int indexOfFunctionDescription(FunctionDescription function) {
		return indexOf(function);
	}

	/**
	 * Returns the index at which the given function name and description occurs
	 *
	 * @param name Name of the function to look for
	 * @param description Description of the function to look for
	 * @param timing Function timing to look for
	 */
	public int indexOfFunctionDescription(String name, String description, String timing) {
		return indexOfFunctionDescription(name,description,new Attributes(),timing);
	}

	/**
	 * Returns the index at which the given function name and description occurs
	 *
	 * @param name Name of the function to look for
	 * @param description Description of the function to look for
	 * @param timing Function timing to look for
	 */
	public int indexOfFunctionDescription(String name, String description, Attributes atts, String timing) {
		return indexOf(new FunctionDescription(name,description,atts,timing));
	}

	/**
	 * Returns the number of FunctionDescriptions in the container
	 *
	 * return the number of FunctionDescriptions in the container
	 */
	public int numFunctionDescriptions() {
		return size();
	}

	/**
	 * This method returns the FunctionDescription with the given name
	 * from this list of FunctionDescriptions.
	 *
	 * @param name of the FunctionDescription to return
	 * @return FunctionDescription with the given name
	 */
	public FunctionDescription getFunctionDescription(String name) {
		for (int i=0; i<numFunctionDescriptions(); i++) {
			FunctionDescription function = getFunctionDescriptionAt(i);
			if (function.getName().equals(name)) {
				return function;
			}
		}
		return null;
	}

}

