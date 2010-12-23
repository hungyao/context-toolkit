/*
 * NonCstAttributeIndexTable.java
 *
 * Created on July 3, 2001, 11:17 AM
 */

package context.arch.discoverer.component.dataModel;

import context.arch.discoverer.ComponentDescription;
import context.arch.discoverer.component.NonConstantAttributeElement;
import context.arch.storage.Attribute;

/**
 *
 * @author Agathe
 * @author Brian Y. Lim
 */
public class NonCstAttributeIndexTable extends IndicesTable<Attribute<?>> {

	private static final long serialVersionUID = 6504174501407532701L;

	public NonCstAttributeIndexTable() {
		super(ComponentDescription.NON_CONST_ATT_ELEMENT, new NonConstantAttributeElement());
	}

}
