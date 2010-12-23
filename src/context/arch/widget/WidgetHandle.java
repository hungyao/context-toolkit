package context.arch.widget;

import context.arch.storage.Attributes;

/**
 * This class implements a widget handler object, encapsulating the information
 * needed to subscribe to a widget.
 * 
 * Agathe: I modified this class so that it may also specify conditions and 
 * attributes conditions
 * 
 * TODO: removed conditions, must replace with queryitem
 *
 * @see context.arch.widget.WidgetHandles
 */
public class WidgetHandle {

  private String id;
  private String hostname;
  private int port;
  private Attributes atts;
  
  /**
   * Basic constructor that creates a WidgetHandle object.
   *
   * @param id ID of the widget being subscribed to 
   * @param hostname Name of the widget's host computer
   * @param port Port number of the widget
   */
  public WidgetHandle(String id, String hostname, int port) {
    this.id = id;
    this.hostname = hostname;
    this.port = port;
  }

  /**
   * Basic constructor that creates a WidgetHandle object.
   *
   * @param id ID of the widget being subscribed to 
   * @param hostname Name of the widget's host computer
   * @param port Port number of the widget
   */
  public WidgetHandle(String id, String hostname, String port) {
    this.id = id;
    this.hostname = hostname;
    this.port = new Integer(port).intValue();
  }

  /**
   * Returns the id of the subscriber
   *
   * @return the subscriber id
   */
  public String getId() {
    return id;
  }

  /**
   * Sets the id of the subscriber
   *
   * @param id ID of the subscriber
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Returns the name of the subscriber's host computer
   *
   * @return the host name of the subscriber
   */
  public String getHostName() {
    return hostname;
  }

  /**
   * Sets the name of the subscriber's host computer
   *
   * @param hostname Name of the subscriber's host computer
   */
  public void setHostName(String hostname) {
    this.hostname = hostname;
  }

  /**
   * Returns the port number to send info to
   *
   * @return the port number of the subscriber
   */
  public int getPort() {
    return port;
  }

  /**
   * Sets the port number to send info to
   *
   * @param port Port number to send information to
   */
  public void setPort(int port) {
    this.port = port;
  }

  /**
   * Sets the Attributes for the widget subscription
   * 
   * @param atts
   */
  public void setAttributes(Attributes atts){
    this.atts = atts;
  }
  
  /**
   * Return the Attributes of the subscription
   *
   * @return Attributes
   */
  public Attributes getAttributes (){
    return this.atts;
  }
  

  /**
   * Returns a printable version of the object
   * 
   * @return String
   */
	public String toString () {
	
		return id + "@" + hostname + ":" + port;
		
	}
}
