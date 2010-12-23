package context.arch.widget;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import context.arch.BaseObject;
import context.arch.comm.DataObject;
import context.arch.comm.DataObjects;
import context.arch.comm.RequestObject;
import context.arch.comm.clients.DiscovererClient;
import context.arch.comm.clients.IndependentCommunication;
import context.arch.comm.language.EncodeException;
import context.arch.comm.language.InvalidEncoderException;
import context.arch.discoverer.ComponentDescription;
import context.arch.discoverer.Discoverer;
import context.arch.discoverer.lease.Lease;
import context.arch.discoverer.query.AbstractQueryItem;
import context.arch.logging.ComponentUpdateLogger;
import context.arch.logging.LoggingException;
import context.arch.logging.WidgetRegistrationLogger;
import context.arch.service.Service;
import context.arch.service.Services;
import context.arch.service.helper.FunctionDescription;
import context.arch.service.helper.FunctionDescriptions;
import context.arch.service.helper.ServiceDescription;
import context.arch.service.helper.ServiceInput;
import context.arch.storage.Attribute;
import context.arch.storage.AttributeNameValue;
import context.arch.storage.Attributes;
import context.arch.storage.Retrieval;
import context.arch.storage.RetrievalResults;
import context.arch.storage.StorageObject;
import context.arch.subscriber.AbstractSubscriber;
import context.arch.subscriber.Callback;
import context.arch.subscriber.Callbacks;
import context.arch.subscriber.Subscriber;
import context.arch.subscriber.Subscribers;
import context.arch.util.Constants;
import context.arch.util.Error;
;

/**
 * This class is the basic context widget, with attributes and
 * methods that should apply to all context widgets.
 * 
 * Modified for generics.
 * @param <D> represents the WidgetData used to encapsulate the widget data.
 * TODO: since data is being transmitted using an encapsulation, should we also use that to store the data?
 *
 * @see context.arch.BaseObject
 * @author Anind 
 * @author Brian Y. Lim
 */
public abstract class Widget extends BaseObject {

	/** Debug flag. Set to true to see debug messages. */
	public static boolean DEBUG = false;

	/** Tag for the class file being used by the widget. */
	public static final String CLASS = "class";

	/** Tag for the type of this object. */
	public static final String WIDGET_TYPE = "widget";

	/** Dummy version number. Subclasses should override this value. */
	public String VERSION_NUMBER = "UNDEFINED";

	/** Default port for widgets to use */
	public static final int DEFAULT_PORT = 5000;

	/** Tag for version number. */
	public static final String VERSION = "version";

	/** Attribute tag for the timestamp of widget data */
	public static final String TIMESTAMP = "timestamp";

	/**
	 * DataObject protocol tag to indicate the widget should return the latest stored data
	 */
	public static final String QUERY = "query";

	/**
	 * DataObject protocol tag to indicate the reply to a QUERY message
	 */
	public static final String QUERY_REPLY = "queryReply";

	/**
	 * DataObject protocol tag to indicate the widget should get the latest data from the generator and return them
	 */
	public static final String UPDATE_AND_QUERY = "updateAndQuery";

	/**
	 * DataObject protocol tag to indicate the reply to an UPDATE_AND_QUERY message
	 */
	public static final String UPDATE_AND_QUERY_REPLY = "updateAndQueryReply";

	/**
	 * DataObject protocol tag to indicate the widget should return its list of attributes
	 */
	public static final String QUERY_ATTRIBUTES = "queryAttributes";

	/**
	 * DataObject protocol tag to indicate the widget should return its list of attributes
	 */
	public static final String QUERY_CONSTANT_ATTRIBUTES = "queryConstantAttributes";

	/**
	 * DataObject protocol tag to indicate the reply to a QUERY_ATTRIBUTES message
	 */
	public static final String QUERY_ATTRIBUTES_REPLY = "queryAttributesReply";

	/**
	 * DataObject protocol tag to indicate the reply to a QUERY_CONSTANT_ATTRIBUTES message
	 */
	public static final String QUERY_CONSTANT_ATTRIBUTES_REPLY = "queryConstantAttributesReply";

	/**
	 * DataObject protocol tag to indicate the widget should return its list of callbacks
	 */
	public static final String QUERY_CALLBACKS = "queryCallbacks";

	/**
	 * DataObject protocol tag to indicate the reply to a QUERY_CALLBACKS message
	 */
	public static final String QUERY_CALLBACKS_REPLY = "queryCallbacksReply";

	/**
	 * DataObject protocol tag to indicate the widget should return its list of services
	 */
	public static final String QUERY_SERVICES = "queryServices";

	/**
	 * DataObject protocol tag to indicate the reply to a QUERY_SERVICES message
	 */
	public static final String QUERY_SERVICES_REPLY = "queryServicesReply";

	/**
	 * DataObject protocol tag to indicate the widget should return its version number
	 */
	public static final String QUERY_VERSION = "queryVersion";

	/**
	 * DataObject protocol tag to indicate the reply to a QUERY_VERSION message
	 */
	public static final String QUERY_VERSION_REPLY = "queryVersionReply";

	/**
	 * DataObject protocol tag to indicate the widget should accept the given data
	 */
	public static final String PUT_DATA = "putData";

	/**
	 * DataObject protocol tag to indicate the reply to a PUT_DATA message
	 */
	public static final String PUT_DATA_REPLY = "putDataReply";

	/**
	 * DataObject protocol tag to indicate an update is being sent
	 */
	public static final String CALLBACK_UPDATE = "update";

	/**
	 * Constant for the widget spacer
	 */
	public static final String SPACER = Constants.SPACER;

	protected Attributes nonConstantAttributes;
	protected Attributes constantAttributes;
	protected Callbacks callbacks;
	protected Services services;
	protected long CurrentOffset;

	/**Object to handle subscriptions to context data
	 * @see context.arch.subscriber.Subscribers
	 * @see context.arch.subscriber.Subscriber
	 */
	public Subscribers subscribers;

	/**Object to keep track of storage
	 * @see context.arch.storage.StorageObject
	 */
	public StorageObject storage;

	/**
	 * Constructor that sets up internal variables for maintaining
	 * the list of widget attributes, callbacks, and services and setting up
	 * the BaseObject info.
	 *
	 * TO COMPLETE : for storage use
	 *
	 * @param clientClass Class to use for client communications
	 * @param serverClass Class to use for server communications
	 * @param serverPort Port to use for server communications
	 * @param encoderClass Class to use for communications encoding
	 * @param decoderClass Class to use for communications decoding
	 * @param storageClass Class to use for storage
	 * @param id String to use for widget id and persistent storage
	 * @see context.arch.storage.StorageObject
	 */
	public Widget(String clientClass, String serverClass, int serverPort, String encoderClass,
			String decoderClass, String storageClass, String id, String widgetClassName) {
		super(clientClass, serverClass, serverPort, encoderClass, decoderClass);
		initFields();
		setId(id);
//		setWidgetClassName(widgetClassName);
//		init(id);
//		initFull();
	}

