/*
 * BooleanQueryItem.java
 *
 * Created on July 5, 2001, 4:17 PM
 */

package context.arch.discoverer.query;

import context.arch.comm.DataObject;
import context.arch.comm.DataObjects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Richer version of AbstractBooleanQueryItem to support boolean operations (AND, OR, etc) on more than two child elements.
 * 
 * @author Agathe
 * @author Brian Y. Lim
 *
 */
public abstract class BooleanQueryItem extends AbstractQueryItem<Object,Object> {

	private static final long serialVersionUID = -5120820499394548730L;

	/** Tags for DataObject building */
	public static final String BOOLEAN_QUERY_ITEM = "abBoolListQI";

	public static final String NAME = "name";

	/** child element */
	protected List<AbstractQueryItem<?,?>> children = new ArrayList<AbstractQueryItem<?,?>>();

	/** the name of this boolean condition object */
	protected String booleanCondition;

	public BooleanQueryItem(String booleanCondition, AbstractQueryItem<?,?> ... childrenArray) {
		this(booleanCondition, Arrays.asList(childrenArray));
		
		// cannot just set Arrays.asList as children list 
		// since that would throw UnsupportedOperationException when add() is called
		// because it is immutable
	}
	
	public BooleanQueryItem (String booleanCondition, Collection<AbstractQueryItem<?,?>> children) {
		this.booleanCondition = booleanCondition;
		this.children.addAll(children);
	}
	
	/**
	 * Returns AbstractBooleanListQueryItem to allow chaining.
	 * @param child
	 * @return
	 */
	public BooleanQueryItem add(AbstractQueryItem<?,?> child) {
		children.add(child);
		return this;
	}
	// TODO: also have remove method?

	/** Returns the name of this element : the boolean condition name */
	public String getBooleanCondition() {
		return booleanCondition;
	}

	/**
	 * Returns a printable version of this object
	 */
	public String toString() {
		String ret = getBooleanCondition();
		for (int i = 0; i < children.size(); i++) {
			ret += "\n\tchild[" + i + "] = " + children.get(i);
		}
		return ret;
	}

	/**
	 * Return the DataObject version of this object
	 *
	 * @return DataObject
	 */
	public DataObject toDataObject(){
		DataObjects v = new DataObjects();
		v.add (new DataObject(NAME, getBooleanCondition()));
		
		for (int i = 0; i < children.size(); i++) {
			DataObjects childVector = new DataObjects();
			childVector.add(children.get(i).toDataObject());
			v.add(new DataObject("CHILD_" + i, childVector));
		}

		return new DataObject(BOOLEAN_QUERY_ITEM, v);
	}

	/**
	 * Takes a dataObject and returns an AbstracQueryItem object
	 * TODO: this static method never gets called, since it is never invoked from this class; how to get it accessible?
	 * 
	 * @param data
	 * @return
	 */
	public static AbstractQueryItem<?,?> fromDataObject(DataObject data) { 
		String name = data.getDataObject(NAME).getValue();

		List<AbstractQueryItem<?,?>> children = new ArrayList<AbstractQueryItem<?,?>>();
		for (DataObject el : data.getChildren()) { 
			if (el.getName().startsWith("CHILD_")) {
				AbstractQueryItem<?,?> child = AbstractQueryItem.fromDataObject(el.getChildren().firstElement());
				children.add(child);
			}
		}
		
		AbstractQueryItem<?,?> ret = BooleanQueryItem.factory(name, children);
//		System.out.println("AbstractBooleanListQueryItem.fromDataObject ret: " + ret);
		return ret;
	}

	/**
	 * Returns the right object corresponding to the specified name
	 *
	 * @param name The name of the object
	 * @param left
	 * @param right
	 * return AbstractBooleanQueryItem
	 */
	public static BooleanQueryItem factory(String name, List<AbstractQueryItem<?,?>> children) {
		if (name.equals(ANDQueryItem.AND_QUERY_ITEM)) {
			return new ANDQueryItem(children);
		}
		else if (name.equals(ORQueryItem.OR_QUERY_ITEM)) {
			return new ORQueryItem(children);
		}
		else if (name.equals(NOTQueryItem.NOT_QUERY_ITEM)) {
			return new NOTQueryItem(children.get(0)); // expect only one child for NOT
		}
		return null;
	}

	public List<AbstractQueryItem<?,?>> getChildren() {
		return Collections.unmodifiableList(children);
	}

}
