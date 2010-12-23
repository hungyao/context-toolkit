package context.arch.comm.protocol;

import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.BufferedReader;
import java.io.StringReader;
import java.io.InputStreamReader;
import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import context.arch.comm.CommunicationsClient;
import context.arch.comm.CommunicationsObject;
import context.arch.comm.DataObject;

/**
 * This class subclasses TCPClientSocket, creating and sending HTTP requests.
 * It implements the CommunicationsClient interface
 *
 * @see context.arch.comm.protocol.TCPClientSocket
 * @see context.arch.comm.CommunicationsClient
 */
public class HTTPClientSocket extends TCPClientSocket implements CommunicationsClient {
	
	private static final Logger LOGGER = Logger.getLogger(HTTPClientSocket.class.getName());
	static {LOGGER.setLevel(Level.WARNING);} // this should be set in a configuration file

	/**
	 * Debug flag. Set to true to see debug messages.
	 */
	public static boolean DEBUG = false;

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

	/**
	 * Default port to use is 5555
	 */
	public static final int DEFAULT_PORT = 5555;

	@SuppressWarnings("unused")
	private CommunicationsObject commObject;

	/** 
	 * Basic constructor for HTTPClientSocket that calls TCPClientSocket
	 * 
	 * @param object Handle of the generic instantiating communications object
	 * @see #DEFAULT_PORT
	 * @see context.arch.comm.protocol.TCPClientSocket
	 */
	public HTTPClientSocket(CommunicationsObject object) { 
		super(DEFAULT_PORT);
		commObject = object;
		portNumber = DEFAULT_PORT;
	}

	/** 
	 * Constructor for HTTPClientSocket that calls TCPClientSocket with the
	 * given port
	 *
	 * @param object Handle of the generic instantiating communications object
	 * @param server Hostname of the remote server to connect to
	 * @param port Port to use to receive communications on
	 * @see context.arch.comm.protocol.TCPServerSocket
	 */
	public HTTPClientSocket(CommunicationsObject object, String server, Integer port) { 
		super(server, port.intValue());
		commObject = object;
	}


	/** 
	 * This method adds the HTTP protocol for a POST request
	 * (POST is the default)
	 * 
	 * @param content The request to send
	 * @return socket for the connection
	 */

	public String addRequestProtocol(String data, String url) throws ProtocolException {
		return addRequestProtocol (data, url, POST);
	}

	/** 
	 * Method that adds the HTTP protocol to a request message
	 *
	 * @param data Request message to add HTTP protocol to
	 * @param url Tag/URL to add to message
	 * @return the request with the HTTP protocol added
	 * @exception context.arch.comm.protocol.ProtocolException if the protocol
	 *		can not be added
	 */
	public String addRequestProtocol(String data, String url, String type) throws ProtocolException {
		int xmlLen = data.length();
		String eol = "\r\n";
		StringBuffer text = new StringBuffer();
		String thisMachine;
		try { // get our machine name
			InetAddress thisInet = InetAddress.getLocalHost();
			thisMachine = thisInet.getHostName();
		} catch (UnknownHostException e) {
			thisMachine = "localhost";
		}

		xmlLen += 2*eol.length(); // add length of end of lines at the end
		if (type.equals (POST)) {
			text.append(POST +  " " + url + " HTTP/1.0" + eol);
		} 
		else {
			text.append (GET + " " + url + " HTTP/1.0" + eol);
		}
		text.append("User-Agent: Context Client" + eol);
		text.append("Host: " + thisMachine + eol);
		if (type.equals (POST)) {
			text.append("Content-Type: text/xml" + eol);
			text.append("Content-Length: " + xmlLen + eol);
			text.append(eol);
			text.append(data + eol);
		}
		text.append(eol);
		//commObject.println("\nHTTPClientSocket : addRequestProtocol\n" + " CONTENT:\n" + text.toString());
		return text.toString();
	}

