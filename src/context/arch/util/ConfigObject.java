package context.arch.util;

/**
 * This class implements a configuration object, encapsulating its hostname,
 * port, id, and a type.
 *
 * @see context.arch.util.ConfigObjects
 */
public class ConfigObject {

  private String id;
  private String host;
  private String port;
  private String type;

  /**
   * Basic constructor
   *
   * @param id Id of the component
   * @param host Hostname of the machine the component is running on
   * @param port Port the component is running on
   * @param type Type of component 
   */
  public ConfigObject(String id, String host, String port, String type) {
    this.id = id;
    this.host = host;
    this.port = port;
    this.type = type;
  }

  /**
   * Returns the component's port
   *
   * @return the component port
   */
  public String getPort() {
    return port;
  }

  /**
   * Sets the component's port
   *
   * @param port the component port
   */
  public void setPort(String port) {
    this.port = port;
  }

  /**
   * Returns the component's host
   *
   * @return the component host
   */
  public String getHost() {
    return host;
  }

  /**
   * Sets the component's hostname
   *
   * @param host the component hostname
   */
  public void setHost(String host) {
    this.host = host;
  }

  /**
   * Returns the component's id
   *
   * @return the component id
   */
  public String getId() {
    return id;
  }

  /**
   * Sets the component's id
   *
   * @param id the component id
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Returns the component's type
   *
   * @return the component type
   */
  public String getType() {
    return type;
  }

  /**
   * Sets the component's type
   *
   * @param type the component type
   */
  public void setType(String type) {
    this.type = type;
  }

  /**
   * Returns a string version of this class
   *
   * @return string version of this class
   */
  public String toString() {
    return new String("id: "+id+", type: "+type+", host: "+host+", port: "+port);
  }

}
