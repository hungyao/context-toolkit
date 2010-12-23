package context.arch.comm;

import context.arch.comm.RequestObject;
import context.arch.comm.protocol.InvalidProtocolException;
import context.arch.comm.protocol.ProtocolException;
import context.arch.comm.protocol.RequestData;
import context.arch.comm.protocol.HTTPServerSocket;
import context.arch.comm.protocol.HTTPClientSocket;
import context.arch.comm.protocol.HTTPMulticastUDPSocket;
import context.arch.comm.language.MessageHandler;
import context.arch.comm.language.DecodeException;
import context.arch.comm.language.EncodeException;
import context.arch.comm.language.InvalidDecoderException;
import context.arch.comm.language.InvalidEncoderException;
import context.arch.util.Constants;
import context.arch.MethodException;
import context.arch.InvalidMethodException;
import context.arch.discoverer.Discoverer;
import context.arch.util.Error;
import context.arch.comm.clients.ClientsPool;
import context.arch.comm.clients.IndependentCommunication;
import context.arch.BaseObject;

import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.Reader;
import java.net.DatagramPacket;

/**
 * This class handles the network communications for the calling class.
 *
 * It is able to send synchronous message, asynchronous messages (with a
 * threaded client pool) and is connected to a multicast address.
 *
 * Agathe - to do : make of the communication client a thread pool
 *
 * @see context.arch.comm.language.MessageHandler
 */
public class CommunicationsObject {

	/**
	 * Debug flag. Set to true to see debug messages.
	 */
	public static boolean DEBUG = false;

	/**
	 * The default port number to use is port 5555.
	 */
	public static final int DEFAULT_PORT = 5555;

	/**
	 * The default remote port number to use is port 5555.
	 */
	public static final int DEFAULT_REMOTE_PORT = 5555;

	/**
	 * The default remote server is localhost.
	 */
	public static final String DEFAULT_REMOTE_SERVER = "127.0.0.1";

	/**
	 * The default server protocol class to use is "context.arch.comm.protocol.HTTPServerSocket".
	 */
	public static final String DEFAULT_SERVER = "context.arch.comm.protocol.HTTPServerSocket";

	/**
	 * The default client protocol class to use is "context.arch.comm.protocol.HTTPClientSocket".
	 */
	public static final String DEFAULT_CLIENT = "context.arch.comm.protocol.HTTPClientSocket";

	/**
	 * The HTTP server protocol class.
	 */
	public static final String HTTP_SERVER = "context.arch.comm.protocol.HTTPServerSocket";

	/**
	 * The HTTP client protocol class.
	 */
	public static final String HTTP_CLIENT = "context.arch.comm.protocol.HTTPClientSocket";

	/**
	 * The default HTTP multicast server/client protocol class
	 */
	private static final String DEFAULT_MULTICAST_CLIENT_SERVER = "context.arch.comm.protocol.HTTPMulticastUDPSocket";

	/**
	 * The HTTP multicast server/client protocol class
	 */
	//	private static final String MULTICAST_CLIENT_SERVER = "context.arch.comm.protocol.HTTPMulticastUDPSocket";

	// The object that is able to be called by this object to handle a request
	private MessageHandler handler;
	// The server port
	private int serverPort;
	// The server class
	private String serverClass;
	// The communication server object that is able to send requests
	private CommunicationsServer server;
	// The communication client class
	private String clientClass;

	// To handle multicast communications
	private CommunicationsMulticast multicastCS;
	private String multicastClass = CommunicationsObject.DEFAULT_MULTICAST_CLIENT_SERVER;

	/**
	 * The threaded communication client. It uses the same protocol than
	 * the communication server.
	 */
	private ClientsPool independentCom;

	/**
	 * The default number of threads in the pool of threads.
	 */
	public static int DEFAULT_POOL_CLIENTS_NUMBER = 10;

	/**
	 * The number of threads in the pool
	 */
	//	private int poolClientsNumber;


	/**
	 * Basic constructor for CommunicationsObject using given
	 * port and protocol server class, and client class.  If either
	 * class is null, the default class is used, and if the port < 0,
	 * the default port is used.
	 *
	 * @param handler Basic message handling object
	 * @param clientClass Class to use for client communications
	 * @param serverClass Class to use for server communications
	 * @param serverPort Port the server recieves communications on
	 * @see #DEFAULT_CLIENT
	 * @see #DEFAULT_PORT
	 * @see #DEFAULT_SERVER
	 */

