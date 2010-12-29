package context.arch;

import java.io.IOException;
import java.io.Reader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;

import context.arch.comm.CommunicationsHandler;
import context.arch.comm.CommunicationsObject;
import context.arch.comm.DataObject;
import context.arch.comm.DataObjects;
import context.arch.comm.RequestObject;
import context.arch.comm.clients.IndependentCommunication;
import context.arch.comm.language.DecodeException;
import context.arch.comm.language.EncodeException;
import context.arch.comm.language.InvalidDecoderException;
import context.arch.comm.language.InvalidEncoderException;
import context.arch.comm.language.MessageHandler;
import context.arch.comm.language.ParserObject;
import context.arch.comm.protocol.InvalidProtocolException;
import context.arch.comm.protocol.ProtocolException;
import context.arch.comm.protocol.RequestData;
import context.arch.discoverer.ComponentDescription;
import context.arch.discoverer.Discoverer;
import context.arch.discoverer.DiscovererDescription;
import context.arch.discoverer.lease.Lease;
import context.arch.discoverer.query.AbstractQueryItem;
import context.arch.handler.AsyncServiceHandler;
import context.arch.handler.AsyncServiceHandlerInfo;
import context.arch.handler.AsyncServiceHandlers;
import context.arch.handler.Handler;
import context.arch.handler.HandlerInfo;
import context.arch.handler.Handlers;
import context.arch.service.Service;
import context.arch.service.helper.FunctionDescription;
import context.arch.service.helper.ServiceInput;
//import context.arch.storage.AttributeFunctions;
import context.arch.storage.Attributes;
import context.arch.storage.Retrieval;
import context.arch.storage.StorageObject;
import context.arch.subscriber.AbstractSubscriber;
import context.arch.subscriber.ClientSideSubscriber;
import context.arch.subscriber.DiscovererSubscriber;
import context.arch.subscriber.Subscriber;
import context.arch.util.Constants;
import context.arch.util.Error;
import context.arch.widget.Widget;

/**
 * This class is the base object for the context-aware infrastructure.
 * It is able to poll and subscribe to other components and can be
 * polled and subscribed to by other components.  It also can generate
 * and handle RPC-style requests.  It consists of 2 main objects, the
 * CommunicationsObject and ParserObject.  It also maintains a list of
 * subscribers and a list of handlers.
 *
 * @see context.arch.comm.CommunicationsObject
 * @see context.arch.comm.language.ParserObject
 * @see context.arch.handler.Handler
 * @author Anind
 */
public class BaseObject implements MessageHandler, CommunicationsHandler {

	/**
	 * Debug flag. Set to true to see debug messages.
	 */
	public static  boolean DEBUG = false;

	/** Indicates that the exit condition is normal */
	public static final int EXIT_OK = 1;

	/** Disables the communications */
	public static final int DISABLE_PORT = -1;

	/**
	 * Tag for id of this component
	 */
	public static final String ID = Constants.ID;

	/**
	 * Tag for a ping
	 */
	public static final String PING = "ping";

	/**
	 * Tag for a ping reply
	 */
	public static final String PING_REPLY = "pingReply";

	/**
	 * Tag for getting the description
	 */
	public static final String QUERY_DESCRIPTION = "queryDescription";

	/**
	 * Tag for reply to a query_description
	 */
	public static final String QUERY_DESCRIPTION_REPLY = "queryDescriptionReply";

	/**
	 * The tag for the type of this object
	 */
	public static final String BASEOBJECT_TYPE = "baseobject";

	/**
	 * Object to handle communications between components
	 *
	 * @see context.arch.comm.CommunicationsObject
	 */
	public CommunicationsObject communications;

	/**
	 * Object to handle the encoding and decoding of communications
	 *
	 * @see context.arch.comm.language.ParserObject
	 */
	public ParserObject parser;

	/**
	 * Object to keep track of context widget handlers
	 *
	 * @see context.arch.handler.Handlers
	 * @see context.arch.handler.Handler
	 */
	public Handlers handlers;

	/**
	 * Object to keep track of asynchronous service handlers
	 *
	 * @see context.arch.handler.AsyncServiceHandlers
	 * @see context.arch.handler.AsyncServiceHandler
	 */
	public AsyncServiceHandlers serviceHandlers;

	/**
	 * Name or IP address of the host
	 */
	protected String host = null;

	/**
	 * Id of the object
	 */
	protected String id;

	/**
	 * Version of the object
	 */
	private String version = "undefined";

	/**
	 * The lease defined by the component, to be sent to the discoverer
	 */
	private Lease myLease;

	/**
	 * This field specifies if the lease is automatically renewed or not.
	 * By default, the lease is renewed.
	 */
	private boolean automaticRenewal = true;

	/**
	 * The description of the discoverer that the base object has found
	 *
	 * @see context.arch.discoverer.Discoverer
	 */
	public DiscovererDescription discoverer = null;

	/**
	 * Basic constructor that creates a CommunicationsObject
	 * with the given port and protocol, and creates a
	 * ParserObject with the given encoder and decoder.  It also
	 * creates a Handlers object to keep track of context widget handlers.
	 *
	 * @param communicationClientClass Class to use for client communications
	 * @param communicationServerClass Class to use for server communications
	 * @param localServerPort Port to use for server communications
	 * @param encoderClass Class to use for communications encoding
	 * @param decoderClass Class to use for communications decoding
	 * @param threadPoolNumber The number of client threads in the clients pool
	 * @see context.arch.comm.CommunicationsObject
	 * @see context.arch.comm.CommunicationsObject#start()
	 * @see context.arch.comm.language.ParserObject
	 * @see context.arch.handler.Handlers
	 */
	public BaseObject(String communicationClientClass, String communicationServerClass,
			int localServerPort, String encoderClass,
			String decoderClass, int threadPoolNumber) {
		try {
			// starts the server and the multicast connection
			if (localServerPort != BaseObject.DISABLE_PORT){
				communications = new CommunicationsObject(this,
						communicationClientClass, communicationServerClass,
						localServerPort, threadPoolNumber);
				communications.start();
			}
			parser = new ParserObject(encoderClass,decoderClass);
			handlers = new Handlers();
			serviceHandlers = new AsyncServiceHandlers();
			discoverer = null;	// the discoverer's information
		} catch (InvalidProtocolException ipe) {
			System.out.println("BaseObject InvalidProtocolException: "+ipe);
		}
	}

	/**
	 * Basic constructor that creates a CommunicationsObject
	 * with the given port and protocol, and creates a
	 * ParserObject with the given encoder and decoder.  It also
	 * creates a Handlers object to keep track of context widget handlers.
	 *
	 * @param communicationClientClass Class to use for client communications
	 * @param communicationServerClass Class to use for server communications
	 * @param localServerPort Port to use for server communications
	 * @param encoderClass Class to use for communications encoding
	 * @param decoderClass Class to use for communications decoding
	 * @see context.arch.comm.CommunicationsObject
	 * @see context.arch.comm.CommunicationsObject#start()
	 * @see context.arch.comm.language.ParserObject
	 * @see context.arch.handler.Handlers
	 */
	public BaseObject(String communicationClientClass, String communicationServerClass,
			int localServerPort, String encoderClass,
			String decoderClass) {
		this(communicationClientClass, communicationServerClass, localServerPort, encoderClass, decoderClass, -1);
	}

	/**
	 * Basic constructor that creates a CommunicationsObject,
	 * ParserObject and Handlers object.
	 *
	 * @see context.arch.comm.CommunicationsObject
	 * @see context.arch.comm.CommunicationsObject#start()
	 * @see context.arch.comm.language.ParserObject
	 * @see context.arch.handler.Handlers
	 */
	public BaseObject() {
		this(null,null,-1,null,null, -1);
	}

	/**
	 * Constructor that just creates a CommunicationsObject
	 * with the given port and ParserObject.
	 * It also creates a Handlers
	 * object to keep track of context widget handlers.
	 *
	 * @param localServerPort Port number to communicate on
	 *
	 * @see context.arch.comm.CommunicationsObject
	 * @see context.arch.comm.CommunicationsObject#start()
	 * @see context.arch.comm.language.ParserObject
	 * @see context.arch.handler.Handlers
	 */
	public BaseObject(int localServerPort) {
		this(null,null,localServerPort,null,null, -1);
		debugprintln(DEBUG, "BO constructor (int)");
	}

	/**
	 * Constructor that just creates a CommunicationsObject
	 * with the given protocol handler class, and
	 * a ParserObject.  It also creates a Handlers object to keep track
	 * of context widget handlers.
	 *
	 * @param protocolHandlerClass Protocol handler class to communicate with
	 *
	 * @see context.arch.comm.CommunicationsObject
	 * @see context.arch.comm.CommunicationsObject#start()
	 * @see context.arch.comm.language.ParserObject
	 * @see context.arch.handler.Handlers
	 */
	public BaseObject(String protocolHandlerClass) {
		this(null,protocolHandlerClass,-1,null,null, -1);
	}

	/**
	 * Constructor that just creates a CommunicationsObject
	 * with the given port and protocol handler class, and
	 * ParserObject.  It also creates a Handlers object to keep track
	 * of context widget handlers.
	 *
	 * @param localServerPort Port number to communicate on
	 * @param protocolHandlerClass Protocol handler class name to communicate with
	 *
	 * @see context.arch.comm.CommunicationsObject
	 * @see context.arch.comm.CommunicationsObject#start()
	 * @see context.arch.comm.language.ParserObject
	 * @see context.arch.handler.Handlers
	 */
	public BaseObject(int localServerPort, String protocolHandlerClass) {
		this(null,protocolHandlerClass,localServerPort,null,null, -1);
	}

	/**
	 * Stub method that decodes the given string using ParserObject
	 *
	 * @param communicationData String to be decoded
	 * @return the decoded data
	 * @exception context.arch.comm.language.DecodeException thrown if the parser can't decode the given string
	 * @exception context.arch.comm.language.InvalidDecoderException thrown if the parser can't create the necessary decoder
	 * @see context.arch.comm.language.ParserObject#decodeData(java.io.Reader)
	 */
	public DataObject decodeData(Reader communicationData) throws DecodeException, InvalidDecoderException {
		return parser.decodeData(communicationData);
	}

	/**
	 * Stub method that encodes the given string using ParserObject
	 *
	 * @param communicationData String to be decoded
	 * @return the encoded data
	 * @exception context.arch.comm.language.EncodeException thrown if the parser can't encode the given string
	 * @exception context.arch.comm.language.InvalidEncoderException thrown if the parser can't create the necessary encoder
	 * @see context.arch.comm.language.ParserObject#encodeData(context.arch.comm.DataObject)
	 */
	public String encodeData(DataObject communicationData) throws EncodeException, InvalidEncoderException {
		//System.out.println("communicationData = " + communicationData);
		return parser.encodeData(communicationData);
	}

	/**
	 * Method that submits a user request for polling/subscription.  The request
	 * is in the form of a DataObject.  It is encoded, sent out and the reply is
	 * decoded, if necessary, and returned.
	 *
	 * @param data DataObject that contains the request
	 * @param requestType RPC tag that indicates the type of request
	 * @return DataObject containing the reply to the request
	 * @exception EncodeException when the encoding can't be completed successfully
	 * @exception DecodeException when the decoding can't be completed successfully
	 * @exception InvalidEncoderException when the encoder can't be created
	 * @exception InvalidDecoderException when the decoder can't be created
	 * @exception ProtocolException when the request can't be sent successfully
	 * @exception InvalidProtocolException when the request can't be sent successfully due to invalid protocol use
	 *
	 * @deprecated
	 */
	public DataObject userRequest(DataObject data, String requestType) throws EncodeException, InvalidProtocolException, ProtocolException, DecodeException, InvalidDecoderException, InvalidEncoderException, IOException {
		DataObject decoded = null;
		RequestObject request = new RequestObject(data, requestType);
		request.setEncodedData(encodeData(request.getNonEncodedData()));

		RequestData replydata = communications.sendRequest(request);
		//println("BaseObject <userRequest deprecated> : wants to send \n"+data + "\nto "+requestType);
		if (replydata.getType().equals(RequestData.DECODE)) {
			decoded = parser.decodeData(replydata.getData());
		}
		//println("and receive reply :\n"+decoded);
		return decoded;
	}

