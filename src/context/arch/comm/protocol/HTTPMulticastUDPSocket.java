/*
 * HTTPMulticastUDPSocket.java
 *
 * Created on 1 avril 2001, 17:16
 */
package context.arch.comm.protocol;

import context.arch.comm.protocol.MulticastUDPSocket;
import context.arch.comm.CommunicationsMulticast;
import context.arch.comm.CommunicationsObject;

import java.net.InetAddress;
import java.net.DatagramPacket;
import java.net.UnknownHostException;
import java.io.BufferedReader;
import java.io.StringReader;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 * This object is able to connect to a multicast group and to send multicast
 * messages
 * @author  Agathe
 */
public class HTTPMulticastUDPSocket 
extends MulticastUDPSocket 
implements CommunicationsMulticast {

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

	private CommunicationsObject commObject;

	/** Basic constructor for HTTPMulticastSocket that calls MulticastUDPSocket
	 * 
	 * @param object Handle of the generic instantiating communications object
	 * @see context.arch.comm.protocol.MulticastUDPSocket
	 */
	public HTTPMulticastUDPSocket(CommunicationsObject object ) {
		super();
		commObject = object;
	}

	/** 
	 * Stub method that handles incoming HTTP multicast packets.  It calls the equivalent
	 * method in the CommunicationsObject and then closes the socket.
	 *
	 * @param packet DatagramPacket to receive HTTP data in
	 * @see context.arch.comm.CommunicationsObject#handleIncomingRequest(java.net.DatagramPacket)
	 */
	public void handleIncomingRequest(DatagramPacket packet) {
		//commObject.println("\nHTTPMulticastUDPSocket handleIncomingRequest (packet)");
		commObject.handleIncomingRequest(packet);
	}

	/**
	 * Abstract method to call when stopping a CommunicationsServer object
	 */
	public void quit() {
	}

	/**
	 * Abstract method to get the communications protocol being used
	 *
	 * @return the protocol being used
	 */
	public String getProtocol() {
		return null;
	}

	/**
	 * This abstract method strips the protocol away from the received request
	 *
	 * @param socket The socket the request is being received on
	 * @return the request with the protocol stripped away
	 * @exception context.arch.comm.protocol.ProtocolException thrown if protocol can't be stripped away
	 * @see #addReplyProtocol(String)
	 * @see CommunicationsClient#addRequestProtocol(String,String)
	 * @see CommunicationsClient#stripReplyProtocol(java.net.Socket)
	 */
	public RequestData stripProtocol(DatagramPacket packet) 
	throws ProtocolException {
		//commObject.println("\nHTTPMulticastUDPSocket stripProtocol (packet)");
		String method;
//		String version = "";
		BufferedReader bufferedReader = null;

		try {
			bufferedReader = new BufferedReader(new StringReader(new String(packet.getData())));
			//commObject.println("RequestData contained in the packet:"+packet.getData());

			String get = bufferedReader.readLine();
			StringTokenizer tokenizer = new StringTokenizer(get);

			int bytesRead = 0;
			int bytesReadThisTime = 0;
			char [] tempdata;

			method = tokenizer.nextToken();
			//commObject.println("HTTPMulticast - method:"+method);

			if (method.equals("GET")) { 
				String file= tokenizer.nextToken();

				if (tokenizer.hasMoreTokens()) {
//					version = 
						tokenizer.nextToken();
				}
				//commObject.println("HTTPMulticastUDPSocketget :"+file +" - version:" + version);
				while ((get = bufferedReader.readLine()) != null) {
					if (get.trim().equals("")) {
						break;
					}
				}
				//commObject.println("HTTPServerSocket stripRequestProtocol:GET\n"+file);
				// Has to define another type to return.... maybe
				return new RequestData(GET,file,null);
			}
			else if (method.equals(POST)) {
				String file= tokenizer.nextToken();
				if (tokenizer.hasMoreTokens()) {
//					version = 
						tokenizer.nextToken();
				}
				//commObject.println("HTTPMulticastUDPSocketmethod :"+method + " -version :"+version);

				String marker = "content-length:";
				while (!(get.toLowerCase().startsWith(marker))) {
					get = bufferedReader.readLine();
				}
				int length = 0;
				try {
					length = new Integer(get.substring(marker.length()).trim()).intValue();
				} catch (NumberFormatException nfe) {
					System.out.println("HTTPMulticastUDPSocket RequestServerSocket run error: "+nfe);
					throw new ProtocolException();
				}

				if (DEBUG) {
					System.out.println("HTTPMulticastUDPSocket Content-Length is: " + length);
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
							System.out.println("read " + bytesReadThisTime + " more bytes, "+bytesRead);
						}

						for (int i = 0; i < bytesReadThisTime; i++) {
							tempdata[ix + i] = postdata[i];
						}

						postdata = new char[length];

					}
				}

				String readerData = new String(tempdata);
				StringReader reader = new StringReader(readerData);
				//commObject.println("After processing-HTTPMulticast stripProtocol - data:\n"+readerData);
				return new RequestData(RequestData.DECODE,file, reader);
			}
			else {
				System.out.println("HTTPMulticast stripProtocol: invalid protocol use");
				throw new ProtocolException();
			}
		} catch (IOException ioe) {
			System.out.println("HTTPMulticast stripProtocol IOException: "+ioe);
			throw new ProtocolException();
		}
	}

	/** 
	 * This method adds the HTTP protocol for a POST request
	 * (POST is the default)
	 * 
	 * @param content The request to send
	 * @return socket for the connection
	 */  
	public String addProtocol(String data, String url) throws ProtocolException {
		return addProtocol (data, url, POST);
	}

	/**
	 * This abstract method adds the protocol to a request to be sent
	 *
	 * @param data The request to add the protocol to
	 * @param listener The recipient of the request (eg, an URL in HTTP). May be null.
	 * @return the request with the protocol added
	 * @exception context.arch.comm.protocol.ProtocolException thrown if protocol can't be added
	 * @see #stripReplyProtocol(Socket)
	 * @see context.arch.comm.CommunicationsServer#stripRequestProtocol(Socket)
	 * @see context.arch.comm.CommunicationsServer#addReplyProtocol(String)
	 */
	public String addProtocol(String data,String url, String type) 
	throws ProtocolException {
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
		//commObject.println("\nHTTPMulticastUDPSocket : addRequestProtocol\n" + " CONTENT:\n" + text.toString());
		return text.toString();
	}

	/**
	 * This abstract method sends a request
	 *
	 * @param request The request to send
	 * @return the reply to the request
	 */
	public void sendMessage(String message) {
		//commObject.println("HTTPMulticastUDPSocket sendMessage message:");
		sendPacket(message);
	}

}