	public CommunicationsObject(MessageHandler handler, String clientClass, String serverClass, int serverPort, int poolClientsNumber) {
		this.handler = handler;
		if (clientClass == null) {
			this.clientClass = DEFAULT_CLIENT;
		} else {
			this.clientClass = clientClass;
		}
		if (serverClass == null) {
			this.serverClass = DEFAULT_SERVER;
		} else {
			this.serverClass = serverClass;
		}
		if (serverPort < 0) {
			this.serverPort = DEFAULT_PORT;
		} else {
			this.serverPort = serverPort;
		}
		// Starts the threaded clients pool
		int nbClients = ((poolClientsNumber < 1) ? DEFAULT_POOL_CLIENTS_NUMBER : poolClientsNumber);
		BaseObject.debugprintln(DEBUG, "CO constructor nb th=" + nbClients);
		independentCom = new ClientsPool(nbClients, this); // the threaded communication client
		independentCom.start();
	}

	/**
	 * Basic constructor for CommunicationsObject using given
	 * port and protocol server class, and client class, and given multicast
	 * protocol class.  If either class is null, the default class is used,
	 * and if the port < 0, the default port is used.
	 *
	 * @param handler Basic message handling object
	 * @param clientClass Class to use for client communications
	 * @param serverClass Class to use for server communications
	 * @param serverPort Port the server receives communications on
	 * @param multicastClass Class to use for multicast communications
	 * @see #DEFAULT_CLIENT
	 * @see #DEFAULT_PORT
	 * @see #DEFAULT_SERVER
	 */

	public CommunicationsObject(MessageHandler handler, String clientClass,
			String serverClass, int serverPort, String multicastClass) {
		this(handler, clientClass, serverClass, serverPort, DEFAULT_POOL_CLIENTS_NUMBER);
		if (multicastClass != null) {
			this.multicastClass = multicastClass;
		}
	}

	/**
	 * Basic constructor for CommunicationsObject using default
	 * port and protocol server class, and default client class,
	 *
	 * @param handler Basic message handling object
	 * @see #DEFAULT_PORT
	 * @see #DEFAULT_SERVER
	 * @see #DEFAULT_CLIENT
	 */
	public CommunicationsObject(MessageHandler handler) {
		this(handler,DEFAULT_CLIENT,DEFAULT_SERVER,DEFAULT_PORT, DEFAULT_POOL_CLIENTS_NUMBER);
	}

	/**
	 * Constructor for CommunicationsObject using given
	 * port. It uses the default server and client class.
	 *
	 * @param handler Basic message handling object
	 * @param port Port number to communicate on
	 * @see #DEFAULT_SERVER
	 * @see #DEFAULT_CLIENT
	 */
	public CommunicationsObject(MessageHandler handler, int port) {
		this(handler,DEFAULT_CLIENT,DEFAULT_SERVER,port, DEFAULT_POOL_CLIENTS_NUMBER);
	}

	/**
	 * Constructor for CommunicationsObject using default
	 * port, default client class and given protocol server.
	 *
	 * @param handler Basic message handling object
	 * @param serverClass Name of server class to use for communications
	 * @see #DEFAULT_PORT
	 * @see #DEFAULT_CLIENT
	 */
	public CommunicationsObject(MessageHandler handler, String serverClass) {
		this(handler,DEFAULT_CLIENT,serverClass,DEFAULT_PORT, DEFAULT_POOL_CLIENTS_NUMBER);
	}

	/**
	 * Constructor for CommunicationsObject using the given
	 * port and given protocol.  It uses the port for both the
	 * server object and for the client object.  The default client
	 * class and default remote hostname is used.
	 *
	 * @param handler Basic message handling object
	 * @param port Port number to communicate on
	 * @param serverClass Class to use for server communications
	 * @see #DEFAULT_CLIENT
	 */
	public CommunicationsObject(MessageHandler handler, int port, String serverClass) {
		this(handler,DEFAULT_CLIENT,serverClass,port, DEFAULT_POOL_CLIENTS_NUMBER);
	}

