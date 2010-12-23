/*
 * ClassnameIndexTable.java
 *
 * Created on July 3, 2001, 11:17 AM
 */

package context.arch.discoverer.component.dataModel;

import context.arch.discoverer.ComponentDescription;
import context.arch.discoverer.component.ClassnameElement;

/**
 *
 * @author Agathe
 * @author Brian Y. Lim
 */
public class ClassnameIndexTable extends IndexTable<String> {

	private static final long serialVersionUID = -3193077791365659372L;

	public ClassnameIndexTable() {
		super(ComponentDescription.CLASSNAME_ELEMENT, new ClassnameElement());
	}

}
