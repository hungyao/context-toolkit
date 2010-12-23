package context.arch.subscriber;

import context.arch.comm.DataObject;
import context.arch.comm.DataObjects;
import context.arch.discoverer.query.AbstractQueryItem;
import context.arch.discoverer.Discoverer;

/**
 * This class implements a subscriber object, encapsulating the information
 * needed to create a subscriber and send information to it.
 * 
 * TODO: consider whether this should be an extension of widget Subscriber --Brian
 *
 * @see context.arch.subscriber.Subscribers
 */
public class DiscovererSubscriber extends AbstractSubscriber {

	public static final String DISCOVERER_TYPE = "disco";

	public static final String DISCOVERER_SUBSCRIPTION_CONTENT = "discoSubContent";

	public static final String DISCOVERER_SUBSCRIPTION_REPLY_CONTENT = "discoSubReplyContent";
	/**
	 * Tag to indicate message is a subscription reply
	 */
	public static final String DISCOVERER_SUBSCRIPTION_REPLY = "discoSubReply";

	/**
	 * These fields are specific to Subcriber 
	 */
	private AbstractQueryItem<?,?> query;
	private boolean fullDescriptionResponse = false;

	/**
	 * Basic constructor that creates a subscriber object.
	 *
	 * @param id ID of the subscriber
	 * @param hostname Name of the subscriber's host computer
	 * @param port Port number to send information to
	 * @param callback Callback the subscriber will implement
	 * @param tag Widget callback the subscriber is subscribing to
	 * @param conditions Any subscription conditions to use
	 * @param attributes Attributes to return to subscriber
	 */
	public DiscovererSubscriber(String componentId,String subHostname,int subPort,String subCallback,
			AbstractQueryItem<?,?> query) {
		super(DiscovererSubscriber.DISCOVERER_TYPE);
		setBaseObjectId(componentId);
		setSubscriberHostname(subHostname);
		setSubscriberPort(subPort);
		setSubscriptionCallback(subCallback);
		this.query = query;
		resetErrors();
	}

	/**
	 * Basic constructor that creates a subscriber object.
	 *
	 * @param id ID of the subscriber
	 * @param hostname Name of the subscriber's host computer
	 * @param port Port number to send information to
	 * @param callback Callback the subscriber will implement
	 * @param tag Widget callback the subscriber is subscribing to
	 * @param conditions Any subscription conditions to use
	 * @param attributes Attributes to return to subscriber
	 */
	public DiscovererSubscriber(String baseObjectId,String subHostname,String subPort,String subCallback,
			AbstractQueryItem<?,?> query) {
		this(baseObjectId,subHostname,new Integer(subPort).intValue(),subCallback,query);
	}

	/**
	 * Basic constructor that creates a subscriber object from a DataObject.
	 * The DataObject must contain a <SUBSCRIBER> tag
	 * The query is in Discoverer.DISCOVERER_QUERY_CONTENT
	 * @param data DataObject containing the subscriber info
	 */
	public DiscovererSubscriber(DataObject data) {
		super(data);
		query = AbstractQueryItem.fromDataObject(data.getDataObject(Discoverer.DISCOVERER_QUERY_CONTENT).getChildren().firstElement());
		fullDescriptionResponse = Boolean.valueOf(data.getDataObject(Discoverer.DISCOVERER_DESCRIPTION_FULL_RESPONSE).getValue()).booleanValue();
		if (context.arch.discoverer.Discoverer.DEBUG) System.out.println("DiscovererSubscriber (dataobject) " + query.toString ());
		if (context.arch.discoverer.Discoverer.DEBUG) System.out.println("this " + this.toString ());
	}

	/**
	 * This method converts the subscriber info to a DataObject
	 *
	 * @return Subscriber object converted to a <SUBSCRIBER> DataObject
	 */
	public DataObject toDataObject() {
		DataObjects v = (super.toDataObject()).getChildren();
		DataObjects v2 = new DataObjects();
		v2.addElement (query.toDataObject());
		v.addElement(new DataObject(Discoverer.DISCOVERER_QUERY_CONTENT, v2));
		v.addElement(new DataObject(Discoverer.DISCOVERER_DESCRIPTION_FULL_RESPONSE,Boolean.toString(fullDescriptionResponse)));
		return new DataObject(SUBSCRIBER, v);
	}


	/**
	 * Sets the subscription conditions, under which the subscriber will be notified
	 *
	 * @param conditions Subscription conditions used for notification
	 */
	public void setQuery(AbstractQueryItem<?,?> query) {
		this.query = query;
	}

	/**
	 * Returns the subscription query, under which the subscriber will be notified
	 *
	 * @return subscription query used for notification
	 */
	public AbstractQueryItem<?,?> getQuery() {
		return query;
	}

	/**
	 * Sets whether or not this subscriber wishes to receive full information
	 * about matching components, or just basic description information.
	 * 
	 * @author alann
	 */
	public void setFullDescriptionResponse(boolean b) {
		fullDescriptionResponse = b;
	}

	public boolean isFullDescriptionResponse() {
		return fullDescriptionResponse;
	}

}
