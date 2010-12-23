package context.arch.comm.protocol;

import java.net.Socket;
import java.io.BufferedReader;
import java.io.StringReader;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import java.util.Date;
import java.io.IOException;

import context.arch.comm.CommunicationsServer;
import context.arch.comm.CommunicationsObject;
import context.arch.comm.DataObject;

/**
 * This class subclasses TCPServerSocket, listening for and handling HTTP requests.
 * It implements the CommunicationsServer interface
 *
 * @see context.arch.comm.protocol.TCPServerSocket
 * @see context.arch.comm.CommunicationsServer
 */
public class HTTPServerSocket extends TCPServerSocket implements CommunicationsServer {

	/**
	 * Debug flag. Set to true to see debug messages.
	 */
	public static boolean DEBUG = false;

	/**
	 * Default port for HTTP communications is 80
	 */
	public static final int DEFAULT_PORT = 80;

	/**
	 * The protocol being used is HTTP 
	 */
	public static final String PROTOCOL = "HTTP";

	/**
	 * HTTP GET request type 
	 */
	public static final String GET = "GET";

	/**
	 * HTTP POST request type
	 */
	public static final String POST = "POST";

	private CommunicationsObject commObject;

	/** 
	 * Basic constructor for HTTPServerSocket that calls TCPServerSocket
	 * 
	 * @param object Handle of the generic instantiating communications object
	 * @see #DEFAULT_PORT
	 * @see context.arch.comm.protocol.TCPServerSocket
	 */
	public HTTPServerSocket(CommunicationsObject object) { 
		super(DEFAULT_PORT);
		commObject = object;
	}

	/** 
	 * Constructor for HTTPServerSocket that calls TCPServerSocket with the
	 * given port
	 *
	 * @param object Handle of the generic instantiating communications object
	 * @param port Port to use to receive communications on
	 * @see context.arch.comm.protocol.TCPServerSocket
	 */
	public HTTPServerSocket(CommunicationsObject object, Integer port) { 
		super(port.intValue());
		commObject = object;
	}

	/** 
	 * Stub method that handles incoming HTTP requests.  It calls the equivalent
	 * method in the CommunicationsObject and then closes the socket.
	 *
	 * @param dataSocket Socket to receive HTTP data from
	 * @see context.arch.comm.CommunicationsObject#handleIncomingRequest(java.net.Socket)
	 */
	public void handleIncomingRequest(Socket dataSocket) {
		commObject.handleIncomingRequest(dataSocket);
		try {
			dataSocket.close();
		} catch (IOException ioe) {
			System.out.println("Couldn't close socket: "+ioe);
		}
	}

	/** 
	 * Method that takes a reply message and adds the necessary HTTP protocol
	 *
	 * @param data Reply to a received request
	 * @return the reply with the added HTTP protocol
	 * @exception context.arch.comm.protocol.ProtocolException if the protocol
	 *		can not be added
	 */
	public String addReplyProtocol(String data) throws ProtocolException {
		StringBuffer sb = new StringBuffer();
		sb.append("HTTP/1.0 200 OK\r\n");
		Date now = new Date();
		sb.append("Date: "+now+"\r\n");
		sb.append("Server: context/1.0\r\n");
		sb.append("Content-type: text/xml\r\n");
		sb.append("Content-length: "+data.length()+"\r\n\r\n"); // AKD added
		sb.append(data);
		//commObject.println("\nHTTPServerSocket addReplyProtocol:\n"+sb.toString());
		return (sb.toString());
	}

