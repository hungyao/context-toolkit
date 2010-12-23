package context.arch.discoverer.component.dataModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import context.arch.discoverer.ComponentDescription;
import context.arch.discoverer.component.AbstractCollectionElement;
import context.arch.discoverer.component.AbstractElement;

/**
 * Overrides IndexTable to add multiple keys each corresponding to an element (i.e. multiple elements are extracted each time).
 * 
 * @author Brian Y. Lim
 *
 */
public abstract class IndicesTable<E> extends IndexTable<E> {

	private static final long serialVersionUID = -6749977918316326913L;

	@SuppressWarnings("unchecked")
	public IndicesTable(String indexName, AbstractCollectionElement<?,?> delegate) {
		super(indexName, (AbstractElement<E, ?, ?>) delegate);
	}

	/**
	 * Deprecated so that subclasses should not use it.
	 * @see #extractAttributes(ComponentDescription)
	 */
	@Override
	@Deprecated
	protected E extractKey(ComponentDescription component) {
		// extractKeys(ComponentDescription) method deprecated; use extractAttributes(ComponentDescription) instead.
		return null;
	}
	
	@SuppressWarnings("unchecked")
	protected Collection<E> extractKeys(ComponentDescription component) {
		return (Collection<E>) delegate.extractElement(component);
	}

	@Override
	public void addComponent(ComponentDescription component) {
		Collection<E> elementsAsKeys = extractKeys(component);
		
		for (E elementAsKey : elementsAsKeys) {
			List<ComponentDescription> components = super.get(elementAsKey);
			
			// create components entry if not done before
			if (components == null) {
				components = Collections.synchronizedList(new ArrayList<ComponentDescription>());
				put(elementAsKey, components);
			}

			// add index to the vector; i.e. one of the indices for this component
			components.add(component);
		}
	}
	
	@Override
	public void removeComponent(ComponentDescription component) {
		Collection<E> elementsAsKeys = extractKeys(component);
		
		for (E elementAsKey : elementsAsKeys) {
			List<ComponentDescription> components = super.get(elementAsKey);
			
			if (components != null) {
				components.remove(component);
				
				if (components.isEmpty()) {
					super.remove(elementAsKey);
				}
			}
		}
	}

}
