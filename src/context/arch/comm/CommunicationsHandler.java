package context.arch.comm;

import context.arch.comm.DataObject;
import context.arch.comm.language.DecodeException;
import context.arch.comm.language.InvalidDecoderException;
import context.arch.comm.language.EncodeException;
import context.arch.comm.language.InvalidEncoderException;
import context.arch.comm.protocol.ProtocolException;
import context.arch.comm.protocol.InvalidProtocolException;
import context.arch.handler.AsyncServiceHandler;
import context.arch.service.helper.ServiceInput;
import context.arch.storage.Attributes;
import context.arch.comm.RequestObject;
import context.arch.comm.clients.IndependentCommunication;

import java.io.IOException;

/**
 * This interface specifies all the basic methods to allow communications with
 * other components.  Currently, this means calling userRequest, 
 * executeAsynchronousWidgetService and executeSynchronousWidgetService.
 */
public interface CommunicationsHandler {

  
  /**
   * Method that allows a component to communicate with another component.
   *
   * @param request The RequestObject containing the data to send, the url
   * name and the destination server hostname and port.
   * @return DataObject containing the reply to the request
   * @exception EncodeException when the encoding can't be completed successfully
   * @exception DecodeException when the decoding can't be completed successfully
   * @exception InvalidEncoderException when the encoder can't be created
   * @exception InvalidDecoderException when the decoder can't be created
   * @exception ProtocolException when the request can't be sent successfully
   * @exception InvalidProtocolException when the request can't be sent successfully due to invalid protocol use
   */
  public abstract DataObject userRequest (RequestObject request)
    throws EncodeException, InvalidProtocolException, ProtocolException, 
    DecodeException, InvalidDecoderException, 
    InvalidEncoderException, IOException;
  
  /**
   * Method that allows a component to communicate with another component.
   *
   * @param data DataObject that contains the request
   * @param url RPC tag that indicates the type of request
   * @return DataObject containing the reply to the request
   * @exception EncodeException when the encoding can't be completed successfully
   * @exception DecodeException when the decoding can't be completed successfully
   * @exception InvalidEncoderException when the encoder can't be created
   * @exception InvalidDecoderException when the decoder can't be created
   * @exception ProtocolException when the request can't be sent successfully
   * @exception InvalidProtocolException when the request can't be sent successfully due to invalid protocol use
   *   
   * @deprecated
   */
  public abstract DataObject userRequest(DataObject data, String url) 
    throws EncodeException, InvalidProtocolException, ProtocolException, 
    DecodeException, InvalidDecoderException, 
    InvalidEncoderException, IOException;

  /**
   * Method that allows a component to communicate with another component.
   *
   * @param data DataObject that contains the request
   * @param url RPC tag that indicates the type of request
   * @param server Hostname of the component to communicate with
   * @return DataObject containing the reply to the request
   * @exception EncodeException when the encoding can't be completed successfully
   * @exception DecodeException when the decoding can't be completed successfully
   * @exception InvalidEncoderException when the encoder can't be created
   * @exception InvalidDecoderException when the decoder can't be created
   * @exception ProtocolException when the request can't be sent successfully
   * @exception InvalidProtocolException when the request can't be sent successfully due to invalid protocol use
   */
  public abstract DataObject userRequest(DataObject data, String url, String server) 
    throws EncodeException, InvalidProtocolException, ProtocolException, 
    DecodeException, InvalidDecoderException, InvalidEncoderException, IOException;

  /**
   * Method that allows a component to communicate with another component.
   *
   * @param data DataObject that contains the request
   * @param url RPC tag that indicates the type of request
   * @param server Hostname of the component to communicate with
   * @param port Port number of the component to communicate with
   * @return DataObject containing the reply to the request
   * @exception EncodeException when the encoding can't be completed successfully
   * @exception DecodeException when the decoding can't be completed successfully
   * @exception InvalidEncoderException when the encoder can't be created
   * @exception InvalidDecoderException when the decoder can't be created
   * @exception ProtocolException when the request can't be sent successfully
   * @exception InvalidProtocolException when the request can't be sent successfully due to invalid protocol use
   */
  public DataObject userRequest(DataObject data, String url, String server, int port) 
    throws EncodeException, ProtocolException, InvalidProtocolException, 
    DecodeException, InvalidDecoderException, InvalidEncoderException, IOException;

  /**
   * This method requests that a widget execute an asynchronous service
   * 
   * TODO: encapsulate many of these parameters into ServiceInput
   * 
   * @param handler Handler to handle the results of the service
   * @param serviceHost Hostname of the widget with the service
   * @param servicePort Port number of the widget with the service
   * @param serviceId Id of the widget with the service
   * @param service Name of the widget service to run
   * @param function Name of the particular service function to run
   * @param input AttributeNameValues object to use to execute the service
   * @param requestTag Unique tag provided by caller to identify result
   * @return DataObject containing the results of the execution request
   */
  public DataObject executeAsynchronousWidgetService(AsyncServiceHandler handler, String serviceHost, int servicePort, 
                           String serviceId, String service, String function, Attributes input, String requestTag);

  /**
   * This method requests that a widget execute a synchronous service
   * 
   * @param remoteHost Hostname of the widget
   * @param remotePort Port number of the widget
   * @param remoteId Id of the widget
   * @param serviceInput to use to execute the service
   * @return DataObject containing the results of the execution request
   */
  public DataObject executeSynchronousWidgetService(String remoteHost, int remotePort, String remoteId,
		  ServiceInput serviceInput);

  /**
   * This method is used to send a message through a threaded communication. 
   * The request is sent by a thread in charge of the communication. 
   *
   * The request is encapsulated in a IndependentCommunication that contains 
   * a RequestObject (data to send and the recipient of the data) and the
   * reply message and the exceptions that occured during the communication.
   * If the communication result is null, the baseObject won't be notified 
   * about how the communication ended. Actually, the thread in charge 
   * of the communication won't notify the
   * baseObject, but update the request object.
   * 
   * If the result is not null, result is the IndependentCommunications object that will 
   * contain the oriinal request and the reply. At the end of the communication, 
   * the thread updates request
   * with the reply message and the exception, and add it into result.
   *
   * The base object is notified of the end of the threaded communication 
   * when the handleIndependentReply is called by the thread.
   *
   * @param request The IndependentCommunication object that contain the RequestObject,
   * and will contain after the communication the reply dataObject and the vector of exception
   * @param result The IndependentCommunications object that may contain many IndependentCommunication
   * object.
   *
   * @see context.arch.BaseObject#handleIndependentReply
   * @see context.arch.comm.clients.IndependentCommunication
   * @see context.arch.comm.clients.IndependentCommunications
   * @see context.arch.util.RequesObject
   */
  public abstract void independentUserRequest (IndependentCommunication request)
     throws EncodeException, InvalidEncoderException;
  
  
  
}// interface end