	/**
	 * This method creates and starts an instance of the class that deals with
	 * the underlying communications protocol being used.  This new class implements
	 * the CommunicationsServer interface.
	 * Now it also creates an instance of the client class.
	 *
	 * @exception context.arch.comm.protocol.InvalidProtocolException if the
	 * class implementing the communications protocol can't be created
	 * @see context.arch.comm.CommunicationsServer
	 * @see context.arch.comm.CommunicationsServer#start()
	 */
	public void start() throws InvalidProtocolException {
		if (System.getProperty("os.name").equals(Constants.WINCE)) {
			server = (CommunicationsServer) new HTTPServerSocket(this,new Integer(serverPort));
			multicastCS = (CommunicationsMulticast) new HTTPMulticastUDPSocket(this);
		}
		else {
			try {
				// HTTP server
				server = (CommunicationsServer) Class.forName(serverClass)
													 .getConstructor(new Class<?>[] {CommunicationsObject.class, Integer.class})
													 .newInstance(new Object[] {this, serverPort});

				// Multicast client/server
				multicastCS = (CommunicationsMulticast) Class.forName(multicastClass)
															 .getConstructor(new Class<?>[] {CommunicationsObject.class})
															 .newInstance(new Object[] {this});

			} catch (NoSuchMethodException nsme) {
				System.out.println("CommunicationsObject NoSuchMethod: "+nsme);
				throw new InvalidProtocolException(serverClass);
			} catch (InvocationTargetException ite) {
				System.out.println("CommunicationsObject InvocationTarget: "+ite);
				throw new InvalidProtocolException(serverClass);
			} catch (IllegalAccessException iae) {
				System.out.println("CommunicationsObject IllegalAccess: "+iae);
				throw new InvalidProtocolException(serverClass);
			} catch (InstantiationException ie) {
				System.out.println("CommunicationsObject Instantiation: "+ie);
				throw new InvalidProtocolException(serverClass);
			} catch (ClassNotFoundException cnfe) {
				System.out.println("CommunicationsObject ClassNotFound: "+cnfe);
				throw new InvalidProtocolException(serverClass);
			}
		}
		server.start();
		multicastCS.start();
	}

	/**
	 * This stub method calls the decodeData method in MessageHandler. The end result
	 * is a decoded message.
	 *
	 * @param message Message to be decoded
	 * @return decoded message in a DataObject
	 * @exception context.arch.comm.language.DecodeException if the message can't be decoded
	 * @exception context.arch.comm.language.InvalidDecoderException if a decoder can't be created
	 * @see context.arch.comm.language.MessageHandler#decodeData(Reader)
	 */
	public DataObject decodeData(Reader message) throws DecodeException, InvalidDecoderException {
		return handler.decodeData(message);
	}

	/**
	 * This stub method calls the encodeData method in MessageHandler. The end result
	 * is a encoded message.
	 *
	 * @param message Message to be encoded in the form of a DataObject
	 * @return encoded message in the form of a DataObject
	 * @exception context.arch.comm.language.EncodeException if the message can't be encoded
	 * @exception context.arch.comm.language.InvalidEncoderException if the encoder can't be created
	 * @see context.arch.comm.language.MessageHandler#encodeData(DataObject)
	 */
	public String encodeData(DataObject message) throws EncodeException, InvalidEncoderException {
		return handler.encodeData(message);
	}

	/**
	 * This method gets the communications protocol being used by the
	 * object that implements the CommunicationsServer interface
	 *
	 * @return the communications protocol being used
	 * @see context.arch.comm.CommunicationsServer#getProtocol()
	 */
	public String getServerProtocol() {
		return server.getProtocol();
	}

	/**
	 * This stub method adds the communications protocol to the given reply using
	 * the CommunicationsServer object
	 *
	 * @param reply The reply that needs the protocol added
	 * @return the reply with added protocol
	 * @exception context.arch.comm.protocol.ProtocolException if protocol can't
	 *		be added to the given reply
	 * @see context.arch.comm.CommunicationsServer#addReplyProtocol(String)
	 */
	public String addReplyProtocol(String reply) throws ProtocolException {
		return server.addReplyProtocol(reply);
	}

