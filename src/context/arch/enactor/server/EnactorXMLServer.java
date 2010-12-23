package context.arch.enactor.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jdom.Document;
import org.jdom.input.SAXBuilder;

import context.arch.comm.DataObject;
import context.arch.comm.DataObjects;
import context.arch.enactor.Enactor;
import context.arch.enactor.EnactorComponentInfo;
import context.arch.enactor.EnactorListener;
import context.arch.enactor.EnactorParameter;
import context.arch.enactor.EnactorReference;
import context.arch.storage.Attribute;
import context.arch.storage.Attributes;

/**
 * An XML server that tunnels all enactor events through sockets to clients, using XML
 * to translate DataObjects to a language-independent serialized format. As client connections
 * are created, each client becomes an EnactorListener to the Enactor passed into the server
 * constructor. In this sense a server represents a single enactor. Different enactors use 
 * different servers and so have different addresses and ports. The specific protocol used
 * for XML is compatible with the Macromedia Flash XMLSocket. Also, the EnactorXMLServer
 * responds with HTML information about the Enactor when it receives HTTP GET requests. So,
 * the one server on the single port acts as both an XML server and as a simple HTTP server.
 * 
 * @author newbergr
 */
public class EnactorXMLServer implements Runnable {
	private static final Logger LOGGER = Logger.getLogger(EnactorXMLServer.class.getName());
	static {LOGGER.setLevel(Level.WARNING);} // this should be set in a configuration file?

	public EnactorXMLServer(Enactor e, int port) {
		this.port = port;
		this.enactor = e;
		start();
	}

	public Enactor getEnactor() {
		return enactor;
	}

	/**
	 * Begins the XML Server by started a thread that opens a server and
	 * listens for external connections.
	 */
	public void start() {
		if (serverThread == null) {
			serverThread = new Thread(this);
			serverThread.start();
		}
	}

	/**
	 * Closes server and stops its thread
	 */
	public void stop() {
		//this causes the while loop in the run() method below to break.
		stop = true;
	}

	/**
	 * when the thread is started, the server begins to listen on its port and
	 * continually accepts new clients.
	 */
	public void run() {
		LOGGER.info("Starting SituationXMLServer on port " + port);
		try {
			server = new ServerSocket(port);
			while(!stop) {
				Socket s = server.accept();
				//avoid buffering since our messages are likely to be short
				s.setTcpNoDelay(true);
				LOGGER.info("Accepted new connection, creating client");
				Client c = null;
				try {
					c = new Client(s);
					c.start();
				} catch (IOException client_ioe) {
					LOGGER.log(Level.SEVERE, "IOException when creating EnactorXMLServer client", client_ioe);
				}
			}
		} catch (IOException ioe) {
			LOGGER.log(Level.SEVERE, "IOException when running EnactorXMLServer", ioe);
		} finally {
			finish();
		}
	}

	private void finish() {
		LOGGER.info("closing server");
		try {
			if (server!= null) server.close();
			//reset stop boolean
			stop = false;
		} catch (IOException ioe) {
			LOGGER.log(Level.SEVERE, "IOException when finishing EnactorXMLServer", ioe);
		}
	}

	/**
	 * takes a set parameter message sent by a client and sets the appropriate enactor parameter.
	 */
	protected void handleSetParameter(DataObject dobject) {
		String paramName = (String) dobject.getDataObjectFirstValue("parameterName");
		EnactorParameter rp = enactor.getParameter(paramName);
		if (rp != null) {
			Attributes atts = Attributes.fromDataObject(dobject.getDataObject(Attributes.ATTRIBUTES));
			Object value = dobject.getDataObjectFirstValue("parameterValue");
			rp.setValue(atts, value);
		}
	}

