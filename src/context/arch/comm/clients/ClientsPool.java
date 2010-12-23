/*
 * ClientsPool.java
 *
 * Created on June 14, 2001, 2:53 PM
 */

package context.arch.comm.clients;

import context.arch.comm.CommunicationsObject;
import context.arch.util.SynchFIFO;

/**
 * This class defines a pool of threads that can handle a communication.
 * When a BaseObject wants to send an asynchronous message to another CTK object,
 * it creates the RequestObject, added it to a IndependentCommunication object,
 * which is the parameter of the BaseObject.independentUserRequest() call.
 * This IndependentCommunication is added to a synchronized FIFO vector (SynchFIFO
 * object) from which the ClientsPool get the request to send.
 *
 * Then, as soon as a IndependentCommunication is added to the SynchFIFO, the ClientsPool object 
 * removes it to give it to a thread (Client object) from the pool. 
 * When a Client object is asked to handle an IndependentCommunication, it is
 * also removed from the pool.
 * The Client object sends the request to the given CTK object, and get the 
 * reply and the exceptions.
 *
 * In order for the BaseObject to get the reply, the IndependentCommunication contains
 * also 2 useful attributes : the DataObject reply and the Vector exceptions.
 * Then the Client object adds the reply to the same object it gets as well as 
 * the exception it could have caught.
 * After updating the IndependentCommunication object, the Client calls the 
 * BaseObject handleIndependentReply method with the parameter an object containing
 * the same IndependentCommunication object.
 *
 * At the end of the communication, the IndependentCommunication object contains
 * the original request, the reply (if any) and the exceptions that occured (if any);
 * and is sent back to the BaseObject, that does nothing by default.
 * If a widget or server or interpreter wants to handle the reply, it overrides the
 * BaseObject handleIndependentReply method, and may check the reply for each
 * request it has sent.
 *
 * In order to group a bunch of requests (for example, a widget sends many PINGs to 
 * its subscribers) we define a IndependentCommunications object to help handling
 * the replies. 
 * Before sending the requests, the widget creates an IndependentCommunications 
 * object in which all IndependentCommunication objects will be added by the Client
 * thread at the end of the communication.
 * The IndependentCommunications object is specified while calling the 
 * BaseObject independentUserRequest. If it is null, the Client thread will create 
 * a new one, add the IndependentCommunication object in it, and send it to 
 * the BaseObject handleIndependentReply method.
 *
 * @author  Agathe
 * @see context.arch.comm.clients.Client
 * @see context.arch.util.SynchFIFO
 */
public class ClientsPool extends Thread {

  /** Debug flag*/
  public static boolean DEBUG = false;
  
  /**
   * The synchronized vector of threads
   */
  protected static SynchFIFO threads = new SynchFIFO();
  
  /**
   * The number of authorized clients
   */
  public static int clientsNumber;
  
  /**
   * The default number of clients
   */
  public static int DEFAULT_CLIENTS_NUMBER = 6;
  
  /**
   * The Communications object
   */
  public CommunicationsObject comm;

  /**
   * This FIFO object contains the IndependentCommunication object that 
   * contain the RequestObject to send
   */
  protected SynchFIFO fifo;
  
  /** 
   * Creates a generic ClientsPool. It creates the pool of numberOfClients threads.
   *
   * @param numberOfClients The number of available clients in the pool
   * @param baseObject The base object
   */
  public ClientsPool (int numberOfClients, CommunicationsObject commObject) {
    clientsNumber = Math.max (1,numberOfClients);
    comm = commObject;
    
//    if (DEBUG) commObject.println("ClientsPool constructor nb th=" + clientsNumber);
    
    fifo = new SynchFIFO();
    // start clients
    for (int i = 0; i < clientsNumber; i++) {
      Client c = new Client(comm, "client#" + i);
      threads.put(c);
      c.start ();
    }
  }
  
  /**
   * Creates a ClientsPool with the default number of threads in the pool of
   * threads.
   *
   * @param baseObject The base object
   */
  public ClientsPool (CommunicationsObject commObject){
    this(ClientsPool.DEFAULT_CLIENTS_NUMBER, commObject);
  }
  
  /**
   * Creates a new empty ClientsPool.
   */
  public ClientsPool (){
    fifo = null;
    comm = null;
  }
  
  /**
   * The run method called to start the thread.
   * The ClientsPool waits for a new IndependentCommunication object in the
   * fifo object.
   * As soon as a new one is added, the ClientsPool waits for the next available
   * Client to handle the request.
   * 
   * @see context.arch.util.SynchFIFO
   * @see context.arch.comm.clients.Client
   */
  public void run(){
    while (true){
      // Get the next request or is blocked waiting for the next
      IndependentCommunication request = (IndependentCommunication) fifo.getNext ();
      // The client that will be in charge of the request
      Client c = null;
      c = (Client) threads.getNext();
//      String clientId = c.id; // not being used
      c.setRequest(request);
    }
  }
  
  
  /**
   * This method is called to add an IndependentCommunication object containing
   * the RequestObject to send, to the fifo object.
   *
   * @param request The IndependentCommunication object containing the RequestObject
   * @see context.arch.util.SynchFIFO
   */
  public synchronized void addRequest(IndependentCommunication request) {
    fifo.put (request);
  }
 
  
  /**
   *
   */
  public void stopAllIdleClients (){
    Object [] obj = threads.removeAll ();
    for (int i = 0 ; i < obj.length ; i++){
      ((Client) obj[i]).interrupt ();
    }
  }
  
  /**
   * to finish : add an array of the working Clients to interrupt them
   */
  /*public void stopAllClients(){
    stopAllIdleClients ();
    // Give the threads a quick chance to die
    try {
      Thread.sleep(1000);
    }
    catch (InterruptedException ie){}
    //Stop all remaining Clients
    synchronized (this.threads){
      for (
    }
  }*/
  
}//end