	/**
	 * Constructor that sets up internal variables for maintaining
	 * the list of widget attributes, callbacks, and services and setting up
	 * the BaseObject info. This version takes a boolean to indicate whether the
	 * default storage class should be used or whether no storage should be
	 * provided.
	 *
	 * @param clientClass Class to use for client communications
	 * @param serverClass Class to use for server communications
	 * @param serverPort Port to use for server communications
	 * @param encoderClass Class to use for communications encoding
	 * @param decoderClass Class to use for communications decoding
	 * @param storageFlag Flag to determine whether storage should be used or not
	 * @param id String to use for widget id and persistent storage
	 * @see context.arch.storage.StorageObject
	 */
	public Widget(String clientClass, String serverClass, int serverPort, String encoderClass,
			String decoderClass, boolean storageFlag, String id, String widgetClassName) {
		super(clientClass,serverClass,serverPort,encoderClass,decoderClass);
		initFields();
		setId(id);
//		setWidgetClassName(widgetClassName);
		
//		init(id); // call this only with start()
//		initFull();
		
		// TODO: not ready --Brian
		if (storageFlag) {
			storage = new StorageObject();
		}
	}

	/**
	 * Constructor that sets up internal variables for maintaining
	 * the list of widget attributes, callbacks and services.  It takes a port
	 * number as a parameter to indicate which port to listen for
	 * messages/connections, the id to use for the widget, and a flag to indicate
	 * whether storage functionality should be turned on or off.
	 *
	 * @param port Port to listen to for incoming messages
	 * @param id Widget id
	 * @param storageFlag Boolean flag to indicate whether storage should be turned on
	 */
	public Widget(int port, String id, String widgetClassName, boolean storageFlag) {
		this(null, null, port, null, null, storageFlag, id, widgetClassName);
	}

	/**
	 * Constructor that sets up internal variables for maintaining
	 * the list of widget attributes, callbacks and services.  It takes a port
	 * number as a parameter to indicate which port to listen for
	 * messages/connections.
	 *
	 * @param port Port to listen to for incoming messages
	 * @param id Widget id
	 */
	public Widget(int port, String id, String widgetClassName) {
		this(null, null, port, null, null, null, id, widgetClassName);
	}

	/**
	 * Constructor that sets up internal variables for maintaining
	 * the list of widget attributes, callbacks and services.  It takes
	 * the id to use for the widget, and a flag to indicate
	 * whether storage functionality should be turned on or off.
	 *
	 * @param id Widget id
	 * @param storageFlag Boolean flag to indicate whether storage should be turned on
	 */
	public Widget(String id, boolean storageFlag, String widgetClassName) {
		this(null, null, -1, null, null, storageFlag, id, widgetClassName);
	}

	/**
	 * Constructor that sets up internal variables for maintaining
	 * the list of widget attributes, callbacks and services.  It takes the
	 * widget id as a parameter
	 *
	 * @param id ID of the widget
	 */
	public Widget(String id, String widgetClassName) {
		//this(null,null,-1,null,null,null,id, widgetClassName);
		/*
		 * The original constructor would create a CommunicationsObject with the DEFAULT_PORT of 5555.
		 * This is not scalable if we have multiple widgets.
		 * So this has been changed to use findFreePort()
		 */
		this(null, null, findFreePort(), null, null, null, id, widgetClassName);
	}
	
	/**
	 * Convenience method to contain all initializations of lists of attributes, callbacks, services
	 */
	protected void initFields() {
		this.nonConstantAttributes = new Attributes();
		this.constantAttributes = new Attributes();
		this.callbacks = new Callbacks();
		this.services = new Services();
	}

	/**
	 * Encapsulation of init processes
	 * 
	 * Don't put this in the constructor, because it is overridable.
	 * http://benpryor.com/blog/2008/01/02/dont-call-subclass-methods-from-a-superclass-constructor/
	 * http://efreedom.com/Question/1-3342784/Using-Abstract-Init-Function-Abstract-Classs-Constructor
	 */
	protected void initFull() {
		// add timestamp attribute for all attributes
		addAttribute(Attribute.instance(TIMESTAMP, Long.class));
		
		init(); // may be implemented by subclass widget
		
		setCallbacks(initCallbacks());
		setSubscribers();
		getNewOffset();
	}
	
	/* ----------------------------------------------------------------
	 * Methods that subclasses should use to populate widget with
	 * attributes, services, callbacks.
	 * ---------------------------------------------------------------- */
	
	/**
	 * Subclasses implement this to do initialization work like adding 
	 * non-constant and constant attributes. This is similar to the
	 * init() function in Java applets. Attributes can be added using
	 * #addAttribute(Attribute) and its variants.
	 */
	protected void init() {
		// adds and does nothing by default
	}
	
	/**
	 * Add non-constant attribute to the Widget.
	 * @param att
	 */
	protected void addAttribute(Attribute<?> att) {
		addAttribute(att, false);
	}
	
	/**
	 * Add constant or non-constant attribute to the Widget.
	 * @param att Attribute to add
	 * @param constant if true, then add attribute as constant, otherwise as non-constant
	 */
	protected void addAttribute(Attribute<?> att, boolean constant) {
		if (constant) {
			constantAttributes.add(att);
		}
		else {
			nonConstantAttributes.add(att);
		}
	}
	
	/**
	 * Add a Service that can be executed remotely on this Widget.
	 * @param service
	 * @see Service
	 */
	public void addService(Service service) {
		services.add(service);
	}
	
	/* ---------------------------------------------------------------- */
	
	public int getPort() {
		return communications.getServerPort();
	}
	
	/**
	 * Must call this method to start the Widget.
	 * TODO this procedure is needed because {@link #initFull()} depends on some instance variables
	 * that may be set only after a widget subclass is constructed.
	 * @param register If null, then doesn't find discoverer; otherwise, it would find the discoverer and 
	 * would register if true
	 */
	@Override
	public void start(final Boolean register) {
		initFull();
		
		if (register != null) {
//			// put into thread
//			final String threadName = this.getId();
//			new Thread(threadName)  {
//				@Override
//				public void run() {
					findDiscoverer(register);
//					System.out.println(threadName + " started (port = " + Widget.this.getPort() + ")");
//				}
//			}.start();
		}
	}

	@Override
	public String getType() {
		return Widget.WIDGET_TYPE;
	}

	/**
	 * Method to find the discoverer. inserts the widget registration entry into the log
	 * Refactored to make it protected, so it should not be called externally. --Brian
	 * @see context.arch.BaseObject#findDiscoverer(boolean, context.arch.discoverer.lease.Lease, boolean)
	 */
	@Override
	protected Error findDiscoverer(boolean registration, Lease registrationLease, boolean automaticRenewal) {
		WidgetRegistrationLogger WRL= WidgetRegistrationLogger.getWRLInstance();
		try{
			WRL.insertWidgetRegistrationEntry(this.getId(), this.constantAttributes, this.nonConstantAttributes, this.callbacks, this.services);
		}catch(LoggingException e){
			System.out.println(e.toString());
		}
		return super.findDiscoverer(registration,  registrationLease,  automaticRenewal);
	}  

	/**
	 * Sets the current state of *some* set of NonConstantAttributes in the widget.
	 * If subscribers or storage is enabled, we send the attributes out to
	 * subscribers.
	 * 
	 * Note: does not enforce strong type checking. If an arbitrary attribute is
	 * set, this widget will 'acquire' that attribute. It will probably not be
	 * sent as a callback, however.
	 * 
	 * @param atts
	 */
	public void addNonConstantAttributes(Attributes atts) {
		// adds or replaces
		nonConstantAttributes.putAll(atts);
	}

