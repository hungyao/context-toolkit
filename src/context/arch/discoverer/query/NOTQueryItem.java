/*
 * NOTQueryItem.java
 *
 * Created on July 5, 2001, 4:22 PM
 */

package context.arch.discoverer.query;

import java.util.ArrayList;
import java.util.Collection;

import context.arch.discoverer.ComponentDescription;
import context.arch.discoverer.component.dataModel.AbstractDataModel;

/**
 *
 * @author Agathe
 * @author Brian Y. Lim
 */
public class NOTQueryItem extends BooleanQueryItem {

	private static final long serialVersionUID = -3548656350971935438L;
	
	public static final String NOT_QUERY_ITEM = "notQueryItem";
	
	protected AbstractQueryItem<?,?> child;

	/** Creates new ANDQueryItem */
	public NOTQueryItem(AbstractQueryItem<?,?> child) {
		super(NOT_QUERY_ITEM, child);
		this.child = child;
	}

	/**
	 * Perform a NOT
	 */
	@Override
	public Collection<ComponentDescription> search(AbstractDataModel dataModel) {
		return process(dataModel, child.search(dataModel));
	}

	/**
	 * Package protected and static to be reused by ElseQueryItem.
	 * @param dataModel
	 * @param children
	 * @return
	 */
	static Collection<ComponentDescription> process(AbstractDataModel dataModel, Collection<ComponentDescription> notComponents) {
		// pre-populate with all components
		Collection<ComponentDescription> components =  new ArrayList<ComponentDescription>(dataModel.getComponents());		
		// remove from components if in child notComponents
		components.removeAll(notComponents);
		return components;
	}

	@Override
	public Boolean match(ComponentDescription component){
		Boolean childResult = child.match(component);
		return childResult != null ? !childResult : null;
	}
	
	public AbstractQueryItem<?,?> getChild() {
		return child;
	}
}
