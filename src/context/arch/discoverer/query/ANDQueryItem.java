/*
 * ANDQueryItem.java
 *
 * Created on July 5, 2001, 4:22 PM
 */

package context.arch.discoverer.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import context.arch.discoverer.ComponentDescription;
import context.arch.discoverer.component.dataModel.AbstractDataModel;

/**
 * 
 * @author Agathe
 * @author Brian Y. Lim
 *
 */
public class ANDQueryItem extends BooleanQueryItem {

	private static final long serialVersionUID = -5396890304095806576L;
	
	public static final String AND_QUERY_ITEM = "andQueryItem";

	public ANDQueryItem(AbstractQueryItem<?,?> ... childrenArray) {
		super(AND_QUERY_ITEM, childrenArray);
	}
	
	public ANDQueryItem(List<AbstractQueryItem<?,?>> children) {
		super(AND_QUERY_ITEM, children);
	}

	/**
	 * Perform a AND 
	 */
	@Override
	public Collection<ComponentDescription> search(AbstractDataModel dataModel) {
		Collection<ComponentDescription> components = new ArrayList<ComponentDescription>(); // use ArrayList for quick iteration
		if (children.isEmpty()) { return components; }
		
		// process first child for components
		components.addAll(children.get(0).search(dataModel));
		if (children.size() == 1) { return components; }

		// process existing components in relation to the remaining children
		for (int c = 1; c < children.size(); c++) { // start from second
			Collection<ComponentDescription> childComponents = children.get(c).search(dataModel);
			
			// remove from original components if not in childComponents
			Iterator<ComponentDescription> it = components.iterator();
			while (it.hasNext()) {
				if (!childComponents.contains(it.next())) {
					it.remove();
				}
			}
		}

		return components;
	}

	@Override
	public Boolean match(ComponentDescription component) {
		Boolean result = true;

		for (AbstractQueryItem<?,?> child : children) {
			Boolean childResult = child.match(component);

//			System.err.println("ANDListQueryItem.process childQuery = " + child);
//			System.err.println("ANDListQueryItem.process childResult = " + childResult);
//			System.err.println("ANDListQueryItem.process component = " + component);
				
			if (childResult == null) { return null; }
			else { result &= childResult; }
		}
		return result;
	}

}
