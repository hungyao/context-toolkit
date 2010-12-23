/*
 * DiscovererClient.java
 *
 * Created on May 16, 2001, 11:11 AM
 */

package context.arch.comm.clients;

import context.arch.BaseObject;

/**
 * This class is used by a context component when it needs to communicate with 
 * a component while continuing a communication with another one.
 * 
 *
 * @author  Agathe
 */
public abstract class IndependentClient extends Thread {
  
 /**
  * The baseobject owning the IndependentClient class
  */
 protected BaseObject parent;

  /**
   * Creates new DiscovererClient 
   *
   * @param baseObject The base object parent
   */
  public IndependentClient(BaseObject baseObject) {
    parent = baseObject;
  }

  /**
   * The run method called to start the Thread
   */
  public void run (){
    handleCommunication();
  }

  /**
   * This method is implemented by inheriting class to process
   * communication
   */
  abstract protected void handleCommunication();
}