	/**
	 * Sends a message to a remote component
	 *
	 * @param request The RequestObject that contains all information about the
	 * remote component and the data to send
	 * @return DataObject The reply
	 */
	public DataObject userRequest(RequestObject request)
	throws EncodeException, InvalidProtocolException, ProtocolException,
	DecodeException, InvalidDecoderException, InvalidEncoderException, IOException {
		RequestData replydata = null;
		DataObject decoded = null;
		//Encode if necessary
		request.setEncodedData(encodeData(request.getNonEncodedData()));
		debugprintln(DEBUG, "userRequest=" + request);
		
		replydata = communications.sendRequest(request);
		if (replydata != null && replydata.getType().equals(RequestData.DECODE)) {
			decoded = parser.decodeData(replydata.getData());
		}
		return decoded;
	}

	/**
	 * Method that submits a user request for polling/subscription.  The request
	 * is in the form of a DataObject.  It is encoded, sent out and the reply is
	 * decoded, if necessary, and returned.
	 *
	 * @param data DataObject that contains the request
	 * @param requestType RPC tag that indicates the type of request
	 * @param remoteHostname Hostname of the component the request is being sent to
	 * @return DataObject containing the reply to the request
	 * @exception EncodeException when the encoding can't be completed successfully
	 * @exception DecodeException when the decoding can't be completed successfully
	 * @exception InvalidEncoderException when the encoder can't be created
	 * @exception InvalidDecoderException when the decoder can't be created
	 * @exception ProtocolException when the request can't be sent successfully
	 * @exception InvalidProtocolException when the request can't be sent successfully due to invalid protocol use
	 *
	 * @deprecated
	 */
	public DataObject userRequest(DataObject data, String requestType, String remoteHostname) throws EncodeException, InvalidProtocolException, ProtocolException, DecodeException, InvalidDecoderException, InvalidEncoderException, IOException {
		DataObject decoded = null;
		RequestObject request = new RequestObject(data, requestType, remoteHostname);
		request.setEncodedData(encodeData(request.getNonEncodedData()));
		RequestData replydata = communications.sendRequest(request);

		if (replydata.getType().equals(RequestData.DECODE)) {
			decoded = parser.decodeData(replydata.getData());
		}
		return decoded;
	}

	/**
	 * Method that submits a user request for polling/subscription.  The request
	 * is in the form of a DataObject.  It is encoded, sent out and the reply is
	 * decoded, if necessary, and returned.
	 *
	 * @param data DataObject that contains the request
	 * @param requestType RPC tag that indicates the type of request
	 * @param remoteHostname Hostname of the component the request is being sent to
	 * @param remotePort Port number of the component the request is being sent to
	 * @return DataObject containing the reply to the request
	 * @exception EncodeException when the encoding can't be completed successfully
	 * @exception DecodeException when the decoding can't be completed successfully
	 * @exception InvalidEncoderException when the encoder can't be created
	 * @exception InvalidDecoderException when the decoder can't be created
	 * @exception ProtocolException when the request can't be sent successfully
	 * @exception InvalidProtocolException when the request can't be sent successfully due to invalid protocol use
	 */
	public DataObject userRequest(DataObject data, String requestType, String remoteHostname, int remotePort) 
	throws EncodeException, ProtocolException, InvalidProtocolException, DecodeException, InvalidDecoderException, InvalidEncoderException, IOException {

		RequestObject request = new RequestObject(data, requestType, remoteHostname, remotePort);
		request.setEncodedData(encodeData(request.getNonEncodedData()));

		RequestData replydata = communications.sendRequest(request);
		//		System.out.println

		if (replydata != null && replydata.getType().equals(RequestData.DECODE)) {
			DataObject decoded = parser.decodeData(replydata.getData());
			return decoded;
		}
		return null;
	}

	/**
	 * This method is used to send a message through a threaded communication.
	 * The request is sent by a thread in charge of the communication.
	 *
	 * The request is encapsulated in a IndependentCommunication that contains
	 * a RequestObject (data to send and the recipient of the data) and the
	 * reply message and the exceptions that occured during the communication.
	 * If the communication result is null, the baseObject won't be notified
	 * about how the communication ended. Actually, the thread in charge
	 * of the communication won't notify the baseObject, but update the request object.
	 *
	 * If the result is not null, result is the IndependentCommunications object that will
	 * contain the original request and the reply. At the end of the communication,
	 * the thread updates request
	 * with the reply message and the exception, and adds it into result.
	 *
	 * The base object is notified of the end of the threaded communication
	 * when the handleIndependentReply is called by the thread.
	 *
	 * @param comm The IndependentCommunication object that contain the RequestObject,
	 * and will contain after the communication the reply dataObject and the vector of exception
	 * @param result The IndependentCommunications object that may contain many IndependentCommunication
	 * object.
	 *
	 * @see context.arch.BaseObject#handleIndependentReply
	 * @see context.arch.comm.clients.IndependentCommunication
	 * @see context.arch.comm.clients.IndependentCommunications
	 * @see context.arch.util.RequesObject
	 */
	public void independentUserRequest(IndependentCommunication comm) throws EncodeException, InvalidEncoderException {
		RequestObject request = comm.getRequest();
		DataObject dobj = request.getNonEncodedData();
		if (dobj != null) {
			request.setEncodedData(encodeData(dobj));
		}
		communications.sendIndependentRequest(comm);
	}




	/**
	 * This method is called after the independentUserRequest has been called.
	 * The thread in charge of the communication sends the results to this method.
	 *
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
	public void handleIndependentReply(IndependentCommunication independentCommunication){
		debugprintln(DEBUG, "BO <handleIndependentReply> " + independentCommunication);
		//IndependentCommunication ic = (IndependentCommunication) independentCommunications.getNext ();
		//println ("BO <handleIndependentReply> exceptions=" + ic.getExceptions ());
		// Stores it... or call a method overridden by classes

		return ;
	}

	/**
	 * This method allows a component to subscribe to changes in other components.
	 * The subscription includes the handler that will handle the callbacks,
	 * the subscriber's hostname and port, the subscription id, the remote component's
	 * hostname, port, and id, the remote component's callback, and the name of the
	 * subscriber's method that will handle the callback.
	 *
	 * @param handler Object that handles context widget callbacks
	 * @param remoteId Id of the context widget being subscribed to
	 * @param remoteHost Hostname of the widget being subscribed to
	 * @param remotePort Port number of the widget being subscribed to
	 * @param subscriber Subscriber object holding the subscription info
	 * @return Error to indicate success of subscription
	 * @see context.arch.handler.Handlers
	 * @see context.arch.handler.Handlers#addHandler(context.arch.handler.HandlerInfo)
	 * @see #userRequest(context.arch.comm.DataObject, String, String, int)
	 */
	public Error subscribeTo(Handler handler, String remoteId, String remoteHost, int remotePort, ClientSideSubscriber subscriber) {
		debugprintln(DEBUG, "\n\nBaseObject <subscribeTo>" + remoteId + " " + remotePort + " " + remoteHost + " - sub="+subscriber);
		DataObjects v = new DataObjects();
		v.addElement(new DataObject(ID, remoteId));
		v.addElement(subscriber.toDataObject());
		DataObject sub = new DataObject(Subscriber.ADD_SUBSCRIBER, v);

		Error error = null;
		try {
			DataObject result = userRequest(new RequestObject(sub, Subscriber.ADD_SUBSCRIBER, remoteHost, remotePort));
			debugprintln(DEBUG, "BO subscribeTo send=" + result);
			error = new Error(result);
			// Get the subscriber id sent by the widget
			DataObject subId = result.getDataObject(AbstractSubscriber.SUBSCRIBER_ID);
			if (subId != null) {
				subscriber.setSubscriptionId(subId.getValue());
			}
			if (error.getError().equals(Error.NO_ERROR)) {
				handlers.addHandler(new HandlerInfo(handler, subscriber.getSubscriptionId(), remoteId, remoteHost, remotePort,
						subscriber.getSubscriptionCallback()));
			}
			return error;
		} catch (EncodeException ee) {
			System.out.println("BaseObject subscribeTo EncodeException: "+ee);
		} catch (DecodeException de) {
			System.out.println("BaseObject subscribeTo DecodeException: "+de);
		} catch (InvalidEncoderException iee) {
			System.out.println("BaseObject subscribeTo InvalidEncoderException: "+iee);
		} catch (InvalidDecoderException ide) {
			System.out.println("BaseObject subscribeTo InvalidDecoderException: "+ide);
		} catch (InvalidProtocolException ipe) {
			System.out.println("BaseObject subscribeTo InvalidProtocolException: "+ipe);
		} catch (ProtocolException pe) {
			System.out.println("BaseObject subscribeTo ProtocolException: "+pe);
		} catch (IOException ioe){
			System.out.println("BaseObject subscribeTo IOException: "+ioe);
			// set the error object
			error = new Error(Error.IO_ERROR);
		}
		return error;
	}

	/**
	 * This method allows a component to unsubscribe from another component.
	 *
	 * @param handler Object that handles context widget callbacks
	 * @param remoteHost Hostname of the widget being unsubscribed from
	 * @param remotePort Port number of the widget being unsubscribed from
	 * @param remoteId Id of the context widget being unsubscribed from
	 * @param subscriber Subscriber object holding the subscription info
	 * @return Error to indicate success of unsubscription
	 * @see context.arch.handler.Handlers
	 * @see context.arch.handler.Handlers#removeHandler(context.arch.handler.HandlerInfo)
	 * @see #userRequest(context.arch.comm.DataObject, String, String, int)
	 */
	public Error unsubscribeFrom(String subscriptionId) {
		debugprintln(DEBUG, "BO <unsubscriberForm> id=" + subscriptionId);

		DataObjects v = new DataObjects();

		HandlerInfo info = handlers.getHandlerInfo(subscriptionId);
		System.out.println("BO <unsubscriberFrom> info " + info);
		v.addElement(new DataObject(ID, info.getRemoteId()));
		v.addElement(new DataObject(Subscriber.SUBSCRIBER_ID, info.getSubId()));
		DataObject sub = new DataObject(Subscriber.REMOVE_SUBSCRIBER, v);
		Error error= null;
		try {
			DataObject result = userRequest(new RequestObject(sub, Subscriber.REMOVE_SUBSCRIBER, info.getRemoteHost(), info.getRemotePort()));
			error = new Error(result);
			if (error.getError().equals(Error.NO_ERROR)) {
				handlers.removeHandler(subscriptionId);
			}
			return error;
		} catch (EncodeException ee) {
			System.out.println("BaseObject subscribeTo EncodeException: "+ee);
		} catch (DecodeException de) {
			System.out.println("BaseObject subscribeTo DecodeException: "+de);
		} catch (InvalidEncoderException iee) {
			System.out.println("BaseObject subscribeTo InvalidEncoderException: "+iee);
		} catch (InvalidDecoderException ide) {
			System.out.println("BaseObject subscribeTo InvalidDecoderException: "+ide);
		} catch (InvalidProtocolException ipe) {
			System.out.println("BaseObject subscribeTo InvalidProtocolException: "+ipe);
		} catch (ProtocolException pe) {
			System.out.println("BaseObject subscribeTo ProtocolException: "+pe);
		} catch (IOException ioe) {
			System.out.println("BaseObject subscribeTo IOException: "+ioe);
			error = new Error(Error.IO_ERROR);
		}

		return error;
	}

