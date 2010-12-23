/*
 * ClassnameIndexTable.java
 *
 * Created on July 3, 2001, 11:17 AM
 */

package context.arch.discoverer.component.dataModel;

import context.arch.discoverer.ComponentDescription;
import context.arch.discoverer.component.TypeElement;

/**
 *
 * @author Agathe
 * @author Brian Y. Lim
 */
public class TypeIndexTable extends IndexTable<String> {

	private static final long serialVersionUID = -2381288928285510837L;

	public TypeIndexTable() {
		super(ComponentDescription.TYPE_ELEMENT, new TypeElement());
	}

}
