/*
 * RequestObject.java
 *
 * Created on June 18, 2001, 9:34 AM
 */

package context.arch.comm;

import context.arch.comm.DataObject;

/**
 * This is the generic object that encapsulates the data and information for sending a request 
 * to another CTK object : the data object and the url of the request, the
 * destination server hostname and port.
 * At the creation, the data type is always DataObject, but it may be change
 * into String when the data has been encoded.
 *
 * @author  Agathe
 * @see context.arch.comm.DataObject
 * @see context.arch.comm.HTTPRequestObject
 */
public class RequestObject {

  /** 
   * Data to send : at instanciation, it contains the DataObject to send, and 
   * giving this object to the CommunicationObject, the data is encoded and put
   * in data as a String.
   */
  private Object data;
  
  /** Name of the message */
  private String url;
  
  /** The destination server */
  private String hostname;
  
  /** The destination port */
  private int port;
  
  /** Type of the request */
  private String type;
  
  /** The id of the receiver to whom this message is sent*/
  private String receiverId;
  
  /**
   * Creates new RequestObject with the dataobject to send and the message
   * url.
   *
   * @param DataObject The data to send
   * @param messageUrl The message url
   */
  public RequestObject (DataObject dataObject, String messageUrl) {
    this (dataObject, messageUrl, null, -1, null);
  }
  
  /** 
   * Creates new RequestObject with the dataobject to send, the message
   * url and the destination server hostname
   *
   * @param dataObject The data to send
   * @param messageUrl The message url
   * @param serverHostname The destination server hostname
   */
  public RequestObject (DataObject dataObject, String messageUrl, String serverHostname) {
    this (dataObject, messageUrl, serverHostname, -1, null);
  }
  
  /** 
   * Creates new RequestObject with the dataobject to send, the message
   * url, the destination server hostname and the destination server port.
   *
   * @param dataObject The data to send
   * @param messageUrl The message url
   * @param serverHostname The destination server hostname
   * @param serverPort The destination server port
   */
  public RequestObject (DataObject dataObject, String messageUrl, String serverHostname, int serverPort){
    this (dataObject, messageUrl, serverHostname, serverPort, null);
  }
  
  /** 
   * Creates new RequestObject with the dataobject to send, the message
   * url, the destination server hostname and the destination server port.
   *
   * @param dataObject The data to send
   * @param messageUrl The message url
   * @param serverHostname The destination server hostname
   * @param serverPort The destination server port
   */
  public RequestObject (DataObject dataObject, String messageUrl, String serverHostname, int serverPort, String receiverId){
    this (dataObject, messageUrl, serverHostname, serverPort, receiverId, null);
  }
  
  /** 
   * Creates new RequestObject with the dataobject to send, the message
   * url, the destination server hostname and the destination server port.
   *
   * @param dataObject The data to send
   * @param messageUrl The message url
   * @param serverHostname The destination server hostname
   * @param serverPort The destination server port
   */
  public RequestObject (DataObject dataObject, String messageUrl, String serverHostname, int serverPort, String receiverId, String type){
    if (dataObject != null)
      this.data = dataObject;
    else
      this.data = "";
    this.url = messageUrl;
    this.hostname = serverHostname;
    this.port = serverPort;
    this.type = type;
    this.receiverId = receiverId;
  }
  
  /**
   * Returns the message data object 
   *
   * @return Object
   */
  public Object getData (){
    return this.data;
  }
  
  public void setData(Object data){
    this.data = data;
  }
  /**
   * Returns the message data object 
   *
   * @return DataObject
   */
  public DataObject getNonEncodedData (){
    if (this.data instanceof DataObject){
      return (DataObject) this.data;
    }
    return null;
  }
  
  /**
   * Returns the enocded message object 
   *
   * @return String
   */
  public Object getEncodedData (){
    if (this.data instanceof String){
      return (String) this.data;
    }
    return null;
  }
  
  public void setEncodedData(String encodedData){
    this.data = encodedData;
  }
  
  /**
   * Returns the destination server hostname
   *
   * @return String
   */
  public String getServerHostname(){
    return this.hostname;
  }
  
  /**
   * Returns the message url
   *
   * @return String
   */
  public String getUrl(){
    return this.url;
  }
  
  public void setUrl(String url){
    this.url = url;
  }
  
  /**
   * Returns the type of the message : this method should be overridden
   *
   * @return String
   */
  public String getType(){
    return this.type;
  }
  
  /**
   * Sets the type of the request
   * 
   * @param type
   */
  public void setType (String type){
    this.type = type;
  }
  
  /**
   * Tests if the destination server port is defined or not
   *
   * @return boolean True if the server port is defined, false otherwise
   */
  public boolean portDefined(){
    if (this.port == -1){
      return false;
    }
    return true;
  }
  
  /**
   * Returns the destination server port
   * 
   * @return int
   */
  public int getPort(){
    return this.port;
  }

  /**
   * Set the receiver id
   */
  public void setReceiverId(String receiverId){
    this.receiverId = receiverId;
  }
  
  /**
   * Get the receiver id
   */
  public String getReceiverId(){
    return receiverId;
  }
  
  /**
   * Tests if 2 request objects are equal or not
   *
   * @param anotherRequestObject
   * @return boolean True if both request objects are equal
   */
  public boolean equals (RequestObject anotherRequestObject){
    if (this.getData().equals(anotherRequestObject.getData ())
      && this.getServerHostname ().equals(anotherRequestObject.getServerHostname ())
      && this.getPort () == anotherRequestObject.getPort ()
      ) {
      return true;
    }
    return false;
  }
  
  /**
   * Returns a printable version of this object
   */
  public String toString (){
    StringBuffer s = new StringBuffer("<RequestObject>: ");
    //s.append ("data="); s.append (data);
    s.append ("to "); s.append (this.hostname);
    s.append (" at "); s.append (this.port);
    s.append (" for "); s.append (this.url);
    s.append (" with "); s.append (this.type);
    return s.toString ();
  }
}// class end
