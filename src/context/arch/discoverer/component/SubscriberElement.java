/*
 * CallbackElement.java
 *
 * Created on July 6, 2001, 1:37 PM
 */

package context.arch.discoverer.component;

import java.util.Collection;

import context.arch.discoverer.ComponentDescription;
import context.arch.subscriber.Subscriber;

/**
 *
 * @author Agathe
 * @author Brian Y. Lim
 */
public class SubscriberElement extends AbstractCollectionValueElement<String> {

	public SubscriberElement () {
		super(ComponentDescription.SUBSCRIBER_ELEMENT,
				String.class);
	}

	public SubscriberElement(String subscriberName) {
		this();
		setValue(subscriberName);
	}

	public SubscriberElement(Subscriber subscriber) {
		this();
		setValue(subscriber.getSubscriptionId());
	}

	@Override
	public Collection<String> extractElement(ComponentDescription component) {
		return component.getSubscribers();
	}

}
