/*
 * IndependentCommunication.java
 *
 * Created on June 18, 2001, 3:05 PM
 */

package context.arch.comm.clients;

import context.arch.BaseObject;
import context.arch.comm.RequestObject;
import context.arch.comm.protocol.RequestData;
import context.arch.comm.DataObject;
import context.arch.comm.language.DecodeException;
import context.arch.comm.language.InvalidDecoderException;

import java.util.Vector;

/**
 * This method is used to encapsulate all information used for an independent
 * communication. Before openning the communication, it contains the request to
 * send When the communication is closed, it contains the reply of the request
 * and/or the exceptions occured during it. At the end, this object is contained
 * in an IndependentCommunications object that allows to group several
 * IndependentCommunication objects. An IndependentCommunication object is added
 * to an IndependentCommunications object if the clients that initialized the
 * communication requires a response.
 * 
 * @author Agathe
 * @see context.arch.BaseObject
 * @see context.arch.comm.IndependentCommunications
 * @see context.arch.comm.clients.ClientsPool
 * @see context.arch.comm.clients.Client
 */
public class IndependentCommunication {

	/**
	 * The Request object containing the DataObject to send, the port and the
	 * host to send to.
	 * 
	 * @see context.arch.util.RequestObject
	 */
	protected RequestObject request;

	/**
	 * The reply of the request. This object is updated during the communication
	 * by the Client (thread) in charge of it.
	 */
	// protected DataObject reply;
	protected Object reply;

	/**
	 * A vector of exceptions occured during the communication. This object may
	 * be updated during the communication by the Client (thread) in charge of
	 * it.
	 */
	protected Vector<Exception> exceptions;

	/**
	 * This boolean says if the client wants to get the reply. True by default
	 * (the reply is sent to the client)
	 */
	protected boolean responseRequired;

	/**
	 * Just to determine the class (baseobject, widget... the reply is intended
	 */
	protected String objectIdentification;

	/**
	 * An object to store something to keep...
	 */
	public Object objectToStore;

	/**
	 * The generic IndependentCommunication constructor, that takes the request,
	 * reply, exceptions given.
	 * 
	 * @param request
	 *            The RequestObject containing the request
	 * @param reply
	 *            The reply of the request
	 * @param exceptions
	 *            The vector of exceptions
	 * @param responseRequired
	 *            When set to false, the response is put int the specified
	 *            IndependentCommunications object, but the base object
	 *            handleIndependentReply is not called. When set to false, the
	 *            method is called.
	 */
	public IndependentCommunication(RequestObject request, DataObject reply, Vector<Exception> exceptions, boolean responseRequired) {
		this.request = request;
		this.reply = reply;
		this.exceptions = exceptions;
		this.responseRequired = responseRequired;
	}

	/**
	 * Creates a new IndependentCommunication with the given RequestObject. The
	 * reply is set to null and exceptions is a empty vector.
	 * 
	 * @param request
	 *            The RequestObject containing the request
	 */
	public IndependentCommunication(RequestObject request) {
		this(request, null, new Vector<Exception>(), true);
	}

	/**
   *
   */
	public IndependentCommunication(RequestObject request, boolean responseRequired) {
		this(request, null, new Vector<Exception>(), responseRequired);
	}

	/**
	 * Returns the RequestObject
	 * 
	 * @return RequestObject
	 */
	public RequestObject getRequest() {
		return request;
	}

	/**
	 * Sets the request object
	 * 
	 * @param request
	 *            The RequestObject
	 */
	public void setRequest(RequestObject request) {
		this.request = request;
	}

	/**
	 * Returns the DataObject reply
	 * 
	 * @return DataObject
	 */
	public DataObject getDecodedReply() {
		if (reply instanceof DataObject) {
			return (DataObject) reply;
		}
		return null;
	}

	/**
	 * Returns the non decoded RequestData reply
	 * 
	 * @return RequestData
	 */
	public RequestData getNonDecodedReply() {
		if (reply instanceof RequestData) {
			return (RequestData) reply;
		}
		return null;
	}

	/**
	 * Sets the reply
	 * 
	 * @param reply
	 *            The DataObject
	 */
	public void setNonDecodedReply(RequestData reply) {
		this.reply = reply;
	}

	/**
	 * Sets the reply
	 * 
	 * @param reply
	 *            The DataObject
	 */
	public void setDecodedReply(DataObject reply) {
		this.reply = reply;
	}

	/**
   *
   */
	public void decodeReply(BaseObject baseObject) {
		DataObject decoded = null;
		if (this.reply != null && this.reply instanceof RequestData) {
			try {
				decoded = baseObject.decodeData(((RequestData) this.reply)
						.getData());
				this.setDecodedReply(decoded);
			} catch (DecodeException de) {
				System.out
						.println("IndependentCommunication <decodeReply> decode exception");
			} catch (InvalidDecoderException ide) {
				System.out
						.println("IndependentCommunication <decodeReply> invalid decoder exception");
			}
		}

	}

	/**
	 * Tests if the communication response need to be sent to the client that
	 * initialized it.
	 * 
	 * @return boolean
	 */
	public boolean getResponseRequired() {
		return this.responseRequired;
	}

	/**
	 * Set the response required condition
	 * 
	 * @param responseRequired
	 */
	public void setResponseRequired(boolean responseRequired) {
		this.responseRequired = responseRequired;
	}

	/**
	 * Tests if some exceptions occured during the communication
	 * 
	 * @return boolean True if there are exceptions, false otherwise
	 */
	public boolean thereAreExceptions() {
		return (!exceptions.isEmpty());
	}

	/**
	 * Returns the exceptions
	 * 
	 * @return Vector
	 */
	public Vector<Exception> getExceptions() {
		return exceptions;
	}

	/**
	 * Adds an exception to the exceptions of this object
	 * 
	 * @param e
	 *            The Exception object
	 */
	public void addException(Exception e) {
		exceptions.addElement(e);
	}

	/**
	 * Sets the exceptions object
	 * 
	 * @param e
	 *            The Vector of exceptions
	 */
	public void setExceptions(Vector<Exception> e) {
		exceptions = null;
		exceptions = e;
	}

	/**
	 * Tests if exception is contained in the exceptions of this object
	 * 
	 * @return boolean True if exception is contained, false otherwise
	 */
	public boolean isThereTheException(Exception exception) {
		return (exceptions.contains(exception));
	}

	/**
   *
   */
	public String getSenderClassId() {
		return this.objectIdentification;
	}

	public void setSenderClassId(String id) {
		this.objectIdentification = id;
	}

	public Object getObjectToStore() {
		return this.objectToStore;
	}

	public void setObjectToStore(Object object) {
		this.objectToStore = object;
	}

	/**
	 * Returns a printable version of this object
	 * 
	 * @return String
	 */
	public String toString() {
		StringBuffer s = new StringBuffer();
		s.append("\n<IndependentCommunication> ");
		s.append(" has been sent by the java object " + getSenderClassId());
		s.append(" - request=" + request);
		s.append(" - reply=" + reply);
		s.append(" - exceptions=" + exceptions);
		s.append(" - objectToStore=" + objectToStore);

		return s.toString();
	}
}
