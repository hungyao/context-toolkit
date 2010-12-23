/*
 * NonCstAttributeIndexTable.java
 *
 * Created on July 3, 2001, 11:17 AM
 */

package context.arch.discoverer.component.dataModel;

import context.arch.discoverer.ComponentDescription;
import context.arch.discoverer.component.NonConstantAttributeNameElement;

/**
 *
 * @author Agathe
 * @author Brian Y. Lim
 */
public class NonCstAttributeNameIndexTable extends IndicesTable<String> {
	
	private static final long serialVersionUID = 8282556921522441386L;

	public NonCstAttributeNameIndexTable() {
		super(ComponentDescription.NON_CONST_ATT_NAME_ELEMENT, new NonConstantAttributeNameElement());
	}

}