	/**
	 * This stub method strips the communications protocol from the given request using
	 * the CommunicationsServer object
	 *
	 * @param socket The socket the request is being received on
	 * @return the request with the protocol stripped away
	 * @exception context.arch.comm.protocol.ProtocolException thrown if protocol can't
	 *		be stripped from the given request
	 * @see context.arch.comm.CommunicationsServer#stripRequestProtocol(java.net.Socket)
	 */
	public RequestData stripRequestProtocol(Socket socket) throws ProtocolException {
		return server.stripRequestProtocol(socket);
	}

	/**
	 * This stub method strips the communications protocol from the given request using
	 * the CommunicationsMulticast object
	 *
	 * @param packet The multicast datagram the request is being received in
	 * @return the request with the protocol stripped away
	 * @exception context.arch.comm.protocol.ProtocolException thrown if protocol can't
	 *		be stripped from the given request
	 * @see context.arch.comm.CommunicationsServer#stripRequestProtocol(java.net.Socket)
	 */
	public RequestData stripRequestProtocol(DatagramPacket packet) throws ProtocolException {
		return multicastCS.stripProtocol(packet);
	}

	/**
	 * This private class creates and instantiates a CommunicationsClient object with
	 * the given hostname and port.
	 *
	 * @param host Hostname the client should be connecting to
	 * @param port Port number the client should be connecting to
	 * @return CommunicationsClient just created
	 * @exception context.arch.comm.protocol.InvalidProtocolException if CommunicationsClient
	 *		can't be instantiated
	 */
	private CommunicationsClient createCommunicationsClient(String host, int port) throws InvalidProtocolException {
		CommunicationsClient client = null;
		if (System.getProperty("os.name").equals(Constants.WINCE)) {
			client = (CommunicationsClient)new HTTPClientSocket(this,host,new Integer(port));
		}
		else {
			try {
				client = (CommunicationsClient) Class.forName(clientClass)
													 .getConstructor(new Class<?>[] {CommunicationsObject.class, String.class, Integer.class})
													 .newInstance(new Object[] {this, host, port});
			} catch (NoSuchMethodException nsme) {
				System.out.println("CommunicationsObject NoSuchMethod: "+nsme);
				throw new InvalidProtocolException(clientClass);
			} catch (InvocationTargetException ite) {
				System.out.println("CommunicationsObject InvocationTarget: "+ite);
				throw new InvalidProtocolException(clientClass);
			} catch (IllegalAccessException iae) {
				System.out.println("CommunicationsObject IllegalAccess: "+iae);
				throw new InvalidProtocolException(clientClass);
			} catch (InstantiationException ie) {
				System.out.println("CommunicationsObject Instantiation: "+ie);
				throw new InvalidProtocolException(clientClass);
			} catch (ClassNotFoundException cnfe) {
				System.out.println("CommunicationsObject ClassNotFound: "+cnfe);
				throw new InvalidProtocolException(clientClass);
			}
		}
		return client;
	}

	/**
	 * This method sends the given request using the CommunicationsClient object.
	 * It does so by adding the request protocol to the given request and request type, sending
	 * the request, gets the reply, strips the protocol from the reply and returns it.
	 *
	 * @param client CommunicationsClient object to use to send the given request
	 * @param request The request to send
	 * @param url The request type
	 * @return the result of the request
	 * @see context.arch.comm.CommunicationsClient#addRequestProtocol(String, String)
	 * @see context.arch.comm.CommunicationsClient#sendRequest(String)
	 * @see context.arch.comm.CommunicationsClient#stripRequestProtocol(String, String)
	 * @exception context.arch.comm.protocol.ProtocolException thrown when request fails due to mistake in protocol
	 */
	/*private RequestData sendRequest(CommunicationsClient client, String request, String url) throws ProtocolException, IOException {
    String fullRequest = null;
    RequestData reply = null;
    Socket s;
    try {
        fullRequest = client.addRequestProtocol (request, url);
    } catch (Exception e) {
          System.out.println ("While addRequestProtocol in sendRequest: " + e);
    }

    s = client.sendRequest(fullRequest);
    reply = client.stripReplyProtocol(s);
    return reply;
  }*/

