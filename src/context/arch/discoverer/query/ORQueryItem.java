package context.arch.discoverer.query;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import context.arch.discoverer.ComponentDescription;
import context.arch.discoverer.component.dataModel.AbstractDataModel;

/**
 * 
 * @author Agathe
 * @author Brian Y. Lim
 *
 */
public class ORQueryItem extends BooleanQueryItem {

	private static final long serialVersionUID = 1094971636952363366L;
	
	public static final String OR_QUERY_ITEM = "orQueryItem";

	public ORQueryItem(AbstractQueryItem<?,?> ... childrenArray) {
		super(OR_QUERY_ITEM, childrenArray);
	}
	
	public ORQueryItem (List<AbstractQueryItem<?,?>> children) {
		super(OR_QUERY_ITEM, children);
	}

	/**
	 * Perform an OR
	 */
	@Override
	public Collection<ComponentDescription> search(AbstractDataModel dataModel) {
		return search(dataModel, children);
	}
	
	/**
	 * Package protected and static to be reused by ElseQueryItem.
	 * @param dataModel
	 * @param children
	 * @return
	 */
	static Collection<ComponentDescription> search(AbstractDataModel dataModel, List<AbstractQueryItem<?, ?>> children) {
		Set<ComponentDescription> components = new HashSet<ComponentDescription>(); // use Set to prevent duplicate entries, so that we can naively add
		if (children.isEmpty()) { return components; }
		
		// process first child for components
		components.addAll(children.get(0).search(dataModel));
		if (children.size() == 1) { return components; }

		// process existing components in relation to the remaining children
		for (int c = 1; c < children.size(); c++) {
			Collection<ComponentDescription> childComponents = children.get(c).search(dataModel);
			
			// add to original components if not already included
			components.addAll(childComponents);
		}

		return components;
	}

	@Override
	public Boolean match(ComponentDescription component){
		Boolean result = false;
		for (AbstractQueryItem<?,?> child : children) {
			Boolean childResult = child.match(component);
			if (childResult == null) { return null; }
			else { result |= childResult; }
		}
		return result;
	}

}
