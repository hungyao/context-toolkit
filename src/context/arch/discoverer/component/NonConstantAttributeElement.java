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
 * For matching both name and value
 * @author Agathe
 * @author Brian Y. Lim
 */
public class NonConstantAttributeElement extends AttributeElement {
	
	public NonConstantAttributeElement() {
		super(ComponentDescription.NON_CONST_ATT_ELEMENT);
	}

	public NonConstantAttributeElement(AttributeNameValue<?> attribute) {
		super(ComponentDescription.NON_CONST_ATT_ELEMENT, 
				attribute);
	}

	@Override
	public Collection<AttributeNameValue<?>> extractElement(ComponentDescription component) {
		return component.getNonConstantAttributeNameValues();
	}

}
