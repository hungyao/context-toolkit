/*
 * HostnameElement.java
 *
 * Created on July 6, 2001, 8:39 AM
 */

package context.arch.discoverer.component;

import java.util.ArrayList;
import java.util.Collection;

import context.arch.discoverer.ComponentDescription;

/**
 * Compares both hostname and host address
 * @author Agathe
 * @author Brian Y. Lim
 */
public class HostnameElement extends AbstractCollectionValueElement<String> {

	public HostnameElement() {
		super(ComponentDescription.HOSTNAME_ELEMENT,
				String.class);
	}

	public HostnameElement(String value){
		this();
		setValue(value);
	}

	@Override
	public Collection<String> extractElement(final ComponentDescription component) {
		return new ArrayList<String>() {
			private static final long serialVersionUID = -3440292770836203352L;
			{
				add(component.hostname);
				add(component.hostaddress);
			}
		};
	}

}