	/**
	 * This method allows a component to poll a remote widget for its attribute
	 * values.  A list of attributes is provided to dictate which attribute
	 * values are wanted.
	 *
	 * @param widgetHost Hostname of the context widget being polled
	 * @param widgetPort Port number of the context widget being polled
	 * @param widgetId Id of the context widget being polled
	 * @param attributes Attributes being requested
	 * @return DataObject containing results of poll
	 * @see #userRequest(context.arch.comm.DataObject, String, String, int)
	 */
	public DataObject pollWidget(String widgetHost, int widgetPort, String widgetId, Attributes attributes) {
		debugprintln(DEBUG, "\nBaseObject <pollWidget> ");
		DataObjects v = new DataObjects();
		v.addElement(new DataObject(ID, widgetId));
		v.addElement(attributes.toDataObject());
		DataObject poll = new DataObject(Constants.QUERY, v);

		try {
			return userRequest(new RequestObject(poll, Constants.QUERY, widgetHost, widgetPort));
		} catch (EncodeException ee) {
			System.out.println("BaseObject pollWidget EncodeException: "+ee);
		} catch (DecodeException de) {
			System.out.println("BaseObject pollWidget DecodeException: "+de);
		} catch (InvalidEncoderException iee) {
			System.out.println("BaseObject pollWidget InvalidEncoderException: "+iee);
		} catch (InvalidDecoderException ide) {
			System.out.println("BaseObject pollWidget InvalidDecoderException: "+ide);
		} catch (InvalidProtocolException ipe) {
			System.out.println("BaseObject pollWidget InvalidProtocolException: "+ipe);
		} catch (ProtocolException pe) {
			System.out.println("BaseObject pollWidget ProtocolException: "+pe);
		} catch (IOException ioe) {
			System.out.println("BaseObject pollWidget IOException: "+ioe);
			return ( new Error(Error.IO_ERROR)).toDataObject();
		}
		return null;
	}

	/**
	 * This method allows a component to force a remote widget to update its data
	 * and return it.  A list of attributes is provided to dictate which attribute
	 * values are wanted.
	 *
	 * @param widgetHost Hostname of the context widget being polled
	 * @param widgetPort Port number of the context widget being polled
	 * @param widgetId Id of the context widget being polled
	 * @param attributes Attributes being requested
	 * @return DataObject containing results of poll
	 * @see #userRequest(context.arch.comm.DataObject, String, String, int)
	 */
	public DataObject updateAndPollWidget(String widgetHost, int widgetPort, String widgetId, Attributes attributes) {
		debugprintln(DEBUG, "\nBaseObject <updatePollWidget>");
		//		println("\nBaseObject <updatePollWidget> widgetId = " + widgetId + ", attributes = ");

		DataObjects v = new DataObjects();
		v.addElement(new DataObject(ID, widgetId));
		v.addElement(attributes.toDataObject());
		DataObject poll = new DataObject(Constants.UPDATE_AND_QUERY, v);

		try {

			DataObject dataObj = userRequest(new RequestObject(poll, Constants.UPDATE_AND_QUERY, widgetHost, widgetPort));
			return dataObj;

		} catch (EncodeException ee) {
			System.out.println("BaseObject pollWidget EncodeException: "+ee);
		} catch (DecodeException de) {
			System.out.println("BaseObject pollWidget DecodeException: "+de);
		} catch (InvalidEncoderException iee) {
			System.out.println("BaseObject pollWidget InvalidEncoderException: "+iee);
		} catch (InvalidDecoderException ide) {
			System.out.println("BaseObject pollWidget InvalidDecoderException: "+ide);
		} catch (InvalidProtocolException ipe) {
			System.out.println("BaseObject pollWidget InvalidProtocolException: "+ipe);
		} catch (ProtocolException pe) {
			System.out.println("BaseObject pollWidget ProtocolException: "+pe);
		} catch (IOException ioe) {
			System.out.println("BaseObject pollWidget IOException: "+ioe);
			return (new Error(Error.IO_ERROR)).toDataObject();
		}

		return null;
	}

	/**
	 * This method allows a component to put context data in a remote
	 * widget.  It is really intended for components that don't use
	 * the context toolkit, but is being included here for possible future
	 * use.  The method takes a callback and an AttributeNameValues object.
	 * The callback is not necessary (can have a null value), and is only
	 * used by the remote widget to determine which of the widget's subscribers
	 * need to be updated.
	 *
	 * @param widgetHost Hostname of the context widget to use
	 * @param widgetPort Port number of the context widget to use
	 * @param widgetId Id of the context widget to use
	 * @param callback Callback of the context widget to associate the data with
	 * @param attributes AttributeNameValues to put in the widget
	 * @return DataObject containing results of put data
	 * @see #userRequest(context.arch.comm.DataObject, String, String, int)
	 */
	public DataObject putDataInWidget(String widgetHost, int widgetPort, String widgetId,
			String callback, Attributes attributes) {
		debugprintln(DEBUG, "\nBaseObject <putDataInWidget>");
		DataObjects v = new DataObjects();
		v.addElement(new DataObject(ID, widgetId));
		if (callback != null) {
			v.addElement(new DataObject(Subscriber.CALLBACK_NAME, callback));
		}
		v.addElement(attributes.toDataObject());
		DataObject put = new DataObject(Constants.PUT_DATA, v);

		try {
			return userRequest(new RequestObject(put, Constants.PUT_DATA, widgetHost, widgetPort));
		} catch (EncodeException ee) {
			System.out.println("BaseObject putDataInWidget EncodeException: "+ee);
		} catch (DecodeException de) {
			System.out.println("BaseObject putDataInWidget DecodeException: "+de);
		} catch (InvalidEncoderException iee) {
			System.out.println("BaseObject putDataInWidget InvalidEncoderException: "+iee);
		} catch (InvalidDecoderException ide) {
			System.out.println("BaseObject putDataInWidget InvalidDecoderException: "+ide);
		} catch (InvalidProtocolException ipe) {
			System.out.println("BaseObject putDataInWidget InvalidProtocolException: "+ipe);
		} catch (ProtocolException pe) {
			System.out.println("BaseObject putDataInWidget ProtocolException: "+pe);
		} catch (IOException ioe) {
			System.out.println("BaseObject putDataInWidget IOException: "+ioe);
			return (new Error(Error.IO_ERROR)).toDataObject();
		}
		return null;
	}

	/**
	 * This method gets the version of the given component (remoteHostname, widget, interpreter).
	 *
	 * @param remoteHost Hostname of the component being queried
	 * @param remotePort Port number of the component being queried
	 * @param remoteId Id of the component being queried
	 * @return DataObject containing version
	 * @see #userRequest(context.arch.comm.DataObject, String, String, int)
	 */
	public DataObject getVersion(String remoteHost, int remotePort, String remoteId) {
		DataObjects v = new DataObjects();
		v.addElement(new DataObject(ID, remoteId));
		DataObject query = new DataObject(Constants.QUERY_VERSION, v);

		try {
			return userRequest(new RequestObject(query, Constants.QUERY_VERSION, remoteHost, remotePort));
		} catch (EncodeException ee) {
			System.out.println("BaseObject getVersion EncodeException: "+ee);
		} catch (DecodeException de) {
			System.out.println("BaseObject getVersion DecodeException: "+de);
		} catch (InvalidEncoderException iee) {
			System.out.println("BaseObject getVersion InvalidEncoderException: "+iee);
		} catch (InvalidDecoderException ide) {
			System.out.println("BaseObject getVersion InvalidDecoderException: "+ide);
		} catch (InvalidProtocolException ipe) {
			System.out.println("BaseObject getVersion InvalidProtocolException: "+ipe);
		} catch (ProtocolException pe) {
			System.out.println("BaseObject getVersion ProtocolException: "+pe);
		} catch (IOException ioe) {
			System.out.println("BaseObject getVersion IOException: "+ioe);
			return (new Error(Error.IO_ERROR)).toDataObject();
		}
		return null;
	}

	/**
	 * This method gets the callbacks of the given widget
	 *
	 * @param widgetHost Hostname of the widget being queried
	 * @param widgetPort Port number of the widget being queried
	 * @param widgetId Id of the widget being queried
	 * @return DataObject containing callbacks
	 * @see #userRequest(context.arch.comm.DataObject, String, String, int)
	 */
	public DataObject getWidgetCallbacks(String widgetHost, int widgetPort, String widgetId) {
		debugprintln(DEBUG, "\nBaseObject getWidgetCallbacks");
		DataObjects v = new DataObjects();
		v.addElement(new DataObject(ID,widgetId));
		DataObject query = new DataObject(Constants.QUERY_CALLBACKS, v);
		try {
			return userRequest(new RequestObject(query, Constants.QUERY_CALLBACKS, widgetHost, widgetPort));
		} catch (EncodeException ee) {
			System.out.println("BaseObject getWidgetCallbacks EncodeException: "+ee);
		} catch (DecodeException de) {
			System.out.println("BaseObject getWidgetCallbacks DecodeException: "+de);
		} catch (InvalidEncoderException iee) {
			System.out.println("BaseObject getWidgetCallbacks InvalidEncoderException: "+iee);
		} catch (InvalidDecoderException ide) {
			System.out.println("BaseObject getWidgetCallbacks InvalidDecoderException: "+ide);
		} catch (InvalidProtocolException ipe) {
			System.out.println("BaseObject getWidgetCallbacks InvalidProtocolException: "+ipe);
		} catch (ProtocolException pe) {
			System.out.println("BaseObject getWidgetCallbacks ProtocolException: "+pe);
		} catch (IOException ioe) {
			System.out.println("BaseObject getWidgetCallbacks IOException: "+ioe);
			return (new Error(Error.IO_ERROR)).toDataObject();
		}
		return null;
	}

	/**
	 * This method gets the services of the given widget
	 *
	 * @param widgetHost Hostname of the widget being queried
	 * @param widgetPort Port number of the widget being queried
	 * @param widgetId Id of the widget being queried
	 * @return DataObject containing services of the widget
	 * @see #userRequest(context.arch.comm.DataObject, String, String, int)
	 */
	public DataObject getWidgetServices(String widgetHost, int widgetPort, String widgetId) {
		debugprintln(DEBUG, "\nBaseObject getWidgetServices");
		DataObjects v = new DataObjects();
		v.addElement(new DataObject(ID,widgetId));
		DataObject query = new DataObject(Constants.QUERY_SERVICES, v);
		try {
			return userRequest(new RequestObject(query, Constants.QUERY_SERVICES, widgetHost, widgetPort));
		} catch (EncodeException ee) {
			System.out.println("BaseObject getWidgetServices EncodeException: "+ee);
		} catch (DecodeException de) {
			System.out.println("BaseObject getWidgetServices DecodeException: "+de);
		} catch (InvalidEncoderException iee) {
			System.out.println("BaseObject getWidgetServices InvalidEncoderException: "+iee);
		} catch (InvalidDecoderException ide) {
			System.out.println("BaseObject getWidgetServices InvalidDecoderException: "+ide);
		} catch (InvalidProtocolException ipe) {
			System.out.println("BaseObject getWidgetServices InvalidProtocolException: "+ipe);
		} catch (ProtocolException pe) {
			System.out.println("BaseObject getWidgetServices ProtocolException: "+pe);
		} catch (IOException ioe) {
			System.out.println("BaseObject getWidgetServices IOException: "+ioe);
			return (new Error(Error.IO_ERROR)).toDataObject();
		}
		return null;
	}

	/**
	 * This method gets the attributes of the given widget
	 *
	 * @param widgetHost Hostname of the widget being queried
	 * @param widgetPort Port number of the widget being queried
	 * @param widgetId Id of the widget being queried
	 * @return DataObject containing callbacks
	 * @see #userRequest(context.arch.comm.DataObject, String, String, int)
	 */
	public DataObject getWidgetAttributes(String widgetHost, int widgetPort, String widgetId) {
		debugprintln(DEBUG, "\nBaseObject getWidgetAttributes");
		DataObjects v = new DataObjects();
		v.addElement(new DataObject(ID,widgetId));
		DataObject query = new DataObject(Constants.QUERY_ATTRIBUTES, v);
		try {
			return userRequest(new RequestObject(query, Constants.QUERY_ATTRIBUTES, widgetHost, widgetPort));
		} catch (EncodeException ee) {
			System.out.println("BaseObject getWidgetAttributes EncodeException: "+ee);
		} catch (DecodeException de) {
			System.out.println("BaseObject getWidgetAttributes DecodeException: "+de);
		} catch (InvalidEncoderException iee) {
			System.out.println("BaseObject getWidgetAttributes InvalidEncoderException: "+iee);
		} catch (InvalidDecoderException ide) {
			System.out.println("BaseObject getWidgetAttributes InvalidDecoderException: "+ide);
		} catch (InvalidProtocolException ipe) {
			System.out.println("BaseObject getWidgetAttributes InvalidProtocolException: "+ipe);
		} catch (ProtocolException pe) {
			System.out.println("BaseObject getWidgetAttributes ProtocolException: "+pe);
		} catch (IOException ioe) {
			System.out.println("BaseObject getWidgetAttributes IOException: "+ioe);
			return (new Error(Error.IO_ERROR)).toDataObject();
		}
		return null;
	}

