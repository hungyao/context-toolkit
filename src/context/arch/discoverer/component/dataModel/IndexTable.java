/*
 * AbstractIndexTable.java
 *
 * Created on July 2, 2001, 2:55 PM
 */

package context.arch.discoverer.component.dataModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import context.arch.discoverer.ComponentDescription;
import context.arch.discoverer.component.AbstractElement;

/**
 * 
 * The IndexTable is sort of a reverse lookup key-value table, where the key is the component, and value is the index.
 * So for each component, there can be multiple indices (dependent on the class type of V_INTERNAL).
 * 
 * IndexTable is refactored from IndexTableIF, from an interface to an abstract class.
 * It uses most of the code from AbstractIndexTableImpl and replaces it, since the latter was the only class implementing IndexTableIF.
 * 
 * Notice the insidious "switcheroo" where the key for IndexTable has a different class than the internal Hashtable key.
 * This is because we take in keys of type K, and convert it to String to store internally.
 * TODO: why do we need to store internally as a string???
 * 
 * @author Agathe
 * @author Brian Y. Lim
 */

public abstract class IndexTable<E> extends ConcurrentHashMap<E, List<ComponentDescription>> {
	
	private static final long serialVersionUID = 7407778683914827251L;
	
	protected String name;
	
	protected AbstractElement<E, ?,?> delegate;

	public IndexTable(String indexName, AbstractElement<E, ?,?> delegate) {
		super();
		this.name = indexName;
		this.delegate = delegate;
	}

	public void addComponent(ComponentDescription component) {
		E elementAsKey = extractKey(component);
		List<ComponentDescription> components = super.get(elementAsKey);
		
		// create components entry if not done before
		if (components == null) {
			components = Collections.synchronizedList(new ArrayList<ComponentDescription>());
			put(elementAsKey, components);
		}

		// add index to the vector; i.e. one of the indices for this component
		components.add(component);
	}
	
	/**
	 * Remove from the table the entry 
	 * @param component
	 */
	public void removeComponent(ComponentDescription component) {
		E elementAsKey = extractKey(component);
		List<ComponentDescription> components = super.get(elementAsKey);
		
		if (components != null) {
			components.remove(component);
			
			if (components.isEmpty()) {
				super.remove(elementAsKey);
			}
		}
	}

	public String getName() {
		return name;
	}

	public String toString() {
		return getName() + super.toString();
	}

	/**
	 * Returns the element of the object key that is used as the key of the 
	 * indexTable.
	 * That is the final class that chooses what element of the complex object
	 * is stored as a key. 
	 * This is unnaturally coupled with #AbstractDescriptionElement.extractElement
	 *
	 * @param object The object from which the class extracts what is relevant for it.
	 * @return index as class type E
	 * @see AbstractElement#extractElement(ComponentDescription)
	 */
	protected E extractKey(ComponentDescription component) {
		return delegate.extractElement(component);
	}

}