	/**
	 * This should at some point programmatically build an XML document that is then 
	 * formatted in the browser via XSL. For now, just returns HTML.
	 * 
	 * TODO: improve data dictionary
	 * 
	 * @return String HTML-formatted data dictionary
	 */
	protected String getDataDictionary() {
		StringBuffer strBuf = new StringBuffer();
		strBuf.append("<HTML><HEAD><TITLE>Data Dictionary</TITLE></HEAD><BODY>");

		strBuf.append("<h1>Data Dictionary</h1>");

		strBuf.append("<h3>Parameters</h3><table border=1 cellpadding=3><tr><th>name</th><th>description</th><th>attributes</th></tr>");    
		for (EnactorParameter rp : enactor.getParameters()) {
			strBuf.append("<tr><td>").append(rp.getName()).append("</td><td>").append(rp.getDescription()).append("</td><td>");
			Attributes attsTemplate = rp.getAttributesTemplate();
			for (Attribute<?> att : attsTemplate.values()) {
				strBuf.append(att.getName());
				strBuf.append(";");
			}
			strBuf.append("</td></tr>");
		}
		strBuf.append("</table>");

		strBuf.append("<h3>References</h3><table border=1 cellpadding=3><tr><th>no.</th><th>descriptionQuery</th></tr>");
		int erCount = 0;
		for (EnactorReference rr : enactor.getReferences()) {
			erCount++;
			strBuf.append("<tr><td>").append(erCount).append("</td><td>").append(rr.getConditionQuery()).append("</td></tr>");
		}
		strBuf.append("</table>");

		strBuf.append("</BODY></HTML>");
		return strBuf.toString();
	}

	/** 
	 * This class handles client activity. Each client is a thread that monitors its
	 * socket for incoming data, and actively scans it. It handles both continuous XML
	 * input delimitied by null characters (as per the Flash XMLSocket protocol), and
	 * one-time HTTP GET requests, for which it returns HTML data dictionaries and closes.
	 * 
	 * @author newbergr
	 */
	class Client extends Thread implements EnactorListener {
		Client(Socket sock) throws IOException {
			socket = sock;
			try {
				in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
				out = new OutputStreamWriter(sock.getOutputStream());
			} catch (IOException ioe) {
				finish();
				throw ioe;
			}
		}

		public void run() {
			enactor.addListener(this);
			try {
				char[] buf = new char[1024];
				StringBuffer strBuf = new StringBuffer();
				while(true) {
					//this call to in.ready() should throw IOException when in is closed,
					//causing the thread to exit.
					if (in.ready()) {
						synchronized (this) {
							int amt = in.read(buf);
							//scan for '0' terminating character, possible multiple occurrence
							int marker = 0;
							for(int i=0; i<amt; i++) {
								if (buf[i] == (char) 0) {
									strBuf.append(buf, marker, i-marker);
									handleMessage(strBuf.toString());
									strBuf.setLength(0);
									marker = i+1;
								}
							}
							if(marker < amt) {
								strBuf.append(buf, marker, amt-marker);
							}
							//check if this is an HTTP GET request, if so send dictionary response
							String str = strBuf.toString();
							if (str.startsWith("GET") && str.indexOf("\r\n\r\n") > 0) {
								sendHTMLDataDictionary();
								in.close();
							}
						}
					}
					Thread.sleep(20);
				}
			} catch (Exception e) {
			} finally {
				finish();
			}
			LOGGER.info("Client thread finishing");
		}

		/**
		 * parses the XML message and converts it to a DataObject. currently only handles
		 * the setting of enactor parameters.
		 * 
		 * @param xmlString the XML formatted message sent by a client
		 */
		private void handleMessage(String xmlString) {
			//we don't want any exceptions from handling to affect receiving other
			//messages on this thread.
			try {
				Document doc = saxBuilder.build(new StringReader(xmlString));
				DataObject dataobject = XMLDataObjectTranslator.fromXML(doc);
				if (dataobject != null) {
					LOGGER.info("DataObject parsed with name " + dataobject.getName());
					if ("setParameter".equals(dataobject.getName())) {
						handleSetParameter(dataobject);
					}
				} else {
					LOGGER.warning("XML received that was not a proper dataobject");
				}
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, "received XML could not be parsed or handled:\n" + xmlString, e);
			}
		}

