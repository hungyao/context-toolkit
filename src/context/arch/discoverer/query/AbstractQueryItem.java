/*
 * AbstractQueryItem.java
 *
 * Created on July 5, 2001, 3:20 PM
 */

package context.arch.discoverer.query;

import java.util.Collection;

import context.arch.discoverer.ComponentDescription;
import context.arch.discoverer.component.dataModel.AbstractDataModel;
import context.arch.comm.DataObject;


/**
 * Design pattern : Composite
 * 
 * An AbstractQueryItem is the abstract element to create queries.
 * 
 * A query may contain QueryItem or AbstractBooleanQueryItem.
 * A QueryItem specifies the type of element searched, the value searched and
 * a type of comparison.
 * 
 * For example : 
 *    type of element = type
 *    value = widget
 *    comparison = equal
 * Another example :
 *    type of element = attribute
 *    value = username
 *    comparison = equal
 * 
 * A AbstractBooleanQueryItem is just a node that contains 1 or 2 other
 * AbstractQueryItem objects.
 *
 * A query is a tree. To process a query, the leaves of this tree returns an
 * array that contains for all existing components in the data model, if each
 * components suits the query or not (0 or 1).
 *
 * Example of query :
 * QueryItem q1 = new QueryItem(new IdElement("PersonNamePresence2_rigatoni_1655")); // by default Equal()
 * // IdElement implements IndexTableIF
 * QueryItem q2 = new QueryItem(new PortElement("1520"),new GreaterEqual());
 * ANDQueryItem and = new ANDQueryItem(q1, q2);
 * result = and.process(abstractDataModel); // abstractDataModel gets access to all IndexTableIF objects.
 *
 * There are 2 ways of processing a query :
 * - when the query is processed for all existing objects in the AbstractDataModel 
 * - when the query is processed only for one object (to handle the notification)
 *
 *
 *                      ------------------------------------------
 *                      |         AbstractQueryItem              |
 *                      ------------------------------------------
 *                      |    process(AbstractDataModel) : Object |
 *                      |    process(Object) : boolean           |
 *                      ------------------------------------------
 *                             /\                  /\
 *                              |                   |
 *    --------------------------------------        |
 *    |  QueryItem                         |        | 
 *    --------------------------------------        |
 *    |comparison : AbstractComparison     |        |
 *    |elToMatch : AbstractDescriptionElement|     -------------------------------
 *    --------------------------------------       |    BooleanQueryItem 		 |
 *    |process(AbstractDataModel) : Object |       -------------------------------
 *    |process(Object) : boolean           |       |son : AbstractQueryItem      |
 *    --------------------------------------       |brother : AbstractQueryItem  |
 *                                                 -------------------------------
 *                                                            /\
 *                                                             |
 *                  ___________________________________________|________________________________________
 *                  |                                          |                                         |
 * -------------------------------------- -------------------------------------- --------------------------------------
 * |          ANDQueryItem              | |        ORQueryItem                 | |           NOTQueryItem             |                     
 * -------------------------------------- -------------------------------------- --------------------------------------
 * |process(AbstractDataModel) : Object | |process(AbstractDataModel) : Object | |process(AbstractDataModel) : Object |
 * |process(Object) : boolean           | |process(Object) : boolean           | |process(Object) : boolean           |
 * -------------------------------------  -------------------------------------  --------------------------------------
 *
 * @author  Agathe
 * @see context.arch.discoverer.component.dataModel
 * 
 * Added that this class implements the Expression interface to plug into the intelligibility framework --Brian
 * @author Brian Y. Lim
 */
public abstract class AbstractQueryItem<C1,C2> {

	private static final long serialVersionUID = -3312073092388774318L;
	
	public static final String ABSTRACT_QUERY_ITEM = "abstractQueryItem";

	/**
	 * Finds ComponentDescriptions that match this query from the dataModel.
	 *
	 * @param dataModel
	 * @return Object
	 */
	public abstract Collection<ComponentDescription> search(AbstractDataModel dataModel);

	/**
	 * Returns true if a component fits this query, false if the query legally fails.
	 * Null if the query is invalid, e.g. the attribute value is null.
	 *
	 * @param component
	 * @return Boolean 
	 */
	public abstract Boolean match(ComponentDescription component);

	/**
	 * Returns a printable version of this object
	 *
	 * @return String
	 */
	public abstract String toString();

	/**
	 * Convert an DataObject into an AbstractQueryItem object.
	 * TODO: find out if this is getting called via reflections.
	 * 
	 * @param data
	 * @return AbstractQueryItem
	 */
	public static AbstractQueryItem<?,?> fromDataObject(DataObject data) {
		if (data == null) { return null; }
		//System.out.println("AbstractQueryItem fromDataObject name="+data.getName ());
		String name = data.getName();
		if (name.equals(RuleQueryItem.QUERY_ITEM)) {
			return RuleQueryItem.fromDataObject(data);
		}
		else if (name.equals(BooleanQueryItem.BOOLEAN_QUERY_ITEM)) {
			return BooleanQueryItem.fromDataObject(data);
		}
		else if (name.equals(ClassifierQueryItem.CLASSIFIER_QUERY_ITEM)) {
			return ClassifierQueryItem.fromDataObject(data);
		}
		else if (name.equals(HmmQueryItem.HMM_QUERY_ITEM)) {
			return HmmQueryItem.fromDataObject(data);
		}
		else {
			// TODO: may want to support extensibility for future AbstractQueryItems
			return null;
		}
	}

	/**
	 * Returns a DataObject version of this object
	 * 
	 * @return DataObject
	 */
	public abstract DataObject toDataObject();

}