	/**
	 * This method gets the attributes of the given widget
	 *
	 * @param widgetHost Hostname of the widget being queried
	 * @param widgetPort Port number of the widget being queried
	 * @param widgetId Id of the widget being queried
	 * @return DataObject containing callbacks
	 * @see #userRequest(context.arch.comm.DataObject, String, String, int)
	 */
	public DataObject getWidgetConstantAttributes(String widgetHost, int widgetPort, String widgetId) {
		debugprintln(DEBUG, "\nBaseObject getWidgetConstantAttributes");
		DataObjects v = new DataObjects();
		v.addElement(new DataObject(ID,widgetId));
		DataObject query = new DataObject(Widget.QUERY_CONSTANT_ATTRIBUTES, v);
		try {
			return userRequest(new RequestObject(query, Widget.QUERY_CONSTANT_ATTRIBUTES, widgetHost, widgetPort));
		} catch (EncodeException ee) {
			System.out.println("BaseObject getWidgetConstantAttributes EncodeException: "+ee);
		} catch (DecodeException de) {
			System.out.println("BaseObject getWidgetConstantAttributes DecodeException: "+de);
		} catch (InvalidEncoderException iee) {
			System.out.println("BaseObject getWidgetConstantAttributes InvalidEncoderException: "+iee);
		} catch (InvalidDecoderException ide) {
			System.out.println("BaseObject getWidgetConstantAttributes InvalidDecoderException: "+ide);
		} catch (InvalidProtocolException ipe) {
			System.out.println("BaseObject getWidgetConstantAttributes InvalidProtocolException: "+ipe);
		} catch (ProtocolException pe) {
			System.out.println("BaseObject getWidgetConstantAttributes ProtocolException: "+pe);
		} catch (IOException ioe) {
			System.out.println("BaseObject getWidgetConstantAttributes IOException: "+ioe);
			return (new Error(Error.IO_ERROR)).toDataObject();
		}
		return null;
	}

	/**
	 * This method allows a component to retrieve data from other components.
	 *
	 * @param remoteHost Hostname of the component being asked for data
	 * @param remotePort Port number of the component being asked for data
	 * @param remoteId Id of the component being asked for data
	 * @param retrieval Description of data to retrieve with any conditions
	 * @return DataObject containing data requested
	 * @see #userRequest(context.arch.comm.DataObject, String, String, int)
	 */
	public DataObject retrieveDataFrom(String remoteHost, int remotePort, String remoteId, Retrieval retrieval) {
		debugprintln(DEBUG, "\nBaseObject retrieveDataFrom");
		DataObjects v = new DataObjects();
		v.addElement(new DataObject(ID, remoteId));
		v.addElement(retrieval.toDataObject());
		DataObject retrieve = new DataObject(StorageObject.RETRIEVE_DATA, v);

		try {
			return userRequest(new RequestObject(retrieve, StorageObject.RETRIEVE_DATA, remoteHost, remotePort));
		} catch (EncodeException ee) {
			System.out.println("BaseObject retrieveDataFrom EncodeException: "+ee);
		} catch (DecodeException de) {
			System.out.println("BaseObject retrieveDataFrom DecodeException: "+de);
		} catch (InvalidEncoderException iee) {
			System.out.println("BaseObject retrieveDataFrom InvalidEncoderException: "+iee);
		} catch (InvalidDecoderException ide) {
			System.out.println("BaseObject retrieveDataFrom InvalidDecoderException: "+ide);
		} catch (InvalidProtocolException ipe) {
			System.out.println("BaseObject retrieveDataFrom InvalidProtocolException: "+ipe);
		} catch (ProtocolException pe) {
			System.out.println("BaseObject retrieveDataFrom ProtocolException: "+pe);
		} catch (IOException ioe) {
			System.out.println("BaseObject retrieveDataFrom IOException: "+ioe);
			return (new Error(Error.IO_ERROR)).toDataObject();
		}
		return null;
	}

	/**
	 * This method allows a component to retrieve data from other components.
	 *
	 * @param remoteHost Hostname of the component being asked for data
	 * @param remotePort Port number of the component being asked for data
	 * @param remoteId Id of the component being asked for data
	 * @param myId Id of the "user" trying to access the data
	 * @param retrieval Description of data to retrieve with any conditions
	 * @return DataObject containing data requested
	 * @see #userRequest(context.arch.comm.DataObject, String, String, int)
	 */
	public DataObject retrieveDataFrom(String remoteHost, int remotePort, String remoteId, String myId, Retrieval retrieval) {
		debugprintln(DEBUG, "\nBaseObject retrieveDataFrom");
		DataObjects v = new DataObjects();
		v.addElement(new DataObject(ID, remoteId));
		v.addElement(new DataObject("requestorId", myId));
		v.addElement(retrieval.toDataObject());
		DataObject retrieve = new DataObject(StorageObject.RETRIEVE_DATA, v);

		try {
			return userRequest(new RequestObject(retrieve, StorageObject.RETRIEVE_DATA, remoteHost, remotePort));
		} catch (EncodeException ee) {
			System.out.println("BaseObject retrieveDataFrom EncodeException: "+ee);
		} catch (DecodeException de) {
			System.out.println("BaseObject retrieveDataFrom DecodeException: "+de);
		} catch (InvalidEncoderException iee) {
			System.out.println("BaseObject retrieveDataFrom InvalidEncoderException: "+iee);
		} catch (InvalidDecoderException ide) {
			System.out.println("BaseObject retrieveDataFrom InvalidDecoderException: "+ide);
		} catch (InvalidProtocolException ipe) {
			System.out.println("BaseObject retrieveDataFrom InvalidProtocolException: "+ipe);
		} catch (ProtocolException pe) {
			System.out.println("BaseObject retrieveDataFrom ProtocolException: "+pe);
		} catch (IOException ioe) {
			System.out.println("BaseObject retrieveDataFrom IOException: "+ioe);
			return (new Error(Error.IO_ERROR)).toDataObject();
		}
		return null;
	}


	/**
	 * This method returns a vector containing AttributeNameValues objects for all the
	 * the data of a given attribute.
	 * e.g. SQL query would be "SELECT attribute FROM table"
	 *
	 * @param remoteHost Hostname of the component being asked for data
	 * @param remotePort Port number of the component being asked for data
	 * @param remoteId Id of the component being asked for data
	 * @param attribute Attribute to retrieve
	 * @return DataObject containing data requested
	 */
	//  public DataObject retrieveDataFrom(String remoteHost, int remotePort, String remoteId, String attribute) {
	//    AttributeFunctions attributes = new AttributeFunctions();
	//    attributes.addAttributeFunction(attribute);
	//    return retrieveDataFrom(remoteHost,remotePort,remoteId,attributes);
	//  }

	/**
	 * This method returns a vector containing AttributeNameValues objects for all the
	 * the data of the given attributes.
	 * e.g. SQL query would be "SELECT attribute1,attribute2,...,attributeN FROM table"
	 *
	 * @param remoteHost Hostname of the component being asked for data
	 * @param remotePort Port number of the component being asked for data
	 * @param remoteId Id of the component being asked for data
	 * @param attributes Names of the attributes to retrieve data for
	 * @return DataObject containing data requested
	 */
	//  public DataObject retrieveDataFrom(String remoteHost, int remotePort, String remoteId, AttributeFunctions attributes) {
	//    return retrieveDataFrom(remoteHost,remotePort,remoteId,new Retrieval(attributes, new Conditions()));
	//  }

	/**
	 * This method returns a vector containing AttributeNameValues objects for all the
	 * the data of the given attributes and a single condition.
	 * e.g. SQL query would be "SELECT attribute1,attribute2,...,attributeN FROM table
	 *      WHERE attributeX > valueY"
	 *
	 * @param remoteHost Hostname of the component being asked for data
	 * @param remotePort Port number of the component being asked for data
	 * @param remoteId Id of the component being asked for data
	 * @param attributes Names of the attributes to retrieve data for
	 * @param attribute Name of the attribute to do conditional on
	 * @param compare Comparison flag
	 * @param value Comparison value to use
	 * @return DataObject containing data requested
	 */
	//  public DataObject retrieveDataFrom(String remoteHost, int remotePort, String remoteId,
	//  AttributeFunctions attributes, String attribute, int compare, Object value) {
	//    Conditions conditions = new Conditions();
	//    conditions.addCondition(attribute,compare,value);
	//    return retrieveDataFrom(remoteHost,remotePort,remoteId,new Retrieval(attributes, conditions));
	//  }

	/**
	 * This method returns a vector containing AttributeNameValues objects for all the
	 * the data.
	 * e.g. SQL query would be "SELECT * FROM table
	 *
	 * @param remoteHost Hostname of the component being asked for data
	 * @param remotePort Port number of the component being asked for data
	 * @param remoteId Id of the component being asked for data
	 * @return DataObject containing data requested
	 */
	//  public DataObject retrieveDataFrom(String remoteHost, int remotePort, String remoteId) {
	//    return retrieveDataFrom(remoteHost,remotePort,remoteId,Attributes.ALL);
	//  }

	/**
	 * This method returns a vector containing AttributeNameValues objects for all the
	 * the data and a single condition.
	 * e.g. SQL query would be "SELECT * FROM table WHERE attributeX > valueY
	 *
	 * @param remoteHost Hostname of the component being asked for data
	 * @param remotePort Port number of the component being asked for data
	 * @param remoteId Id of the component being asked for data
	 * @param attribute Name of the attribute to do conditional on
	 * @param compare Comparison flag
	 * @param value Comparison value to use
	 * @return DataObject containing data requested
	 */
	//  public DataObject retrieveDataFrom(String remoteHost, int remotePort, String remoteId,
	//  String attribute, int compare, Object value) {
	//    AttributeFunctions attributes = new AttributeFunctions();
	//    attributes.addAttributeFunction(Attributes.ALL);
	//   Conditions conditions = new Conditions();
	//    conditions.addCondition(attribute,compare,value);
	//    return retrieveDataFrom(remoteHost,remotePort,remoteId,new Retrieval(attributes, conditions));
	//  }

	/**
	 * This method asks an interpreter to interpret some data.  It passes the
	 * data to be interpreted and gets back the interpreted data.
	 *
	 * @param remoteHost Hostname of the component being asked for data
	 * @param remotePort Port number of the component being asked for data
	 * @param remoteId Id of the component being asked for data
	 * @param data AttributeNameValues object containing the data to be interpreted
	 * @return DataObject containing the interpreted data
	 */
	public DataObject askInterpreter(String remoteHost, int remotePort, String remoteId,
			Attributes data) {
		debugprintln(DEBUG, "\nBaseObject askInterpreter");
		DataObjects v = new DataObjects();
		DataObject interpret = new DataObject(Constants.INTERPRET, v);
		v.addElement(new DataObject(ID, remoteId));
		v.addElement(data.toDataObject());
		try {
			return userRequest(new RequestObject(interpret, Constants.INTERPRET, remoteHost, remotePort));
		} catch (DecodeException de) {
			System.out.println("BaseObject askInterpreter() Decode: "+de);
		} catch (EncodeException ee) {
			System.out.println("BaseObject askInterpreter() Encode: "+ee);
		} catch (InvalidDecoderException ide) {
			System.out.println("BaseObject askInterpreter() InvalidDecoder: "+ide);
		} catch (InvalidEncoderException iee) {
			System.out.println("BaseObject askInterpreter() InvalidEncoder: "+iee);
		} catch (InvalidProtocolException ipe) {
			System.out.println("BaseObject askInterpreter() InvalidProtocol: "+ipe);
		} catch (ProtocolException pe) {
			System.out.println("BaseObject askInterpreter() Protocol: "+pe);
		} catch (IOException ioe) {
			System.out.println("BaseObject askInterpreter IOException: "+ioe);
			return (new Error(Error.IO_ERROR)).toDataObject();
		}
		return null;
	}