	/**
	 * Sets the callbacks for the widget
	 */
	protected Callbacks initCallbacks() {
		Callbacks calls = new Callbacks();
		
		Attributes atts = new Attributes();
		atts.putAll(new Attributes(nonConstantAttributes)); // make copy, so that callbacks don't point to changed data
		atts.putAll(new Attributes(constantAttributes));
		
		// add a callback corresponding to the UPDATE tag
		calls.addCallback(CALLBACK_UPDATE, atts);
		
		return calls;
	}

	/**
	 * Sets the attributes; may add if an attribute is not already contained.
	 * @param atts
	 */
	protected void setNonConstantAttributes(Attributes atts) {
		nonConstantAttributes.putAll(atts);
	}

	protected void setConstantAttributes(Attributes atts) {
		constantAttributes.putAll(atts);
	}

	protected void setCallbacks(Callbacks calls) {
		callbacks.putAll(calls);
	}

	protected void setServices(Services svcs) {
		services.putAll(svcs);
	}

	/**
	 * Returns the attribute value with the given name
	 *
	 * @param name Name of the attribute to get
	 */
	protected Class<?> getAttributeType(String name) {
		return nonConstantAttributes.get(name).getType();
	}

	/**
	 * Checks if the given attribute is an attribute of this widget.
	 *
	 * @param name Name of the attribute to check
	 */
	protected boolean isNonConstantAttribute(String name) {
		return nonConstantAttributes.containsName(name);
	}
	
	protected boolean isConstantAttribute(String name) {
		return constantAttributes.containsName(name);
	}

	/**
	 * Checks if the given callback is a callback of this widget
	 *
	 * @param name Name of the callback to check
	 * @return boolean True if name is a known callback name
	 */
	protected boolean isCallback(String name) {
		return callbacks.containsKey(name);
	}
	
	/**
	 * Call this to update widget with new data.
	 * It will also notify listeners of this change.
	 * This is called in memory, when the caller has a direct memory reference to this widget.
	 * @param data that encapsulates some or all of the widget attributes
	 * @see #updateData(Attributes)
	 */
	public void updateData(WidgetData data) {
		updateData(data.toAttributes());
	}

	/**
	 * Method of updating several attributes of the Widget.
	 * It will ignore attributes that do not match what the widget contains.
	 * @param attrs
	 */
	public void updateData(Attributes attrs) {
		notify(Widget.CALLBACK_UPDATE, attrs);
//		System.out.println(this.getClass().getSimpleName() + ".updateData(Attributes attrs: " + attrs + ")");
	}
	
	/**
	 * Convenience method of directly updating an attribute of the Widget.
	 * It also adds an attribute TIMESTAMP specifying the current time if that was not set.
	 * @param attName
	 * @param value
	 */
	@SuppressWarnings("serial")
	public <T extends Comparable<? super T>> void updateData(final String attName, final T value) {
		Attributes attrs = new Attributes() {{
			if (!containsName(Widget.TIMESTAMP)) { // set time if not already set
				add(AttributeNameValue.instance(Widget.TIMESTAMP, System.currentTimeMillis()));
			}
			add(AttributeNameValue.instance(attName, value));
		}};
		updateData(attrs);
	}
	
	/**
	 * This actually notifies subscribers, and
	 * also stores the attribute values
	 * TODO: consider refactoring the name of the method
	 * @param callbackName
	 * @param attrs
	 */
	protected void notify(String callbackName, Attributes attrs) {
//		new RuntimeException("widget.notify attrs = " + attrs).printStackTrace();
//		System.out.println("widget.notify attrs = " + attrs);
		if (attrs == null) { return; }

		setNonConstantAttributes(attrs); // update widget in memory
		store(attrs); // update widget in storage
		sendToSubscribers(callbackName); // notify subscribers

		// This is very useful for debugging, but it is very verbose, especially for widgets with many attributes
//		System.out.println(getClassName() + ".notify attrs: " + attrs);
	}
	
	/** 
	 * <p>
	 * Convenience class to wrap Attributes as an encapsulation to pass around to different components.
	 * Subclasses can have their own instance fields to clarify which attributes are valid.
	 * Nested class to support transmission of widget data.
	 * </p>
	 * <p>
	 * If only using Attributes, and not specifying specific attributes as fields in code, then just use {@link Attributes} instead
	 * </p>
	 */
	public static class WidgetData {
		
		private String widgetName; // name of widget that this data is associated to
		private Attributes atts;
		
		/**
		 * Sets timestamp to default: current time 
		 */
		public WidgetData(String widgetName) {
			this(widgetName, System.currentTimeMillis());
		}
		
		public WidgetData(String widgetName, long timestamp) {
			this.widgetName = widgetName;
			atts = new Attributes();
			atts.add(new AttributeNameValue<Long>(TIMESTAMP, timestamp));
		}
		
		public WidgetData(Class<? extends Widget> widgetClass, long timestamp) {
			this(widgetClass.getName(), timestamp);
		}
		
		@SuppressWarnings("unchecked")
		public <T extends Comparable<? super T>>  T getAttributeValue(String name, Class<T> type) {
			T value = ((AttributeNameValue<T>) atts.get(name)).getValue();
			return value;
		}
		
		public <T extends Comparable<? super T>> void setAttributeValue(String name, T value) {
			atts.add(new AttributeNameValue<T>(name, value));			
		}
		
		public Attributes toAttributes() {
			// may want to add more things to att
		    return atts;
		}
		
		/**
		 * Convenience method to get ComponentDescription w/o using the discovery mechanism.
		 * Should be overridden by subclasses to consider constant attributes. TODO
		 */
		public ComponentDescription toWidgetState() {
			ComponentDescription widgetState = new ComponentDescription();
//			widgetState.id = this.getClass().getName(); // need this or it would be invalidated before classification
			// actually, don't need to set anymore --Brian

			for (Attribute<?> att : this.toAttributes().values()) {
				widgetState.addNonConstantAttribute(att);
			}
			
			return widgetState;
		}
		
		public String toString() {
			return widgetName + " data: " + atts;
		}
	}

	/**
	 * This method is called when a remote component sends an UPDATE_AND_QUERY message.
	 * It calls the widget's queryGenerator method to get the latest generator info,
	 * and then stores it.
	 */
	protected void updateWidgetInformation() {
		Attributes atts = queryGenerator();
		if (atts != null) {
			if (storage != null) {
				storage.store(atts);
			}
		}
	}

//	/**
//	 * This abstract method is called when the widget wants to get the latest generator
//	 * info.
//	 *
//	 * @return AttributeNameValues containing the latest generator information
//	 */
//	protected abstract Attributes queryGenerator();

	/**
	 * This method is called when the widget wants to get the latest generator info.
	 * Default returns an empty AttributeNameValues object; widget cannot be polled.
	 * @return empty AttributeNameValues
	 * 
	 * TODO: this functionality has been superceded by Generators that subclass Enactors
	 */
	protected Attributes queryGenerator() {
		return new Attributes();
	}

