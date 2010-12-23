package context.arch.discoverer.query;

import java.util.List;

import context.arch.comm.DataObject;
import context.arch.comm.DataObjects;
import context.arch.discoverer.component.AbstractElement;
import context.arch.discoverer.query.comparison.AbstractComparison;

/**
 * QueryItem that can handle multiple dimensions (or attributes or element), rather than just one.
 * Useful for cases such as calculating distance based on latitude and longitude, 
 * with a third attribute for error margin.
 * 
 * TODO: rename to MultiQueryItem?
 * 
 * @author Brian Y. Lim
 *
 */
public abstract class RuleQueryItemND<C1,C2> extends AbstractQueryItem<C1,C2> {

	private static final long serialVersionUID = -3647103054483435901L;
	
	/**
	 * Stores internally as an array of abstract elements.
	 * Up to subclasses to make sense of it with specifics.
	 */
	protected AbstractElement<?,?,?>[] elementsToMatch;
	protected AbstractComparison<C1,C2> comparison;

	public RuleQueryItemND(AbstractComparison<C1,C2> comparison, AbstractElement<?,?,?> ... elements) {
		this.elementsToMatch = elements;
		this.comparison = comparison;
	}

	public AbstractElement<?,?,?>[] getElementsToMatch() {
		return elementsToMatch;
	}

	public AbstractComparison<C1,C2> getComparison() {
		return comparison;
	}
	
	public abstract String getName();

	@Override
	public DataObject toDataObject() {
		DataObjects v = new DataObjects();

		// add comparison first
		DataObject comp = comparison.toDataObject ();
		v.add (comp);
		
		// then add elements as remainder
		for (AbstractElement<?,?,?> elementToMatch : elementsToMatch) {
			DataObject elementToMatchDO = elementToMatch.toDataObject ();
			v.add (elementToMatchDO);
		}
				
		return new DataObject(getName(), v);
	}

	/**
	 * Maybe use this as a reference implementation for subclasses to override.
	 * @param data
	 * @return
	 */
	public static AbstractQueryItem<?,?> fromDataObject(DataObject data) {
		String name = data.getName();
		AbstractComparison<?,?> comp = getComparison(data);
		AbstractElement<?,?,?>[] elementsToMatch = getElementsToMatch(data);
		
		return RuleQueryItemND.factory(name, comp, elementsToMatch);
	}

	public static AbstractQueryItem<?,?> factory(String name, AbstractComparison<?,?> comparison, AbstractElement<?,?,?> ... elements) {
		assert false; // I don't expect this to be called if not used in subscription
		System.out.println("QueryItemND.factory name: " + name);
		
		return null; // TODO: do I need to implement this?
	}

	@Override
	public String toString(){
		String s = "QueryItemND : ";
		for (AbstractElement<?,?,?> elementToMatch : elementsToMatch) {
			s += elementToMatch.toString() + " ";
		}
		s += comparison.toString();
		return s;
	}
	
	/* -------------------------------------------------------------------------------------------
	 * Convenience methods
	 * These methods can be used by subclasses to retrieve the object from a DataObject.
	 * ------------------------------------------------------------------------------------------- */
	
	protected static AbstractComparison<?,?> getComparison(DataObject data) {
		DataObject compDO = data.getDataObject(AbstractComparison.ABSTRACT_COMPARISON);
		AbstractComparison<?,?> comp = AbstractComparison.fromDataObject(compDO);
		return comp;
	}
	
	protected static AbstractElement<?,?,?>[] getElementsToMatch(DataObject data) {
		List<DataObject> descDOs = data.getDataObjects(AbstractElement.ABSTRACT_DESCRIPTION_ELEMENT);
		AbstractElement<?,?,?>[] elementsToMatch = new AbstractElement[descDOs.size()];
		for (int i = 0; i < elementsToMatch.length; i++) {
			elementsToMatch[i] = AbstractElement.fromDataObject(descDOs.get(i));
		}
		return elementsToMatch;
	}

}
