/*
 * Discoverer.java
 *
 * Created on 22 mars 2001, 10:40
 */

package context.arch.discoverer;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import context.arch.comm.DataObject;
import context.arch.comm.DataObjects;
import context.arch.comm.RequestObject;
import context.arch.comm.clients.IndependentCommunication;
import context.arch.comm.language.EncodeException;
import context.arch.comm.language.InvalidEncoderException;
import context.arch.discoverer.lease.Lease;
import context.arch.discoverer.query.AbstractQueryItem;
import context.arch.service.Services;
import context.arch.storage.Attribute;
import context.arch.storage.Attributes;
import context.arch.subscriber.AbstractSubscriber;
import context.arch.subscriber.Callback;
import context.arch.subscriber.Callbacks;
import context.arch.subscriber.DiscovererSubscriber;
import context.arch.util.Error;
import context.arch.widget.Widget;



/**
 * This class allows to add a discovery system to the context toolkit. 
 * <p>The discoverer is found by 
 * context components thanks to multicast communications. 
 * The discoverer allow them to register to it (to send their complete description),
 * to query to it (to retrieve the description of components that
 * fit several characteristics), to subscribe to notification (the subscriber wants to be notified when the
 * discoverer registers a new component that fit some characteristics), to lease it (when a component
 * registers to the discoverer, it registers for a given period that is identified by a lease), to unregister
 * (when the component lease ends or when the components wants).
 *
 *<P>When a context component is created, it does not know the other components. And it can
 * subscribes to another component only if it knows the component's name, port and hostname.
 * The discoverer registers all context components when they are created, it stores for each of them
 * their description. So that a component is able to do a request to the discoverer to get one or more
 * context components that fit some characteristics (for example : I want all widgets that has information
 * about Anind in the CRB)
 *
 * @see context.arch.discoverer.SearchEngine
 * @see context.arch.discoverer.ComponentDescription
 * @see context.arch.discoverer.DiscovererDescription
 * @see edu.cmu.intelligibility.query.query.Query
 * @see context.arch.discoverer.lease.Lease
 * @see context.arch.discoverer.lease.LeasesWatcher
 * 
 * @author Agathe
 * @author Brian Y. Lim
 * 
 */
public class Discoverer extends Widget {
	
	private static final Logger LOGGER = Logger.getLogger(Discoverer.class.getName());
	static {LOGGER.setLevel(Level.WARNING);} // this should be set in a configuration file

	/**
	 * Debug flag. Set to true to see debug messages.
	 */
	public static final boolean DEBUG = false;

	/**
	 * Default port for discoverer to use
	 */
	public static final int DEFAULT_PORT = 6000;

	/**
	 * Classname tag
	 */
	public static final String CLASSNAME = "Discoverer";

	/**
	 * Message tag used by components to send a multicast message to lookup a discoverer
	 */
	public static final String LOOKUP_DISCOVERER = "lookupDiscoverer";

	/**
	 * Tag used in LOOKUP_DISCOVERER to contain the description of the caller component
	 */
	public static final String CALLER = "caller";

	/**
	 * Tag used in LOOKUP_DISCOVERER inside CALLER tags for the component id
	 * Also used in LOOKUP_DISCOVERER_REPLY by discoverers
	 */
	public static final String CALLER_ID = "callerId";

	/**
	 * Tag used in LOOKUP_DISCOVERER inside CALLER tags for the component type 
	 * (baseobject(applications), widget, server, interpreter)
	 */
	public static final String TYPE = "type";

	/**
	 * Tag used in LOOKUP_DISCOVERER inside CALLER tags for the component hostname
	 * Also used in LOOKUP_DISCOVERER_REPLY, DISCOVERER_REGISTRATION
	 */
	public static final String HOSTNAME = "hostname";

	/**
	 * Tag used in LOOKUP_DISCOVERER inside CALLER tags for the component  IP address
	 */
	public static final String HOSTADDRESS = "hostAddress";

	/**
	 * Tag used in LOOKUP_DISCOVERER inside CALLER tags for the component port
	 * Also used in LOOKUP_DISCOVERER_REPLY, DISCOVERER_REGISTRATION
	 */
	public static final String PORT = "port";

	/**
	 * Message tag used by discoverers to reply to a lookup message 
	 */
	public static final String LOOKUP_DISCOVERER_REPLY = "lookupDiscovererReply";

	/**
	 * Tag used in LOOKUP_DISCOVERER_REPLY to contain the discoverer description
	 */
	public static final String DISCOVERER = "discoverer";

	/**
	 *
	 */
	public static final String DISCOVERER_TYPE = "discoverer";

	/**
	 * Tag used in LOOKUP_DISCOVERER_REPLY for the discoverer id
	 * Also used in DISCOVERER_REGISTRATION
	 */
	public static final String DISCOVERER_ID = "discovererId";

//	/**
//	 * Tag used in DISCOVERER_QUERY_REPLY to give the resulting component
//	 * id
//	 */
//	public static final String COMPONENT_ID = "componentId";
	// refactored to delete this tag, since it is essentially tags the same value (id) as #BaseObject.ID, and wasn't used much --Brian

	/**
	 * Message tag used by discoverers to reply to a lookup message 
	 */
	public static final String LOOKUP_DISCOVERER_OK = "lookupDiscovererOk";

	/**
	 * Message tag used by components to send to the discoverer their description
	 */
	public static final String DISCOVERER_REGISTRATION = "discovererRegistration";

	/**
	 * Message tag used by components to unregister from the discoverer
	 */
	public static final String DISCOVERER_UNREGISTRATION = "discovererUnregistration";

