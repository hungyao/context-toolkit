package context.arch.comm.protocol;

import java.io.Reader;

/** 
 * This class maintains the data received in a request
 */
public class RequestData {

  private String type = null;
  private String line = null;
  private Reader data = null;

  /**
   * Specifies that this type of request requires decoding
   */
   public static final String DECODE = "DECODE";

  /**
   * Basic empty constructor
   */
  public RequestData() {
  }

  /**
   * Constructor with request type, single input line and complete data
   *
   * @param type Type of request
   * @param line Single line of input
   * @param data Rest of request data in an Reader
   */
  public RequestData(String type, String line, Reader data) {
    this.type = type;
    this.line = line;
    this.data = data;
  }

  /**
   * Returns the type of request
   *
   * @return the type of request
   */
  public String getType() {
    return type;
  }

  /**
   * Returns the single line of request data
   *
   * @return the single line of request data
   */
  public String getLine() {
    return line;
  }

  /**
   * Returns the complete request data
   *
   * @return the complete request data in an Reader
   */
  public Reader getData() {
    return data;
  }

  /**
   * Sets the single line of request data
   *
   * @param line The single line of request data
   */
  public void setLine(String line) {
    this.line = line;
  }

  /**
   * Sets the complete request data Reader
   *
   * @param data The complete request data
   */
  public void setData(Reader data) {
    this.data = data;
  }

  /**
   * Sets the type of request
   *
   * @param data The type of request
   */
  public void setType(String type) {
    this.type = type;
  }
  
  /**
   * Returns a string version of this object
   *
   * @return String The printable string
   */
  public String toString(){
    StringBuffer sb = new StringBuffer("RequestData:");
    sb.append ("type=" + this.type);
    sb.append ("line=" + this.line);
    sb.append ("data=" + this.data);
    return sb.toString ();
  }
  
}