		/**
		 * sends XML data down to a client, followed by a 0 byte as 
		 * per the Flash XMLSocket protocol.
		 * 
		 * @param xml the data to send down to the client
		 */
		public synchronized void sendXML(String xml) {
			if (xml == null) return;
			try {
				out.write( xml );
				out.write( (byte)0 );
				out.flush();
			} catch (IOException ioe) {
				LOGGER.info("IOException while sending XML, closing client");
				finish();
			}
		}

		/**
		 * sends the data dictionary for the enactor, and instructs the 
		 * HTTP client that we will close its connection.
		 * 
		 * @throws IOException
		 */
		private void sendHTMLDataDictionary() throws IOException {
			out.write("HTTP/1.1 200 OK\r\nConnection: close\r\n\r\n");

			String datadict = getDataDictionary();
			out.write(datadict);
			out.flush();
			finish();
		}

		public void finish() {
			LOGGER.info("closing client");
			enactor.removeListener(this);
			try {
				in.close();
				out.close();
				socket.close();
			} catch (IOException ioe) {
				LOGGER.log(Level.SEVERE,"error while finishing client",ioe);
			}
		}

		/**
		 * converts a data object into XML and sends it to all clients.
		 * 
		 * @param result DataObject containing information for client receipt
		 */
		public void sendDataObject(DataObject result) {
			String xmlResult = XMLDataObjectTranslator.toXML(result);
			sendXML(xmlResult);
		}

		/////////////////////////
		// Begin Listener methods
		/////////////////////////

		public void componentEvaluated(EnactorComponentInfo eci) {
			DataObjects v = new DataObjects();
			v.add(eci.getCurrentState().toDataObject());
			sendDataObject(new DataObject(EnactorListener.COMPONENT_EVALUATED, v));
		}

		public void componentAdded(EnactorComponentInfo eci, Attributes paramAtts) {
			DataObjects v = new DataObjects();
			v.add(eci.getCurrentState().toDataObject());
			if (paramAtts != null) {
				DataObjects subv = new DataObjects();
				subv.add(paramAtts.toDataObject());
				v.add(new DataObject("parameters",subv));
			}
			sendDataObject(new DataObject(EnactorListener.COMPONENT_ADDED,v));
		}

		public void componentRemoved(EnactorComponentInfo eci, Attributes paramAtts) {
			DataObjects v = new DataObjects();
			v.add(eci.getCurrentState().toDataObject());
			if (paramAtts != null) {
				DataObjects subv = new DataObjects();
				subv.add(paramAtts.toDataObject());
				v.add(new DataObject("parameters",subv));
			}
			sendDataObject(new DataObject(EnactorListener.COMPONENT_REMOVED,v));
		}

		public void parameterValueChanged(EnactorParameter parameter, Attributes paramAtts, Object value) {
			DataObjects v = new DataObjects();
			if (paramAtts != null) {
				v.add(paramAtts.toDataObject());
			}
			v.add(new DataObject(EnactorListener.PARAMETER_VALUE_CHANGED+"Name",parameter.getName()));
			v.add(new DataObject(EnactorListener.PARAMETER_VALUE_CHANGED+"Value",String.valueOf(value)));
			sendDataObject(new DataObject(EnactorListener.PARAMETER_VALUE_CHANGED,v));
		}

		public void serviceExecuted(EnactorComponentInfo eci, String serviceName, String functionName, Attributes input, DataObject returnDataObject) {
			//TODO: make data object to hold serviceExecuted information and send to Client      
		}

		///////////////////////
		// End Listener methods
		///////////////////////

		private Socket socket;
		private BufferedReader in;
		private OutputStreamWriter out;
		private SAXBuilder saxBuilder = new SAXBuilder();
	}

	private int port;
	private ServerSocket server;
	private Thread serverThread;
	private Enactor enactor;
	private boolean stop = false;
}
