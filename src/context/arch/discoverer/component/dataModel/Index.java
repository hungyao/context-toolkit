package context.arch.discoverer.component.dataModel;

import java.util.Collection;

import context.arch.discoverer.ComponentDescription;

public interface Index<E>  {

	public void addComponent(ComponentDescription component);
	
	/**
	 * 
	 * @param component
	 * @return the key for this component in the Index
	 */
	public void removeComponent(ComponentDescription component);
	
	public String getName();

	public Collection<E> keySet();
	public ComponentDescription get(Object key);

}
