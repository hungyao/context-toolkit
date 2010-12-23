/*
 * IdIndexTable.java
 *
 * Created on July 3, 2001, 11:08 AM
 */

package context.arch.discoverer.component.dataModel;

import context.arch.discoverer.ComponentDescription;
import context.arch.discoverer.component.IdElement;

/**
 *
 * @author Agathe
 * @author Brian Y. Lim
 */
public class IdIndexTable extends IndexTable<String> {

	private static final long serialVersionUID = 8865669219840752864L;

	public IdIndexTable() {
		super(ComponentDescription.ID_ELEMENT, new IdElement());
	}

}