	/**
	 * Message tag used by the discoverer to reply to a DISCOVERER_UNREGISTRATION message
	 */
	public static final String DISCOVERER_UNREGISTRATION_REPLY = "discovererUnregistrationReply";

	/**
	 * Message tag used by components to send to the discoverer an update
	 */
	public static final String DISCOVERER_UPDATE = "discovererUpdate";

	/**
	 * Message tag used by components to send to the discoverer a subscription
	 */
	public static final String DISCOVERER_SUBSCRIBE = "discovererSubscribe";

	/**
	 * Message tag used by components to specify the update's type
	 */
	public static final String UPDATE_TYPE = "updateType";

	/**
	 * Tag used in UPDATE_TYPE message to specify the method used to update information.
	 * Add type is used to add information.
	 */
	public static final String UPDATE_ADD_TYPE = "updateAddType";

	/**
	 * Tag used in UPDATE_TYPE message to specify the method used to update information
	 * Replace type is used to remove old information before adding new one.
	 */
	public static final String UPDATE_REPLACE_TYPE = "updateReplaceType";

	/**
	 * Tag used in DISCOVERER_REGISTRATION to contain the description of the
	 * component registring
	 */
	public static final String REGISTERER = "registerer";

//	/**
//	 * Tag used in DISCOVERER_REGISTRATION for the component id
//	 */
//	public static final String REGISTERER_ID = "registererId";
	// refactored to delete this tag, since it is essentially tags the same value (id) as #BaseObject.ID, and wasn't used much --Brian

	/**
	 * Tag used in DISCOVERER_REGISTRATION for component classname
	 * Tag also used in DISCOVERER_QUERY
	 */
	public static final String COMPONENT_CLASSNAME = "componentClassname";

	/**
	 * Tag used in DISCOVERER_REGISTRATION to contain the constant attributes of
	 * the component
	 * Tag also used in DISCOVERER_QUERY
	 *
	 * This tag is also used in QueryElement to query the discoverer. Then it is used to match
	 * the name AND the value of a constant attribute. The match will be successful if 
	 * the combination of the name&value exists for a component description
	 *
	 * @see context.arch.discoverer.query.QueryElement
	 * @see edu.cmu.intelligibility.query.query.Query
	 */
	public static final String CONSTANT_ATTRIBUTE_NAME_VALUES = "CANVS";

	/**
	 * Tag used in DISCOVERER_REGISTRATION to contain the non constant attributes of
	 * the component
	 * Tag also used in DISCOVERER_QUERY
	 */
	public static final String NON_CONSTANT_ATTRIBUTE_NAME_VALUES = "NCANVS";

	/**
	 * Tag used in QueryElement to query the discoverer. It is used to match just the name of
	 * a constant attribute
	 *
	 * @see context.arch.discoverer.query.QueryElement
	 * @see edu.cmu.intelligibility.query.query.Query
	 */
	public static final String CONSTANT_ATTRIBUTE_NAME = "CAN";

	/**
	 * Tag used in QueryElement to query the discoverer. It is used to match just the name of
	 * a non constant attribute
	 *
	 * @see context.arch.discoverer.query.QueryElement
	 * @see edu.cmu.intelligibility.query.query.Query
	 */
	public static final String NON_CONSTANT_ATTRIBUTE_NAME = "NCAN";

	/**
	 * Tag used in QueryElement to query the discoverer. It is used to match just the value
	 * of a constant attribute
	 *
	 * @see context.arch.discoverer.query.QueryElement
	 * @see edu.cmu.intelligibility.query.query.Query
	 */
	public static final String CONSTANT_ATTRIBUTE_VALUE = "CAV";

	/**
	 * Tag used in DISCOVERER_REGISTRATION to contain the server constant attributes of
	 * the component
	 */
	public static final String SERVER_CONSTANT_ATTRIBUTES = "serverConstantAttributes";

	/**
	 * Tag used in DISCOVERER_REGISTRATION to contain the server non constant attributes of
	 * the component
	 */
	public static final String SERVER_NON_CONSTANT_ATTRIBUTES = "serverNonConstantAttributes";

	/**
	 * Tag used in DISCOVERER_REGISTRATION to contain the incoming attribute name
	 * values
	 * Tag also used in DISCOVERER_QUERY
	 */
	public static final String INCOMING_ATTRIBUTE_NAME_VALUES = "InANVS";

	/**
	 * Tag used in DISCOVERER_REGISTRATION to contain the outgoing attribute name
	 * values
	 * Tag also used in DISCOVERER_QUERY
	 */
	public static final String OUTGOING_ATTRIBUTE_NAME_VALUES = "OutANVS";

	/**
	 * Tag used in DISCOVERER_REGISTRATION to contain the widget callback names
	 * Tag also used in DISCOVERER_QUERY
	 */
	public static final String WIDGET_CALLBACKS = "widgetCallbacks";

	/**
	 * Tag used in DISCOVERER_REGISTRATION to contain the server callback names
	 */
	public static final String SERVER_CALLBACKS = "serverCallbacks";

	/**
	 * Tag used in DISCOVERER_REGISTRATION to contain the widget service names
	 * Tag also used in DISCOVERER_QUERY
	 */
	public static final String WIDGET_SERVICES = "widgetServices";

	/**
	 * Tag used in DISCOVERER_REGISTRATION to contain the server service names
	 */
	public static final String SERVER_SERVICES = "serverServices";

	/**
	 * Tag used by communicationsObject to retrieve the destination address
	 */
	public static final String TEMP_DEST = "tempDest";

