package context.arch.storage;

import context.arch.comm.DataObject;
import context.arch.comm.DataObjects;

/**
 * The Retrieval class allows a component to specify what context it wants to retrieve from a widget. 
 * It contains an AttributeFunctions object and a Conditions  object. The AttributeFunctions object 
 * contains the set of attributes and corresponding functions to attach to each. The Conditions object 
 * contains the set of conditions the context should meet to be returned to the requesting component. 
 * 
 * @author Anind K. Dey
 */
public class Retrieval {

	private AttributeFunctions attributes;
	private Conditions conditions;

	/**
	 * Tag for retrieval
	 */
	public static final String RETRIEVAL_CONDITIONS = "retrievalConditions";

	/**
	 * Empty constructor 
	 */
	public Retrieval() {
	}

	/**
	 * Constructor that sets attribute list and conditions 
	 *
	 * @param attributes List of AttributeFunctions to retrieve
	 * @param conditions Conditions object with retrieval conditions
	 */
	public Retrieval(AttributeFunctions attributes, Conditions conditions) {
		this.attributes = attributes;
		this.conditions = conditions;
	}

	/**
	 * Constructor that takes a DataObject as a parameter.  The DataObject
	 * should contain the <RETRIEVALCONDITIONS> tag.  It stores the encoded data.
	 *
	 * @param retrieval DataObject that contains info for retrieval
	 */
	public Retrieval(DataObject retrieval) {
		DataObject ret = retrieval.getDataObject(RETRIEVAL_CONDITIONS);
		attributes = new AttributeFunctions(ret);
		conditions = new Conditions(ret);
	}

	/**
	 * Converts to a DataObject
	 *
	 * @return Retrieval object converted to a <RETRIEVAL_CONDITIONS> DataObject
	 */
	public DataObject toDataObject() {
		DataObjects retrieval = new DataObjects();
		retrieval.addElement(conditions.toDataObject());
		retrieval.addElement(attributes.toDataObject());
		return new DataObject(RETRIEVAL_CONDITIONS, retrieval);
	}

	/**
	 * Sets the Conditions object for retrieval.
	 *
	 * @param conditions Conditions object for retrieval
	 */
	public void setConditions(Conditions conditions) {
		this.conditions = conditions;
	}

	/**
	 * Sets the list of attributes to retrieve.
	 *
	 * @param attributes List of attributeFunctions to retrieve
	 */
	public void setAttributeFunctions(AttributeFunctions attributes) {
		this.attributes = attributes;
	}

	/**
	 * Returns the Conditions object used for retrieval.
	 *
	 * @return Conditions object used for retrieval
	 */
	public Conditions getConditions() {
		return conditions;
	}

	/**
	 * Returns the list of AttributeFunctions to retrieve.
	 *
	 * @return List of AttributeFunctions to retrieve
	 */
	public AttributeFunctions getAttributeFunctions() {
		return attributes;
	}

}