	/**
	 * This method sends the given request using the CommunicationsClient object.
	 * It does so by adding the request protocol to the given request and request type, sending
	 * the request, gets the reply, strips the protocol from the reply and returns it.
	 *
	 * @param client CommunicationsClient object to use to send the given request
	 * @param request The request to send
	 * @param url The request type
	 * @param type Whether the request is a GET or a POST
	 * @return the result of the request
	 * @see context.arch.comm.CommunicationsClient#addRequestProtocol(String, String,String)
	 * @see context.arch.comm.CommunicationsClient#sendRequest(String)
	 * @see context.arch.comm.CommunicationsClient#stripRequestProtocol(String, String)
	 * @exception context.arch.comm.protocol.ProtocolException thrown when request fails due to mistake in protocol
	 */
	/*private RequestData sendRequest(CommunicationsClient client, String request, String url, String type) throws ProtocolException, IOException {
    String fullRequest = null;
    RequestData reply = null;
    try {
      fullRequest = client.addRequestProtocol(request, url, type);
    } catch(Exception e) {
        System.out.println("While addRequestProtocol in sendRequest: " + e);
    }
    Socket s = client.sendRequest(fullRequest);
    reply = client.stripReplyProtocol(s);
    return reply;
  }*/
	private RequestData sendRequest(CommunicationsClient client, RequestObject request)
	throws ProtocolException, IOException {
		BaseObject.debugprintln(DEBUG, "CommObject <sendRequest(CommClient, RequestObject)> ");
		String fullRequest = null;
		RequestData reply = null;
		
		try {
			BaseObject.debugprintln(DEBUG, "CO request=" + request);
			BaseObject.debugprintln(DEBUG, "CO type=" + request.getType());
			if (request.getType() != null){
				fullRequest = client.addRequestProtocol((String)request.getData(),request.getUrl(),request.getType());
			}
			else {
				fullRequest = client.addRequestProtocol((String)request.getData(),request.getUrl());
			}
			BaseObject.debugprintln(DEBUG, "CO fullRequest=" + fullRequest);
			
		} catch(Exception e) {
			e.printStackTrace();
			System.out.println("While addRequestProtocol in sendRequest: " + e);
		}
		
		BaseObject.debugprintln(DEBUG, "CO before send request");
		Socket socket = client.sendRequest(fullRequest);
		
		if (socket != null) {
			BaseObject.debugprintln(DEBUG, "CO before strip reply proto");
			reply = client.stripReplyProtocol(socket);
		}
		
		BaseObject.debugprintln(DEBUG, "\nReply="+reply);		
		return reply;
	}

	/**
	 * This method creates a communications client with the default hostname and port
	 * and sends the given request using the newly created CommunicationsClient object
	 *
	 * @param request The request to send
	 * @param url The request type
	 * @return the result of the request
	 * @exception context.arch.comm.protocol.InvalidProtocolException if request can't be sent successfully
	 * @exception context.arch.comm.protocol.ProtocolException thrown when request fails due to mistake in protocol
	 */
	/*public RequestData sendRequest(String request, String url) throws InvalidProtocolException, ProtocolException, IOException {
    return sendRequest(request,url,DEFAULT_REMOTE_SERVER,DEFAULT_REMOTE_PORT);
  }*/

	/**
	 * This method creates a communications client with the given hostname and default port
	 * and sends the given request using the newly created CommunicationsClient object
	 *
	 * @param request The request to send
	 * @param url The request type
	 * @param host Hostname of the server to connect to
	 * @return the result of the request
	 * @exception context.arch.comm.protocol.InvalidProtocolException if request can't be sent successfully
	 * @exception context.arch.comm.protocol.ProtocolException thrown when request fails due to mistake in protocol
	 */
	/*public RequestData sendRequest(String request, String url, String host) throws InvalidProtocolException, ProtocolException, IOException {
    return sendRequest(request, url, host, DEFAULT_REMOTE_PORT);
  }*/

