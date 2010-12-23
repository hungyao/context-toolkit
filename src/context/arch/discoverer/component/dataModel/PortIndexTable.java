/*
 * ClassnameIndexTable.java
 *
 * Created on July 3, 2001, 11:17 AM
 */

package context.arch.discoverer.component.dataModel;

import context.arch.discoverer.ComponentDescription;
import context.arch.discoverer.component.PortElement;

/**
 * 
 * Stores the port
 *
 * @author Agathe
 * @author Brian Y. Lim
 */
public class PortIndexTable extends IndexTable<Integer> {

	private static final long serialVersionUID = 9181368821871004106L;

	public PortIndexTable() {
		super(ComponentDescription.PORT_ELEMENT, new PortElement());
	}

}
