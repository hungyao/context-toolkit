package context.arch.handler;

/**
 * This class acts as a container for handler info.  It holds a reference to
 * a widget handler, the subscription id, the id of the widget, the name of
 * the callback on the subscriber side, and the name of the callback on the 
 * widget side.
 *
 * @see context.arch.handler.Handler
 */
public class HandlerInfo {

  /**
   *
   */
  private Handler handler;
  
  /**
   *
   */
  private String subId;
  
  /**
   *
   */
  private String remoteId;
  
  /**
   * Added by Agathe
   */
  private String remoteHost;
  
  /**
   * Added by Agathe
   */
  private int remotePort;
  
  /**
   *
   */
  private String subscriptionCallback;

  /**
   * Basic empty constructor
   */
  public HandlerInfo() {
  }

  /**
   * Full constructor that takes all input parameters
   *
   * @param handler Reference to a context widget handler
   * @param subId Subscription id of a subscriber. This is returned anytime a callback 
   *		message is sent between the subscriber to the widget (in either direction).
   * @param remoteId Id of the widget object
   * @param callback Name of the callback on the subscriber side
   * @param remoteCallback Name of the callback on the widget side
   */
  public HandlerInfo(Handler handler, String subId, String remoteId, String remoteHost, int remotePort, 
    String subCallback) 
  {
    this.handler = handler;
    this.subId = subId;
    this.remoteId = remoteId;
    this.remoteHost = remoteHost;
    this.remotePort = remotePort;
    this.subscriptionCallback = subCallback;
  }

  
  /**
   * Constructor
   *
   * @param handler Reference to a context widget handler
   * @param subId Subscription id of a subscriber. This is returned anytime a callback 
   *		message is sent between the subscriber to the widget (in either direction).
   * @param remoteId Id of the widget object
   * @param callback Name of the callback on the subscriber side
   * @param remoteCallback Name of the callback on the widget side
   */
  public HandlerInfo(Handler handler, String subId, String remoteId, String subCallback) {
    this.handler = handler;
    this.subId = subId;
    this.remoteId = remoteId;
    this.subscriptionCallback = subCallback;
  }

  
  /**
   * Returns the subscription id
   *
   * @return the subscription id
   */
  public String getSubId() {
    return subId;
  }

  /**
   * Sets the subscription id
   *
   * @param the subscription id
   */
  public void setSubId(String id) {
    subId = id;
  }

  /**
   * Returns the widget id
   *
   * @return the widget id
   */
  public String getRemoteId() {
    return remoteId;
  }

  /**
   * Sets the widget id
   *
   * @param the widget id
   */
  public void setRemoteId(String id) {
    remoteId = id;
  }

  /**
   * Returns the widget host
   *
   * @return the widget host
   */
  public String getRemoteHost() {
    return remoteHost;
  }

  /**
   * Sets the widget host
   *
   * @param the widget host
   */
  public void setRemoteHost(String host) {
    remoteHost = host;
  }
  
  /**
   * Returns the widget port
   *
   * @return the widget port
   */
  public int getRemotePort() {
    return remotePort;
  }

  /**
   * Sets the widget port
   *
   * @param the widget port
   */
  public void setRemotePort(int port) {
    remotePort = port;
  }
  
  /**
   * Returns the name of the subscription callback
   *
   * @return the name of the subscription callback
   */
  public String getSubscriptionCallback() {
    return subscriptionCallback;
  }

  /**
   * Sets the name of the subscription callback
   *
   * @param the name of the subscription callback
   */
  public void setSubscriptionCallback(String subCallback) {
    this.subscriptionCallback = subCallback;
  }

  /**
   * Returns the context widget handler
   *
   * @return the context widget handler
   */
  public Handler getHandler() {
    return handler;
  }

  /**
   * Sets the context widget handler
   *
   * @param the context widget handler
   */
  public void setHandler(Handler handler) {
    this.handler = handler;
  }
}