	/**
	 * This method creates a communications client with the given hostname and port
	 * and sends the given request using the newly created CommunicationsClient object
	 *
	 * @param request The request to send
	 * @param url The request type
	 * @param host Hostname of the server to connect to
	 * @param port Port number of the server to connect to
	 * @return the result of the request
	 * @exception context.arch.comm.protocol.InvalidProtocolException if request can't be sent successfully
	 * @exception context.arch.comm.protocol.ProtocolException thrown when request fails due to mistake in protocol
	 */
	/*public RequestData sendRequest(String request, String url, String host, int port) throws InvalidProtocolException, ProtocolException, IOException {
    CommunicationsClient client = createCommunicationsClient(host, port);
    return sendRequest (client, request, url);
  }*/

	/**
	 * This method creates a communications client with the given hostname and port
	 * and sends the given request using the newly created CommunicationsClient object
	 *
	 * @param request The request to send
	 * @param url The request type
	 * @param host Hostname of the server to connect to
	 * @param port Port number of the server to connect to
	 * @param type Whether the request is a GET or a POST
	 * @return the result of the request
	 * @exception context.arch.comm.protocol.InvalidProtocolException if request can't be sent successfully
	 * @exception context.arch.comm.protocol.ProtocolException thrown when request fails due to mistake in protocol
	 */
	/*public RequestData sendRequest(String request, String url, String host, int port, String type) throws InvalidProtocolException, ProtocolException, IOException {
    CommunicationsClient client = createCommunicationsClient(host, port);
    return sendRequest (client, request, url, type);
  }*/
	public RequestData sendRequest(RequestObject request)
	throws InvalidProtocolException, ProtocolException, IOException {
		BaseObject.debugprintln(DEBUG, "CommObject <sendRequest (RequestObject)>");
		CommunicationsClient client = null;
		if (request.getServerHostname() != null) {
			if (request.portDefined()) {
				client = createCommunicationsClient(request.getServerHostname(), request.getPort());
			}
			else {
				client = createCommunicationsClient(request.getServerHostname(), CommunicationsObject.DEFAULT_REMOTE_PORT);
			}
		}
		else {
			client = createCommunicationsClient(CommunicationsObject.DEFAULT_REMOTE_SERVER, CommunicationsObject.DEFAULT_REMOTE_PORT);
		}
		
		BaseObject.debugprintln(DEBUG, "client=" + client);
		return sendRequest(client, request);
	}

	/**
	 *
	 */
	public void handleIndependentReply(IndependentCommunication independentComm){
		handler.handleIndependentReply(independentComm);
	}

	public void sendIndependentRequest(IndependentCommunication request){
		independentCom.addRequest(request);
	}

	/**
	 * This method sends the given request using the CommunicationsMulticast object.
	 * It does so by adding the request protocol to the given request and request type, sending
	 * the request.
	 *
	 * @param client CommunicationsClient object to use to send the given request
	 * @param request The request to send
	 * @param url The request type
	 */
	public void sendMulticastRequest(String request, String url) throws ProtocolException {
		//println("\nCommunicationsObject sendMulticastRequest request:"+request);
		String fullRequest = null;
		try {
			fullRequest = multicastCS.addProtocol(request,url, HTTPMulticastUDPSocket.POST);
		} catch (Exception e) {
			System.out.println("While addProtocol in sendMulticastRequest: " + e);
		}

		multicastCS.sendMessage(fullRequest);
	}

	/**
	 * This stub method stops the communications server from receiving more data by
	 * using the CommunicationsServer object
	 *
	 * @see context.arch.comm.CommunicationsServer#quit()
	 */
	public void quit() {
		server.quit();
	}

	/**
	 * This stub method runs the specified request using the MessageHandler
	 *
	 * @param line Single line specifying the type of request
	 * @param data Data for the specified RPC
	 * @exception context.arch.InvalidMethodException thrown if specified RPC can't be found
	 * @exception context.arch.MethodException thrown if specified RPC can't be successfully executed
	 * @see context.arch.comm.language.MessageHandler#runMethod(String, DataObject)
	 */
	public DataObject runMethod(String line, DataObject data) throws InvalidMethodException, MethodException {
		return handler.runMethod(line, data);
	}