	/**
	 * This method asks an component to run some non-standard method.  It passes
	 * id of the component, attributes, and parameters and gets back the result
	 * of the method.
	 *
	 * @param remoteHost Hostname of the component
	 * @param remotePort Port number of the component
	 * @param remoteId Id of the component
	 * @param methodName Name of the method to run
	 * @param parameters AttributeNameValues object that is parameters with values
	 * @param attributes Attributes object that is parameters with values
	 * @return DataObject containing the interpreted data
	 */
	public DataObject runComponentMethod(String remoteHost, int remotePort, String remoteId,
			String methodName,Attributes parameters, Attributes attributes) {
		debugprintln(DEBUG, "\nBaseObject runcomponentMethod");
		DataObjects v = new DataObjects();
		DataObject method = new DataObject(methodName, v);
		v.addElement(new DataObject(ID, remoteId));
		if (parameters != null) {
			v.addElement(parameters.toDataObject());
		}
		if (attributes != null) {
			v.addElement(attributes.toDataObject());
		}
		try {
			return userRequest(new RequestObject(method, methodName, remoteHost, remotePort));
		} catch (DecodeException de) {
			System.out.println("BaseObject runComponentMethod() Decode: "+de);
		} catch (EncodeException ee) {
			System.out.println("BaseObject runComponentMethod() Encode: "+ee);
		} catch (InvalidDecoderException ide) {
			System.out.println("BaseObject runComponentMethod() InvalidDecoder: "+ide);
		} catch (InvalidEncoderException iee) {
			System.out.println("BaseObject runComponentMethod() InvalidEncoder: "+iee);
		} catch (InvalidProtocolException ipe) {
			System.out.println("BaseObject runComponentMethod() InvalidProtocol: "+ipe);
		} catch (ProtocolException pe) {
			System.out.println("BaseObject runComponentMethod() Protocol: "+pe);
		} catch (IOException ioe) {
			System.out.println("BaseObject runComponentMethod IOException: "+ioe);
			return (new Error(Error.IO_ERROR)).toDataObject();
		}
		return null;
	}

	/**
	 * This method requests that a widget execute an asynchronous service
	 *
	 * @param handler Handler to handle the results of the service
	 * @param serviceHost Hostname of the widget with the service
	 * @param servicePort Port number of the widget with the service
	 * @param serviceId Id of the widget with the service
	 * @param service Name of the widget service to run
	 * @param function Name of the particular service function to run
	 * @param input AttributeNameValues object to use to execute the service
	 * @param requestTag Unique tag provided by caller to identify result
	 * @return DataObject containing the results of the execution request
	 */
	@Override
	public DataObject executeAsynchronousWidgetService(AsyncServiceHandler handler, String serviceHost, int servicePort,
			String serviceId, String service, String function, Attributes input, String requestTag) {
		debugprintln(DEBUG, "\nBaseObject executeAsynchronousWidgetService");

		DataObjects v = new DataObjects();
		v.addElement(new DataObject(ID, serviceId));
		v.addElement(new DataObject(FunctionDescription.FUNCTION_SYNCHRONICITY, FunctionDescription.FUNCTION_ASYNC));
		v.addElement(new ServiceInput(
				serviceId, service, function, input, getHostAddress(), communications.getServerPort(),
				getId(),requestTag).toDataObject());
		DataObject request = new DataObject(Service.SERVICE_REQUEST, v);

		try {
			DataObject result = userRequest(new RequestObject(request, Service.SERVICE_REQUEST, serviceHost, servicePort));
			Error error = new Error(result);
			if (error.getError().equals(Error.NO_ERROR)) {
				serviceHandlers.addHandler(new AsyncServiceHandlerInfo(handler,getId(),serviceId,service,function,requestTag));
			}
			return result;
		} catch (DecodeException de) {
			System.out.println("BaseObject executeWidgetService() Decode: "+de);
		} catch (EncodeException ee) {
			System.out.println("BaseObject executeWidgetService() Encode: "+ee);
		} catch (InvalidDecoderException ide) {
			System.out.println("BaseObject executeWidgetService() InvalidDecoder: "+ide);
		} catch (InvalidEncoderException iee) {
			System.out.println("BaseObject executeWidgetService() InvalidEncoder: "+iee);
		} catch (InvalidProtocolException ipe) {
			System.out.println("BaseObject executeWidgetService() InvalidProtocol: "+ipe);
		} catch (ProtocolException pe) {
			System.out.println("BaseObject executeWidgetService() Protocol: "+pe);
		} catch (IOException ioe) {
			System.out.println("BaseObject executeWidgetService IOException: "+ioe);
			return (new Error(Error.IO_ERROR)).toDataObject();
		}
		return null;
	}

	/**
	 * This method requests that a widget execute a synchronous service
	 *
	 * @param remoteHost Hostname of the widget
	 * @param remotePort Port number of the widget
	 * @param remoteId Id of the widget
	 * @param service Name of the widget service to run
	 * @param function Name of the particular service function to run
	 * @param input AttributeNameValues object to use to execute the service
	 * @return DataObject containing the results of the execution request
	 */
	@Override
	public DataObject executeSynchronousWidgetService(
			String remoteHost, int remotePort, String remoteId,
			ServiceInput serviceInput) {

		DataObjects v = new DataObjects();
		v.addElement(new DataObject(ID, remoteId));
		v.addElement(new DataObject(FunctionDescription.FUNCTION_SYNCHRONICITY, FunctionDescription.FUNCTION_SYNC));
		v.addElement(serviceInput.toDataObject());
		DataObject request = new DataObject(Service.SERVICE_REQUEST, v);

		try {
			return userRequest(new RequestObject(request, Service.SERVICE_REQUEST, remoteHost, remotePort));
		} catch (DecodeException de) {
			System.out.println("BaseObject executeWidgetService() Decode: "+de);
		} catch (EncodeException ee) {
			System.out.println("BaseObject executeWidgetService() Encode: "+ee);
		} catch (InvalidDecoderException ide) {
			System.out.println("BaseObject executeWidgetService() InvalidDecoder: "+ide);
		} catch (InvalidEncoderException iee) {
			System.out.println("BaseObject executeWidgetService() InvalidEncoder: "+iee);
		} catch (InvalidProtocolException ipe) {
			System.out.println("BaseObject executeWidgetService() InvalidProtocol: "+ipe);
		} catch (ProtocolException pe) {
			System.out.println("BaseObject executeWidgetService() Protocol: "+pe);
		} catch (IOException ioe) {
			System.out.println("BaseObject executeWidgetService IOException: "+ioe);
			return (new Error(Error.IO_ERROR)).toDataObject();
		}
		return null;
	}

	/**
	 * This method pings a component (widget,remoteHostname, or interpreter)
	 * to make sure that it is functioning ok.
	 *
	 * @param remoteHost Hostname of the component being asked for data
	 * @param remotePort Port number of the component being asked for data
	 * @param remoteId Id of the component being asked for data
	 * @return DataObject containing the results of the ping
	 */
	public DataObject pingComponent(String remoteHost, int remotePort, String remoteId, boolean independentCom) {
		DataObjects v = new DataObjects();
		DataObject ping = new DataObject(PING, v);
		v.addElement(new DataObject(ID, remoteId));
		try {
			return userRequest(new RequestObject(ping, PING, remoteHost, remotePort));
		} catch (DecodeException de) {
			System.out.println("BaseObject pingComponent() Decode: "+de);
		} catch (EncodeException ee) {
			System.out.println("BaseObject pingComponent() Encode: "+ee);
		} catch (InvalidDecoderException ide) {
			System.out.println("BaseObject pingComponent() InvalidDecoder: "+ide);
		} catch (InvalidEncoderException iee) {
			System.out.println("BaseObject pingComponent() InvalidEncoder: "+iee);
		} catch (InvalidProtocolException ipe) {
			System.out.println("BaseObject pingComponent() InvalidProtocol: "+ipe);
		} catch (ProtocolException pe) {
			System.out.println("BaseObject pingComponent() Protocol: "+pe);
		} catch (IOException ioe) {
			System.out.println("BaseObject pingComponent IOException: "+ioe);
			return (new Error(Error.IO_ERROR)).toDataObject();
		}
		return null;
	}

	public void pingComponent(IndependentCommunication independentCommunication){
		debugprintln(DEBUG, "baseObject <ping independent> ");
		DataObjects v = new DataObjects();
		DataObject ping = new DataObject(PING, v);
		v.addElement(new DataObject(ID, independentCommunication.getRequest().getReceiverId()));
		independentCommunication.getRequest().setUrl(BaseObject.PING);
		independentCommunication.getRequest().setData(ping);
		try {
			independentUserRequest(independentCommunication);
			return ;
		}
		catch (EncodeException ee) {
			System.out.println("BaseObject <pingComponent> Encode: "+ee);
		} catch (InvalidEncoderException ide) {
			System.out.println("BaseObject <pingComponent> InvalidEncoder: "+ide);
		}
	}

	/**
	 * This method should be called when the object is going to exit
	 * under normal conditions. It stops the CommunicationsObject from
	 * receiving any more requests and exits.
	 *
	 * @see context.arch.comm.CommunicationsObject#quit()
	 */
	public void quit() {
		communications.quit();
		//System.exit(EXIT_OK);

	}

	/**
	 * This is an empty method that should be overridden by the object that
	 * extends this class.  It will contain the user-defined RPCs.
	 *
	 * @param data DataObject containing data for user-defined RPC
	 * @return result of RPC
	 * @exception context.arch.InvalidMethodException thrown if specified RPC couldn't be found
	 * @exception context.arch.MethodException thrown if specified RPC had an error
	 * @see #runMethod(String, DataObject)
	 */
	public DataObject runUserMethod(DataObject data) throws InvalidMethodException, MethodException {
		String name = data.getName();
		Error err = new Error();
		err.setError(Error.UNKNOWN_METHOD_ERROR);
		DataObjects v = new DataObjects();
		v.addElement(err.toDataObject());
		return new DataObject(name, v);
	}

	/**
	 * This method handles both the system-defined, callbacks and user-defined RPCs.
	 * If a user-defined RPC is called, this method calls runUserMethods.  If a
	 * callback is specified, this method runs userCallback.  Currently, the only
	 * system-defined methods are queryVersion, and userCallback.
	 *
	 * @param methodType Name of method to run
	 * @param data DataObject containing data for the method call
	 * @exception context.arch.InvalidMethodException thrown if specified RPC couldn't be found
	 * @exception context.arch.MethodException thrown if specified RPC had an error
	 * @see #userCallback(context.arch.comm.DataObject)
	 * @see #runUserMethod(context.arch.comm.DataObject)
	 * @see #queryVersion(context.arch.comm.DataObject)
	 * @see #setDiscoverer(context.arch.comm.DataObject)
	 */
	public DataObject runMethod(String methodType, DataObject data) throws InvalidMethodException, MethodException {
		debugprintln(DEBUG, "\nBaseObject runMethod " + methodType);
//		System.out.println("BaseObject.runMethod methodType = " + methodType);
//		System.out.println("BaseObject.runMethod data = " + data);

		if (methodType.equals(AbstractSubscriber.SUBSCRIPTION_CALLBACK)) {
			return userCallback(data);
		}
		if (methodType.equals(Constants.QUERY_VERSION)) {
			return queryVersion(data);
		}
		else if (methodType.equals(PING)) {
			return returnPing(data);
		}
		else if (methodType.equals(Service.SERVICE_RESULT)) {
			return serviceResult(data);
		}
		else if (methodType.equals(Discoverer.LOOKUP_DISCOVERER_REPLY)){
			return setDiscoverer(data);
		}
		else if (methodType.equals(Lease.LEASE_END_NOTIFICATION)){
			return leaseEndNotified(data);
		}
		else if (methodType.equals(QUERY_DESCRIPTION)){
			return getDescription();
		}
		/*else if (methodType.equals (DiscovererSubscriber.SUBSCRIPTION_CALLBACK)){
	      return discovererSubscriptionNotification(data);
	    }*/
		return runUserMethod(data);
	}