	/**
	 * Tag used for query messages to the discoverer
	 */
	public static final String DISCOVERER_QUERY = "discovererQuery";

	/**
	 * Tag used in DISCOVERER_QUERY messages to the discoverer
	 */
	public static final String DISCOVERER_QUERY_CONTENT = "discovererQueryContent";

	/**
	 * Tag used to determine level of detail of component description in 
	 * response to subscribers
	 */
	public static final String DISCOVERER_DESCRIPTION_FULL_RESPONSE = "discovererFullResponse";

	/**
	 * Tag used for query reply messages to the discoverer
	 */
	public static final String DISCOVERER_QUERY_REPLY = "discovererQueryReply";

	/**
	 * Tag used in DISCOVERER_QUERY_REPLY messages to the discoverer
	 */
	public static final String DISCOVERER_QUERY_REPLY_CONTENTS = "discovererQueryReplyContents";

	/**
	 * Tag used in DISCOVERER_QUERY_REPLY_CONTENTS messages
	 */
	public static final String DISCOVERER_QUERY_REPLY_CONTENT = "discovererQueryReplyContent";

	/**
	 * Tag used in DISCOVERER_QUERY_CONTENT to indicate the type of query reply 
	 */
	public static final String DISCOVERER_QUERY_TYPE = "type";

	/**
	 * Tag used in the DISCOVERER_QUERY_REPLY_CONTENT to give
	 * the query id
	 */
	public static final String QUERY_ID = "queryId";

	/**
	 * Tag used inside the DISCOVERER_QUERY_REPLY_CONTENT to
	 * give the order of the response
	 */
	public static final String QUERY_ORDER = "queryOrder";

	/**
	 * Tag used inside the DISCOVERER_QUERY_REPLY_CONTENT to give
	 * the total number of answers to the query
	 */
	public static final String QUERY_NUM_ANSWERS = "numAnswers";

	/**
	 * Type of query reply : returns the first element
	 */
	public static final String FIRST_RESULT = "first";

	/**
	 * Type of query reply : returns no query id and all results (with a maximum
	 * set by 
	 */
	public static final String ALL_RESULTS = "all";

	/**
	 * Default number of responses sent in replyt to a DISOVERER_QUERY message
	 * with a ALL_RESULTS type
	 */
	public static final int DEFAULT_MAX_RESULTS = 10;

	/**
	 * Type of query reply : returns the next result
	 */
	public static final String NEXT_RESULT = "next";

	/**
	 * Tag used if DISCOVERER_QUERY to set the maximum number of responses wanted
	 */
	public static final String MAXIMUM_RESULTS = "maximumResults";

	/**
	 * Type of matching for the query
	 */
	public static final String IGNORE_CASE = "ingnoreCase";

	/**
	 * Type of matching for the query
	 */
	public static final String CASE_SENSITIVE = "caseSensitive";

	/**
	 * Attribute tag used in the DISCOVERER_QUERY_CONTENT
	 */
	public static final String PRIORITY = "priority";

	/**
	 * First or weakest  priority for the query element
	 */
	public static final int PRIORITY_1 = 1;

	/**
	 * Second or middle priority for the query element
	 */
	public static final int PRIORITY_2= 2;

	/**
	 * Last or stronger priority for the query element
	 */
	public static final int PRIORITY_3 = 3;

	/** 
	 * Component type : for application
	 */
	public static final String APPLICATION = "application";

	/** 
	 * Component type : for widget
	 */
	public static final String WIDGET = "widget";

	/** 
	 * Component type : for servers
	 */
	public static final String SERVER = "server";

	/** 
	 * Component type : for interpreter
	 */
	public static final String INTERPRETER = "interpreter";

	/**
	 * Separator used to separate fields (as for constant attribute name and value)
	 */
	public static final String FIELD_SEPARATOR = "+";

	/**
	 * This tag is used in error object for DISCOVERER_UNREGISTRATION_REPLY messages
	 * when the context component that want to be unregistered is not found.
	 */
	public static final String ERROR_REGISTRATION_NOT_FOUND = "errorRegistrationNotFound";

	/**
	 * This tag is used when a NEXT_RESULT query is received and the queryId is 
	 * not reckognized
	 */
	public static final String ERROR_QUERY_NOT_FOUND = "errorQueryNotFound";

	/**
	 * This tag is used when there is no response to the received query
	 */
	public static final String ERROR_EMPTY_QUERY = "errorEmptyQuery"; 

	/**
	 * This tag is the callback name used to notify subscriber of a new component
	 */
	public static final String NEW_COMPONENT = "newComponent";


	/**
	 * 
	 */
	protected DiscovererMediator mediator;

	/**
	 * Constructor that sets up internal variables for maintaining
	 * the list of discoverer attributes, callbacks, and services and setting up
	 * the BaseObject info.
	 *
	 * @param clientClass Class to use for client communications
	 * @param serverClass Class to use for server communications
	 * @param serverPort Port to use for server communications
	 * @param encoderClass Class to use for communications encoding
	 * @param decoderClass Class to use for communications decoding
	 * @param storageClass Class to use for storage
	 * @param id String to use for discoverer id and persistent storage
	 * @see context.arch.storage.StorageObject
	 */
	public Discoverer(String clientClass, String serverClass, int serverPort, 
			String encoderClass, String decoderClass, 
			String storageClass) {
		super(clientClass, serverClass, serverPort, encoderClass, decoderClass,
				storageClass, Discoverer.getId(Discoverer.CLASSNAME,serverPort), CLASSNAME);
		initFull();
	}    

