/*
 * CommunicationsMulticast.java
 *
 * Created on 1 avril 2001, 17:12
 */

package context.arch.comm;

import java.net.DatagramPacket;

import context.arch.comm.protocol.ProtocolException;
import context.arch.comm.protocol.RequestData;


/**
 * This interface specifies all the methods a CommunicationsMulticast object must support
 * allowing the details of the specific protocol used to be abstracted away.
 *
 * @see context.arch.comm.CommunicationsObject
 */

public interface CommunicationsMulticast{

  /** 
   * Abstract method to call when starting a CommunicationsServer object
   */
  public abstract void start();

  /** 
   * Abstract method to call when stopping a CommunicationsServer object
   */
  public abstract void quit();

  /** 
   * Abstract method to get the communications protocol being used
   *
   * @return the protocol being used
   */
  public abstract String getProtocol();

  /** 
   * This abstract method strips the protocol away from the received request
   * 
   * @param packet The datagram packet the message is being received in
   * @return the message with the protocol stripped away
   * @exception context.arch.comm.protocol.ProtocolException thrown if protocol can't be stripped away
   * @see #addProtocol(String)
   */
  public abstract RequestData stripProtocol(DatagramPacket packet) 
  throws ProtocolException;

  /** 
   * This abstract method adds the protocol to a request to be sent
   * 
   * @param data The request to add the protocol to
   * @param listener The recipient of the request (eg, an URL in HTTP). May be null.
   * @return the request with the protocol added
   * @exception context.arch.comm.protocol.ProtocolException thrown if protocol can't be added
   * @see #stripProtocol(DatagramPacket)
   */
  public abstract String addProtocol(String data, String url, String type) 
  throws ProtocolException;

  /** 
   * This abstract method handles incoming datagram packet
   * 
   * @param packet The datagram packet containing the message
   */
  public abstract void handleIncomingRequest(DatagramPacket packet);

  /** 
   * This abstract method sends a message
   * 
   * @param message The message to send
   * @return the packet to send
   */
  //public abstract DatagramPacket sendMessage(String message);
    public abstract void sendMessage(String message);
}

