package context.arch.storage;

import java.util.Vector;

import context.arch.comm.DataObject;
import context.arch.comm.DataObjects;

/**
 * This class is a container for a group of related Condition objects.
 * Condition objects can be added and found in the container.
 * 
 * Changes made by Brian Y. Lim: made class public so that context.arch.server.SUser can see it
 * 
 * @author Anind K. Dey
 * deprecated? Superseded by Enactors framework, but actually used in the Retrieval subsystem
 */
public class Conditions extends Vector<Condition> {

	private static final long serialVersionUID = -2487117664786768644L;

	/**
	 * Tag for conditions
	 */
	public static final String CONDITIONS = "conditions";

	/**
	 * Empty constructor 
	 */
	public Conditions() {
		super();
	}

	/**
	 * Constructor that takes a DataObject as a parameter.  The DataObject
	 * must contain the <CONDITIONS> tag.  It stores the encoded data.
	 *
	 * @param data DataObject that contains condition info for retrieval
	 */
	public Conditions(DataObject data) {
		super();
		DataObject conds = data.getDataObject(CONDITIONS);
		DataObjects v = conds.getChildren();
		for (int i=0; i<v.size(); i++) {
			addElement(new Condition(v.elementAt(i)));
		}
	}

	/**
	 * Converts the Condition objects to a DataObject
	 *
	 * @return Conditions object converted to a <CONDITIONS> DataObject
	 */
	public DataObject toDataObject() {
		DataObjects conditions = new DataObjects();
		for (int i=0; i<size(); i++) {
			conditions.addElement(getConditionAt(i).toDataObject());
		}
		return new DataObject(CONDITIONS, conditions);
	}

	/**
	 * Adds the given Condition object to the container.
	 *
	 * @param cond Condition to add
	 */
	public void addCondition(Condition cond) {
		addElement(cond);
	}

	/**
	 * Adds the given attribute name, comparison and comparison value to the container.
	 *
	 * @param attribute Name of the attribute to add
	 * @param compare Comparison to use
	 * @param value Value for comparison
	 */
	public void addCondition(String attribute, int compare, Object value) {
		addElement(new Condition(attribute, compare, value));
	}

	/**
	 * Returns the Condition object at the given index
	 *
	 * @param index Index into the container
	 * @return Condition at the specified index
	 */
	public Condition getConditionAt(int index) {
		return (Condition)elementAt(index);
	}

	/**
	 * Determines whether the given Condition object is in the container
	 *
	 * @param cond Condition to check
	 * @return whether Condition is in the container
	 */
	public boolean hasCondition(Condition cond) {
		return contains(cond);
	}

	/**
	 * Determines whether the given attribute name and value are in the container,
	 * using the default datatype.
	 *
	 * @param attribute Name of the attribute to check
	 * @param compare Comparison to check
	 * @param value Comparison value of the attribute to check
	 * @return whether the given Condition name, comparison, and value are in the container
	 */
	public boolean hasCondition(String attribute, int compare, Object value) {
		return contains(new Condition(attribute, compare, value));
	}

	/**
	 * Returns the index at which the Condition object occurs
	 *
	 * @param att Condition to look for
	 * @return index of the specified Condition
	 */
	public int indexOfCondition(Condition cond) {
		return indexOf(cond);
	}

	/**
	 * Returns the index at which the given Condition attribute name, comparison 
	 * and value occurs.
	 *
	 * @param attribute Name of the attribute to look for
	 * @param compare Comparison to look for
	 * @param value Comparison value to look for
	 * @return index of the specified Condition
	 */
	public int indexOfCondition(String name, int compare, Object value) {
		return indexOf(new Condition(name,compare,value));
	}

	/**
	 * Returns the number of Condition objects in the container
	 *
	 * return the number of Condition objects in the container
	 */
	public int numConditions() {
		return size();
	}

	/** Return true if both Conditions objects are equal.
	 *
	 * @param conds
	 * @return boolean
	 */
	//  public boolean equals(Conditions conds){
	////    if (ORconditions == null) 
	//      return ((Vector) this).equals((Vector)conds);
	////    else
	////      return ((Vector) this).equals((Vector)conds) && ORconditions.equals(conds.ORconditions);
	//  }
	// don't need to override

	public void setORConditions(Conditions c) {
		ORconditions = c;
	}

	public Conditions getORConditions() {
		return ORconditions;
	}

	private Conditions ORconditions;
}
