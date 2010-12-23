/*
 * ConstantAttributeElement.java
 *
 * Created on July 6, 2001, 1:37 PM
 */

package context.arch.discoverer.component;

import java.util.Collection;

import context.arch.discoverer.ComponentDescription;
import context.arch.storage.Attribute;

/**
 * Represents element for matching only the name of a non-constant attribute names
 * @author Brian Y. Lim
 */
public class NonConstantAttributeNameElement extends AbstractCollectionValueElement<String> {
	
	public NonConstantAttributeNameElement() {
		super(ComponentDescription.NON_CONST_ATT_NAME_ELEMENT, 
				String.class);
	}

	public NonConstantAttributeNameElement(Attribute<?> attribute) {
		this(attribute.getName());
	}

	public NonConstantAttributeNameElement(String attributeName) {
		super(ComponentDescription.NON_CONST_ATT_NAME_ELEMENT, 
				String.class,
				attributeName);
	}

	@Override
	public Collection<String> extractElement(ComponentDescription component) {
		return component.getNonConstantAttributeNames();
	}

}