	/**
	 * This method is called when a callback message is received.  It determines which
	 * of its registered handlers should receive the callback message and passes it on
	 * accordingly.  It creates a reply message to the callback request in the form
	 * of a DataObject.  It handles all error checking.
	 *
	 * @param data DataObject containing the callback request
	 * @return DataObject containing the callback reply
	 * @see context.arch.handler.Handlers#getHandler(String)
	 * @see context.arch.handler.Handler#handleSubscriptionCallback(String, context.arch.comm.DataObject)
	 */
	public DataObject userCallback(DataObject data) {
		debugprintln(DEBUG, "\nBaseObject <userCallback> get a subscription notification");
		DataObjects v = new DataObjects();
		Error error = new Error();
		String subId = null;
		DataObject subIdObj = data.getChild(Subscriber.SUBSCRIBER_ID);
		//DataObject callbackObj = data.getDataObject(Subscriber.CALLBACK_TAG);
		DataObject value = data.getChild(Discoverer.REGISTERER); //For callback from widget, contains the component ID
		DataObject discoValue = data.getChild(Discoverer.DISCOVERER_QUERY_REPLY_CONTENT); // For callback from discoverer
		// TODO: try to separate processing discoValue and value

		debugprintln(DEBUG, "BO subId = " + subIdObj + "\nBO- att value " + value + "\nBO- discoValue" + discoValue);
		
//		new RuntimeException("BaseObject.userCallback value = " + value).printStackTrace();

		if (subIdObj == null || (value == null && discoValue == null)) {
			if (subIdObj != null) {
				v.addElement(new DataObject(Subscriber.SUBSCRIBER_ID, subIdObj.getValue()));
			}
			error.setError(Error.MISSING_PARAMETER_ERROR);
		}
		
		else {
			subId = subIdObj.getValue();
			//String callback = (String)callbackObj.getValue().firstElement();
			Handler handler = handlers.getHandler(subId);
			DataObject result = null;	// the result returned by the handler

			if (handler != null) {
				try {
					if (discoValue != null) { // discoverer callback
						debugprintln(DEBUG, "\nBO userCallback comes from discoverer");
						debugprintln(DEBUG, "\n\n\nBO sends disco notification to Handler id" + subId);
						result = handler.handleSubscriptionCallback(subId, discoValue);
					} else { // any widget callback
						result = handler.handleCallback(subId, value);
					}
				} catch (InvalidMethodException ime) {
					System.out.println("BaseObject <userCallback> error: InvalidMethod: " + ime);
					error.setError(Error.UNKNOWN_CALLBACK_ERROR);
					return new DataObject(Subscriber.SUBSCRIPTION_CALLBACK_REPLY, v);
				} catch (MethodException me) {
					System.out.println("BaseObject <userCallback> error Method: " + me);
					error.setError(Error.MISSING_PARAMETER_ERROR);
				}
				if (error.getError() == null) {
					error.setError(Error.NO_ERROR);
					if (result != null) {
						v.addElement(result);
					}
				}
			}
			else {
				error.setError(Error.UNKNOWN_SUBSCRIBER_ERROR);
			}
			v.addElement(new DataObject(Subscriber.SUBSCRIBER_ID,subId));
		}
		v.addElement(error.toDataObject());
		return new DataObject(Subscriber.SUBSCRIPTION_CALLBACK_REPLY,v);
	}

	/**
	 * Returns the list of handlers this object made. A handler is defined
	 * for a subscription to a widget, or the discoverer
	 */
	public String getListOfHandlers() {
		StringBuffer sb = new StringBuffer();		
		for (HandlerInfo hInfo : handlers.getHandlers()) {
			sb.append("\n");
			sb.append(hInfo.toString());
		}
		return sb.toString();
	}

	/**
	 * This method returns the version number of this component.
	 *
	 * @param query DataObject containing the query
	 * @return DataObject containing the results of the query
	 */
	public DataObject queryVersion(DataObject query) {
		DataObject component = query.getDataObject(ID);
		Error error = new Error();

		if (component == null) {
			error.setError(Error.INVALID_ID_ERROR);
		}
		else {
			String queryId = component.getValue();
			if (!queryId.equals(getId())) {
				error.setError(Error.INVALID_ID_ERROR);
			}
		}

		DataObjects v = new DataObjects();
		if (error.getError() == null) {
			v.addElement(new DataObject(Constants.VERSION, getVersion()));
			error.setError(Error.NO_ERROR);
		}
		v.addElement(error.toDataObject());
		return new DataObject(Constants.QUERY_VERSION_REPLY, v);
	}

	/**
	 * This method returns an error message as an answer to a ping.
	 *
	 * @param ping DataObject containing the ping request
	 * @return DataObject containing the results of the ping
	 */
	public DataObject returnPing(DataObject ping) {
		DataObject component = ping.getDataObject(ID);
		Error error = new Error();

		if (component == null) {
			error.setError(Error.INVALID_ID_ERROR);
		}
		else {
			String pingId = component.getValue();
			if (!pingId.equals(getId())) {
				error.setError(Error.INVALID_ID_ERROR);
			}
		}

		DataObjects v = new DataObjects();
		if (error.getError() == null) {
			error.setError(Error.NO_ERROR);
		}
		v.addElement(error.toDataObject());
		return new DataObject(PING_REPLY, v);
	}

	/**
	 * This method handles the results of an asynchronous service request.
	 *
	 * @param result DataObject containing the results of the aysnchronous service request
	 * @return DataObject containing a reply to the results message
	 */
	public DataObject serviceResult(DataObject result) {
		debugprintln(DEBUG, "\nBaseObject serviceResult");
		DataObject component = result.getDataObject(ID);
		Error error = new Error();
		DataObjects v = new DataObjects();
		DataObject result2 = null;

		if (component == null) {
			error.setError(Error.INVALID_ID_ERROR);
		}
		else {
			String resultId = component.getValue();
			if (!resultId.equals(getId())) {
				error.setError(Error.INVALID_ID_ERROR);
			}
			else {
				ServiceInput si = new ServiceInput(result);
				AsyncServiceHandler handler = serviceHandlers.getHandler(resultId+si.getServiceId()+si.getServiceName()+si.getFunctionName()+si.getRequestTag());
				if (handler != null) {
					try {
						result2 = handler.asynchronousServiceHandle(si.getRequestTag(),result);
					} catch (InvalidMethodException ime) {
						System.out.println("BaseObject serviceResult InvalidMethod: "+ime);
						error.setError(Error.INVALID_REQUEST_ERROR);
					} catch (MethodException me) {
						System.out.println("BaseObject serviceResult Method: "+me);
						error.setError(Error.MISSING_PARAMETER_ERROR);
					}
					if (error.getError() == null) {
						error.setError(Error.NO_ERROR);
						if (result2 != null) {
							v.addElement(result2);
						}
					}
					serviceHandlers.removeHandler(new AsyncServiceHandlerInfo(handler,resultId,si.getServiceId(),
							si.getServiceName(),si.getFunctionName(),si.getRequestTag()));
				}
				else {
					error.setError(Error.INVALID_REQUEST_ID_ERROR);
				}
			}
		}
		v.addElement(error.toDataObject());
		return new DataObject(Service.SERVICE_RESULT_REPLY, v);
	}

	/**
	 * This method returns the version number of this object.
	 *
	 * @return version number
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * This method sets the version number of this object.
	 *
	 * @param version of the object
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * This method sets the id of classes that subclass this object, for use
	 * in sending messages.
	 *
	 * @param id ID of the class
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * This method returns the id of the class that subclass this object, for use
	 * in sending messages.
	 *
	 * @return id of the class
	 */
	public String getId() {
		return id;
	}

	/**
	 * Returns a uniq id constructed as classname_hostname_port_givenUserLocation,
	 * or if there is an error, returns the user location given by the user.
	 *
	 * @param location The location string used as indentifier for a context component
	 * @param className The classname of the object
	 * @param port The port of the object
	 * @return String The uniq identification
	 * @author Agathe
	 */
	public static String createId(String className, int port){
		return BaseObject.createId(className, String.valueOf(port));
	}

	/**
	 * Convenience method to create an ID for the base object.
	 * @param className
	 * @param suffix
	 * @return className + {@link Constants.SPACER} + hostname + Constants.SPACER + suffix
	 */
	public static String createId(String className, String suffix) {
		String hostname;
		try {
			InetAddress inet = InetAddress.getLocalHost();
			hostname = inet.getHostName();
			return className + Constants.SPACER + hostname + Constants.SPACER + suffix;
		}
		catch(UnknownHostException uhe){
			System.out.println("Discoverer getId(String, int) UnknownHostException " + uhe.toString());
		}
		return className + suffix;
	}

	/**
	 * Returns the type of the component in the Context Toolkit framework.
	 * This method should be overridden.
	 *
	 * @return String
	 */
	public String getType() {
		return BaseObject.BASEOBJECT_TYPE;
	}

	/**
	 * This method gets the address of the machine this component is running on.
	 *
	 * @return the address of the machine this component is running on
	 */
	public String getHostAddress() {
		if (host != null) {
			return host;
		}

		try {
			host = InetAddress.getLocalHost().getHostAddress();
			InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException uhe) {
			System.out.println("BaseObject UnknownHost: "+uhe);
		}
		if (host == null) {
			return new String("127.0.0.1");
		}
		return host;
	}

	private static String hostname;

	/**
	 * This method gets the name of the machine this component is running on.
	 * @return the name of the machine this component is running on
	 */
	public static String getHostName() {
		if (hostname != null) { return hostname; }

		try {
			hostname = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException uhe) {
			System.out.println("BaseObject UnknownHost : "+uhe);
		}
		if (hostname == null) {
			return new String("localhost");
		}
		return hostname;
	}

	/**
	 *
	 */
	public int getPort(){
		return communications.getServerPort();
	}

	/**
	 * This method handles the lookup response from the discoverer. It sets the discoverer
	 * description. And returns an error code
	 *
	 * @param result DataObject containing the results of the discoverer description
	 * @return DataObject En error code
	 * @see context.arch.discoverer.DiscovererDescription
	 * @author Agathe
	 */
	public DataObject setDiscoverer(DataObject data){
		debugprintln(DEBUG, "\nBaseObject <setDiscoverer>");
		DataObject component = data.getDataObject(ID);

		Error error = new Error();
		DataObjects v = new DataObjects();
		//		DataObject result = null; // was not being used

		if (component == null) {
			error.setError(Error.INVALID_ID_ERROR);
		}
		else {
			String resultId = component.getValue();
			if (!resultId.equals(getId())) {
				error.setError(Error.INVALID_ID_ERROR);
			}
			else {
				// Get the information about the discoverer from the data
				DataObject disco = data.getDataObject(Discoverer.DISCOVERER);
				if (discoverer == null){
					discoverer = new DiscovererDescription();
				}
				Error errDisco = discoverer.setDescription(disco);
				error.setError(errDisco.getError());
			}
		}
		v.addElement(new DataObject(ID, discoverer.getName()));
		v.addElement(error.toDataObject());
		return new DataObject(Discoverer.LOOKUP_DISCOVERER_OK, v);
	}

	/**
	 * This method is used to send the component description to the discoverer.
	 * It sends a DISCOVERER_REGISTRATION message to the discoverer
	 * with the object description.
	 * It
	 *
	 * @return Error The error code of the registration
	 * @see context.arch.discoverer.ComponentDescription
	 * @author Agathe
	 */
	public Error discovererRegistration(){
		// For the first call without specified lease
		if (myLease != null)
			return discovererRegistration(new Lease());
		else
			return discovererRegistration(myLease);
	}