	/**
	 * Constructor that sets up internal variables for maintaining
	 * the list of discoverer attributes, callbacks, and services and setting up
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
	 * @param id String to use for discoverer id and persistent storage
	 * @see context.arch.storage.StorageObject
	 */
	public Discoverer(String clientClass, String serverClass, int serverPort, String encoderClass,
			String decoderClass, boolean storageFlag) {          
		super(clientClass, serverClass, serverPort, encoderClass, decoderClass, 
				storageFlag, getId(CLASSNAME,serverPort), CLASSNAME);
	}    

	/**
	 * Constructor that sets up internal variables for maintaining
	 * the list of discoverer attributes, callbacks and services.  It takes a port
	 * number as a parameter to indicate which port to listen for
	 * messages/connections, the id to use for the widget, and a flag to indicate
	 * whether storage functionality should be turned on or off.
	 *
	 * @param port Port to listen to for incoming messages
	 * @param id Discoverer id
	 * @param storageFlag Boolean flag to indicate whether storage should be turned on
	 */
	public Discoverer(int port, boolean storageFlag) {
		this(null, null, port, null, null, storageFlag);
	}

	/**
	 * Constructor that sets up internal variables for maintaining
	 * the list of discoverer attributes, callbacks and services.  It takes a port
	 * number as a parameter to indicate which port to listen for
	 * messages/connections.
	 *
	 * @param port Port to listen to for incoming messages
	 * @param id Discoverer id
	 */
	public Discoverer(int port) {
		this(null, null, port, null, null, null);
	}

	/**
	 * Constructor that sets up internal variables for maintaining
	 * the list of discoverer attributes, callbacks and services.  It takes 
	 * the id to use for the widget, and a flag to indicate
	 * whether storage functionality should be turned on or off.
	 *
	 * @param id Discoverer id
	 * @param storageFlag Boolean flag to indicate whether storage should be turned on
	 */
	public Discoverer(String id, boolean storageFlag) {
		this(null, null, -1, null, null, storageFlag);
	}

	/**
	 * Constructor that sets up internal variables for maintaining
	 * the list of widget attributes, callbacks and services.  It takes the 
	 * widget id as a parameter
	 *
	 * @param id ID of the discoverer
	 */
	public Discoverer() {
		this(null, null, -1, null, null, null);
	}

	@Override
	protected void initFull() {
		super.initFull();
		
		// Set the SearchEngine
		mediator = new DiscovererMediator(this, true);

		this.setLease (new Lease(10000));
		this.discoverer = new DiscovererDescription(this.getId (), this.getHostAddress (), this.getPort());
		discovererRegistration(new Lease(10000));
	}
	
	@Override
	protected void init() {
		/*
		 * Non-constant attributes
		 * Sets the attributes for the discoverer: they specify the information the
		 * discoverer is storing about the registered context components.
		 */
		addAttribute(Attribute.instance(ComponentDescription.ID_ELEMENT, String.class));
		addAttribute(Attribute.instance(ComponentDescription.CLASSNAME_ELEMENT, String.class));
		addAttribute(Attribute.instance(ComponentDescription.HOSTNAME_ELEMENT, String.class));
//		addAttribute(Attribute.instance(ComponentDescription.HOSTADDRESS_ELEMENT, String.class));
		addAttribute(Attribute.instance(ComponentDescription.PORT_ELEMENT, String.class));
		addAttribute(Attribute.instance(ComponentDescription.CONST_ATT_ELEMENT, String.class));
		addAttribute(Attribute.instance(ComponentDescription.NON_CONST_ATT_ELEMENT, String.class));
		addAttribute(Attribute.instance(ComponentDescription.NON_CONST_ATT_NAME_ELEMENT, String.class));
		addAttribute(Attribute.instance(ComponentDescription.CALLBACK_ELEMENT, String.class));
		addAttribute(Attribute.instance(ComponentDescription.SUBSCRIBER_ELEMENT, String.class));
		
		// constant attributes
		
	}

	/**
	 * Sets the callbacks for the discoverer
	 *
	 * @return Callbacks The callbacks of the discoverer
	 */
	protected Callbacks initCallbacks() {
		Callbacks calls = new Callbacks();
		Attributes atts = new Attributes();
		
		atts.putAll(nonConstantAttributes);
		atts.putAll(constantAttributes);
		
		calls.addCallback(NEW_COMPONENT, atts);
		return calls;
	}

	/**
	 * Sets the services for the discoverer
	 *
	 * @return Services The services provides by the discoverer
	 */
	protected Services initServices(){
		return new Services();
	}

	/**
	 * This abstract method is called when the discoverer wants to get the latest generator
	 * info.
	 * 
	 * @return AttributeNameValues containing the latest generator information
	 */
	protected Attributes queryGenerator(){
		return new Attributes();
	}

	/**
	 * This method is meant to handle any internal methods that the baseObject doesn't
	 * handle.  In particular, this method handles the common details for query requests,
	 * update and query requests, and version requests that each widget should provide.
	 * If the method is not one of these queries, then it calls runWidgetMethod which each widget
	 * should provide.
	 *
	 * @param dataObject DataObject containing the method to run and parameters
	 * @param error Error object
	 * @return DataObject containing the results of the executed method
	 * @see #LOOKUP_DISCOVERER
	 * @see #DISCOVERER_REGISTRATION
	 * @see #DISCOVERER_UNREGISTRATION
	 * @see #DISCOVERER_UPDATE
	 * @see #DISCOVERER_QUERY
	 * @see #DISCOVERER_SUBSCRIBE
	 */

