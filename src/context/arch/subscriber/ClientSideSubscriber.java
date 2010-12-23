/*
 * ClientSideSubscriber.java
 *
 * Created on October 10, 2001, 6:19 PM
 */

package context.arch.subscriber;

import context.arch.storage.Attributes;
import context.arch.comm.DataObject;
import context.arch.comm.DataObjects;
import context.arch.discoverer.query.AbstractQueryItem;

/**
 * This class implements a client side subscriber object
 *
 * This code is awful.... we need to rewrite the subscription system
 * What is a client side subscriber? How does it differ from other subscribers? --Brian
 *
 * @see context.arch.subscriber.Subscribers
 * @see context.arch.subscriber.ClientSideSubscriber
 */
public class ClientSideSubscriber extends AbstractSubscriber {

	// This is bad... this way, on the client side, we create a ClientSideSub
	// and we transmit this object to the server that will recreate a 
	// widget Subscriber
	public static final String CLIENT_TYPE = "clientSideSubscriber";

	/**
	 * These fields are specific to Susbcriber (conditions for the callback 
	 * and attributes
	 */
	private AbstractQueryItem<?,?> condition;
	private Attributes attributes;

	/** Creates new ClientSideSubscriber
	 *
	 * @param id ID of the component
	 * @param hostname Name of the subscriber's host computer
	 * @param port Port number to send information to
	 * @param callback Callback the subscriber will implement
	 * @param tag Widget callback the subscriber is subscribing to
	 * @param conditions Any subscription conditions to use
	 * @param attributes Attributes to return to subscriber
	 */
	public ClientSideSubscriber(String baseObjectId, String subHostname,int subPort,
			String subCallback, AbstractQueryItem<?,?> condition, Attributes attributes) {
		super(ClientSideSubscriber.CLIENT_TYPE);
		setBaseObjectId(baseObjectId);
		setSubscriberHostname(subHostname);
		setSubscriberPort (subPort);
		setSubscriptionCallback(subCallback);
		this.condition = condition;
		if (attributes == null) {
			this.attributes = new Attributes();
		}
		else {
			this.attributes = attributes;
		}
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
	public ClientSideSubscriber(String baseObjectId, String subHostname,String subPort,String subCallback,
			AbstractQueryItem<?,?> condition,Attributes attributes) 
	{
		this(baseObjectId, subHostname,new Integer(subPort).intValue(),subCallback,condition,attributes);
	}

	/**
	 * Basic constructor that creates a subscriber object from a DataObject.
	 * The DataObject must contain a <SUBSCRIBER> tag
	 *
	 * @param data DataObject containing the subscriber info
	 */
	public ClientSideSubscriber(DataObject data) {
		super(data);
		DataObject sub = data.getDataObject(SUBSCRIBER);
		DataObject aqi = sub.getDataObject(AbstractQueryItem.ABSTRACT_QUERY_ITEM);
		if (aqi != null) {
			condition = AbstractQueryItem.fromDataObject((DataObject)aqi.getChildren().firstElement());
		}
		attributes = Attributes.fromDataObject(sub);
	}

	/**
	 * This method converts the subscriber info to a DataObject
	 *
	 * @return Subscriber object converted to a <SUBSCRIBER> DataObject
	 */
	public DataObject toDataObject() {
		DataObjects v = (super.toDataObject()).getChildren();
		if (condition != null) {
			DataObjects c = new DataObjects();
			c.add(condition.toDataObject());
			v.addElement(new DataObject(AbstractQueryItem.ABSTRACT_QUERY_ITEM, c));
		}
		if (attributes != null) v.addElement(attributes.toDataObject());
		return new DataObject(SUBSCRIBER, v);
	}


	/**
	 * Sets the subscription conditions, under which the subscriber will be notified
	 *
	 * @param conditions Subscription conditions used for notification
	 */
	public void setCondition(AbstractQueryItem<?,?> condition) {
		this.condition = condition;
	}

	/**
	 * Returns the subscription conditions, under which the subscriber will be notified
	 *
	 * @return subscription conditions used for notification
	 */
	public AbstractQueryItem<?,?> getCondition() {
		return condition;
	}

	/**
	 * Sets the attributes to return to the subscriber
	 *
	 * @param attributes Attributes to return to the subscriber
	 */
	public void setAttributes(Attributes attributes) {
		this.attributes = attributes;
	}

	/**
	 * Returns the subscription attributes to be returned
	 *
	 * @return subscription attributes to return to subscriber
	 */
	public Attributes getAttributes() {
		return attributes;
	}


	/**
	 *
	 */
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append (super.toString ());
		if (condition != null) sb.append (" condition " + condition.toString());
		if (attributes != null) sb.append (" attributes " + attributes.toString ());
		return sb.toString ();
	}

}