	/**
	 * This is an empty method that should be overridden by objects
	 * that subclass from this class.  It is called when another component
	 * tries to run a method on the widget, but it's not a query.
	 *
	 * @param data DataObject containing the data for the method
	 * @param error String containing the incoming error value
	 * @return DataObject containing the method results
	 */
	protected DataObject runWidgetMethod(DataObject data, String error) {
		@SuppressWarnings("unused")
		String name = data.getName();
		Error err = new Error(error);
		if (err.getError() == null) {
			err.setError(Error.UNKNOWN_METHOD_ERROR);
		}
		DataObjects v = new DataObjects();
		v.addElement(err.toDataObject());
		return new DataObject(data.getName(),v);
	}

	/**
	 * This method is meant to handle any internal methods that the baseObject doesn't
	 * handle.  In particular, this method handles the common details for query requests,
	 * update and query requests, and version requests that each widget should provide.
	 * If the method is not one of these queries, then it calls runWidgetMethod which each widget
	 * should provide.
	 *
	 * @param data DataObject containing the method to run and parameters
	 * @return DataObject containing the results of running the method
	 * @see #QUERY
	 * @see #QUERY_VERSION
	 * @see #UPDATE_AND_QUERY
	 */
	public DataObject runUserMethod(DataObject data) {
		debugprintln(DEBUG, "\nWidget runUserMethod " + data.getName ());
		DataObject widget = data.getDataObject(ID);
		String error = null;

		if (widget == null) {
			error = Error.INVALID_ID_ERROR;
		}
		else {
			String queryId = widget.getValue();
			if (!queryId.equals(getId())) {
				error = Error.INVALID_ID_ERROR;
			}
		}

		String methodType = data.getName();
		if (methodType.equals(UPDATE_AND_QUERY)) {
			return queryWidget(data,true,error);
		}
		else if (methodType.equals(QUERY)) {
			return queryWidget(data,false,error);
		}
		else if (methodType.equals(QUERY_ATTRIBUTES)) {
			return queryAttributes(data,error);
		}
		else if (methodType.equals(QUERY_CONSTANT_ATTRIBUTES)) {
			return queryConstantAttributes(data,error);
		}
		else if (methodType.equals(QUERY_CALLBACKS)) {
			return queryCallbacks(data,error);
		}
		else if (methodType.equals(QUERY_SERVICES)) {
			return queryServices(data,error);
		}
		else if (methodType.equals(Subscriber.ADD_SUBSCRIBER)) {
			return addSubscriber(data,error);
		}
		else if (methodType.equals(Subscriber.REMOVE_SUBSCRIBER)) {
			return removeSubscriber(data,error);
		}
		else if (methodType.equals(StorageObject.RETRIEVE_DATA)) {
			return retrieveData(data,error);
		}
		else if (methodType.equals(PUT_DATA)) {
			Error err = putData(data, error);
			DataObjects v = new DataObjects();
			v.addElement(err.toDataObject());
			return new DataObject(PUT_DATA_REPLY, v);
		}
		else if (methodType.equals(Service.SERVICE_REQUEST)) {
			return executeService(data,error);
		}
		else {
			return runWidgetMethod(data,error);
		}
	}

	/**
	 * This method puts context data in a widget.  It is expected
	 * that widgets will get data from a generator.  But for some
	 * widgets, the generator will not use the context toolkit directly,
	 * but may use a web CGI script, for example.  For this case, the
	 * widget provides this method to collect the data and makes it available
	 * to subscribers and for retrieval.
	 *
	 * @param data DataObject containing the context data to write
	 * @param error String containing the incoming error value
	 * @return DataObject containing the results of writing the data
	 */
	protected Error putData(DataObject data, String error) {
		Error err = new Error(error);
		if (err.getError() == null) { return err; }
		
		Attributes atts = Attributes.fromDataObject(data);
		
		// empty data
		if (atts == null || atts.isEmpty()) { return err.setError(Error.INVALID_DATA_ERROR); }
		
		DataObject callbackObj = data.getDataObject(Subscriber.CALLBACK_NAME);
		String callbackName = null;
		if (callbackObj != null) {  callbackName = callbackObj.getValue(); }
		
		if (callbackName != null) {
			Callback callback = callbacks.get(callbackName);			
			if (callback == null) { return err.setError(Error.INVALID_CALLBACK_ERROR); }

			Attributes callAtts = callback.getAttributes();
			
			// check if atts contains all atttributes in callAtts
			for (String name : callAtts.keySet()) {
				if (!atts.containsKey(name)) {
					return err.setError(Error.INVALID_ATTRIBUTE_ERROR);
				}
			}
			
			setNonConstantAttributes(atts);
			sendToSubscribers(callbackName);
			store(atts);
			return err.setError(Error.NO_ERROR);
		}
		
		// nevertheless, if it can handle attributes, then process
		else if (canHandle(atts)) {
			store(atts);
			return err.setError(Error.NO_ERROR);
		}
		else {
			return err.setError(Error.INVALID_ATTRIBUTE_ERROR);
		}
	}

	/**
	 * This method queries the callbacks of a widget.
	 *
	 * @param query DataObject containing the query
	 * @param error String containing the incoming error value
	 * @return DataObject containing the results of the query
	 */
	protected DataObject queryCallbacks(DataObject query, String error) {
		DataObjects v = new DataObjects();
		Error err = new Error(error);
		if (err.getError() == null) {
			if (callbacks == null || callbacks.isEmpty()) {
				err.setError(Error.EMPTY_RESULT_ERROR);
			}
			else {
				v.addElement(callbacks.toDataObject());
				err.setError(Error.NO_ERROR);
			}
		}
		v.addElement(err.toDataObject());
		return new DataObject(QUERY_CALLBACKS_REPLY, v);
	}

	/**
	 * This method queries the attributes of a widget.
	 *
	 * @param query DataObject containing the query
	 * @param error String containing the incoming error value
	 * @return DataObject containing the results of the query
	 */
	protected DataObject queryAttributes(DataObject query, String error) {
		DataObjects v = new DataObjects();
		Error err = new Error(error);
		
		if (err.getError() == null) {
			if (nonConstantAttributes == null || nonConstantAttributes.isEmpty()) {
				err.setError(Error.EMPTY_RESULT_ERROR);
			}
			else {
				err.setError(Error.NO_ERROR);
				v.addElement(nonConstantAttributes.toDataObject());
			}
		}
		
		v.addElement(err.toDataObject());
		return new DataObject(QUERY_ATTRIBUTES_REPLY, v);
	}

	/**
	 * This method queries the constant attributes of a widget.
	 *
	 * @param query DataObject containing the query
	 * @param error String containing the incoming error value
	 * @return DataObject containing the results of the query
	 *
	 * @author Agathe
	 */
	protected DataObject queryConstantAttributes(DataObject query, String error) {
		DataObjects v = new DataObjects();
		Error err = new Error(error);
		if (err.getError() == null) {
			if (constantAttributes == null) {
				err.setError(Error.EMPTY_RESULT_ERROR);
			}
			else {
				err.setError(Error.NO_ERROR);
				v.addElement(constantAttributes.toDataObject());
			}
		}
		v.addElement(err.toDataObject());
		return new DataObject(QUERY_CONSTANT_ATTRIBUTES_REPLY, v);
	}
	