	/**
	 * This method handles an incoming request on the given socket and sends
	 * a reply.  It should only be called by the underlying CommunicationsServer
	 * object
	 *
	 * @param socket Socket on which the request is being received
	 * @see context.arch.comm.language.MessageHandler#runMethod(String, DataObject)
	 */
	public void handleIncomingRequest(Socket socket) {
		String reply = null;
		DataObject results = null;
		String encoded = null;
		try {
			RequestData data = server.stripRequestProtocol(socket);

			BaseObject.debugprintln(DEBUG, "\n\nCommObject data received " + data.getData());

			DataObject decoded = null;
			if (data.getType().equals(RequestData.DECODE)) {
				decoded = decodeData(data.getData());
			}

			BaseObject.debugprintln(DEBUG, "CommunicationsObject decoded = "+decoded);

//			System.out.println();
//			System.out.println("decoded");
//			System.out.println("CommunicationsObject.handleIncomingRequest decoded DataObject: "+decoded);
//			System.out.println();
			results = runMethod(data.getLine(), decoded);
			BaseObject.debugprintln(DEBUG, "CommunicationsObject result is "+results);

		} catch (ProtocolException pe) {
			System.out.println("CommunicationsObject handleIncomingRequest Protocol: "+pe);
			results = server.getErrorMessage();
		} catch (DecodeException de) {
			System.out.println("CommunicationsObject handleIncomingRequest Decode: "+de);
			results = server.getErrorMessage();
		} catch (InvalidDecoderException ide) {
			System.out.println("CommunicationsObject handleIncomingRequest InvalidDecoder: "+ide);
			results = server.getErrorMessage();
		} catch (InvalidMethodException ime) {
			System.out.println("CommunicationsObject handleIncomingRequest InvalidMethod: "+ime);
			results = server.getErrorMessage();
		} catch (MethodException me) {
			System.out.println("CommunicationsObject handleIncomingRequest Method: "+me);
			results = server.getErrorMessage();
		}

		try {
			encoded = encodeData(results);

			reply = server.addReplyProtocol(encoded);
			BaseObject.debugprintln(DEBUG, "reply = "+reply);

		} catch (EncodeException ee) {
			System.out.println("CommunicationsObject handleIncomingRequest Encode: "+ee);
			ee.printStackTrace();
			reply = server.getFatalMessage();
		} catch (InvalidEncoderException iee) {
			System.out.println("CommunicationsObject handleIncomingRequest InvalidEncoder: "+iee);
			reply = server.getFatalMessage();
		} catch (ProtocolException ee) {
			System.out.println("CommunicationsObject handleIncomingRequest Protocol: "+ee);
			reply = server.getFatalMessage();
		}
		try {
			PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
			printWriter.println(reply);
			printWriter.flush();
			BaseObject.debugprintln(DEBUG, "wrote this reply to socket: " +reply);
		} catch (IOException ioe) {
			System.out.println("CommunicationsObject handleIncomingRequest IO: "+ioe);
			// what do I do here??
		}
	}

	/**
	 * Sets the class to use as the communications client.
	 *
	 * @param client Class to use as the communications client.
	 */
	public void setClientClass(String client) {
		this.clientClass = client;
	}

	/**
	 * Returns the class being used as the communications client.
	 *
	 * @return being used as the communications client.
	 */
	public String getClientClass() {
		return clientClass;
	}

	/**
	 * Sets the class to use as the communications server.
	 *
	 * @param server Class to use as the communications server.
	 */
	public void setServerClass(String server) {
		this.serverClass = server;
	}

	/**
	 * Returns the class being used as the communications client.
	 *
	 * @return class being used as the communications server.
	 */
	public String getServerClass() {
		return serverClass;
	}

	/**
	 * Sets the port to use for incoming communications (server).
	 *
	 * @param port Port to use for incoming communications (server).
	 */
	public void setServerPort(int port) {
		this.serverPort = port;
	}

	/**
	 * Returns the port being used for incoming communications (server).
	 *
	 * @return port being used for incoming communications (server).
	 */
	public int getServerPort() {
		return serverPort;
	}

