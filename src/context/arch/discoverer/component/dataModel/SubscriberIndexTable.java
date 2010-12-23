/*
 * SubscriberIndexTable.java
 *
 * Created on July 3, 2001, 11:17 AM
 */

package context.arch.discoverer.component.dataModel;

import context.arch.discoverer.ComponentDescription;
import context.arch.discoverer.component.SubscriberElement;

/**
 *
 * @author Agathe
 * @author Brian Y. Lim
 */
public class SubscriberIndexTable extends IndicesTable<String> {

	private static final long serialVersionUID = -3426999810600526153L;

	public SubscriberIndexTable() {
		super(ComponentDescription.SUBSCRIBER_ELEMENT, new SubscriberElement());
	}

}
