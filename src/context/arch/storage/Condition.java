package context.arch.storage;

import context.arch.comm.DataObject;
import context.arch.comm.DataObjects;

/**
 * This class is a container for a condition attribute, comparison and value.
 * 
 * A Condition is used to specify what data a component is interested in, in the case of a poll/subscribe/retrieval. 
 * It is meant to help a component more easily get the data it is interested in. Each Condition has the name of an 
 * attribute, a comparison operator, and a value. For example, an application may only want to know about temperature 
 * changes when the temperature is above 20 degrees. It would set a condition on its subscription, where the 
 * attribute name was "temperature", operator was ">", and value was "20". Conditions are a collection of AND'ed 
 * Condition statements. 
 * 
 * @author Anind K. Dey
 * deprecated? Superseded by Enactors framework; actually used by Retrieval system
 */
class Condition {

  private String attribute;
  private Object value;
  private int compare;

  /**
   * Tag for attribute name to use in comparison
   */
  public static final String NAME = "name";

  /**
   * Tag for type of comparison
   */
  public static final String COMPARE = "compare";

  /**
   * Tag for value to use for comparison
   */
  public static final String VALUE = "value";

  /**
   * Tag for AttributeCondition
   */
  public static final String CONDITION = "condition";

  /**
   * Empty constructor
   */
  public Condition() {
  }

  /**
   * Constructor that takes an attribute, value and comparison
   *
   * @param name Name of attribute
   * @param compare Comparison to make. see values in Storage
   * @param value Value of attribute to compare to
   */
  public Condition(String attribute, int compare, Object value) {
    this.attribute = attribute;
    this.value = value;
    this.compare = compare;
  }

  /**
   * Constructor that creates a Condition object from a DataObject.
   * The DataObject must have a <CONDITION> tag at the top level.
   *
   * @param data DataObject containing the condition info
   */
  public Condition(DataObject data) {
    this.attribute = data.getDataObject(NAME).getValue();
    this.compare = new Integer(data.getDataObject(COMPARE).getValue()).intValue();
    this.value = data.getDataObject(VALUE).getValue();
  }
    
  /**
   * Converts this object to a DataObject
   *
   * @return Condition object converted to an <CONDITION> DataObject
   */
  public DataObject toDataObject() {
    DataObjects v = new DataObjects();
    v.addElement(new DataObject(NAME,attribute));
    v.addElement(new DataObject(COMPARE,Integer.toString(compare)));
    v.addElement(new DataObject(VALUE,value.toString()));
    return new DataObject(CONDITION, v);
  }
    
  /**
   * Sets the name of an attribute 
   *
   * @param attribute Name of the attribute
   */
  public void setAttribute(String attribute) {
    this.attribute = attribute;
  }

  /**
   * Sets the value of an attribute
   *
   * @param value Value of the attribute
   */
  public void setValue(Object value) {
    this.value = value;
  }

  /**
   * Sets the comparison to make
   *
   * @param compare Comparison to make
   */
  public void setCompare(int compare) {
    this.compare = compare;
  }

  /**
   * Returns the name of the attribute
   *
   * @return name of the attribute
   */
  public String getAttribute() {
    return attribute;
  }
  
  /**
   * Returns the value of the attribute to use for comparison
   *
   * @return value of the attribute to use for comparison
   */
  public Object getValue() {
    return value;
  }

  /**
   * Returns the type of comparison
   *
   * @return type of comparison
   */
  public int getCompare() {
    return compare;
  }

  /**
   * Returns a printable version of the condition object
   *
   * @return printable version of the condition object
   */
  public String toString() {
    return new String("[name="+getAttribute()+",compare="+getCompare()+",value="+getValue()+"]");
  }
}
