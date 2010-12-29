package context.arch.enactor;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import context.arch.BaseObject;
import context.arch.InvalidMethodException;
import context.arch.MethodException;
import context.arch.comm.DataObject;
import context.arch.comm.clients.IndependentCommunication;
import context.arch.discoverer.ComponentDescription;
import context.arch.discoverer.Discoverer;
import context.arch.discoverer.component.TypeElement;
import context.arch.discoverer.query.BooleanQueryItem;
import context.arch.discoverer.query.AbstractQueryItem;
import context.arch.discoverer.query.ORQueryItem;
import context.arch.discoverer.query.RuleQueryItem;
import context.arch.enactor.WidgetReferenceRegistry.WidgetReferenceRegEntry;
import context.arch.handler.Handler;
import context.arch.server.Server;
import context.arch.service.helper.ServiceInput;
import context.arch.storage.Attributes;
import context.arch.subscriber.ClientSideSubscriber;
import context.arch.subscriber.DiscovererSubscriber;
import context.arch.widget.Widget;

/**
 * This class manages CTK subscriptions on behalf of an enactor. It generates 
 * discovery queries out of enactor references, and notifies enactors of any
 * new widgets that match those references.
 * 
 * @author alann
 * @author Brian Y. Lim
 * @author Kanupriya Tavri
 */
public class EnactorSubscriptionManager implements Handler {
	private static final Logger LOGGER = Logger.getLogger(EnactorSubscriptionManager.class.getName());
	static { LOGGER.setLevel(Level.WARNING); }
	
	/**
	 * The enactor that depends on this manager.
	 */
	protected Enactor enactor;

	/**
	 * 
	 * @param enactor needs to have been fully constructed, so do not instantiate EnactorSubscriptionManager in the constructor of Enactor
	 */
	EnactorSubscriptionManager(Enactor enactor) {
		init(enactor.getId(), enactor.getPort());
		setEnactor(enactor);
	}

	/**
	 * Initializes the subscription manager with with and id and a port. This
	 * method must be called prior to use.
	 *
	 */
	protected void init(String id, int port) {
		LOGGER.info("starting SituationManager on port " + port);
		baseObjDelegate = new BaseObject(port);
		//initialize ctk object to set ID and find discoverer
		baseObjDelegate.setId(id);
		//TODO: actually register with Discover (passing in "true"), with all enactor info, to allow runtime or designtime inspection of enactor
		//    ctkObject.findDiscoverer(false);
		baseObjDelegate.start(true);

	    final AbstractQueryItem<?,?> q = new ORQueryItem(
	    		RuleQueryItem.instance(new TypeElement(Widget.WIDGET_TYPE)),
	    		RuleQueryItem.instance(new TypeElement(Server.SERVER_TYPE)) // added compatibility for Server aggregator type, Jan 2010 --Brian
	    );

		// make a new discoverersubscriber, send subscription.
		discoSub = new DiscovererSubscriber(baseObjDelegate.getId(), BaseObject.getHostName(), baseObjDelegate.getPort(), Discoverer.NEW_COMPONENT, q);
		
		// we want to receive full ComponentDescriptions 
		discoSub.setFullDescriptionResponse(true);
//		System.out.println("ctkObject.discovererSubscribe(this, discoSub); - this: " + this);
		baseObjDelegate.discovererSubscribe(this, discoSub);

//		initialized = true;
	}

	/**
	 * Sets enactor to the ESM. If a new enactor, its references will be processed
	 * for subscriptions.
	 * 
	 * @param enactor enactor to be added.
	 * @return <tt>true</tt> if rule was not already contained in the SM.
	 */
	public void setEnactor(Enactor enactor) {
		this.enactor = enactor;

		// add the references of the enactor
		for (EnactorReference enactorRef : enactor.getReferences()) {
			addEnactorReference(enactorRef);
		}
		
		// TODO: why not adding EnactorParameters?

		// need to put this after adding enactorRef due to some dependencies
		handleNew();
	}
	
	/**
	 * TODO still not sure what to properly name this method with.
	 * Note similarity to {@link #handleSubscriptionCallback()}
	 */
	protected void handleNew() {
		for (AbstractQueryItem<?,?> subscriptionQuery : enactor.getSubscriptionQueries()) {	
//			System.out.println("enactor = " + enactor);
//			System.out.println("subscriptionQuery = " + subscriptionQuery + '\n' +
//							   "sendDiscovererAttributeQuery = " + sendDiscovererAttributeQuery(subscriptionQuery));
			
			for (ComponentDescription cd : sendDiscovererAttributeQuery(subscriptionQuery)) {	
				// save pointers to widgets that were successfully subscribed to; do this before subscribing enactorRefs
				saveWidgetComponentDescriptions(cd, subscriptionQuery);
				
				// subscribe for each reference
				for (EnactorReference er : enactor.getReferences()) {
					subscribe(cd, er);
				}				
			}
		}
	}

