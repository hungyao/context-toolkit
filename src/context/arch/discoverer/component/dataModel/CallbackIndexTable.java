/*
 * CallbackIndexTable.java
 *
 * Created on July 3, 2001, 11:17 AM
 */

package context.arch.discoverer.component.dataModel;

import context.arch.discoverer.ComponentDescription;
import context.arch.discoverer.component.CallbackElement;

/**
 *
 * @author Agathe
 * @author Brian Y. Lim
 */
public class CallbackIndexTable extends IndicesTable<String> {

	private static final long serialVersionUID = -133294274845053776L;

	public CallbackIndexTable() {
		super(ComponentDescription.CALLBACK_ELEMENT, new CallbackElement());
	}

}
