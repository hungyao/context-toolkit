/*
 * Client.java
 *
 * Created on June 14, 2001, 3:00 PM
 */

package context.arch.comm.clients;

import context.arch.comm.CommunicationsObject;
import context.arch.comm.protocol.RequestData;
import context.arch.comm.RequestObject;

/**
 * This class allows to get an IndependentCommunication object, to send the 
 * contained RequestObject via the BaseObject, to get the reply or the exceptions,
 * and to send back all that information to the caller BaseObject thru
 * the BaseObject handleIndependentReply method.
 *
 * @author  Agathe
 * @see context.arch.comm.clients.ClientsPool
 * @see context.arch.BaseObject
 */
public class Client extends ClientsPool {

	/** Debug flag */ 
	public static  boolean DEBUG = false;

	/**
	 * The id of the client
	 */
	public String id;

	/**
	 * The IndependentCommunication object containing the RequestObject to send
	 */
	protected IndependentCommunication indComm;


	/** 
	 * Creates new Client.
	 *
	 * @param baseObject The base object
	 * @param name The name of the client
	 */
	public Client (CommunicationsObject commObject, String name) {
		this.id = name;
		this.comm = commObject;
	}

	/**
	 * The method called to start the Client.
	 * It waits for the next IndependentCommunication object sent by the ClientsPool,
	 * and then sends the RequestObject. It adds the reply and the exception occured
	 * in the same IndependentCommunication object. If an IndependentCommunications 
	 * object is defined in the IndependentCommunication object, it adds the last
	 * in the groups and calls the handleIndependentReply of the BaseObject.
	 * It adds itself back to the pool of thread of the ClientsPool and then it 
	 * waits for the next IndependentCommunication.
	 */
	public void run () {
		while (true){
			waitForRequest();
			debugprintln("\n\nThread will send its message" + this.id);
			try {
				RequestObject ro = indComm.getRequest();
				RequestData reply = comm.sendRequest (ro);

				debugprintln("\nThread has got the reply" + this.id);
				indComm.setNonDecodedReply (reply);
			}
			catch (Exception e) {
				System.out.println(id + " exception in run " + e);
				indComm.addException(e);
			}

			// sends the results to the baseobject
			sendReply();
			// adds this thread back to the pool or not
			synchronized (ClientsPool.threads) {
				if (! (ClientsPool.threads.size () >= ClientsPool.clientsNumber)) {
					ClientsPool.threads.put(this);
				}
			}
			debugprintln("\n" +id + " <run> end");
		}
	}

	/**
	 * This method allows to wait for the next IndependentCommunication object
	 */
	protected synchronized void waitForRequest(){
		while (getRequest() == false){
			try {
				wait();
			}
			catch (Exception e) {
				System.out.println("Client <run> exception " + e);
			}
		}
	}

	/**
	 * This method tests if the IndependentCommunication is set or not
	 * 
	 * @return boolean True if the Client has an IndependentCommunication, false otherwise
	 */
	public synchronized boolean getRequest (){
		if (indComm != null) {
			return true;
		}
		return false;
	}

	/**
	 * This method sets the IndependentCommunication of the Client and notifies it.
	 *
	 * @param independentCommunication The IndependentCommunication object
	 */
	public synchronized void setRequest(IndependentCommunication independentCommunication) {
		this.indComm = independentCommunication;
		notifyAll();
	}

	/**
	 * This method allows to send to the BaseObject the result of the communication,
	 * if the client requires the response.
	 * The result is put in the original IndependentCommunication updated with the reply 
	 * and the exceptions of the communication.
	 */
	public void sendReply (){
		if (indComm.getResponseRequired ()){ //Send the response to the handler of communications
			// call the base object handleIndependentReply
			comm.handleIndependentReply(indComm);
			//System.out.println("handleIndependentReply sent");
		}
		releaseRequest();
		//System.out.println(" The sendReply of" + id + " has ended");
	}

	/**
	 * This method allows to clear the IndependentCommunication of the Client
	 */
	public synchronized void releaseRequest(){
		this.indComm = null;
	}


	/**
	 * 
	 */
	//protected static Integer extraNumber;
	/**
	 *
	 */
	/*public Client (BaseObject baseObject) {
    this (baseObject, null);
    this.id = incExtraNumber();
  }
	 */

	// These method are used to give the Client an id if the Client does not
	// belong to the pool
	/**
	 *
	 *
	 */
	/*public synchronized String incExtraNumber(){
    if (extraNumber == null) {
      extraNumber = new Integer(0);
    }
    else {
      int value = extraNumber.intValue () + 1;
      extraNumber = null;
      extraNumber = new Integer(value);
    }
    return "EXTRAclient#" + extraNumber.intValue ();
  }/

  /**
	 *
	 */
	/*public synchronized void decExtraNumber (){
    if (extraNumber != null) {
      int value = extraNumber.intValue () - 1;
      extraNumber = null;
      extraNumber = new Integer(value);
    }
  }
	 */

	/** Print a message if the DEBUG mode is active
	 *
	 * @param s Any object, even null
	 */
	public void debugprintln(Object s){
		if (DEBUG) {
			System.out.println("" + s);
			System.out.flush();
		}
	}

}// class' end