	/**
	 * This method registers an EnactorReference with the ESM. The reference must
	 * be part of a registered Enactor. Enactors can call this method if they add
	 * new references to themselves at runtime.
	 * 
	 * @param enactorRef the EnactorReference to add.
	 */
	protected void addEnactorReference(EnactorReference enactorRef) {
		widgetReferences.put(enactorRef, new WidgetReferenceRegEntry());

		// replaced with #handleNew()
//		for (AbstractQueryItem<?,?> subscriptionQuery : enactorRef.getEnactor().getSubscriptionQueries()) { // TODO why do this at enactorRef?
//			//subscribe to each component matching our reference
//			for (ComponentDescription cd : sendDiscovererAttributeQuery(subscriptionQuery)) { // TODO: check for obsolescence
//				subscribe(cd, enactorRef);
//			}
//		}
	}

	/**
	 * removed EnactorReference from manager. This can be called at runtime by Enactors when they
	 * change their internal structure to notify the manager, but the Enactor must be part
	 * of the manager first by calling addRule. 
	 * 
	 * @param enactorRef the EnactorReference to be removed.
	 */
	protected void removeEnactorReference(EnactorReference enactorRef) throws EnactorException {
		WidgetReferenceRegEntry wrre = widgetReferences.remove(enactorRef);
		if (wrre != null) {
			for (String subId : wrre.getWidgetSubscriptions()) {
				EnactorComponentInfo wsre = widgetSubscriptions.get(subId);
				if (wsre != null) {
					wsre.removeReference(enactorRef);
					if (wsre.getReferences().isEmpty()) {
						unsubscribe(subId);
					}
				}
			}
		}
	}

	/**
	 * @param sml
	 */
	protected void fireAddEventsForAll(Enactor enactor, EnactorListener sml) {
//		for (String subId : widgetSubscriptions.keySet()) {
//			EnactorComponentInfo eci = widgetSubscriptions.get(subId);
		for (EnactorComponentInfo eci : widgetSubscriptions.values()) {
						
//			ComponentDescription cd = eci.getComponentDescription();
			
			// TODO: what new info is gained from calling this for each reference???
//			for (EnactorReference wr : eci.getReferences()) { // fire event for each reference
//				enactor.fireComponentAdded(sml, eci, null);
//			}
			
			enactor.fireComponentAdded(sml, eci, null);
		}
	}

	//////////////////////////////////////
	// Begin CTK External Interaction Code
	//////////////////////////////////////

	protected BaseObject getBaseObject() {
		return baseObjDelegate;
	}

	public DataObject executeWidgetService(ComponentDescription cd, ServiceInput serviceInput) {
		return baseObjDelegate.executeSynchronousWidgetService(
				cd.hostname, cd.port, cd.id,
				serviceInput);
	}
	
	public DataObject updateOutWidget(ComponentDescription cd, Attributes input) {		
		// this would happen probably when the Enactor is not yet fully initialized,
		// but something else is asking it to update.
		if (cd == null) { return null; } // TODO: should notify with an error to say it is not yet ready

		return baseObjDelegate.updateAndPollWidget(
				cd.hostname, cd.port, cd.id,
				input);
	}

	public void handleIndependentReply(IndependentCommunication independentCommunication) {
		LOGGER.info("independent reply");
	}
	
	public DataObject handleCallback(String subscriptionId, DataObject data) throws InvalidMethodException, MethodException {
		ComponentDescription widgetState  = ComponentDescription.fromDataObject(data);
		
		// check if there's actually any non-constants to compare against
//		if (cd.getNonConstantAttributes().isEmpty()) {
//			// TODO not really sure why this would happen... but sometimes want to query about constants!
////			new RuntimeException("cd.getNonConstantAttributes().isEmpty()").printStackTrace();
//			return null;
//		}
		
		EnactorComponentInfo eci = widgetSubscriptions.get(subscriptionId);
		if (eci == null) { return null; }

		// put state into WSRegistry
		eci.setCurrentState(widgetState);
		
		// lookup listeners by widget's description
		for (EnactorReference ref : eci.getReferences()) {
			AbstractQueryItem<?,?> query = ref.getConditionQuery();

//			System.out.println("EnactorSubscriptionManager.handleCallback ------: cd = " + cd);
//			System.out.println("EnactorSubscriptionManager.handleCallback ------: query = " + query);
//			new RuntimeException("EnactorSubscriptionManager.handleCallback ------: query.match(cd) = " + query.match(cd)).printStackTrace();
			
			Boolean queryResult;
			if (	query != null && 
					(queryResult = query.match(widgetState)) != null && queryResult) {
				// execute references if they match
				ref.evaluateComponent(eci);
			}
		}

		return null; // then what is the point of returning? --Brian
	}

