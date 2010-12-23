package context.arch.service.helper;

import context.arch.comm.DataObject;
import context.arch.comm.DataObjects;

/**
 * This class implements a service description object.
 *
 * @see context.arch.service.Services
 */
public class ServiceDescription {

  /**
   * Tag for a service
   */
  public static final String SERVICE = "service";

  /**
   * Tag for a service name
   */
  public static final String SERVICE_NAME = "serviceName";

  private String name;
  private FunctionDescriptions descriptions;

  /**
   * Basic constructor that creates a service object.
   *
   * @param name Name of the service
   * @descriptions FunctionDescriptions for this service
   */
  public ServiceDescription(String name, FunctionDescriptions descriptions) {
    this.name = name;
    this.descriptions = descriptions;
  }

  /**
   * Basic constructor that creates a service object from a DataObject.  This
   * dataobject is expected to have a <SERVICE> tag at the top level.
   *
   * @param data DataObject containing the service description info
   */
  public ServiceDescription(DataObject data) {
    DataObject nameObj = data.getDataObject(SERVICE_NAME);
    name = nameObj.getValue();
    descriptions = new FunctionDescriptions(data);
  }

  /**
   * This method converts the service info to a DataObject
   *
   * @return Service object converted to a <SERVICE> DataObject
   */
  public DataObject toDataObject() {
    DataObjects v = new DataObjects();
    v.addElement(new DataObject(SERVICE_NAME,name));
    v.addElement(descriptions.toDataObject());
    return new DataObject(SERVICE, v);
  }

  /**
   * Sets the service name
   *
   * @param name Name of the service
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Returns the service name
   *
   * @return name of the service
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the function descriptions
   *
   * @param descriptions Function descriptions for the service
   */
  public void setFunctionDescriptions(FunctionDescriptions descriptions) {
    this.descriptions = descriptions;
  }

  /**
   * Returns the function descriptions
   *
   * @return function descriptions for the service
   */
  public FunctionDescriptions getFunctionDescriptions() {
    return descriptions;
  }

}
