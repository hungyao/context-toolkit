package context.arch.comm;

import java.net.Socket;

import context.arch.comm.protocol.ProtocolException;
import context.arch.comm.protocol.RequestData;

/**
 * This interface specifies all the methods a CommunicationsServer object must support
 * allowing the details of the specific protocol used to be abstracted away.
 *
 * @see context.arch.comm.CommunicationsObject
 */
public interface CommunicationsServer {

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
   * @param socket The socket the request is being received on
   * @return the request with the protocol stripped away
   * @exception context.arch.comm.protocol.ProtocolException thrown if protocol can't be stripped away
   * @see #addReplyProtocol(String)
   * @see CommunicationsClient#addRequestProtocol(String,String)
   * @see CommunicationsClient#stripReplyProtocol(java.net.Socket)
   */
  public abstract RequestData stripRequestProtocol(Socket socket) throws ProtocolException;

  /** 
   * This abstract method strips the protocol away from the received request
   * 
   * @param reply The reply to add the protocol to
   * @return the reply with the protocol added
   * @exception context.arch.comm.protocol.ProtocolException thrown if protocol can't be added
   * @see #stripRequestProtocol(java.net.Socket)
   * @see CommunicationsClient#addRequestProtocol(String,String)
   * @see CommunicationsClient#stripReplyProtocol(java.net.Socket)
   */
  public abstract String addReplyProtocol(String reply) throws ProtocolException;

  /** 
   * This abstract method handles incoming requests on a given socket
   * 
   * @param socket The socket requests are coming in on
   */
  public abstract void handleIncomingRequest(Socket socket);

  /** 
   * This abstract method generates an error message if a request can't
   * be handled properly, to the point where a contextual error message 
   * can still be sent as the reply
   *
   * @return DataObject containing the error message
   * @see #getFatalMessage()
   */
  public abstract DataObject getErrorMessage();

  /** 
   * This abstract method generates an fatal message if a request can't
   * be handled properly, to the point where no contextual error message 
   * can be sent as the reply
   *
   * @return String containing the fatal message
   * @see #getErrorMessage()
   */
  public abstract String getFatalMessage();

}
