package context.arch.discoverer.component;

import java.util.ArrayList;
import java.util.Collection;

import context.arch.discoverer.ComponentDescription;
import context.arch.discoverer.query.comparison.AbstractComparison;

public abstract class AbstractCollectionElement<C1,C2> extends AbstractElement<Collection<C1>, C1, C2> {

	@SuppressWarnings("unchecked")
	protected AbstractCollectionElement(String elementName, 
			Class<C1> c1, Class<C2> c2) {
		super(elementName, 
				/*
				 * This quite a hack to be able to get a class
				 * representation of Collection<C1> to inform about the E generic parameter.
				 */
				(Class<Collection<C1>>) new ArrayList<C1>().getClass(), 
				c1, c2);
	}

	@SuppressWarnings("unchecked")
	protected AbstractCollectionElement(String elementName, 
			Class<C1> c1, Class<C2> c2,
			C2 value) {
		super(elementName, 
				(Class<Collection<C1>>) new ArrayList<C1>().getClass(), 
				c1, c2, value);
	}

	@Override
	public Boolean processQueryItem(ComponentDescription component, AbstractComparison<C1,C2> comparison) {
		Boolean result;
		for (C1 element : extractElement(component)) {
			result = comparison.compare(element, getValue());
			
			if (result == null) { return null; }
			else if (result) { return true; }
		}
		return false;
	}

}
