/*
 * DiscovererDataModel.java
 *
 * Created on July 2, 2001, 3:27 PM
 */

package context.arch.discoverer.component.dataModel;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import context.arch.discoverer.ComponentDescription;


/**
 * This class stores the ComponentDescriptions of the CTK object
 * that register to the discoverer.
 * It stores the complete ComponentDescription in a Hashtable and 
 * maintain a set of IndexTableIF that allows to search more quickly for
 * particular CTK objects. They are index on each of the description element.
 * 
 * TODO: this seems to maintain redundant info with the widget Storage mechanism! So there is an unnaturally tight coupling --Brian
 *
 * @author Agathe
 * @author Brian Y. Lim 
 */
public class DiscovererDataModel extends AbstractDataModel {

	/** 
	 * Stores the whole ComponentDescription objects 
	 * key = String(component.id) => value=ComponentDescription 
	 */
	private Map<String, ComponentDescription> components;

	/** 
	 * Creates new DiscovererDataModel 
	 */
	public DiscovererDataModel () {
		super();
		components = new ConcurrentHashMap<String, ComponentDescription>();

		// nameToIndexTableIF is inherited from the AbstractDataModel class
		// Here a put the index tables : the key is the name of the table
		addIndexTable(new IdIndexTable());
		addIndexTable(new ClassnameIndexTable());
		addIndexTable(new HostnameIndexTable()); 
		addIndexTable(new TypeIndexTable());
		addIndexTable(new PortIndexTable());
		addIndexTable(new CstAttributeIndexTable());
		addIndexTable(new NonCstAttributeIndexTable());
		addIndexTable(new NonCstAttributeNameIndexTable());
		addIndexTable(new CallbackIndexTable());
		addIndexTable(new ServiceIndexTable());
		addIndexTable(new SubscriberIndexTable());
		//nameToIndexTableIF.put(LOCATION_INDEX, new LocationIndexTable(LOCATION_INDEX));
	}

	/** 
	 * Convenience method to put IndexTable associated with its name. 
	 */
	public void addIndexTable(IndexTable<?> table) {
		indexTables.put(table.getName(), table);
	}

	@Override
	public String add(ComponentDescription component) {
		components.put(component.id, component);
		addToIndexTables(component);
		return component.id;
	}
	
	@Override
	public int size() {
		return components.size();
	}

	/**
	 * Update all hashtables (general and index tables) to add a new component
	 *
	 * @param component The new component to add
	 * @return the Integer corresponding to the index of the component
	 */
	@Override
	public String update(ComponentDescription component) {
		/*
		 * Essential to synchronize to this lookup table
		 * Since we are adding and removing it; many other threads may call this method simultaneously,
		 * and we don't want to encounter a state mid-update when the index was just removed
		 */
		synchronized (components) {			
			remove(component.id);  // delete old index to this component
			add(component); // replace with new index
			return component.id;
		}
	}

	@Override
	public ComponentDescription getComponent(String componentId) {
		return components.get(componentId);
	}

	@Override
	public Collection<ComponentDescription> getComponents() {
		return components.values();
	}


	/**
	 * 
	 * @param integer Integer
	 * @return Object The removed object (ComponentDescription)
	 */
	@Override
	public ComponentDescription remove(String componentId) {
		if (!components.containsKey(componentId)) { return null; }
		
		// remove from the all component descriptions
		ComponentDescription removed = components.remove(componentId);
		
		// remove from the index tables
		removeFromIndexTables(removed);
		return removed;
	}

	/**
	 * Add the reference to a component description to all IndexTable tables.
	 */
	private void addToIndexTables(ComponentDescription component) {
		for (IndexTable<?> table : indexTables.values()) {
			table.addComponent(component);
		}
	}

	/**
	 * Remove the reference to a component description from all IndexTable tables.
	 */
	private void removeFromIndexTables(ComponentDescription component) {
		for (IndexTable<?> table : indexTables.values()) {
			table.removeComponent(component);
		}
	}

	/**
	 *
	 */
	public String toString() {
		return "DiscovererDataModel - " +
		"Nb Elements=" + size() + "\n\n" +
		indexTables;
	}
	
}

