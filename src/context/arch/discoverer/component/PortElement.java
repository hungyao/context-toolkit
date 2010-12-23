/*
 * PortElement.java
 *
 * Created on July 6, 2001, 8:39 AM
 */

package context.arch.discoverer.component;

import context.arch.discoverer.ComponentDescription;

/**
 *
 * @author Agathe
 * @author Brian Y. Lim
 */
public class PortElement extends AbstractValueElement<Integer> {

	public PortElement () {
		super(ComponentDescription.PORT_ELEMENT,
				Integer.class);
	}

	public PortElement(int value) {
		this();
		setValue(value);
	}

	@Override
	public Integer extractElement(ComponentDescription component) {
		return component.port;
	}

}