	@Override
	protected DataObject runWidgetMethod(DataObject dataObject, String error){
		DataObject data = dataObject;
		String methodType = data.getName();

//		System.out.println("BaseObject.runMethod methodType = " + methodType);
//		System.out.println("BaseObject.runMethod data = " + data);
		
		if (methodType.equals(LOOKUP_DISCOVERER)) {
			LOGGER.info("Discoverer: in lookup_discoverer");
			return getDiscovererDescription(data);
		}
		else if (methodType.equals(DISCOVERER_REGISTRATION)) {
			return componentRegistration(data);
		}
		else if (methodType.equals(DISCOVERER_UNREGISTRATION)) {
			return componentUnregistration(data);
		}
		else if (methodType.equals(DISCOVERER_UPDATE)) {
			return componentUpdate(data);
		}
		else if (methodType.equals(DISCOVERER_QUERY)) {
			return handleQuery(data);
		}
		else if (methodType.equals (DISCOVERER_SUBSCRIBE)){
			return componentSubscription(data);
		}
		else {
			return runDiscovererMethod(data, error);
		}
	}

	/**
	 * This method should be overriden by inheriting classes to handle other
	 * messages.
	 *  
	 * @param dataObject The data object containing the message
	 * @param error The Error object
	 * @return DataObject The result
	 */
	protected DataObject runDiscovererMethod(DataObject dataObject, String error){
		return new DataObject();
	}

	/**
	 * ?? TO DO : add errors control
	 *
	 * This method returns the DataObject replying to the LOOKUP_DISCOVERER
	 * message from a component.
	 * The reply contains the description of the discoverer and add the id, address
	 * and port of the component to reply to.
	 *
	 * @param data The DataObject containing the identification of the caller component
	 * @return DataObject It contains the LOOKUP_DISCOVERER_REPLY message
	 * @see context.arch.discoverer.DiscovererDescription
	 */
	public DataObject getDiscovererDescription(DataObject data){
		LOGGER.info("Discoverer getDiscovererDescription");
		DataObject result = null;

		// Get component id
		DataObject caller = data.getDataObject(CALLER);
		DataObject callerId = data.getDataObject(CALLER_ID);
//		Vector vId = callerId.getValue();
		String cId = callerId.getValue();
		DataObject id = new DataObject(ID, cId);

		// Set the LOOKUP_DISCOVERER_REPLY tag
		DataObject discovId = new DataObject(DISCOVERER_ID, getId());
		DataObject host = new DataObject(HOSTNAME, getHostAddress());
		DataObject port = new DataObject(PORT, new Integer(communications.getServerPort()).toString());
		DataObjects v1 = new DataObjects();
		v1.addElement(discovId);
		v1.addElement(host);
		v1.addElement(port);
		DataObject disco = new DataObject(DISCOVERER, v1);
		DataObjects v2 = new DataObjects();
		v2.addElement(id);
		v2.addElement(disco);
		DataObject part2 = new DataObject(LOOKUP_DISCOVERER_REPLY, v2);

		//Add a temporary tag useful just for the communicationsObject that has no
		// reply address to send the message to.
		// Because the comm object has just received a datagram packet, and it must
		// open a new tcp connexion, so it needs the hostname port.
		DataObject dAdd = caller.getDataObject(HOSTNAME);
		DataObject dPort = caller.getDataObject(PORT);
		DataObject dId = new DataObject(ID, cId);
		DataObjects tempDest = new DataObjects();
		tempDest.addElement(dAdd);
		tempDest.addElement(dPort);
		tempDest.addElement(dId);
		DataObject part1 = new DataObject(TEMP_DEST, tempDest);

		DataObjects v3 = new DataObjects();
		v3.addElement(part1);
		v3.addElement(part2);

		result = new DataObject (LOOKUP_DISCOVERER_REPLY, v3);
		return result;
	}

	/**
	 * This method handles a DISCOVERER_REGISTRATION message.
	 * It registers a component by storing its description and replies
	 * with an error code.
	 *
	 * @param data The DataObject containing the description on the caller component
	 * @return DataObject An error code
	 * @see context.arch.discoverer.ComponentDescription
	 */
	protected DataObject componentRegistration(DataObject data) {
		LOGGER.info("Discoverer - componentRegistration");
		Error error = new Error(Error.NO_ERROR);
		DataObject component = data.getDataObject(ID);
//		DataObject result = null;

		if (component == null) {
			error.setError(Error.INVALID_ID_ERROR);
		}
		else {
			String resultId = component.getValue();
			if (!resultId.equals(getId())) {
				error.setError(Error.INVALID_ID_ERROR);
			}
			else {
				ComponentDescription comp = 
					ComponentDescription.fromDataObject(data.getDataObject (Discoverer.REGISTERER));
				LOGGER.info("comp to data object " + comp.toDataObject ());
				Lease lease = Lease.dataObjectToLease(data.getDataObject (Lease.LEASE));
				error.setError (mediator.add (comp, lease).getError ()); // Add to the database
				checkSubscribers(new Callback(Discoverer.NEW_COMPONENT, null), comp);
			}
		}
		return error.toDataObject();
	}

