package context.arch.discoverer.query;

import java.util.Collection;
import java.util.List;
import context.arch.discoverer.ComponentDescription;
import context.arch.discoverer.component.dataModel.AbstractDataModel;

/**
 * Takes its constituent queries and checks if any (OR) of them is true. If so, then this returns false.
 * Essentially, it is a NOT of OR (equivalently AND or NOTs).
 * Use this similarly to OR or AND query item by adding queries to it.
 * @author Brian Y. Lim
 *
 */
public class ElseQueryItem extends BooleanQueryItem {
	
	private static final long serialVersionUID = -5922149356714874831L;
	
	public static final String ELSE_QUERY_ITEM = "elseQueryItem";
	
	public ElseQueryItem(AbstractQueryItem<?,?> ... childrenArray) {
		super(ELSE_QUERY_ITEM, childrenArray);
	}
	
	public ElseQueryItem (List<AbstractQueryItem<?,?>> children) {
		super(ELSE_QUERY_ITEM, children);
	}

	/**
	 * Perform a NOT(OR(...)) which is the same as AND(NOT(...), NOT(...), ...)
	 */
	@Override
	public Collection<ComponentDescription> search(AbstractDataModel dataModel) {
		// OR processing
		Collection<ComponentDescription> notComponents = ORQueryItem.search(dataModel, children);

		// NOT processing
		Collection<ComponentDescription> components = NOTQueryItem.process(dataModel, notComponents);
		
		return components;
	}

	@Override
	public Boolean match(ComponentDescription component) {
		Boolean result = false;
//		if (component.id.contains("WAvailabilityFusion")) {
//			System.out.println();
//			System.out.println("ElseQueryItem.process component = " + component);
//		}
		for (AbstractQueryItem<?,?> child : children) {
			Boolean childResult = child.match(component);
//			if (component.id.contains("WAvailabilityFusion")) {
//				System.out.println("ElseQueryItem.process child = " + child);
//				System.out.println("ElseQueryItem.process childResult = " + childResult);
//			}
			
			if (childResult == null) { 
				return null; 
			}
			else { 
				result |= childResult;
			}
		}
//		if (component.id.contains("WAvailabilityFusion")) {
//			System.out.println("ElseQueryItem.process !result = " + (!result));
//		}
		return !result; // NOT of OR
	}

}