	/**
	 * 
	 * @param subscriptionId refers to the enactor (?) subscribing to the widget (?)
	 * @param data referring to the ComponentDescription to be tested
	 */
	@Override
	public DataObject handleSubscriptionCallback(String subscriptionId, DataObject data) throws InvalidMethodException, MethodException {
		// make new ClientSubscriber object, subscribe to widget.
		//TODO: we must need to do extra checking on messages from the discoverer (e.g. deletions), figure it out
		handleNewComponent(ComponentDescription.fromDataObject(data));
		
		return null; // then what is the point of returning? --Brian
	}

	/**
	 * We see if any registered enactors (through references) are interested in this
	 * component.  If not we do nothing.  If some enactor at a later date wants it, 
	 * we'll get it again as a result of a query.
	 * 
	 * @param cd description of component to be registered
	 */
	protected synchronized void handleNewComponent(ComponentDescription cd) {
		LOGGER.info("handling new component " + cd.id);
		
//		for (EnactorReference er : widgetReferences.getWidgetReferences()) {
//			Enactor enactor = er.getEnactor();
//			
//			//TODO gather all descriptionQueries in the widget before subscribing
//			for (AbstractQueryItem<?,?> subscriptionQuery : enactor.getSubscriptionQueries()) {			
//				Boolean queryResult = subscriptionQuery.match(cd);
//				if (queryResult != null && queryResult) { // matches subscription
//					subscribe(cd, er);
//					
//				}
//			}
//		}

		for (AbstractQueryItem<?,?> subscriptionQuery : enactor.getSubscriptionQueries()) {			
			Boolean queryResult = subscriptionQuery.match(cd);

			if (queryResult != null && queryResult) { // matches subscription				
				// save pointers to widgets that were successfully subscribed to; do this before subscribing enactorRefs
				saveWidgetComponentDescriptions(cd, subscriptionQuery);
				
				// subscribe for each reference
				for (EnactorReference er : enactor.getReferences()) {
					subscribe(cd, er);
				}
			}
		}
	}
	
	/**
	 * This method is called after successfully subscribing to the widgets, so that we can have soft pointers back to them.
	 * @param cd
	 * @param enactor
	 */
	protected void saveWidgetComponentDescriptions(ComponentDescription cd, AbstractQueryItem<?,?> subscriptionQuery) {
		// store component descriptions of widgets
		// useful to update either of them later through the subscription mechanism (w/o direct memory reference)

		if (enactor.widgetSubscriptionQueries[Enactor.IN_WIDGET_INDEX] == subscriptionQuery) {
			enactor.widgetComponentDescriptions[Enactor.IN_WIDGET_INDEX] = cd;
			//new RuntimeException("EnactorSubscriptionManager.saveWidgetComponentDescriptions IN_WIDGET cd = " + cd).printStackTrace();
		}
		
		// note this is not "else if", since both IN and OUT may be the same
		// e.g. for querying part of a widget, and manipulating another part
		else if (enactor.widgetSubscriptionQueries[Enactor.OUT_WIDGET_INDEX] == subscriptionQuery) {
			enactor.widgetComponentDescriptions[Enactor.OUT_WIDGET_INDEX] = cd;
			//new RuntimeException("EnactorSubscriptionManager.saveWidgetComponentDescriptions OUT_WIDGET cd = " + cd).printStackTrace();
		}
	}