	/**
	 * This method handles an incoming datagram packet from the multicast channel
	 * It should only be called by the underlying CommunicationsMulticast object
	 *
	 * @param packet DatagramPacket which is being received
	 * ??@see context.arch.comm.language.MessageHandler#runMethod(String, DataObject)
	 */
	public void handleIncomingRequest(DatagramPacket packet) {
		// Replies from the BaseObject
//		String reply = null;
		DataObject results = null;
		DataObject results_temp = null;
		String encoded = null;

		// Strip protocol and decode the packet
		try {
			RequestData data = multicastCS.stripProtocol(packet);
			DataObject decoded = null;

			if (data.getType().equals(RequestData.DECODE)) {
				decoded = decodeData(data.getData());
			}
			BaseObject.debugprintln(DEBUG, "CO <handleincomingrequest(packet)> - data.getLine():"+data.getLine());

			// Send the data request to the BaseObject
			results_temp = runMethod(data.getLine(), decoded);

			BaseObject.debugprintln(DEBUG, "result is "+results);

		} catch (ProtocolException pe) {
			System.out.println("CommunicationsObject handleIncomingRequest(packet) Protocol: "+pe);
			//results = server.getErrorMessage();
		} catch (DecodeException de) {
			System.out.println("CommunicationsObject handleIncomingRequest(packet) Decode: "+de);
			//results = server.getErrorMessage();
		} catch (InvalidDecoderException ide) {
			System.out.println("CommunicationsObject handleIncomingRequest(packet) InvalidDecoder: "+ide);
			//results = server.getErrorMessage();
		} catch (InvalidMethodException ime) {
			System.out.println("CommunicationsObject handleIncomingRequest(packet) InvalidMethod: "+ime);
			//results = server.getErrorMessage();
		} catch (MethodException me) {
			System.out.println("CommunicationsObject handleIncomingRequest(packet) Method: "+me);
			//results = server.getErrorMessage();
		}

		// id the result does not content errors, one sends a reply to the caller, else nothing
		DataObject error = results_temp.getDataObject(Error.ERROR_CODE);
		BaseObject.debugprintln(DEBUG, "error? " + error);
		String yesno = (error==null? "yes":"no");
		BaseObject.debugprintln(DEBUG, "CommunicationsObject : does it reply to the multicast message ??? ( errorcode empty?)" + yesno);

		if (error == null){
			String componentName = null ,
			componentHost = null;
			int componentPort = CommunicationsObject.DEFAULT_PORT;
			String replyName = null;
			RequestObject request = null;
			try {
				BaseObject.debugprintln(DEBUG, "commobject handleincomingrequest(packet) - before sending response...");

				// Extract the destination component address from the result

				replyName = results_temp.getName();
				DataObject dest = results_temp.getDataObject(replyName);
				DataObject componentAdd = dest.getDataObject(Discoverer.TEMP_DEST);

				componentName = componentAdd.getDataObject(Discoverer.ID).getValue();
				componentHost = componentAdd.getDataObject(Discoverer.HOSTNAME).getValue();
				Integer intPort = new Integer(componentAdd.getDataObject(Discoverer.PORT).getValue());
				componentPort = intPort.intValue();

				//Create another DataRequest to reply
				DataObjects vContent = results_temp.getChildren();
				DataObject doContent = (DataObject) vContent.elementAt(1);
				DataObjects contentToSend = doContent.getChildren();
				results = new DataObject(replyName, contentToSend);

				request = new RequestObject(results, replyName, componentHost, componentPort, HTTPClientSocket.POST);
				request.setEncodedData(encodeData(request.getNonEncodedData()));

			} catch (EncodeException ee) {
				System.out.println("CommunicationsObject handleIncomingRequest(packet) Encode: "+ee);
				//reply = server.getFatalMessage();
			} catch (InvalidEncoderException iee) {
				System.out.println("CommunicationsObject handleIncomingRequest(packet) InvalidEncoder: "+iee);
				//reply = server.getFatalMessage();
			}

			try{
				BaseObject.debugprintln(DEBUG, "try to send : \n"+encoded +"\n\tto "+ componentName+"\n\ton "+ componentHost+"\n\tat "+ componentPort );
//				RequestData descriptionComponent = // never read 
					sendRequest(request);
					
			} catch (InvalidProtocolException ipe) {
				System.out.println("CommunicationsObject handleIncomingRequest(packet) InvalidProtocolException: "+ipe);
			} catch (ProtocolException pe) {
				System.out.println("CommunicationsObject handleIncomingRequest(packet) ProtocolException: "+pe);
			} catch (IOException ioe) {
				System.out.println("CommunicationsObject handleIncomingRequest(packet) IOException: "+ioe);
			}
		}
	}
	
}
