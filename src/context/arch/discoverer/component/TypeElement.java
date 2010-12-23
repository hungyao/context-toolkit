/*
 * TypeElement.java
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
public class TypeElement extends AbstractValueElement<String> {

	public TypeElement() {
		super(ComponentDescription.TYPE_ELEMENT,
				String.class);
	}

	public TypeElement(String value){
		this();
		setValue(value);
	}

	@Override
	public String extractElement(ComponentDescription component) {
		return component.type;
	}

}
