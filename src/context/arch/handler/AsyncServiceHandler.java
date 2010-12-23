package context.arch.handler;

import context.arch.comm.DataObject;
import context.arch.InvalidMethodException;
import context.arch.MethodException;

/**
 * This empty interface allows the ServiceHandlers class to keep track of
 * servic handlers.  Any component that calls asynchronous services
 * should implement this interface.
 *
 * @see context.arch.handler.AsyncServiceHandlers
 */
public interface AsyncServiceHandler {

  /**
   * This abstract method is used to generically handle the results from any
   * asynchronous service that a widget may support.  An asynchronous service
   * will trigger this method to be called when the object implementing this
   * interface calls the remote asynchronous service.
   *
   * @param requestTag The requestTag used to identify the service request
   * @param data DataObject containing the data for service execution
   * @return DataObject containing any directives to the service that was executed
   * @exception context.arch.InvalidMethodException if the specified callback can't be found
   * @exception context.arch.MethodException is thrown if the specified callback can not be 
   *		executed successfully
   */
  public abstract DataObject asynchronousServiceHandle(String requestTag, DataObject data) throws InvalidMethodException, MethodException;
}