	/**
	 * This method allows to send a message to all subscribers whose interests are
	 * for the new registered component.
	 *
	 * @param comp The component newly registered
	 * @return int The number of subscribers a message has been sent to
	 */
	public int checkSubscribers(Callback callback, ComponentDescription comp) {
		LOGGER.info("Discoverer <checkSubscribers>");
		LOGGER.info("The new comp is= " + comp + " \n for callback " + callback );

		AbstractQueryItem<?,?> subQuery;
		int nbCorresponding = 0;
		for (AbstractSubscriber asub : subscribers.values()) {
			DiscovererSubscriber sub = (DiscovererSubscriber) asub;
			LOGGER.info("Sub  id= " + sub.getSubscriptionId ()+" - query= " + sub.getQuery () +  " - sub callback " + sub.getSubscriptionCallback ());

			LOGGER.info(" Test callbacks");
			if (callback.getName ().equals(sub.getSubscriptionCallback ())) {
				LOGGER.info("\ncallback are equal ");
				subQuery = sub.getQuery ();
				
				// Check if the component description corresponds to the subscriber query
				Boolean queryResult = subQuery.match (comp);
				LOGGER.info("Disco result of checkSubs " + queryResult);
				if (queryResult != null && queryResult){ 
					nbCorresponding ++;
					DataObject subid = new DataObject(AbstractSubscriber.SUBSCRIBER_ID, sub.getSubscriptionId ());
					DataObjects v = new DataObjects();
					v.addElement(subid);
					// If the discoSub wants a full description we give it to them, otherwise just the basic summary.
					if (sub.isFullDescriptionResponse()) {
						/**
						 * this is an ugly, ugly hack due to the fact that toDataObject()
						 * calls essentially type their return values without knowing what
						 * they will be used for! we will clean this up at some point by
						 * cleaning up the ComponentDescription code, but for now
						 * side effects are feared, so we stick to the hack.
						 */
						DataObjects vComp = comp.toDataObject().getChildren();
						v.addElement(new DataObject(Discoverer.DISCOVERER_QUERY_REPLY_CONTENT,vComp));
					} else {
						v.addElement(comp.getBasicDataObject());
					}
					DataObject send = new DataObject(DiscovererSubscriber.SUBSCRIPTION_CALLBACK, v);
					String host = sub.getSubscriberHostName ();
					int port = new Integer(sub.getSubscriberPort ()).intValue();
					
					// use independentUserRequest
					try {
						LOGGER.info("Discoverer before independent");
						IndependentCommunication ic = new IndependentCommunication (
								new RequestObject(send, DiscovererSubscriber.SUBSCRIPTION_CALLBACK, host, port));
						independentUserRequest (ic);
						sub.resetErrors();
					} catch (EncodeException ee) {
						LOGGER.severe("Widget sendToSubscribers EncodeException: "+ee);
					} catch (InvalidEncoderException iee) {
						LOGGER.severe("Widget sendToSubscribers InvalidEncoderException: "+iee);
					}
				}
				else {
					LOGGER.info("\nthe query doesn't correspond to the comp");
				}
			}
		}
		LOGGER.info("# of sub that corresponds " + nbCorresponding);
		return nbCorresponding;
	}

	/**
	 * This method is used to unregister a context component from the discoverer.
	 * After the unregistration, the context component description will not
	 * be referenced in the discoverer.
	 *
	 * @param dataObject The content of the DISCOVERER_UNREGISTRATION message
	 * @return DataObject The result of the unregistration
	 * @see #DISCOVERER_UNREGISTRATION
	 * @see #ERROR_COMPONENT_NOT_FOUND
	 */
	protected DataObject componentUnregistration(DataObject dataObject) {
		DataObject data = dataObject;
//		DataObject result;
		Error error = new Error();

		DataObject discoId = data.getDataObject(ID);
		if (discoId == null) {
			error.setError(Error.INVALID_ID_ERROR);
		}
		else {
			String id = discoId.getValue();
			if (!id.equals(getId())) {
				error.setError(Error.INVALID_ID_ERROR);
			}
			else {
				// Get the component name
				DataObject registerer = (DataObject) data.getDataObject(Discoverer.REGISTERER);
				String compId = registerer.getDataObject(Discoverer.ID).getValue();
				LOGGER.info("Discoverer removed : " + compId);
				error = mediator.remove(compId);
			}
		}
		return error.toDataObject();

	}

	/**
	 * This method handles the UPDATE_DISCOVERER messages. 
	 * It gets the modified fields and update the component description
	 * and returns an error code.
	 *
	 * @param data The DataObject containing the modified fields
	 * @return DataObject The data containing an error code
	 * @see context.arch.discoverer.ComponentDescription
	 */
	public DataObject componentUpdate(DataObject dataObject) {
		LOGGER.info("Discoverer - componentUpdate " + dataObject);
		
		DataObject data = dataObject.getDataObject(Discoverer.DISCOVERER_UPDATE);
		DataObject component = data.getDataObject(ID);

		Error error = new Error();
//		DataObject result = null;

		if (component == null) {
			error.setError(Error.INVALID_ID_ERROR);
		}
		else {
			String resultId = component.getValue();
			if (! resultId.equals(getId())) {
				error.setError(Error.INVALID_ID_ERROR);
			}
			else {
				ComponentDescription comp = ComponentDescription.fromDataObject(data.getDataObject (Discoverer.REGISTERER));
				Lease lease = null; // TODO do we remove the previous lease or not??
									// TODO pretty useless to have set this to null --Brian
				error = mediator.update(comp, lease);
				LOGGER.info("Discoverer <componentUpdate> error from mediator " + error);
			}
		}
		return error.toDataObject();
	}

