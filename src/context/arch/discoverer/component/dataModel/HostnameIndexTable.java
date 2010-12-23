/*
 * ClassnameIndexTable.java
 *
 * Created on July 3, 2001, 11:17 AM
 */

package context.arch.discoverer.component.dataModel;

import context.arch.discoverer.ComponentDescription;
import context.arch.discoverer.component.HostnameElement;

/**
 *
 * @author Agathe
 * @author Brian Y. Lim
 */
public class HostnameIndexTable extends IndicesTable<String> {
	
	private static final long serialVersionUID = -4921781669488396516L;

	public HostnameIndexTable() {
		super(ComponentDescription.HOSTNAME_ELEMENT, new HostnameElement());
	}

}
