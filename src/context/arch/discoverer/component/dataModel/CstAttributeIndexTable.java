/*
 * CstAttributeIndexTable.java
 *
 * Created on July 3, 2001, 11:17 AM
 */

package context.arch.discoverer.component.dataModel;

import context.arch.discoverer.ComponentDescription;
import context.arch.discoverer.component.ConstantAttributeElement;
import context.arch.storage.AttributeNameValue;

import java.util.Collection;

/**
 * Used for indexing ComponentDescription by AttributeNameValue's name and value, in a name+value format.
 * 
 * @author Agathe
 * @author Brian Y. Lim
 */
public class CstAttributeIndexTable extends IndicesTable<AttributeNameValue<?>> {

	private static final long serialVersionUID = -1448637867124882647L;

	public CstAttributeIndexTable() {
		super(ComponentDescription.CONST_ATT_ELEMENT, new ConstantAttributeElement());
	}

	/**
	 * Returns the relevant key that is stored : the constant attributes
	 *
	 * @param key ComponentDescription
	 * @return collection of the constant attributes
	 */
	@Override
	public Collection<AttributeNameValue<?>> extractKeys(ComponentDescription component) {
//		return component.getConstantAttributeNameValues();
		return component.getConstantAttributes();
	}

}