	/**
	 * This method is handling the subscription of a context component
	 * that wants to subscribe to be notified of the registration of 
	 * context components of its interest.
	 *
	 * The content of the message is like a query message. The subscriber sends
	 * a query containing the description of the components of its interest.
	 *
	 * The discoverer adds this subscriber, and replies with the corresponding
	 * components that have already registered.
	 *
	 * @param data The data object containing the subscription details
	 * @return DataObject contains the reply to the subscriber
	 */
	public DataObject componentSubscription(DataObject dataObject){
		LOGGER.info("\nDiscoverer <componentSubscription>");
		DataObject data = dataObject.getDataObject(Discoverer.DISCOVERER_SUBSCRIBE);
		DataObject component = data.getDataObject(ID);

		Error error = new Error();
//		DataObject result = null;

		if (component == null) {
			error.setError(Error.INVALID_ID_ERROR);
		}
		else {
			String resultId = component.getValue();
			if (! resultId.equals(getId())) {
				error.setError(Error.INVALID_ID_ERROR);
			}
			else {
				// Retrieves the query
				DataObject qc = data.getDataObject(Discoverer.DISCOVERER_QUERY_CONTENT);
				if (qc != null){
					DataObjects vv = qc.getChildren ();
					qc = ( vv!=null? (DataObject)vv.firstElement () : null);
				}
				DiscovererSubscriber sub = new DiscovererSubscriber(data);
				LOGGER.info("Disco add sub " + sub.toString ());
				// Add the subscriber
				subscribers.add(sub);
				// Update the dataModel to say that the discoverer has a new subscriber
				ComponentDescription discoItself = ComponentDescription.fromDataObject (this.getDescription ());
				mediator.update (discoItself, getLease ());
				DataObjects v = new DataObjects();
				v.addElement(new DataObject(AbstractSubscriber.SUBSCRIBER_ID, sub.getSubscriptionId ()));
				error.setError (Error.NO_ERROR);
				v.add (error.toDataObject ());
				return new DataObject(DiscovererSubscriber.DISCOVERER_SUBSCRIPTION_REPLY, v); 
			}
		}
		return error.toDataObject();

	}

	/**
	 * Handles a DISCOVERER_QUERY message from components and 
	 * returns a DataObject containing the identification of the response 
	 * and the first response.
	 *
	 * TODO To complete ...
	 *
	 * @param data The DataObject containing the query
	 * @return DataObject The first result of the query
	 * @see context.arch.intelligibility.query.Query
	 * @see context.arch.discoverer.QueryElement
	 * @see context.arch.discoverer.Response
	 * @see context.arch.discoverer.ResponseElement
	 * @see context.arch.discoverer.SearchEngine
	 */

	public DataObject handleQuery(DataObject data) {
		LOGGER.info("\nDiscoverer - handleQuery : " + data);
		DataObject component = data.getDataObject(ID);

		Error error = new Error();

		String componentId = null;

		if (component == null) {
			error.setError(Error.INVALID_ID_ERROR);
		}
		
		else {
			String resultId = component.getValue();
			
			if (! resultId.equals(getId())) {
				error.setError(Error.INVALID_ID_ERROR);
			}
			else {
				componentId = data.getDataObject (Discoverer.CALLER_ID).getValue();
				// Retrieves the query
				DataObject qc = data.getDataObject(Discoverer.DISCOVERER_QUERY_CONTENT);
				DataObject q = null;
				if (qc != null){
					DataObjects vv = qc.getChildren ();
					q = ( vv != null ? vv.firstElement () : null);
				}
//				System.out.println();
				LOGGER.log(Level.FINE, "Discoverer.handleQuery q = " + q);

				DataObject results = null;

				if (q != null){
					AbstractQueryItem<?,?> query = AbstractQueryItem.fromDataObject (q);
//					System.out.println("Discoverer.handleQuery query: " + query);
//					System.out.println();
					
					// Searchs in the SearchEngine 
					results = mediator.search(query);
//					System.out.println("Discoverer.handleQuery resp: " + resp);
				}

				if (results == null) {
					error.setError (Discoverer.ERROR_QUERY_NOT_FOUND);
				}
				else{
					//Information about the component to reply to
					DataObjects v1 = new DataObjects();
					v1.addElement(new DataObject(Discoverer.ID, componentId));
					v1.addElement(results);
					DataObject result = new DataObject(Discoverer.DISCOVERER_QUERY_REPLY, v1);
					return result ;
				}
			}
		}
		
		return error.toDataObject();
	}

	/**
	 * Returns the content of the search engine
	 *
	 * @return String The content of the search engine
	 */
	public String getSearchEngineContent(){
		return mediator.toString ();
	}

	/**
	 * This method allows to send a lease end notification to each
	 * component whose lease ends. The reply received by the component
	 * either renew the lease or confirms it.
	 *
	 * @param componentIndices The components index
	 */
	public void sendLeaseEndNotificationTo(ArrayList<String> componentIndices) {
		if (componentIndices.isEmpty()) { return; }

		// For each component index, sends a lease end notification and handles the
		// answer
		int sentNb = 0;
		for (String index : componentIndices) {
			// Get the component description object corresponding to the index from the search engine
			ComponentDescription comp = mediator.getComponentDescription(index);
			if (comp != null) {
				DataObject toSend;
				DataObjects v = new DataObjects();
				v.addElement(new DataObject(Discoverer.ID, comp.id));
				toSend = new DataObject (Lease.LEASE_END_NOTIFICATION, v);

				RequestObject ro = new RequestObject(toSend,Lease.LEASE_END_NOTIFICATION, comp.hostname, comp.port, comp.id); 
				// Sends the message , the result will be stored in results
				if (sendLeaseEndNotificationTo(new IndependentCommunication(ro, false)))
					sentNb++;
			}
		}
	}

