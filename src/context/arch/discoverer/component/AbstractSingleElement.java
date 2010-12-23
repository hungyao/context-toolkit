package context.arch.discoverer.component;

import context.arch.discoverer.ComponentDescription;
import context.arch.discoverer.query.comparison.AbstractComparison;

public abstract class AbstractSingleElement<C1,C2> extends AbstractElement<C1, C1,C2> {

	protected AbstractSingleElement(String elementName, 
			Class<C1> c1, Class<C2> c2) {
		super(elementName, c1, c1, c2);
	}

	protected AbstractSingleElement(String elementName, 
			Class<C1> c1, Class<C2> c2,
			C2 value) {
		super(elementName, c1, c1, c2, value);
	}

	@Override
	public Boolean processQueryItem(ComponentDescription component, AbstractComparison<C1,C2> comparison) {
		return comparison.compare(extractElement(component), this.getValue());
	}

}