	/**
	 * This method queries the services of a widget.
	 *
	 * @param query DataObject containing the query
	 * @param error String containing the incoming error value
	 * @return DataObject containing the results of the query
	 */
	protected DataObject queryServices(DataObject query, String error) {
		DataObjects v = new DataObjects();
		Error err = new Error(error);
		if (err.getError() == null) {
			err.setError(Error.NO_ERROR);
			v.addElement(services.toDataObject());
		}
		v.addElement(err.toDataObject());
		return new DataObject(QUERY_SERVICES_REPLY, v);
	}

	/**
	 * This method runs a query on a widget, asking for either it's latest
	 * acquired data (QUERY) or asking for the widget to acquire and return
	 * new data (UPDATE_AND_QUERY)
	 *
	 * @param query DataObject containing the query request
	 * @param update Whether or not to acquire new data
	 * @param error String containing the incoming error value
	 * @return DataObject containing the reply to the query
	 */
	protected DataObject queryWidget(DataObject query, boolean update, String error) {
		debugprintln(DEBUG, "Widget queryWidget query:"+query.toString() + "\nerror:"+ error);
//		println("Widget.queryWidget query:"+query.toString() + "\nerror:"+ error);
		
		DataObject result = null;
		DataObjects v = new DataObjects();
		if (update) {
			result = new DataObject(UPDATE_AND_QUERY_REPLY, v);
		}
		else {
			result = new DataObject(QUERY_REPLY, v);
		}

		Attributes atts = Attributes.fromDataObject(query);
		Error err = new Error(error);
		if (err.getError() == null) {
			if (atts == null) {
				err.setError(Error.MISSING_PARAMETER_ERROR);
			}
			else if (!canHandle(atts)) {
				err.setError(Error.INVALID_ATTRIBUTE_ERROR);
			}
		}

		if (err.getError() != null) {
			v.addElement(err.toDataObject());
			return result;
		}
		
		// update widget with data in Attributes
		if (atts != null) { // TODO shouldn't this also check for whether to update?
			updateData(atts);

			if (storage != null) {
				if (update) {
					updateWidgetInformation();
				}
				storage.flushStorage();
				Attributes values = storage.retrieveLastAttributes();

				if (values != null) {
					Attributes subset = values.getSubset(atts);
					if (subset.isEmpty()) {
						err.setError(Error.INVALID_DATA_ERROR);
					}
					else {
						v.addElement(subset.toDataObject());
						if (subset.size() >= atts.size()) {
							err.setError(Error.NO_ERROR);
						}
						else {
							err.setError(Error.INCOMPLETE_DATA_ERROR);
						}
					}
				}
				else {
					err.setError(Error.INVALID_DATA_ERROR);
				}
			}
		}
		
		v.addElement(err.toDataObject());
		debugprintln (DEBUG, "Widget queryWidget return:"+result.toString());
		return result;
	}

	/**
	 * This method checks the list of attributes to ensure
	 * that the widget contains these attributes.
	 *
	 * @param attributes Attributes object containing attributes to check
	 * @return whether the list of attributes is valid
	 */
	protected boolean canHandle(Attributes atts) {
		purgeConstantAttributes(atts);
		
		for (Attribute<?> att : atts.values()) {
			String name = att.getName();

			if (!isNonConstantAttribute(name)) { // not a modifiable attribute
				return false;
			}
		}

		return true;
	}
	
	protected void purgeConstantAttributes(Attributes atts) {
		for (Iterator<Attribute<?>> it = atts.values().iterator(); it.hasNext();) {
			Attribute<?> a = it.next();
			if (isConstantAttribute(a.getName())) {
				it.remove();
			}
		}
	}

	/**
	 * This method attempts to execute a widget service.
	 *
	 * @param request DataObject containing the service request
	 * @param error String containing the incoming error value
	 * @return DataObject containing the results of the service request
	 */
	protected DataObject executeService(DataObject request, String error) {
		DataObjects v = new DataObjects();
		Error err = new Error(error);
		DataObject result;
		if (err.getError() == null) {
			ServiceInput si = new ServiceInput(request);

			if (!services.hasService(si.getServiceName())) {
				err.setError(Error.UNKNOWN_SERVICE_ERROR);
			}
			else {
				Service service = services.getService(si.getServiceName());
				FunctionDescriptions fds = service.getFunctionDescriptions();
				
				if (!fds.hasFunctionDescription(si.getFunctionName())) {
					err.setError(Error.UNKNOWN_FUNCTION_ERROR);
				}
				else {
					String synchronicity = request.getDataObject(FunctionDescription.FUNCTION_SYNCHRONICITY).getValue();
					FunctionDescription fd = fds.getFunctionDescription(si.getFunctionName());

					if (!fd.getSynchronicity().equals(synchronicity)) {
						err.setError(Error.INVALID_TIMING_ERROR);
					}
					else {
						result = service.execute(si);
						err.setError(Error.NO_ERROR);
						
						// make sure result is meaningful, then add
						if (result != null && result.getName() != null) {
							v.add(result);
						}
					}
				}
			}
		}
		v.addElement(err.toDataObject());
		return new DataObject(Service.SERVICE_REQUEST_REPLY, v);
	}
	
	public Attributes getNonConstantAttributes() {
		return new Attributes(nonConstantAttributes); // duplicate so that it is not accidentally overridden
	}
	
	public <T extends Comparable<? super T>> T getNonConstantAttributeValue(String attName) {
		return nonConstantAttributes.getAttributeValue(attName);
	}
	
	public Attributes getConstantAttributes() {
		return new Attributes(constantAttributes); // duplicate so that it is not accidentally overridden
	}

