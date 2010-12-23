package context.arch.comm;

import java.net.Socket;
import java.io.IOException;
import context.arch.comm.protocol.ProtocolException;
import context.arch.comm.protocol.RequestData;


/**
 * This interface specifies all the methods a CommunicationsClient object must support
 * allowing the details of the specific protocol used to be abstracted away.
 *
 * @see context.arch.comm.CommunicationsObject
 */
public interface CommunicationsClient{

  /** 
   * Abstract method to get the communications protocol being used
   *
   * @return the protocol being used
   */
  public abstract String getProtocol();

  /** 
   * This abstract method strips the protocol away from the received reply
   * 
   * @param socket The socket the reply is being received on
   * @return the reply with the protocol stripped away in the form of a RequestData object
   * @exception context.arch.comm.protocol.ProtocolException thrown if protocol can't be stripped away
   * @see #addRequestProtocol(String,String)
   * @see context.arch.comm.CommunicationsServer#stripRequestProtocol(Socket)
   * @see context.arch.comm.CommunicationsServer#addReplyProtocol(String)
   */
  public abstract RequestData stripReplyProtocol(Socket socket) throws ProtocolException;
	
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
  public abstract String addRequestProtocol(String data, String listener) throws ProtocolException;

  /** 
   * This abstract method adds the protocol to a request to be sent
   * 
   * @param data The request to add the protocol to
   * @param listener The recipient of the request (eg, an URL in HTTP). May be null.
   * @param type The type of the request: GET or POST.
   * @return the request with the protocol added
   * @exception context.arch.comm.protocol.ProtocolException thrown if protocol can't be added
   * @see #stripReplyProtocol(Socket)
   * @see context.arch.comm.CommunicationsServer#stripRequestProtocol(Socket)
   * @see context.arch.comm.CommunicationsServer#addReplyProtocol(String)
   */
  public abstract String addRequestProtocol(String data, String listener, String type) throws ProtocolException;

  /** 
   * This abstract method sends a request
   * 
   * @param request The request to send
   * @return the reply to the request
   */
  public abstract Socket sendRequest(String request) throws IOException;
  /** 
   * This abstract method generates an error message if a request can't
   * be handled properly, to the point where a contextual error message 
   * can still be sent as the reply
   *
   * @param DataObject containing the error message
   * @see #getFatalMessage()
   */
  public abstract DataObject getErrorMessage();

  /** 
   * This abstract method generates an fatal message if a request can't
   * be handled properly, to the point where no contextual error message 
   * can be sent as the reply
   *
   * @param String containing the fatal message
   * @see #getErrorMessage()
   */
  public abstract String getFatalMessage();

  
  
}
