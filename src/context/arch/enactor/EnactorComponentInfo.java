/*
 * Created on Apr 21, 2004
 *
 * $Id: EnactorComponentInfo.java,v 1.1 2004/05/01 23:23:55 squiddity Exp $
 */
package context.arch.enactor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import context.arch.discoverer.ComponentDescription;
import context.arch.subscriber.ClientSideSubscriber;


/**
 * State that is useful for tracking widgets we are subscribed to.
 * We could in principle regenerate a correct regkey from the
 * component description every time we needed it, but by keeping one
 * around we hopefully cache the expensive hashCode calculation.
 * 
 * note that while the ComponentDescription inside the regkey will
 * match 'signatures' with componentDescription, only the componentDescription
 * completely describes this particular widget (i.e. has its hostname, port,...).
 * In fact, it is best to try and make the descriptionRegKey be the _exact_
 * object instance as the key in the descriptionRegistry, because then
 * registry lookups will be extremely efficient (validating on '==' instead of '.equals').
 * This is not a strict requirement, however.
 * 
 * This class was originally architected to support multiple references at once,
 * and most of that functionality remains. However, convenience functions have been
 * introduced, like getReference(), that work correctly for the current case of one
 * reference.
 * 
 * @author alann
 */
public class EnactorComponentInfo {
	
	private ArrayList<EnactorReference> widgetReferences = new ArrayList<EnactorReference>();
	//  private List<EnactorReference> retWR = Collections.unmodifiableList(widgetReferences); // I think this would have locked retWR to be empty; put into method instead
	private ComponentDescription componentDescription, currentState;
	private ClientSideSubscriber subInfo;
	private String subscriptionID;

	boolean removeReference(EnactorReference rwr) {
		return widgetReferences.remove(rwr);
	}

	public EnactorReference getReference() {
		return (EnactorReference) widgetReferences.get(0);
	}

	List<EnactorReference> getReferences() {
		//    return retWR; 
		return Collections.unmodifiableList(widgetReferences);
	}

	/**
	 * Note similarity and difference with {@link #getCurrentState()}
	 * @return
	 */
	public ComponentDescription getComponentDescription() {
		return componentDescription;
	}

	public void setComponentDescription(ComponentDescription cd) {
		componentDescription = cd;
	}

	public ClientSideSubscriber getClientSideSubscriber() {
		return subInfo;
	}

	public void setClientSideSubscriber(ClientSideSubscriber css) {
		subInfo = css;
	}

	String getSubscriptionID() {
		return subscriptionID;
	}

	void setSubscriptionID(String subscriptionID) {
		this.subscriptionID = subscriptionID;
	}

	boolean addReference(EnactorReference rwr) {
		return widgetReferences.add(rwr);
	}

	/**
	 * This just mainly contains the names and values of the Widget attributes (for widgets).
	 * Doesn't contain the subscription info (id, hostname, port, etc).
	 * @see #getComponentDescription() for subscription info. 
	 * @return
	 */
	public ComponentDescription getCurrentState() {
		return currentState;
	}

	public void setCurrentState(ComponentDescription cd) {
		currentState = cd;
	}
}