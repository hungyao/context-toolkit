package context.arch.handler;

/**
 * This class acts as a container for handler info.  It holds a reference to
 * an asynchronous service handler, the id of the requesting component, the id
 * of the the component offering the service, the name of the service to 
 * execute, the name of the service function to execute, and the unique request
 * tag to use.
 *
 * @see context.arch.handler.AsyncServiceHandler
 */
public class AsyncServiceHandlerInfo {

  private AsyncServiceHandler handler;
  private String localId;
  private String serviceId;
  private String serviceName;
  private String functionName;
  private String requestTag;

  /**
   * Basic empty constructor
   */
  public AsyncServiceHandlerInfo() {
  }

  /**
   * Full constructor taking all input parameters
   *
   * @param handler Reference to an asynchronous service handler
   * @param localId Id of the component calling the service
   * @param serviceId Id of the widget providing the service
   * @param serviceName Name of the service to execute
   * @param functionName Name of the service function to execute
   * @param requestTag Tag to identify the request
   */
  public AsyncServiceHandlerInfo(AsyncServiceHandler handler, String localId, String serviceId, String serviceName, 
                                 String functionName, String requestTag) {
    this.handler = handler;
    this.localId = localId;
    this.serviceId = serviceId;
    this.serviceName = serviceName;
    this.functionName = functionName;
    this.requestTag = requestTag;
  }

  /**
   * Returns the local id
   *
   * @return the local id
   */
  public String getLocalId() {
    return localId;
  }

  /**
   * Sets the local id
   *
   * @param localId Id of the component requesting the service
   */
  public void setLocalId(String localId) {
    this.localId = localId;
  }

  /**
   * Returns the service id
   *
   * @return the service id
   */
  public String getServiceId() {
    return serviceId;
  }

  /**
   * Sets the service id
   *
   * @param serviceId Id of the component providing the service
   */
  public void setServiceId(String serviceId) {
    this.serviceId = serviceId;
  }

  /**
   * Returns the name of the service
   *
   * @return the name of the service
   */
  public String getServiceName() {
    return serviceName;
  }

  /**
   * Sets the name of the service
   *
   * @param serviceName Name of the service to execute
   */
  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }

  /**
   * Returns the name of the service function
   *
   * @return the name of the service function
   */
  public String getFunctionName() {
    return functionName;
  }

  /**
   * Sets the name of the service function to execute
   *
   * @param functionName Name of the service function to execute
   */
  public void setFunctionName(String functionName) {
    this.functionName = functionName;
  }

  /**
   * Returns the request tag
   *
   * @return the request tag
   */
  public String getRequestTag() {
    return requestTag;
  }

  /**
   * Sets the request tag
   *
   * @param the request tag
   */
  public void setRequestTag(String requestTag) {
    this.requestTag = requestTag;
  }

  /**
   * Returns the handler
   *
   * @return the handler
   */
  public AsyncServiceHandler getHandler() {
    return handler;
  }

  /**
   * Sets the handler
   *
   * @param handler Handler to use
   */
  public void setHandler(AsyncServiceHandler handler) {
    this.handler = handler;
  }

  /**
   * Returns a unique id for this handler
   *
   * @return unique id for this handler
   */
  public String getUniqueId() {
    return localId+serviceId+serviceName+functionName+requestTag;
  }

}