	/** 
	 * Method that strips the HTTP protocol from a request message.  This only
	 * deals with GET and POST headers.  If any other header is received, it will
	 * throw a ProtocolException
	 *
	 * @param data Socket the request is coming from
	 * @return the request with the HTTP protocol stripped away
	 * @exception context.arch.comm.protocol.ProtocolException if the protocol
	 *		can not be stripped away
	 */
	public RequestData stripRequestProtocol(Socket data) throws ProtocolException {
		String method;
		//    String version = "";
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(new InputStreamReader(data.getInputStream()), 2048);
			String get = bufferedReader.readLine();
			StringTokenizer tokenizer = new StringTokenizer(get);
			int bytesRead = 0;
			int bytesReadThisTime = 0;
			char [] tempdata;

			method = tokenizer.nextToken();
			if (method.equals("GET")) { 
				String file= tokenizer.nextToken();
				if (tokenizer.hasMoreTokens()) {
					//          version = 
					tokenizer.nextToken();
				}

				while ((get = bufferedReader.readLine()) != null) {
					if (get.trim().equals("")) {
						break;
					}
				}
				//commObject.println("HTTPServerSocket stripRequestProtocol:GET\n"+file);
				return new RequestData(GET,file,null);
			}
			else if (method.equals(POST)) {
				String file= tokenizer.nextToken();
				if (tokenizer.hasMoreTokens()) {
					//          version = 
					tokenizer.nextToken();
				}
				// DS, 10/27/1998: I found out that some clients don't capitalize Content-Length correctly.
				// Thus, I've modified the following line from:
				// while (!(get.startsWith("Content-Length:"))) { to:
				String marker = "content-length:";
				while (!(get.toLowerCase().startsWith(marker))) {
					get = bufferedReader.readLine();
				}
				int length = 0;
				try {
					length = new Integer(get.substring(marker.length()).trim()).intValue();
				} catch (NumberFormatException nfe) {
					System.out.println("HTTPServerSocket <stripReplyProtocol> RequestServerSocket run error: "+nfe);
					throw new ProtocolException();
				}

//				if (DEBUG) {
//					commObject.println ("HTTPServerSocket <stripReplyProtocol> Content-Length is: " + length);
//				}

				while (!(get.trim().equals(""))) {
					get = bufferedReader.readLine();
				}

				char[] postdata = new char[length];

				tempdata = new char [length];

				while (bytesRead < length) {
					if (bufferedReader.ready()){
						int ix = bytesRead;	// index to current end of tempdata

						bytesReadThisTime = bufferedReader.read(postdata, 0, length);  // DS: check we've read what we should
						bytesRead += bytesReadThisTime;

						if (DEBUG) {
							System.out.println("HTTPServerSocket <stripReplyProtocol> read " + bytesReadThisTime + " more bytes, "+bytesRead);
						}

						for (int i = 0; i < bytesReadThisTime; i++) {
							tempdata[ix + i] = postdata[i];
						}

						postdata = new char[length];

						//          Not sure why this is commented out // Daniel, 9/30/1998
						//            if (bytesRead < length) {
						//              System.out.println ("HTTPServerSocket stripRequestProtocol: could read only " + bytesRead + " bytes instead of " + length);
						//              throw new ProtocolException ();
						//            }
					}
				}

				String readerData = new String(tempdata);
				StringReader reader = new StringReader(readerData);
				//commObject.println("\nHTTPServerSocket stripRequestProtocol -POST- file:\n"+ file + "\ndata:\n"+readerData);
				return new RequestData(RequestData.DECODE,file, reader);
			}
			else {
				System.out.println("HTTPServerSocket stripRequestProtocol: invalid protocol use");
				throw new ProtocolException();
			}
		} catch (IOException ioe) {
			System.out.println("HTTPServerSocket stripRequestProtocol IOException: "+ioe);
			throw new ProtocolException();
		}
	}

	/** 
	 * This method generates an error message if a request can't
	 * be handled properly, to the point where a contextual error message 
	 * can still be sent as the reply.  NOT IMPLEMENTED YET, CURRENTLY RETURNS
	 * EMPTY DATAOBJECT - AKD
	 * @return error message in the form of a DataObject
	 * @see #getFatalMessage()
	 */
	public DataObject getErrorMessage() {
		return new DataObject();
	}

	/** 
	 * This method generates an fatal message if a request can't
	 * be handled properly, to the point where no contextual error message 
	 * can be sent as the reply.  NOT IMPLEMENTED YET, CURRENTLY RETURNS
	 * EMPTY STRING - AKD
	 *
	 * @return fatal error message
	 * @see #getErrorMessage()
	 */
	public String getFatalMessage() {
		return new String("");
	}

	/**
	 * This method stops the server from receiving more data
	 *
	 * @see context.arch.comm.protocol.TCPServerSocket#stopServer()
	 */
	public void quit() {
		super.stopServer();
	}

	/** 
	 * Method to get the communications protocol being used
	 *
	 * @return communications protocol being used
	 * @see #PROTOCOL
	 */
	public String getProtocol() {
		return PROTOCOL;
	}

}
