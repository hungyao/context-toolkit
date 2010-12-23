/*
 * IdElement.java
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
public class IdElement extends AbstractValueElement<String> {

	public IdElement () {
		super(ComponentDescription.ID_ELEMENT, 
				String.class);
	}

	public IdElement(String value) {
		this();
		setValue(value);
	}

	@Override
	public String extractElement(ComponentDescription component) {
		return component.id;
	}

}