	/**
	 * This method is used to send the component description to the discoverer.
	 * It sends a DISCOVERER_REGISTRATION message to the discoverer
	 * with the object description
	 *
	 * @param lease The lease specified for the registration
	 * @return Error The error code of the registration
	 * @see context.arch.discoverer.ComponentDescription
	 * @author Agathe
	 */
	public Error discovererRegistration(Lease registrationLease){
		// If discoverer == null, we don't register, because it has not been found
		if (discoverer == null){
			return new Error("Discoverer not enabled");
		}
		
		// Update myLease field
		if (registrationLease != null){
			myLease = registrationLease;
		}
		else {
			myLease = new Lease();
		}
		
		// Get the description of this object (list of attributes,...)
		Error error = null;
		DataObject description = getDescription();
		try {
			
			DataObject result = userRequest(new RequestObject(description,Discoverer.DISCOVERER_REGISTRATION,discoverer.getHostname(),discoverer.getPort()));
			error = new Error(result);
			debugprintln(DEBUG, "\nBaseObject <discovererRegistration> error:" + error.toString());
			return error;

		} catch (IOException ioe) {
			System.out.println("BaseObject discovererRegistration IOException: "+ioe);
			error = new Error(Error.IO_ERROR);
		} catch (Exception e) {
			System.out.println("BaseObject discovererRegistration: " + e);
		}
		return error;
	}

	public ComponentDescription getComponentDescription() {
		return ComponentDescription.fromDataObject(getDescription());
	}

	/**
	 * Returns the common description of the component.
	 *
	 * @return DataObject It contains the component description
	 * @see context.arch.discoverer.ComponentDescription
	 * @see #getUserDescription()
	 * @author Agathe
	 */
	public synchronized DataObject getDescription() {
		if (discoverer==null){
			return null;
		}
		else {
			//<id>
			DataObject discoId = new DataObject(ID,discoverer.getName());
			//<registerId>
			DataObject regId = new DataObject(Discoverer.ID, getId());
			//<hostname>
			DataObject regHost = new DataObject(Discoverer.HOSTNAME, getHostName());
			//<hostAddress>
			DataObject regAddress = new DataObject(Discoverer.HOSTADDRESS, getHostAddress());
			//<port>
			DataObject regPort = new DataObject(Discoverer.PORT, new Integer(communications.getServerPort()).toString());
			//<className>
			DataObject regClass = new DataObject(Discoverer.COMPONENT_CLASSNAME, 
//					getClass().getName()); // using some proprietary naming method
					getClassname());
			//<version>
			DataObject regVersion = new DataObject(Discoverer.VERSION, getVersion());
			// Returns the type : application
			DataObject regType = new DataObject(Discoverer.TYPE, getType());

			DataObjects v1 = new DataObjects();
			v1.addElement(regId);
			v1.addElement(regHost);
			v1.addElement(regAddress);
			v1.addElement(regPort);
			v1.addElement(regClass);
			v1.addElement(regVersion);
			v1.addElement(regType);

			//Get the specific description
			DataObject doDescrip = getUserDescription();
			if (doDescrip != null) {
				// Add the content of vDescrip to v1
				for (DataObject child : doDescrip.getChildren()){
					v1.addElement(child);
				}
			}
			DataObject regist = new DataObject(Discoverer.REGISTERER, v1);
			DataObjects v2 = new DataObjects();
			v2.addElement(discoId); // the disco Id
			v2.addElement(regist); // the description
			v2.addElement(myLease.toDataObject()); // the lease

			DataObject result = new DataObject(Discoverer.DISCOVERER_REGISTRATION, v2);
			return result;
		}
	}

	/**
	 * Essentially equivalent to Object.getClass().getName(), but may be overridden,
	 * particularly, for surrogate components that use other methods (e.g. Reflection, or XML)
	 * to create instances.
	 * 
	 * @return
	 */
	public String getClassname() {
		return this.getClass().getName();
	}

	/**
	 * Returns the added description of the component
	 * That method should be overriden. If not, we deduct that
	 * the current object is an application
	 *
	 * @return DataObject The description
	 * @see context.arch.discoverer.Discoverer
	 * @author Agathe
	 */
	public DataObject getUserDescription(){
		// Does nothing
		return null;
	}


	/**
	 * This method allows to find a discoverer. The component sends a multicast
	 * message containing its id, port, hostname. The response of the existing
	 * discoverer is handled by another method. It sends a LOOOKUP_DISCOVERER
	 * message.
	 * By default, the component registers the discoverer and the end of the lease
	 * is confirmed automatically.
	 *
	 * @return Error The error object
	 */
	protected Error findDiscoverer() {
		return findDiscoverer(false, getLease(), true);
	}

	/**
	 * This method allows to find a discoverer. The component sends a multicast
	 * message containing its id, port, hostname. The response of the existing
	 * discoverer is handled by another method. It sends a LOOOKUP_DISCOVERER
	 * message.
	 * The component register the discoverer if registration is set true.
	 * It sends a default lease
	 *
	 * @param registration True if the component registers the discoverer
	 * @return Error The error object
	 * @see context.arch.discoverer.lease.Lease
	 */
	protected Error findDiscoverer(boolean registration){
		return findDiscoverer(registration, getLease(), true);
	}

