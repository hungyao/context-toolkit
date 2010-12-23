/*
 * ConstantAttributeElement.java
 *
 * Created on July 6, 2001, 1:37 PM
 */

package context.arch.discoverer.component;

import java.util.Collection;

import context.arch.discoverer.ComponentDescription;
import context.arch.storage.AttributeNameValue;

/**
 *
 * @author  Agathe
 * @author Brian Y. Lim
 */
public class ConstantAttributeElement extends AttributeElement {

	public ConstantAttributeElement() {
		super(ComponentDescription.CONST_ATT_ELEMENT);
	}
	
	public ConstantAttributeElement(AttributeNameValue<?> attribute) {
		super(ComponentDescription.CONST_ATT_ELEMENT, 
				attribute);
	}

	@Override
	public Collection<AttributeNameValue<?>> extractElement(ComponentDescription component) {
		return component.getConstantAttributes();
	}

}
