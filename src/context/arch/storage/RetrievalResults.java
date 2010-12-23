package context.arch.storage;

import context.arch.comm.DataObject;
import context.arch.comm.DataObjects;

import java.util.Vector;

/**
 * The RetrievalResults class is really a collection of individual AttributeNameValues objects. 
 * It is used for passing the results of a data retrieval back to a requesting component. Each 
 * AttributeNameValues object corresponds to a single record (with each AttributeNameValue object 
 * within it corresponding to a single attribute within the record), and the RetrievalResults 
 * corresponds to all the matching records. 
 * 
 * @author Anind K. Dey
 */
public class RetrievalResults extends Vector<Attributes> {

	private static final long serialVersionUID = 4692099167674137647L;

	/**
	 * Tag for retrieval results
	 */
	public static final String RETRIEVAL_RESULTS = "retrievalResults";

	/**
	 * Empty constructor
	 */
	public RetrievalResults() {
	}

	/**
	 * Constructor that takes a DataObject holding the callback info.
	 * The DataObject is expected to contain the <RETRIEVAL_RESULTS> tag.
	 *
	 * @param data DataObject containing the results of a retrieval
	 */
	public RetrievalResults(DataObject data) {
		DataObject retrieveData = data.getDataObject(RETRIEVAL_RESULTS);
		if (retrieveData == null) {
			return;
		}

		for (DataObject dObj : retrieveData.getChildren()) {
			Attributes retrieveAtts = Attributes.fromDataObject(dObj);
			if (retrieveAtts != null) {
				addAttributes(retrieveAtts);
			}
		}
	}

	/** 
	 * This method converts the RetrievalResults object to a DataObject
	 *
	 * @return RetrievalResults object converted to a <RETRIEVAL_RESULTS> DataObject
	 */
	public DataObject toDataObject() {
		DataObjects v = new DataObjects();
		for (int i=0; i<numAttributeNameValues(); i++) {
			v.addElement(getAttributesAt(i).toDataObject());
		}
		return new DataObject(RETRIEVAL_RESULTS, v);
	}

	/**
	 * This method adds an AttributeNameValues object to this
	 * container
	 *
	 * @param anvs AttributeNameValues object to be added
	 */
	public void addAttributes(Attributes anvs) {
		addElement(anvs);
	}

	/**
	 * This method retrieves the AttributeNameValues object at the
	 * given index.
	 *
	 * @param index at which to retrieve the AttributeNameValues object
	 * @return AttributeNameValues object at the given index
	 */
	public Attributes getAttributesAt(int index) {
		return (Attributes)elementAt(index);
	}

	/**
	 * This method returns the number of AttributeNameValues objects
	 * contained in this container.
	 *
	 * @return the number of AttributeNameValues objects in this container
	 */
	public int numAttributeNameValues() {
		return size();
	}

}
