/*
 * AbstractDataModel.java
 *
 * Created on July 2, 2001, 3:21 PM
 */

package context.arch.discoverer.component.dataModel;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import context.arch.discoverer.ComponentDescription;

/**
 * Abstract class for the data model. Concrete subclasses may implement various search mechanisms to match queries to the appropriate ComponentDescriptions
 * 
 * @see DiscovererDataModel
 * @author  Agathe
 */
public abstract class AbstractDataModel {

	/**
	 * This hashtable contains the IndexTableIF object that are the index tables 
	 * used in this data model.
	 * key=string=name of the indexTable => value=IndexTable
	 */
	protected Map<String, IndexTable<?>> indexTables;

	public AbstractDataModel() {
		indexTables = new ConcurrentHashMap<String, IndexTable<?>>();
	}

	/**
	 *
	 */
	public IndexTable<?> getIndexTable(String indexName) {
		return indexTables.get(indexName);
	}

	/**
	 * Adds the ComponentDescription object to the list of components, and 
	 * updates the index tables based on the component description.
	 *
	 * @param component ComponentDescription object
	 * @return the internal index of the ComponentDescription; corresponds to the component id
	 */
	public abstract String add(ComponentDescription component);

	/**
	 *
	 */
	public abstract String update(ComponentDescription component);

	/**
	 *
	 */
	public abstract ComponentDescription remove(String componentId);

	/**
	 *
	 */
	public abstract ComponentDescription getComponent(String componentId);

	public abstract Collection<ComponentDescription> getComponents();

	/**
	 * Number of components (added to) in the model.
	 */
	public abstract int size();
	
}
