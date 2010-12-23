package context.arch.subscriber;

import context.arch.storage.Attributes;
import context.arch.comm.DataObject;
import context.arch.comm.DataObjects;
import context.arch.discoverer.query.AbstractQueryItem;

/**
 * This class implements a server or widget side subscriber object, encapsulating the information
 * needed to talk to a widget's subscriber.
 *
 * @see context.arch.subscriber.Subscribers
 * @see context.arch.subscriber.ClientSideSubscriber
 */
public class Subscriber extends AbstractSubscriber {

	public static final String GENERAL_TYPE = "widgetSubscriber";
	/**
	 * These fields are specific to Susbcriber (conditions for the callback 
	 * and attributes
	 */
	private AbstractQueryItem<?,?> condition;
	private Attributes attributes;

	/**
	 * Basic constructor that creates a subscriber object.
	 *
	 * @param id ID of the component
	 * @param hostname Name of the subscriber's host computer
	 * @param port Port number to send information to
	 * @param callback Callback the subscriber will implement
	 * @param tag Widget callback the subscriber is subscribing to
	 * @param condition A query that conditions the type of widget data returned
	 * @param attributes Attributes to return to subscriber
	 */
	public Subscriber(String subBaseObjectId, String subHostname,int subPort,String subCallback,
			AbstractQueryItem<?,?> condition ,Attributes attributes) {
		super(Subscriber.GENERAL_TYPE);

//		setSubscriptionId(subBaseObjectId); // wrong --Brian
		setBaseObjectId(subBaseObjectId);
		setSubscriberHostname(subHostname);
		setSubscriberPort(subPort);
		setSubscriptionCallback(subCallback);
		this.condition = condition;
		this.attributes = attributes;
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
	 * @param condition A query that conditions the type of widget data returned
	 * @param attributes Attributes to return to subscriber
	 */
	public Subscriber(String subBaseObjectId, String subHostname,String subPort,String subCallback,
			AbstractQueryItem<?,?> condition, Attributes attributes) {
		this(subBaseObjectId, subHostname,new Integer(subPort).intValue(),subCallback,condition,attributes);
	}

	/**
	 * Basic constructor that creates a subscriber object from a DataObject.
	 * The DataObject must contain a <SUBSCRIBER> tag
	 *
	 * @param data DataObject containing the subscriber info
	 */
	public Subscriber(DataObject data) {
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
		v.addElement(attributes.toDataObject());
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

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Subscriber)) { return false; }
		
		Subscriber other = (Subscriber) o;
		
//		if (this.subscriptionUniqueId == null) {
//			System.out.println("subscriptionUniqueId == null & this.class = " + this.getClass());
//		}
		// subscriptionUniqueId may be null if never added to a collection of Subscribers
		if (this.subscriptionUniqueId == null && other.subscriptionUniqueId != null) {
			return false;
		}
		
		return this.subscriptionUniqueId.equals(other.subscriptionUniqueId) &&
			   this.subscriberHostname.equals(other.subscriberHostname) &&
			   this.subscriberPort == other.subscriberPort &&
			   this.subscriptionCallback.equals(other.subscriptionCallback) && //TODO: repair this, not sure if QueryItems can be so easily compared --alann
			   this.attributes.equals(other.attributes);
	}

	@Override
	public int hashCode() {
		return super.hashCode();
		// TODO: could be made more efficient if this object is used as an index in a HashMap, but at the moment, there are no plans for such usage --Brian
	}

}
