/*
 * ServiceIndexTable.java
 *
 * Created on July 3, 2001, 11:17 AM
 */

package context.arch.discoverer.component.dataModel;

import context.arch.discoverer.ComponentDescription;
import context.arch.discoverer.component.ServiceElement;

/**
 *
 * @author Agathe
 * @author Brian Y. Lim
 */
public class ServiceIndexTable extends IndicesTable<String> {
	
	private static final long serialVersionUID = 7475094348604691099L;

	public ServiceIndexTable() {
		super(ComponentDescription.SERVICE_ELEMENT, new ServiceElement());
	}

}
