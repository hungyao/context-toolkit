/*
 * QueryItem.java
 *
 * Created on July 5, 2001, 3:25 PM
 */

package context.arch.discoverer.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import context.arch.comm.DataObject;
import context.arch.comm.DataObjects;
import context.arch.discoverer.ComponentDescription;
import context.arch.discoverer.component.AbstractElement;
import context.arch.discoverer.component.dataModel.AbstractDataModel;
import context.arch.discoverer.component.dataModel.IndexTable;
import context.arch.discoverer.query.comparison.AbstractComparison;

/**
 *
 * @param <C1>
 * @param <C2>
 * @author Agathe
 * @author Brian Y. Lim
 */
public final class RuleQueryItem<C1,C2> extends AbstractQueryItem<C1,C2> {

	private static final long serialVersionUID = -6384542390323950065L;

	public static final String QUERY_ITEM = "queryItem";

	/** The type of comparison : equal, greater, lower, different... */
	private AbstractComparison<C1,C2> comparison;

	/** The object that specifies the type of description element and the value
	 * wanted
	 * i.e. : type = id
	 * value = PersonNamePresence
	 */
	private AbstractElement<?,C1,C2> elementToMatch;

	/** 
	 * Creates a RuleQueryItem 
	 */
	private RuleQueryItem(AbstractElement<?,C1,C2> element, AbstractComparison<C1,C2> comparison) {
		this.elementToMatch = element;
		this.comparison = comparison;
//		System.out.println("QueryItem this.hashCode(): " + this.hashCode());
//		System.out.println("QueryItem comparison: " + comparison);
//		System.out.println("QueryItem elementToMatch: " + elementToMatch);
	}

	/**
	 * Creates a RuleQueryItem with {@link Equal} as the comparator.
	 * @param element
	 */
	private RuleQueryItem(AbstractElement<?,C1,C2> element) {
		this(element, element.getDefaultComparison());
	}
	
	/**
	 * Convenience method to instantiate without knowing the generics parameters beforehand.
	 * @param <C1>
	 * @param <C2>
	 * @param element
	 * @return
	 */
	public static <C1,C2> RuleQueryItem<C1,C2> instance(AbstractElement<?,C1,C2> element, AbstractComparison<C1,C2> comparison) {
		return new RuleQueryItem<C1,C2>(element, comparison);
	}

	public static <C1,C2> RuleQueryItem<C1,C2> instance(AbstractElement<?,C1,C2> element) {
		return new RuleQueryItem<C1,C2>(element);
	}

	/**
	 * Searches for ComponentDescription in the dataModel that satisfies this RuleQueryItem.
	 * Seems to be mainly called by {@link DiscovererMediator#rawSearch(AbstractQueryItem<)}
	 * 
	 * TODO: should this method actually be in AbstractDataModel?
	 * @see DiscovererMediator
	 * @param dataModel AbstractDataModel passed from DiscovererMediator
	 * @return 
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Collection<ComponentDescription> search(AbstractDataModel dataModel) {
		Collection<ComponentDescription> selectedComponents = new ArrayList<ComponentDescription>();

		IndexTable<?> indexTable = dataModel.getIndexTable(elementToMatch.getElementName());
//		System.out.println("QueryItem.process dataModel: " + dataModel);
//		System.out.println("QueryItem.process elementToMatch: " + elementToMatch);
//		System.out.println("QueryItem.process elementToMatch.getElementName(): " + elementToMatch.getElementName());
//		System.out.println("QueryItem.process table: " + table);		

		if (indexTable == null) {
			System.err.println("indexTable == null; elementToMatch.getElementName() = " + elementToMatch.getElementName());
			return selectedComponents; // empty
		}

		C2 toMatch = elementToMatch.getValue();
		for (Object elementAsKey : indexTable.keySet()) {
//			System.out.print("elementAsKey("+elementAsKey.getClass().getSimpleName()+") = " + elementAsKey + ", toMatch("+toMatch.getClass().getSimpleName()+") = " + toMatch);
//			System.out.print(": " + comparison);
//			System.out.println(" = " + comparison.compare((C1)elementAsKey, toMatch));
			if (comparison.compare((C1)elementAsKey, toMatch)) {
				List<ComponentDescription> components = indexTable.get(elementAsKey);
				selectedComponents.addAll(components);
			}
		}
		
		return selectedComponents;
	}

	@Override
	public Boolean match(ComponentDescription component) {
		Boolean result = false;
//		if (comparison == null) {
//			System.out.println("QueryItem elementToMatch: " + elementToMatch);
//			System.out.println("QueryItem component: " + component);
//			System.out.println("QueryItem comparison: " + comparison);
//		}

		result = elementToMatch.processQueryItem(component, comparison);
		return result;
	}

	public String toString(){
		return "QueryItem: " + elementToMatch + " " + comparison;
	}
	
	public static AbstractQueryItem<?,?> fromDataObject(DataObject data) {
		String c1 = data.getDataObject(AbstractComparison.ABSTRACT_COMPARISON_C1).getValue();
		String c2 = data.getDataObject(AbstractComparison.ABSTRACT_COMPARISON_C2).getValue();
		try {
			return fromDataObject(data, Class.forName(c1), Class.forName(c2));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * TODO: is this only used for subscription, and not for polling enactor rules?
	 */
	@SuppressWarnings("unchecked")
	protected static <C1,C2> AbstractQueryItem<C1,C2> fromDataObject(DataObject data, Class<C1> c1, Class<C2> c2) { 
		// AbstractDescriptionElement
		DataObject doAbsDes = data.getDataObject(AbstractElement.ABSTRACT_DESCRIPTION_ELEMENT);		
		AbstractElement<?,C1,C2> abDes = (AbstractElement<?,C1,C2>) AbstractElement.fromDataObject(doAbsDes);
		
		// AbstractComparison
		AbstractComparison<C1,C2> comp = AbstractComparison.fromDataObject(data, c1,c2);

		return new RuleQueryItem<C1,C2>(abDes, comp);
	}

	/**
	 *
	 */
	@Override
	public DataObject toDataObject() {
		DataObject absEl = elementToMatch.toDataObject();
		DataObject comp = comparison.toDataObject();
		DataObjects v = new DataObjects();
		v.add (absEl);
		v.add (comp);		
		DataObject doj = new DataObject(QUERY_ITEM, v);
//		new RuntimeException("doj = " + doj).printStackTrace();
		return doj;
	}

	public AbstractElement<?,C1,C2> getElementToMatch() {
		return elementToMatch;
	}

	public AbstractComparison<C1,C2> getComparison() {
		return comparison;
	}

}//class end