	/**
	 * This method should be called to send data to subscribers when a context
	 * widget's callback is triggered.  It sends data only to those subscribers
	 * that have subscribed to the specified callback.
	 *
	 * @author Agathe, to use independentCommunication
	 *
	 *
	 * @param callbackTag Context widget callback that was triggered
	 * @param atts AttributeNameValues to send to subscribers
	 * @param data DataObject version of atts
	 * @see BaseObject#userRequest(DataObject, String, String, int)
	 * @see context.arch.subscriber.Subscribers
	 */
	protected void sendToSubscribers(String callbackName) {
		if (!callbacks.containsKey(callbackName)) { return; }
		if (subscribers.isEmpty()) { return; }
		
//		new RuntimeException("widget.sendToSubscribers attributes = " + nonConstantAttributes).printStackTrace();
		
		//ADDED FOR EXPLANATIONS
		ArrayList<ComponentDescription> logSubscribers = new ArrayList<ComponentDescription>();
		
		debugprintln(DEBUG, "\n\nWidget <sendToSubscribers> callback=" + callbackName);
		Callback callback = callbacks.get(callbackName);
		
		// For each subscriber, see if the subscriber is interested
		debugprintln(DEBUG, "widget <sendToSubs> nb subs? " + subscribers.size());
//println("widget <sendToSubs> subs? " + subscribers); 
		
		for (AbstractSubscriber asub : subscribers.values()) {
			Subscriber sub = (Subscriber) asub;
			DataObject result = null;      // callback reply
			
			// Check if the subscriber wants this callback
			debugprintln(DEBUG, "Widget <sendToSubs> test callback=" + callbackName + " ?? equal to sub call=" + sub.getSubscriptionCallback ());
			
			if (callbackName.equals(sub.getSubscriptionCallback())) {		
				// Checks if the subscriber has specified conditions
				if (dataValid(callback, sub.getCondition())) {
					debugprintln(DEBUG, "Widget <sendToSubscribers> datavalid TRUE");
//println("widget callback = " + callback);
					Attributes callAtts = new Attributes(callback.getAttributes());

					Attributes subAtts = nonConstantAttributes.getSubset(callAtts);//.getSubset(sub.getAttributes());
					Attributes constSubAtts = constantAttributes.getSubset(callAtts);//.getSubset(sub.getAttributes());
//println("widget nonConstantAttributes = " + nonConstantAttributes);
//println("widget callAtts = " + callAtts);
//println("widget sub = " + sub);
//println("widget sub.getAttributes() = " + sub.getAttributes());
//println("widget subAtts = " + subAtts);
					
					//only process if we have attribute to return
					if (subAtts.isEmpty() && constSubAtts.isEmpty()) { continue; }
					
					DataObject subId = new DataObject(Subscriber.SUBSCRIBER_ID, sub.getSubscriptionId());
					
					DataObjects v = new DataObjects();
					v.addElement(subId);
					DataObject compDescription = buildCallbackComponentDescription(subAtts, constSubAtts);
//println("widget compDescription = " + compDescription + "\n");
					v.addElement(compDescription);
					DataObject send = new DataObject(Subscriber.SUBSCRIPTION_CALLBACK, v);

					// Agathe: change to use independentUserRequest
					try {
						result = null;
						
						// Create the independent comm object
						IndependentCommunication comm = new IndependentCommunication(
															new RequestObject(send, Subscriber.SUBSCRIPTION_CALLBACK, 
																sub.getSubscriberHostName(), 
																sub.getSubscriberPort()));
						
						// Store the sub object to remove it if it does not exist anymore
						comm.setObjectToStore(sub);
						
						// Store some reference for this communication
						comm.setSenderClassId(Widget.WIDGET_TYPE + Subscriber.SUBSCRIPTION_CALLBACK);
						
						// Send the notification
						independentUserRequest(comm);
						
					} catch (EncodeException ee) {
						System.out.println("Widget sendToSubscribers EncodeException: "+ee);
					} catch (InvalidEncoderException iee) {
						System.out.println("Widget sendToSubscribers InvalidEncoderException: "+iee);
					}

					//ADDED FOR LOGGING:
					ComponentDescription logCompDescription = new ComponentDescription();
					logCompDescription.id = sub.getBaseObjectId();
					logCompDescription.setConstantAttributes(constSubAtts);
					logCompDescription.setNonConstantAttributes(subAtts);

					sub.resetErrors(); 
					
					// we pass the result on for processing
					// TODO: pass it on only if it's not an error message?
					try {          		
						processCallbackReply(result, sub);

						//ADDED FOR EXPLANATIONS
						logSubscribers.add(logCompDescription);
					} catch (Exception e) {
						System.out.println ("Widget sendToSubscribers Exception during processCallbackReply: "+e);
					}
				}
				else {
					debugprintln(DEBUG, "Widget <sendToSubscribers> datavalid FALSE");
				}
			}
		}
		
		//ADDED FOR EXPLANATIONS
		ComponentUpdateLogger CUL = ComponentUpdateLogger.getCULInstance();
		try{
			CUL.insertComponentUpdateEntry(this.getId(), callbackName, logSubscribers);
		} catch (LoggingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * constructs an abbreviated ComponentDescription containing only the necessary information
	 * for the callback.
	 * @param nonConstantAtts
	 * @param constantAtts
	 */
	private DataObject buildCallbackComponentDescription(Attributes nonConstantAtts, Attributes constantAtts) {
		DataObjects cdv = new DataObjects();

		cdv.addElement(new DataObject(Discoverer.ID, getId()));

		DataObjects v = new DataObjects();
		v.addElement(constantAtts.toDataObject());
		cdv.addElement(new DataObject(Discoverer.CONSTANT_ATTRIBUTE_NAME_VALUES, v));

		v = new DataObjects();
		v.addElement(nonConstantAtts.toDataObject());
		cdv.addElement(new DataObject(Discoverer.NON_CONSTANT_ATTRIBUTE_NAME_VALUES, v));

		return new DataObject(Discoverer.REGISTERER, cdv);
	}

	/**
	 * This private method checks that the given data falls within the given conditions.
	 *
	 * @param atts AttributeNameValues containing data to validate
	 * @param conditions Conditions to validate against
	 * @return whether the data falls within the given conditions
	 */
	private boolean dataValid(Callback callback, AbstractQueryItem<?,?> condition) {
		if (condition == null) { return true; }

		Boolean match = condition.match(getComponentDescription());
		if (match == null) { return false; }
		else { return match; }
	}

	/**
	 * This method should be overridden to process the results of subscription callbacks.
	 *
	 * @param result DataObject containing the result
	 * @param sub Subscriber that returned this reply
	 */
	protected void processCallbackReply (DataObject result, Subscriber sub) {
	}

	/**
	 * This method adds a subscriber to this object.  It calls
	 * Subscribers.addSubscriber() if it can add the subscriber.  It returns a
	 * DataObject containing the reply information, including any error information.
	 *
	 * Agathe: change the Subscriber calls to use AbstractSubscriber
	 *
	 * @param sub DataObject containing the subscription information
	 * @param error String containing the incoming error value
	 * @return DataObject with the reply to the subscription request
	 * @see context.arch.subscriber.Subscribers#addSubscriber(String,String,int,String,String,Conditions,Attributes)
	 */
	public DataObject addSubscriber(DataObject sub, String error) {
		DataObjects v = new DataObjects();
		Error err = new Error(error);
		debugprintln(DEBUG, "Widget <addSubscriber> " + sub);

		if (err.getError() == null) {
//			Subscriber subscriber = (Subscriber) AbstractSubscriber.fromDataObject(sub); // seemed obsolete --Brian
			Subscriber subscriber = new Subscriber(sub);
			debugprintln(DEBUG, "Widget <addSubscriber> has created sub=" + subscriber);
			debugprintln(DEBUG, "\nWidget <addSubscriber> Subscription callback=" + subscriber.getSubscriptionCallback());
			//debugprintln("Widget <addSubscriber> Callbacks =" + this.callbacks);

			
			// Test if this widget may handle the specified attributes and conditions
			//TODO: add this validation code back in after query system updated --alann
			//      else if (!canHandle(subscriber.getAttributes(),subscriber.getCondition()) {
			//        debugprintln(DEBUG, "Widget <addSubscriber> cannot handle att");
			//        err.setError(Error.INVALID_ATTRIBUTE_ERROR);
			//      }
			
			// Test if this widget may handle the specified callback
			if (!isCallback(subscriber.getSubscriptionCallback())) {
				debugprintln(DEBUG, "Widget <addSubscriber> doesn't know the callback");
				err.setError(Error.INVALID_CALLBACK_ERROR);
			}

			// Add the subscriber
			else {
				debugprintln(DEBUG, "Widget <addSubscriber> has added it");
				subscribers.add(subscriber);
				debugprintln(DEBUG, "Widget <addSubscriber> The sub is now " + subscriber);

				v.addElement(new DataObject(AbstractSubscriber.SUBSCRIBER_ID, subscriber.getSubscriptionId()));
				err.setError(Error.NO_ERROR);
			}
		}

		// Send an update to the discoverer
		if (discoverer != null)
			discovererUpdate ();

		v.addElement(err.toDataObject());
		debugprintln(DEBUG, "Widget <addSubscriber> data to send back " + v);
		return new DataObject(Subscriber.SUBSCRIPTION_REPLY, v);
	}

	/**
	 * This method removes a subscriber to this object.  It calls
	 * Subscribers.removeSubscriber() if it can remove the subscriber.  It returns a
	 * DataObject containing the reply information, including any error information.
	 *
	 * @param sub DataObject containing the subscription information
	 * @param error String containing the incoming error value
	 * @return DataObject with the reply to the subscription request
	 * @see context.arch.subscriber.Subscribers#removeSubscriber(String, String, String, String, String)
	 */
	public DataObject removeSubscriber(DataObject sub, String error) {
		DataObjects v = new DataObjects();
		Error err = new Error(error);
		if (err.getError() == null) {
			DataObject dobj = sub.getDataObject(AbstractSubscriber.SUBSCRIBER_ID);
			
			if (dobj != null) {			
				String subId = dobj.getValue();
				
				if (subId != null) {
					boolean done = subscribers.removeSubscriber(subId);

					if (!done) {
						err.setError(Error.UNKNOWN_SUBSCRIBER_ERROR);
					}
					else {
						v.addElement(new DataObject(Subscriber.SUBSCRIBER_ID, subId));
						err.setError(Error.NO_ERROR);
					}
				}			
			}
		}

		// Send an update to the discoverer - start a thread for that
		DiscovererClient discoClient = new DiscovererClient(this, Discoverer.DISCOVERER_UPDATE,
				getSubscribersDescription(), Discoverer.UPDATE_REPLACE_TYPE);
		discoClient.start();

		v.addElement(err.toDataObject());
		return new DataObject(Subscriber.SUBSCRIPTION_REPLY, v);
	}

	/**
	 * This method retrieves data from the widget's storage.  It returns a
	 * DataObject containing the retrieved data information, including any error information.
	 *
	 * @param data DataObject containing the subscription information
	 * @param error String containing the incoming error value
	 * @return DataObject with the reply to the subscription request
	 */
	protected DataObject retrieveData(DataObject data, String error) {
		debugprintln (DEBUG, "Widget retrieveData data :"+data + "\nerror:" + error);
		DataObjects v = new DataObjects();
		Error err = new Error(error);
		if (err.getError() == null) {
			Retrieval retrieval = new Retrieval(data);

			if (storage == null) {
				err.setError(Error.EMPTY_RESULT_ERROR);
			}
			else {
				RetrievalResults results = storage.retrieveAttributes(retrieval);
				if (results == null) {
					err.setError(Error.INVALID_REQUEST_ERROR);
				}
				else if (results.size() == 0) {
					err.setError(Error.EMPTY_RESULT_ERROR);
				}
				else {
					err.setError(Error.NO_ERROR);
				}
				if (results != null) {
					v.addElement(results.toDataObject());
				}
			}
		}
		v.addElement(err.toDataObject());
		debugprintln(DEBUG, "Widget retrieve data return:"+v.toString());
		return new DataObject(StorageObject.RETRIEVE_DATA_REPLY, v);
	}

	/**
	 * This stub method stores the data in the given DataObject
	 *
	 * @param data Data to store
	 * @see context.arch.storage.StorageObject#store(DataObject)
	 */
	protected void store(DataObject data) {
		debugprintln(DEBUG, "Widget <store (DO)>");
		if (storage != null) {
			storage.store(data);
		}
	}

	/**
	 * This stub method stores the data in the given AttributeNameValues object
	 *
	 * @param data Data to store
	 * @see context.arch.storage.StorageObject#store(AttributeNameValues)
	 */
	protected void store(Attributes data) {
		debugprintln(DEBUG, "Widget <store(ANVS)>");
		if (storage != null) {
			storage.store(data);
		}
	}

	/**
	 * This method creates a thread that retrieves a global time clock and determines
	 * the offset between the local clock and the global clock. It checks this
	 *
	 * @return the offset between the global and local clocks
	 * @see context.arch.widget.OffsetThread
	 */
	protected void getNewOffset() {
		OffsetThread offset = new OffsetThread();
		CurrentOffset = offset.getCurrentOffset();
		offset = new OffsetThread(120);
	}

	/**
	 * This method retrieves the offset between the local clock and a global clock
	 * with no delay.
	 *
	 * @return the offset between the global and local clocks
	 * @see context.arch.widget.OffsetThread
	 */
	protected long getNewOffsetNoDelay() {
		OffsetThread offset = new OffsetThread();
		return offset.getCurrentOffset();
	}

	/**
	 * This method returns the current time to use as a timestamp
	 *
	 * @return the current time, corrected using a global clock offset
	 */
	protected Long getCurrentTime() {
		long temp = new Date().getTime();
		return new Long(temp + CurrentOffset);
	}

	/**
	 * This method builds the widget description which contains the constant and
	 * non constant attributes, the callbacks, the services, the subscribers
	 * This method overloads the BaseObject's getUserDescription method
	 *
	 * @return DataObject The description of the widget
	 * @see #getWidgetDescription()
	 * @author Agathe
	 */
	public DataObject getUserDescription(){
		// TO complete !!!!!
		DataObject result;

		// Get the non constant attributes
		DataObject doAtt_ = nonConstantAttributes.toDataObject();
		DataObjects vAtt = new DataObjects();
		vAtt.addElement(doAtt_);
		DataObject doAtt = new DataObject(Discoverer.NON_CONSTANT_ATTRIBUTE_NAME_VALUES, vAtt);

		// Get the constant attributes
		DataObject doCstAtt_ = constantAttributes.toDataObject();
		DataObjects vCstAtt = new DataObjects();
		vCstAtt.addElement(doCstAtt_);
		DataObject doCstAtt = new DataObject(Discoverer.CONSTANT_ATTRIBUTE_NAME_VALUES, vCstAtt);

		// Get the callbacks
		DataObject doCallbacks_ = callbacks.toDataObject();
		DataObjects vCall_ = doCallbacks_.getChildren();
		DataObjects vCall = new DataObjects();
		if ( ! vCall_.isEmpty()) {
			Enumeration<DataObject> eCall_ = vCall_.elements();
			DataObject element;
			while ( eCall_.hasMoreElements()){
				element = (DataObject) eCall_.nextElement();
				vCall.addElement(element.getDataObject(Callback.CALLBACK_NAME));
			}
		}
		DataObject doCallbacks = new DataObject(Discoverer.WIDGET_CALLBACKS, vCall);

		// Get the services
		DataObject doServices_ = services.toDataObject();
		DataObjects vSer_ = doServices_.getChildren();
		DataObjects vSer = new DataObjects();
		if ( ! vSer_.isEmpty()) {
			Enumeration<DataObject> eSer_ = vSer_.elements();
			DataObject element;
			while ( eSer_.hasMoreElements()){
				element = (DataObject) eSer_.nextElement();
				vSer.addElement(element.getDataObject(ServiceDescription.SERVICE_NAME));
			}
		}
		DataObject doServices = new DataObject(Discoverer.WIDGET_SERVICES, vSer);

		//Get the subscribers
		DataObject doSubs = getSubscribersDescription();

		DataObjects v = new DataObjects();
		v.addElement(doAtt);
		v.addElement(doCstAtt);
		v.addElement(doCallbacks);
		v.addElement(doServices);
		v.addElement(doSubs);

		// Get getWidgetDescription
		DataObject doDescrip = getWidgetDescription();
		if (doDescrip != null) {
			for (DataObject temp : doDescrip.getChildren()){
				v.addElement(temp);
			}
		}

		result = new DataObject(Discoverer.TEMP_DEST, v);

		return result;
	}

	/**
	 * This method returns a DataObject containig the list of subscribers
	 *
	 * @return DataObject The list of subscribers
	 * @author Agathe
	 */
	public DataObject getSubscribersDescription(){
		// Get the subscribers
		DataObjects subs = new DataObjects();

		// need to lock subscribers, as some other thread may modify it meanwhile
		synchronized (subscribers) {
			for (AbstractSubscriber sub : subscribers.values()) {
				subs.addElement(new DataObject(AbstractSubscriber.SUBSCRIBER_ID, sub.getBaseObjectId()));
			}
		}

		return new DataObject(Subscribers.SUBSCRIBERS, subs);
	}

	/**
	 * This method returns the desciption specific to a widget.
	 * By default, it returns the type of the object that is 'WIDGET' type
	 * This method should be overloaded.
	 *
	 * @return DataObject The DataObject containing the description of the widget
	 * @see #getUserDescription()
	 * @author Agathe
	 */
	public DataObject getWidgetDescription(){
		return null;
	}

	/**
	 * This method is called when the widget is restarted. This method restarts
	 * the subscriptions, that is, the widget creates the subscribers that are
	 * described in its logfile. To each identified subscriber, the widget sends a
	 * PING message to check their liveliness. The URL sent is WIDGET+SUBSCRIBERS+PING.
	 * This PING is done through an independent connection (a thread handles the
	 * communication), so the result of the PING is got in the widget.handleIndependentReply.
	 *
	 * @author Agathe
	 */
	protected void setSubscribers(){
		debugprintln(DEBUG, "\n\nWidget <setSubscribers> ");
		// Get the subscribers retrieved from the log file
		Subscribers notCheckedSubs = new Subscribers(this, this.getId());
		// Check them to be sure they are still alive
		int numSubsToCheck = 0;
		
		for (AbstractSubscriber temp : notCheckedSubs.values()) {
			subscribers = notCheckedSubs;
			debugprintln(DEBUG, "widget <setSubs> send a PING for " + temp);
			
			IndependentCommunication indComm =
				new IndependentCommunication(
						new RequestObject(null, null, temp.getSubscriberHostName (),temp.getSubscriberPort (), temp.getSubscriptionId ()),
						true);
			indComm.setObjectToStore (temp);
			indComm.setSenderClassId (Widget.WIDGET_TYPE+Subscribers.SUBSCRIBERS+BaseObject.PING);
			
			pingComponent(indComm);
			numSubsToCheck++;
		}
		subscribers = notCheckedSubs;
		debugprintln(DEBUG, "End setSubscriber # subs to check= " + numSubsToCheck);
	}

	/**
	 * This method overrides the handleIndependentReply defined in the BaseObject
	 * class.
	 *
	 * This method handles the reply to : <ul>
	 *  <li>PING messages sent to subscriber to check out if they are still alive.
	 *  This test is done when the widget is restarted and restarts the subscriptions
	 *  based on its logfile. The subscribers are checked
	 *  The url is WIDGET+SUBSCRIBERS+PING
	 * <li> Notification messages sent to the subscribers. If the communication failed
	 * (due to connection errors) the subscriber is removed from the widget and the
	 * widget updates the discoverer
	 * The url is WIDGET+SUBSCRIPTION_CALLBACK
	 *
	 * </ul>
	 * If the url is not recognized, the message is sent to the
	 * BaseObject.handlIndependentReply
	 *
	 * @param independentCommunication The object sent back by the thread
	 * @author Agathe
	 */

	public void handleIndependentReply(IndependentCommunication independentCommunication){
		debugprintln(DEBUG, "Widget <handleIndependentReply>");

		// Reply from the subscribers that are checked with a PING : WIDGET+SUBSCRIBERS+PING
		if (independentCommunication != null){
			String senderId= independentCommunication.getSenderClassId ();

			// The reply of a message sent to ping a subscriber from the widget
			if (senderId != null && senderId.equals(Widget.WIDGET_TYPE+Subscribers.SUBSCRIBERS+BaseObject.PING)) {
				independentCommunication.decodeReply (this);
				DataObject replyContent = independentCommunication.getDecodedReply ();
				debugprintln(DEBUG, "\nWidget <handleIndependentReply> Reply=" + replyContent + " - exceptions " + independentCommunication.getExceptions ());
				if (independentCommunication.getRequest ().getUrl ().equals (BaseObject.PING)){
					if ( ! independentCommunication.getExceptions ().isEmpty () // There are exceptions
							|| replyContent == null) {
						debugprintln(DEBUG, "Widget <handleIndependentReply> removes subscriber");
						subscribers.removeSubscriber ((AbstractSubscriber)independentCommunication.getObjectToStore ());
						this.discovererUpdate ();
					}
				}
			}

			// The reply comes from a subscription notification
			else if (senderId != null && senderId.equals(Widget.WIDGET_TYPE+Subscriber.SUBSCRIPTION_CALLBACK)){
				if ( ! independentCommunication.getExceptions ().isEmpty ()){
					// If there are exception, remove the subscriber corresponding to that notification
					Subscriber sub = (Subscriber) independentCommunication.getObjectToStore ();
					debugprintln (DEBUG, "IndependentCommunication ERROR - remove the subscriber=" + sub);
					subscribers.removeSubscriber (sub);
					this.discovererUpdate ();
				}
			}

			// Else, asks to the super class
			else{
				super.handleIndependentReply (independentCommunication);
			}
		}
	}

	/** This method overrides the BaseObject setId(String) method so that
	 * the baseobject id specified in the subscribers object be also updated.
	 * @param id The id of this ctk component
	 */
	public void setId(String id) {
		super.setId(id);
		if (this.subscribers != null) {
			this.subscribers.setBaseObjectId(id);
		}
	}
	
	/* --------------------------------------------------------------------------------
	 * Shut down code
	 * -------------------------------------------------------------------------------- */

	public void initShutdownHook() {
		ShutdownHook shutdownHook = new ShutdownHook();
		Runtime.getRuntime().addShutdownHook(shutdownHook);

	}
	public void shutdown() {
		System.out.println(this.getId() + " shutting down");
	}
	class ShutdownHook extends Thread {
		public void run() {
			shutdown();
		}
	}
	
	/**
	 * By default, this returns null.
	 * Subclasses should implement it to return relevant definitions for each attribute.
	 * @param attributeTag
	 * @return
	 */
	public static String getAttributeDefinition(String attributeTag) {
		return null;
	}

}