	/**
	 * Subscribes to the widget described by the component description, and binds
	 * it to the enactor reference. We use the registry to bind the reference to an
	 * existing subscription if possible, before making a new CTK subscription.
	 * 
	 */
	protected void subscribe(ComponentDescription cd, EnactorReference er) {
//		LOGGER.info("subscribing to widget " + cd.id + 
//				" \n\tclassname = " + cd.classname + 
//				", hostaddress = " + cd.hostaddress + 
//				", hostname = " + cd.hostname + 
//				", port = " + cd.port);
//		System.out.println("subscribing to widget " + cd.id + 
//		" \n\tclassname = " + cd.classname + 
//		", hostaddress = " + cd.hostaddress + 
//		", hostname = " + cd.hostname + 
//		", port = " + cd.port);
		
		EnactorComponentInfo eci = null;

		//Enactor enactor = er.getEnactor();
		//saveWidgetComponentDescriptions(cd, enactor);

		if (!widgetIds.containsKey(cd.id)) {
			AbstractQueryItem<?,?> subscriptionQuery = er.getEnactor().getInWidgetSubscriptionQuery();
			if (subscriptionQuery == null) { return; } // may be null if Enactor is actually a Generator with no In, but only and Out
			
			eci = new EnactorComponentInfo();

			// create subscription; do we set the subscriberID here???
			ClientSideSubscriber subscriber = new ClientSideSubscriber(
					baseObjDelegate.getId(), BaseObject.getHostName(), baseObjDelegate.getPort(),
					Widget.CALLBACK_UPDATE, er.getEnactor().getInWidgetSubscriptionQuery(), null);
			baseObjDelegate.subscribeTo(this, cd.id, cd.hostname, cd.port, subscriber);
			
			// put an entry in the widgetSubscriptions that will allow us to lookup the description & css later
			eci.addReference(er);
			eci.setClientSideSubscriber(subscriber);
			eci.setComponentDescription(cd);
			
			// initialize current state to be initial component description
			eci.setCurrentState(cd);
			String  subscriptionId = subscriber.getSubscriptionId(); 
			widgetSubscriptions.put(subscriptionId, eci);
			discoSub.setSubscriptionId(subscriptionId); // TODO: not sure if this is correct, seems like it would be changing for each enactorRef --Brian
						
			widgetReferences.get(er).addWidgetSubscription(subscriptionId);
			widgetIds.put(cd.id, subscriptionId);

		} else {
			// for now we assume only one subscription per widget. This could change in the future.
			String subscriptionId = widgetIds.get(cd.id);
			eci = widgetSubscriptions.get(subscriptionId);
			eci.addReference(er);
			updateWidgetSubscriptionCondition(eci);
		}

		//notify EnactorReference
		er.componentAdded(eci);
	}

	/**
	 * Unsubscribes to the widget with the given subscription id, and notifies
	 * all bound enactor references.
	 */
	protected void unsubscribe(String subscriptionId) {
		LOGGER.info("unsubscribing to widget");
		EnactorComponentInfo eci = widgetSubscriptions.remove(subscriptionId);
		if (eci != null) {
			//potentially tell all widgetreferences about unsubscription
			//remove entry from widgetIds
			ComponentDescription cd = eci.getComponentDescription();
			String widgetId = cd.id;
			//WidgetIdRegEntry wire = widgetIds.get(widgetId);
			
			if (widgetIds.containsKey(widgetId)) {
				widgetIds.remove(widgetId);
			}
			
			baseObjDelegate.unsubscribeFrom(subscriptionId);
			
			//notify widgets of removal
			for (EnactorReference er : eci.getReferences()) {
				er.componentRemoved(eci);
			}
		}
	}

	/**
	 * Resubscribe to a currently subscribed widget with new conditions.
	 */
	protected void updateWidgetSubscriptionCondition(EnactorComponentInfo eci) {
		if (eci != null) {
			ClientSideSubscriber css = eci.getClientSideSubscriber();
			ComponentDescription cd = eci.getComponentDescription();
			
			BooleanQueryItem query = new ORQueryItem();
			for (EnactorReference er : eci.getReferences()) {
				for (AbstractQueryItem<?,?> subscriptionQuery : er.getEnactor().getSubscriptionQueries()) {
					query.add(subscriptionQuery);
				}
			}
			css.setCondition(query);
			
			// resubscribe using the same Subscriber: widget should take care of removing the old one
			baseObjDelegate.subscribeTo(this, cd.id, cd.hostname, cd.port, css);
		}
	}

	/**
	 * 
	 * @param q
	 * @return
	 */
	protected Collection<ComponentDescription> sendDiscovererAttributeQuery(AbstractQueryItem<?,?> q) {
		if (q != null) {
			Collection<ComponentDescription> comps = baseObjDelegate.discovererQuery(q);
			return comps;
		}
		else {
			return Collections.emptySet();
		}
	}

	////////////////////////////////////
	// End CTK External Interaction Code
	////////////////////////////////////

	/**
	 * Delegate base object to handle communication with the discoverer
	 */
	private BaseObject baseObjDelegate;

	//to be replace by an embedded database...
	private WidgetReferenceRegistry widgetReferences = new WidgetReferenceRegistry();
	private Map<String, EnactorComponentInfo> widgetSubscriptions = new HashMap<String, EnactorComponentInfo>();
	private Map<String, String> widgetIds= new HashMap<String, String>(); // <ComponentDescription.id, subscriptionId>

	private DiscovererSubscriber discoSub;
}
