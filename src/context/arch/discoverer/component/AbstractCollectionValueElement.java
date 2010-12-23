package context.arch.discoverer.component;

import context.arch.discoverer.query.comparison.AbstractComparison;
import context.arch.discoverer.query.comparison.ValueComparison;

public abstract class AbstractCollectionValueElement<T extends Comparable<T>> extends AbstractCollectionElement<T, T> {

	protected AbstractCollectionValueElement(String elementName, Class<T> c1) {
		super(elementName, c1, c1);
	}

	protected AbstractCollectionValueElement(String elementName, Class<T> c1, T value) {
		super(elementName, c1, c1, value);
	}
	

	@Override
	public AbstractComparison<T, T> getDefaultComparison() {
		return ValueComparison.instance(ValueComparison.Comparison.EQUAL, c1);
	}

}