	/**
	 * This method allows to find a discoverer. The component sends a multicast
	 * message containing its id, port, hostname. The response of the existing
	 * discoverer is handled by another method. It sends a LOOOKUP_DISCOVERER
	 * message.
	 *
	 * /lookup
	 *    caller
	 *        callerId
	 *        type
	 *        hostname
	 *        port
	 *    /caller
	 * /lookup
	 *
	 * @return Error An error code
	 * @see #setDiscoverer(context.arch.comm.DataObject)
	 * @see context.arch.discoverer.Discoverer
	 * @author Agathe
	 */
	protected Error findDiscoverer(boolean registration, Lease registrationLease, boolean automaticRenewal) {
		debugprintln(DEBUG, "BO <findDiscoverer>");
		discoverer = new DiscovererDescription();
		debugprintln(DEBUG, discoverer);
		this.automaticRenewal = automaticRenewal;
		
		if (discoverer != null && discoverer.getName() == null) {
			debugprintln(DEBUG, "in loop");
			//			DataObject callerId, type, hostname, port; // type was not being used
			DataObject callerId, hostname, port;
			Error error = new Error();

			callerId = new DataObject(Discoverer.CALLER_ID, getId());
			hostname = new DataObject(Discoverer.HOSTNAME, getHostAddress());
			port = new DataObject(Discoverer.PORT, new Integer(communications.getServerPort()).toString());

			DataObjects v = new DataObjects();
			v.addElement(callerId);
			v.addElement(hostname);
			v.addElement(port);

			DataObject caller = new DataObject(Discoverer.CALLER, v);
			DataObjects vCaller = new DataObjects();
			vCaller.addElement(caller);
			DataObject lookup = new DataObject(Discoverer.LOOKUP_DISCOVERER, vCaller);

			try {
				/* Find the discoverer */
				String encoded = encodeData(lookup);
				// Delay for sending a new multicast message each (delay * inc*=2)
				long delay = 1000l;
				int inc = 1;

				externLoop:
					do {
						communications.sendMulticastRequest(encoded, Discoverer.LOOKUP_DISCOVERER);
						debugprintln(DEBUG, "BO <findDiscoverer> has sent a multicast message and now is waiting for = " + delay * (long)inc);
						
						// TODO: this looping wait functionality should be delegated to DiscovererDescription
						synchronized (discoverer) {
							discoverer.waitAvailable(delay * (long)inc);
						}
						if (discoverer.available) {
							break externLoop;
						}
						inc *= 2; // lengthen waiting time
						
					} while (discoverer.getName() == null && inc < 10); // Wait for discoverer' information

				// Registers to the discoverer if asked
				Error err = null;
				if (registration) {
					err = discovererRegistration(registrationLease);
				}
				if (err == null){
					return null;
				}
				else {
					error.setError(err.getError());
					return error;
				}
			}
			catch (EncodeException ee){
				System.out.println("BaseObject findDiscoverer EncodeException " + ee);
			}
			catch (ProtocolException pe){
				System.out.println("BaseObject findDiscoverer ProtocolException " + pe);
			}
			catch (InvalidEncoderException iee){
				System.out.println("BaseObject findDiscoverer InvalidEncoderException " + iee);
			}
			catch(NullPointerException npe){
				System.out.println("BaseObject findDiscoverer NullPointerException " + npe);
				System.out.println("Vector v="+v.toString());
				System.out.println("lookup="+lookup.toString());
				npe.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * Must call this method to start the BaseObject.
	 * It calls {@link #init()} and finds the discover.
	 * @param register whether to register when finding the Discoverer. 
	 * If register == null, then it doesn't find the discoverer.
	 */
	public void start(Boolean register) {
		findDiscoverer(register);
	}

	/**
	 * This method allows to send a message to the discoverer
	 * to update its own description stored in the discoverer.
	 * It sends a DISCOVERER_UPDATE message.
	 *
	 * @param data The DataObject containing the modified fields
	 * @param updateType The update type used by the discoverer : it may be
	 * the add type or the replace type. (Discoverer.UPDATE_ADD_TYPE or Discoverer.UPDATE_REPLACE_TYPE)
	 * @return Error The error code received from the discoverer
	 * @see context.arch.discoverer.Discoverer
	 * @author Agathe
	 */
	public Error discovererUpdate() {
		// Do nothing if the discoverer is null. That is: the discovery system has
		// not been enabled
		if (discoverer == null){
			return new Error("Discoverer not enabled");
		}
		else {
			DataObject data = getDescription();
			debugprintln(DEBUG, "BaseObject <discovererUpdate>");

			// Add the tag ID
			DataObjects v1 = new DataObjects();
			v1.addElement(new DataObject(ID, discoverer.getName()));
			DataObjects v2 = new DataObjects();
			v2.addElement(new DataObject(Discoverer.ID, getId()));
			v2.addElement(data);
			v1.addElement(new DataObject(Discoverer.REGISTERER, v2));

			DataObject toSend = new DataObject(Discoverer.DISCOVERER_UPDATE, v1);

			// IndependentCommunication
			RequestObject r = new RequestObject(toSend, Discoverer.DISCOVERER_UPDATE, 
					discoverer.getHostname(), discoverer.getPort());
			try {
				IndependentCommunication ic = new IndependentCommunication(r);
				independentUserRequest(ic);
			}
			catch (EncodeException e) {
				System.out.println("BaseObject <discovererUpdate> EncodeException");
			}
			catch (InvalidEncoderException e) {
				System.out.println("BaseObject <discovererUpdate> InvalidEncoderException");
			}
			return new Error();
		}
	}

	/**
	 * This method is used to subscribe to the discoverer.
	 * 
	 *
	 *
	 */
	public void discovererSubscribe(Handler handler, DiscovererSubscriber discoSub){
		if (discoverer == null){
			return ;
		}
		debugprintln(DEBUG, "\nBO <discovererSubscribe>");
		// Sends the subscription to the discoverer
		DataObject result = discovererSendSubscription(handler, discoSub);

		// Has got the result, converts it into a Response object
		if (result != null){
			DataObject doError = result.getDataObject(Error.ERROR_CODE);
			if ( doError != null){
				debugprintln(DEBUG, "BO <discovererSubscribe> error = " + doError.toString());
			}
			else {
				DataObject answer = result.getDataObject(Discoverer.DISCOVERER_QUERY_REPLY_CONTENTS);
				debugprintln(DEBUG, "\nBO discoverQuery answer (discoqueryreplycontent) = " + answer);
			}
		}
	}

	/**
	 *
	 *
	 * to improve
	 */
	protected DataObject discovererSendSubscription(Handler handler, DiscovererSubscriber discoSub){
		if (discoverer == null){
			return null;
		}
		DataObject result;

		// Specifies the id of the discoverer
		DataObjects v1 = new DataObjects();
		v1.addElement(new DataObject(ID, discoverer.getName()));
		v1.addElement(discoSub.toDataObject());

		DataObjects v = new DataObjects();
		v.addElement(discoSub.getQuery().toDataObject());

		v1.addElement(new DataObject(Discoverer.DISCOVERER_QUERY_CONTENT, v));
		DataObject toSend = new DataObject(Discoverer.DISCOVERER_SUBSCRIBE, v1);

		//println("BaseObject discovererSubscribe - description :"+toSend.toString ());
		try {
			Error error;
			result = userRequest(toSend, Discoverer.DISCOVERER_SUBSCRIBE,discoverer.getHostname(), discoverer.getPort());
			error = new Error(result);
			if (error.getError().equals(Error.NO_ERROR)) {
				//Update the sub id
				discoSub.setSubscriptionId(result.getDataObject(AbstractSubscriber.SUBSCRIBER_ID).getValue());
				handlers.addHandler(new HandlerInfo(handler, discoSub.getSubscriptionId(), discoverer.getName(), discoverer.getHostname(),
						discoverer.getPort(), discoSub.getSubscriptionCallback()));
			}
			return result;

		} catch (EncodeException ee) {
			System.out.println("BaseObject discovererQuery EncodeException: "+ee);
		} catch (DecodeException de) {
			System.out.println("BaseObject discovererQuery DecodeException: "+de);
		} catch (InvalidEncoderException iee) {
			System.out.println("BaseObject discovererQuery InvalidEncoderException: "+iee);
		} catch (InvalidDecoderException ide) {
			System.out.println("BaseObject discovererQuery InvalidDecoderException: "+ide);
		} catch (InvalidProtocolException ipe) {
			System.out.println("BaseObject discovererQuery InvalidProtocolException: "+ipe);
		} catch (ProtocolException pe) {
			System.out.println("BaseObject discovererQuery ProtocolException: "+pe);
		} catch (IOException ioe) {
			System.out.println("BaseObject discovererQuery IOException: "+ioe);
			return (new Error(Error.IO_ERROR)).toDataObject();
		}

		return null;

	}
	/**
	 * This method allows to send a query to the discoverer.
	 *
	 * @param query The Query object containing the request for the discoverer
	 * @return ComponentDescription When the discoverer has found a context object corresponding to
	 * the request, the ComponentDescription object contains information about it, else it is null
	 * @author Agathe
	 * @see #discovererQuery(DataObject)
	 */
	public Collection<ComponentDescription> discovererQuery(AbstractQueryItem<?,?> query){
		//		System.err.println("BaseObject.discovererQuery query " + query);
		if (discoverer == null){ return null; }		
		debugprintln(DEBUG, "BO discovererQuery");

		DataObject result = discovererQuery(query.toDataObject());
		//		System.err.println("BaseObject.discovererQuery result " + result);

		// Converts the result into a Response object
		if (result != null){
			DataObject doError = result.getDataObject(Error.ERROR_CODE);
			if (doError != null) {
				debugprintln(DEBUG, "BO discoverer Query error = " + doError.toString());
				return null;
			}
			else {
				DataObject reply = result.getDataObject(Discoverer.DISCOVERER_QUERY_REPLY_CONTENTS);
				debugprintln(DEBUG, "\nBO discoverQuery answer = " + reply);

				DataObjects replyChildren = reply.getChildren();
				Collection<ComponentDescription> compResults = null;

				if (replyChildren != null){
					compResults = new ArrayList<ComponentDescription>(); // extract ComponentDescription's from DataObject's
					for (DataObject replyChild : replyChildren){
						compResults.add(ComponentDescription.fromDataObject(replyChild));
					}
				}
				return compResults;
			}
		}

		// no result
		return null;
	}

	/**
	 * This method allows to send a query to the discoverer containing the
	 * description of the context component the object would like to know.
	 * It sends a DISCOVERER_QUERY message
	 *
	 * @param query The Query containing the asked component description
	 * @return DataObject The result from the discoverer containing the first
	 * component description fitting the request
	 * @see context.arch.discoverer.Discoverer
	 * @author Agathe
	 */
	protected DataObject discovererQuery(DataObject data){
		DataObject result;

		// Adds the tag ID
		DataObjects v1 = new DataObjects();
		v1.addElement(new DataObject(ID, discoverer.getName()));

		DataObjects v2 = new DataObjects();
		v2.addElement(new DataObject(Discoverer.CALLER_ID, getId()));
		v2.addElement(new DataObject(Discoverer.HOSTNAME, getHostAddress()));
		v2.addElement(new DataObject(Discoverer.PORT, new Integer(communications.getServerPort()).toString()));
		v1.addElement(new DataObject(Discoverer.CALLER, v2));

		DataObjects v = new DataObjects();
		v.addElement(data);
		v1.addElement(new DataObject(Discoverer.DISCOVERER_QUERY_CONTENT, v));

		DataObject toSend = new DataObject(Discoverer.DISCOVERER_QUERY, v1);
		//		System.out.println("BaseObject.discovererQuery toSend: " + toSend);

		try {
			result = userRequest(toSend, Discoverer.DISCOVERER_QUERY, discoverer.getHostname(), discoverer.getPort());
			//			System.out.println("BaseObject.discovererQuery result: " + result);
			return result;

		} catch (EncodeException ee) {
			System.out.println("BaseObject discovererQuery EncodeException: "+ee);
		} catch (DecodeException de) {
			System.out.println("BaseObject discovererQuery DecodeException: "+de);
		} catch (InvalidEncoderException iee) {
			System.out.println("BaseObject discovererQuery InvalidEncoderException: "+iee);
		} catch (InvalidDecoderException ide) {
			System.out.println("BaseObject discovererQuery InvalidDecoderException: "+ide);
		} catch (InvalidProtocolException ipe) {
			System.out.println("BaseObject discovererQuery InvalidProtocolException: "+ipe);
		} catch (ProtocolException pe) {
			System.out.println("BaseObject discovererQuery ProtocolException: "+pe);
		} catch (IOException ioe) {
			System.out.println("BaseObject discovererQuery IOException: "+ioe);
			return new Error(Error.IO_ERROR).toDataObject();
		}

		return null; // some exception happened
	}

	/**
	 * This method allows to unregister from the discoverer
	 *
	 * @return Error The error message
	 * @see context.arch.discoverer.Discoverer
	 */
	public Error discovererUnregistration(){
		// Do nothing if the discoverer is null
		if (discoverer == null){
			return new Error("Discoverer not enabled");
		}
		Error error = new Error();
		DataObject toSend;

		// Adds the tag ID
		DataObjects v1 = new DataObjects();
		v1.addElement(new DataObject(ID, discoverer.getName()));
		// Adds the context object information
		DataObjects v2 = new DataObjects();
		v2.addElement(new DataObject(Discoverer.ID, getId()));
		v2.addElement(new DataObject(Discoverer.HOSTNAME, getHostAddress()));
		v2.addElement(new DataObject(Discoverer.PORT, new Integer(communications.getServerPort()).toString()));
		v1.addElement(new DataObject(Discoverer.REGISTERER, v2));

		toSend = new DataObject(Discoverer.DISCOVERER_UNREGISTRATION, v1);

		if (toSend != null) {
			debugprintln(DEBUG, "BaseObject discovererUnregistration :"+toSend.toString());
			try {
				DataObject result = userRequest(toSend,Discoverer.DISCOVERER_UNREGISTRATION,discoverer.getHostname(),discoverer.getPort());
				DataObject err;
				if ((err = result.getDataObject(Error.ERROR_CODE)) != null) {
					error.setError(err.toString());
				}
				debugprintln(DEBUG, "\nBaseObject discovererUnregistration - result : " + error);
				return error;

			} catch (EncodeException ee) {
				System.out.println("BaseObject discovererUnregistration EncodeException: "+ee);
			} catch (DecodeException de) {
				System.out.println("BaseObject discovererUnregistration DecodeException: "+de);
			} catch (InvalidEncoderException iee) {
				System.out.println("BaseObject discovererUnregistration InvalidEncoderException: "+iee);
			} catch (InvalidDecoderException ide) {
				System.out.println("BaseObject discovererUnregistration InvalidDecoderException: "+ide);
			} catch (InvalidProtocolException ipe) {
				System.out.println("BaseObject discovererUnregistration InvalidProtocolException: "+ipe);
			} catch (ProtocolException pe) {
				System.out.println("BaseObject discovererUnregistration ProtocolException: "+pe);
			} catch (IOException ioe) {
				System.out.println("BaseObject discovererUnregistration IOException: "+ioe);
				error = new Error(Error.IO_ERROR);
			}
		}

		return null;

	}

	/**
	 *
	 */
	public DataObject discovererSubscriptionNotification(DataObject data){
		debugprintln(DEBUG, "BO <discovererSubscriptionNotification>");
		DataObject res;
		DataObjects v = new DataObjects();
		Error e = new Error();
		e.setError(Error.NO_ERROR);
		v.addElement(e.toDataObject());
		res = new DataObject(DiscovererSubscriber.SUBSCRIPTION_CALLBACK_REPLY, v);
		debugprintln(DEBUG, "BO <discovererSubscriptionNotification> " + data);
		return res;

	}


	/**
	 * This method sends a lease end confirmation if necessary.
	 *
	 * @param data The data object specifying the lease end
	 * @return DataObject The reply to the notification
	 */
	protected DataObject leaseEndNotified(DataObject data){
		DataObject result = null;
		DataObjects v = new DataObjects();
		Error err = new Error();
		err.setError(Error.NO_ERROR);
		v.addElement(new DataObject(Discoverer.ID, discoverer.getName()));
		v.addElement(err.toDataObject());
		if (this.automaticRenewal){
			v.addElement(this.myLease.toDataObject());
			result = new DataObject(Lease.LEASE_RENEWAL,v);
		}
		else {
			result = new DataObject(Lease.LEASE_END, v);
		}
		return result;
	}

	/**
	 * This method allows to specify a lease used to register the discoverer.
	 * Each time the component has to register or to renew its registration, it
	 * uses this lease.
	 * This method has to be overridden by inheriting classes.
	 * If not, it returns the default lease
	 *
	 * ??? : is it useful after all???
	 *
	 * @return Lease The specified lease
	 */
	public Lease getLease(){
		if (this.myLease == null)
			return new Lease();
		else
			return myLease;
	}

	/**
	 * Set the lease used to register the discoverer
	 */
	public void setLease(Lease lease){
		myLease = lease;
	}

	/** Print a message if the DEBUG mode is active
	 *
	 * @param s Any object, even null
	 */
	public static void debugprintln(boolean DEBUG_flag, Object s){
		if (DEBUG_flag) {
			System.out.println("" + s);
			System.out.flush();
		}
	}

	/** Set the CTK classes in the DEBUG status
	 *
	 */
	public void setAllDebug(boolean baseObjectDebug, boolean communicationDebug, boolean componentDebug ) {
		if (baseObjectDebug){
			BaseObject.DEBUG = true;
		}
		if (communicationDebug) {
			BaseObject.DEBUG = true;
			context.arch.comm.CommunicationsObject.DEBUG = true;
			context.arch.comm.clients.Client.DEBUG = true;
			context.arch.comm.protocol.TCPServerSocket.DEBUG = true;
			context.arch.comm.protocol.HTTPMulticastUDPSocket.DEBUG = true;
			context.arch.comm.clients.DiscovererClient.DEBUG = true;
			context.arch.comm.protocol.HTTPClientSocket.DEBUG = true;
			context.arch.comm.protocol.MulticastUDPSocket.DEBUG = true;
			context.arch.comm.language.ParserObject.DEBUG = true;
			context.arch.comm.DataObject.DEBUG = true;
			context.arch.comm.protocol.HTTPServerSocket.DEBUG = true;
			context.arch.comm.language.SAX_XMLDecoder.DEBUG = true;
			context.arch.comm.clients.ClientsPool.DEBUG = true;
		}
	}

	/**
	 * Print a string, on the displayed frame if it is activated or on the
	 * default output stream. Flushes the output stream
	 */
	public void println(String s){
		//		if (display==true && gFrame!=null)
		//			gFrame.addObservation(s + "\n");
		//		else {
		System.out.println(s);
		//System.out.flush();
		//		}
	}

	/**
	 * Rather than manually specifying a port, use this method to find a free port for the widget.
	 * @return
	 */
	public static int findFreePort() {
		try {
			ServerSocket server = new ServerSocket(0);
			int port = server.getLocalPort();
			server.close();
			return port;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}

}
