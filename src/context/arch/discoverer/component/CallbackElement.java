/*
 * CallbackElement.java
 *
 * Created on July 6, 2001, 1:37 PM
 */

package context.arch.discoverer.component;

import java.util.Collection;

import context.arch.discoverer.ComponentDescription;
import context.arch.subscriber.Callback;

/**
 *
 * @author Agathe
 * @author Brian Y. Lim
 */
public class CallbackElement extends AbstractCollectionValueElement<String> {

	/** Creates new CallbackElement */
	public CallbackElement() {
		super(ComponentDescription.CALLBACK_ELEMENT,
				String.class);
	}

	public CallbackElement(String callbackName){
		this();
		setValue(callbackName);
	}
	
	public CallbackElement(Callback callback) {
		this();
		setValue(callback);
	}

	@Override
	public void setValue(String callbackName){
		super.setValue(callbackName);
	}
	
	public void setValue(Callback callback) {
		this.setValue(callback.getName());
	}

	@Override
	public Collection<String> extractElement(ComponentDescription component) {
		return component.getCallbacks();
	}

}
