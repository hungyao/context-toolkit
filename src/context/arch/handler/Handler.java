package context.arch.handler;

import context.arch.comm.DataObject;
import context.arch.InvalidMethodException;
import context.arch.MethodException;
import context.arch.comm.clients.IndependentCommunication;

/**
 * This empty interface allows the Handlers class to keep track of
 * handlers.  Any component that subscribes to widgets should extend this interface.
 *
 * @see context.arch.handler.Handlers
 */
public interface Handler {

  /**
   * This abstract method is used to generically handle any callbacks that a widget
   * may support.  A context widget will call handle when a callback is triggered
   * in the widget and the handler has subscribed to that callback.
   *
   * @param callback The name of the widget callback (on the subscriber side) triggered
   * @param data DataObject containing the data for the widget callback 
   * @return DataObject containing any directives to the widget that created the callback
   * @exception context.arch.InvalidMethodException if the specified callback can't be found
   * @exception context.arch.MethodException is thrown if the specified callback can not be 
   *		executed successfully
   */
  public abstract DataObject handleCallback(String subscriptionId, DataObject data) throws InvalidMethodException, MethodException;
  
  public abstract DataObject handleSubscriptionCallback(String subscriptionId, DataObject data) throws InvalidMethodException, MethodException;
  
  /**
   * This method is used to forward the result of an independent communication
   */
  public abstract void handleIndependentReply(IndependentCommunication independentCommunication);
}
