/*
 * ServiceElement.java
 *
 * Created on July 6, 2001, 1:37 PM
 */

package context.arch.discoverer.component;

import java.util.Collection;

import context.arch.discoverer.ComponentDescription;
import context.arch.service.Service;

/**
 * 
 * @author Agathe
 * @author Brian Y. Lim
 */
public class ServiceElement extends AbstractCollectionValueElement<String> {

	/** Creates new ServiceElement */
	public ServiceElement () {
		super(ComponentDescription.SERVICE_ELEMENT,
				String.class);
	}

	public ServiceElement(String serviceName) {
		this();
		setValue(serviceName);
	}

	public ServiceElement(Service service) {
		this();
		setValue(service);
	}

	public void setValue(Service service) {
		this.setValue(service.getName());
	}

	@Override
	public Collection<String> extractElement(ComponentDescription component) {
		return component.getServices();
	}

}