	/**
	 * This method allows to send the Lease.LEASE_END_NOTIFICATION to a context
	 * component. 
	 *
	 * TO complete to handle the lease_renewal
	 *
	 * @param data The data to send
	 * @param compHostname The hostname of the component to send the notification to
	 * @param compPort The component port
	 * @return Lease The lease object if the reply from the component is to renew the lease,
	 * or null if it confirms the lease end.
	 */
	protected boolean sendLeaseEndNotificationTo(IndependentCommunication comm) {
//		Error error = new Error();
		try {
			LOGGER.info("Discoverer <sendLeaseEndNotification> before independent comm :" + comm);
			comm.setResponseRequired (true);
			independentUserRequest (comm);
			return true; // message sent
		} catch (EncodeException ee) {
			LOGGER.severe("Discoverer <sendLeaseEndNotification> EncodeException: "+ee);
		} catch (InvalidEncoderException iee) {
			LOGGER.severe("Discoverer <sendLeaseEndNotification> InvalidEncoderException: "+iee);
		}
		return false; // message not sent
	}

	/**
	 * This method is called after the independentUserRequest has been called.
	 * The thread in charge of the communication sends the results to this method.
	 * This method should be overridden by classes that need to handle the responses.
	 * 
	 * @param originalRequest The request sent by the thread
	 * @param reply The reply of the message
	 * @param exception If an exception occured during the communication, a copy of it
	 * @see context.arch.comm.clients.ClientsPool
	 * @see context.arch.comm.clients.Client
	 * @see context.arch.util.RequestObject
	 * @see context.arch.comm.DataObject
	 */
	public void handleIndependentReply(IndependentCommunication independentCommunication) {
		LOGGER.info("The discoverer gets the reply from the the element =" + independentCommunication.getRequest().getUrl());
		
		if (independentCommunication != null) {
			independentCommunication.decodeReply (this);
			DataObject replyContent = independentCommunication.getDecodedReply();

			// For LEASE_END_NOTIFICATION
			if (independentCommunication.getRequest().getUrl().equals(Lease.LEASE_END_NOTIFICATION)) {
				// There are exceptions => remove the component
				if ( ! independentCommunication.getExceptions().isEmpty()
						|| replyContent == null
						|| (replyContent != null  && replyContent.getName().equalsIgnoreCase(Lease.LEASE_END))){
					String compId = independentCommunication.getRequest().getReceiverId();
					mediator.remove(compId);
				}
				// Renew the lease
				else {
					String compId = independentCommunication.getRequest ().getReceiverId ();
					Lease newLease = Lease.dataObjectToLease (replyContent.getDataObject (Lease.LEASE));
					mediator.updateLease (compId, newLease);
				}
			}
			else if (independentCommunication.getRequest ().getUrl ().equals (DiscovererSubscriber.SUBSCRIPTION_CALLBACK_REPLY)){
				// does nothing
				LOGGER.info("\nReply from a component/subscriber with" + new Error(independentCommunication.getDecodedReply ()).getError ());
			}
			else if (independentCommunication.getRequest ().getUrl ().equals (PING) 
					&& independentCommunication.getSenderClassId ().equals (Discoverer.DISCOVERER+Discoverer.REGISTERER+PING)){
				LOGGER.info(" Discoverer for mediator");
				mediator.handleIndependentReply (independentCommunication);
			}
			else {
				super.handleIndependentReply (independentCommunication);
			}
		}
		return;

	}

	/**
	 *
	 */
	public String getType() {
		return Discoverer.DISCOVERER_TYPE;
	}

	/**
	 * Returns a printable version of the list of discoverer subscribers
	 *
	 * @return String
	 */
	public String subscribersToString() {
		StringBuffer sb = new StringBuffer();
		sb.append ("Number of subscribers = " + subscribers.size());
		for (AbstractSubscriber asub : subscribers.values()) {
			DiscovererSubscriber sub = (DiscovererSubscriber) asub;
			sb.append("\n - id= " +sub.getSubscriptionId());
			sb.append(" - callback= " + sub.getSubscriptionCallback());
			sb.append(" - query= " + sub.getQuery ());
		}
		return sb.toString();
	}

	/**
	 * Main method to create a discoverer with location and port specified by 
	 * command line arguments
	 */
	public static void main(String argv[]) {

		if (argv.length == 0) {
			if (DEBUG) {
				System.out.println("Attempting to create a discoverer on 5555 at with storage disabled");
			}
//			Discoverer disco = 
				new Discoverer(Discoverer.DEFAULT_PORT, false);
		}
		else if (argv.length == 1) {
			if ((argv[0].equals("false")) || (argv[0].equals("true"))) {
				if (DEBUG) {
					System.out.println("Attempting to create a Discoverer on "+DEFAULT_PORT+" with storage set to "+argv[0]);
				}
//				Discoverer disco = 
					new Discoverer(Discoverer.DEFAULT_PORT, Boolean.valueOf(argv[0]).booleanValue());
			}
			else {
				if (DEBUG) {
					System.out.println("Attempting to create a Discoverer on "+argv[0]+" with storage enabled");
				}
//				Discoverer disco = 
					new Discoverer(Integer.parseInt(argv[0]));
			}
		}
		else if (argv.length == 2) {
			if (DEBUG) {
				System.out.println("Attempting to create a Discoverer on "+argv[0]+" with storage set to "+argv[1]);
			}
//			Discoverer disco = 
				new Discoverer(Integer.parseInt(argv[0]), Boolean.valueOf(argv[1]).booleanValue());
		}
		else {
			System.out.println("USAGE: java context.arch.discoverer.Discoverer [port] [storageFlag]");
		}
	}

}