	/** 
	 * Method that strips away the HTTP protocol from a reply message
	 *
	 * @param socket Socket on which reply is coming from
	 * @return the reply with the HTTP protocol stripped away
	 * @exception context.arch.comm.protocol.ProtocolException if the protocol
	 *		can not be stripped away
	 */
	public RequestData stripReplyProtocol(Socket data) throws ProtocolException {

		BufferedReader bufferedReader = null;
		try {
			
			bufferedReader = new BufferedReader(new InputStreamReader(data.getInputStream()));
			String get = bufferedReader.readLine();
			
			if (get == null) { // may be because port is open, but in the midst of being shut down by the OS, or used by something else unrelated to the toolkit
				return null;
			}
			
//			StringTokenizer tokenizer = new StringTokenizer(get);
			int bytesRead = 0;
			int bytesReadThisTime = 0;
			char [] tempdata;

			String marker = "content-length:";
			while (get != null &&
					!get.toLowerCase().startsWith(marker)) {
				get = bufferedReader.readLine();
				//System.out.println("BaseObject get = " + get + ": " + (get == null));
			}
			// get may be null in later lines...don't know why, maybe when there is no network connectivity
			if (get == null) { return null; }
			
			int length = 0;
			try {
				length = new Integer(get.substring(marker.length()).trim()).intValue();
			} catch (NumberFormatException nfe) {
				System.out.println("RequestServerSocket run error: "+nfe);
				throw new ProtocolException();
			}

			if (DEBUG) {
				System.out.println ("Content-Length is: " + length);
			}

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
						System.out.println ("read " + bytesReadThisTime + " more bytes, "+bytesRead);
					}

					for (int i = 0; (i < bytesReadThisTime) && (ix + i < length); i++) {
						tempdata[ix + i] = postdata[i];
					}

					postdata = new char[length];

				}
			}

			String readerData = new String(tempdata);
			//if (DEBUG) commObject.println("\nHTTPClientSocket : stripReplyProtocol :" + readerData);

			StringReader sreader = new StringReader(readerData);

			return new RequestData(RequestData.DECODE,null,sreader);

		} catch (IOException ioe) {
			System.out.println("HTTPClientSocket stripReplyProtocol IOException: "+ioe);
			throw new ProtocolException();
		} catch (Exception e) {		// DS, 9/1/98: catch all (the request failed)
			e.printStackTrace ();
			System.out.println("HTTPClientSocket stripReplyProtocol Exception: "+e);
			throw new ProtocolException();
		}
	}

	/** 
	 * This method generates an error message if a request can't
	 * be handled properly, to the point where a contextual error message 
	 * can still be sent as the reply.  CURRENTLY RETURNS EMPTY DATAOBJECT - 
	 * NEEDS WORK (AKD).
	 *
	 * @return error message in the form of a DataObject
	 * @see #getFatalMessage()
	 */
	public DataObject getErrorMessage() {
		return new DataObject();
	}

	/** 
	 * This method generates an fatal message if a request can't
	 * be handled properly, to the point where no contextual error message 
	 * can be sent as the reply.  CURRENTLY RETURNS EMPTY STRING - 
	 * NEEDS WORK (AKD).
	 *
	 * @return fatal error message
	 * @see #getErrorMessage()
	 */
	public String getFatalMessage() {
		return new String("");
	}


	/** 
	 * This method sends a request to a remote server
	 * 
	 * @param content The request to send
	 * @return socket for the connection
	 */
	public Socket sendRequest(String content) throws IOException{
		OutputStream rawOut = null;
		String requestToSend = content;

		//if (content == null)
		//  throw new RPCException("No XML content set for request");

		if (DEBUG) {		
			//System.out.println("HTTPClientSocket <sendRequest> about to create socket " + remoteServer + " " + portNumber);
			System.out.println("HTTPClientSocket <sendRequest> CONTENT is:\n" + requestToSend);
		}

		Socket socket = null;
		try {
			socket = new Socket(remoteServer, portNumber);
		} catch (Exception e) {
			LOGGER.info("HTTPClientSocket <sendRequest> While creating socket in sendRequest (remoteServer = " + remoteServer + ", portNumber = " + portNumber + "): " + e);
			
			/*
			 * Socket may be closed because this is trying to connect to an old subscriber (read from a .log file)
			 * but the subscriber no longer exists 
			 */
		}

		if (socket != null){
			try {
				rawOut = socket.getOutputStream();
			} catch (Exception e) {
				System.out.println ("While getting OutputStream in sendRequest: " + e);
			}

			if (rawOut != null){
				BufferedOutputStream buffOut = new BufferedOutputStream(rawOut);
				DataOutputStream out = new DataOutputStream(buffOut);
				try {
					out.writeBytes(content);
					out.flush();
				} catch (Exception e) {
					System.out.println ("While creating socket in sendRequest: " + e);
				}
			}
		}
		return socket;
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

	public String toString(){
		return "HTTP client socket port=" + this.portNumber + " - protocol=" + this.getProtocol () 
		+ " - server=" + this.getServer (); 
	}

}
