package context.arch.service.helper;

import context.arch.comm.DataObject;
import context.arch.comm.DataObjects;
import context.arch.storage.Attributes;

/**
 * This class implements a service function description object.
 *
 * @see context.arch.service.Services
 */
public class FunctionDescription {

	/**
	 * Tag for a service function
	 */
	public static final String FUNCTION = "function";

	/**
	 * Tag for a service function name
	 */
	public static final String FUNCTION_NAME = "functionName";

	/**
	 * Tag for a service function description
	 */
	public static final String FUNCTION_DESCRIPTION = "functionDescription";

	/**
	 * Tag for a service function timing - synchronous or asynchronous
	 */
	public static final String FUNCTION_SYNCHRONICITY = "functionSync";

	private String name;
	private String description;
	private Attributes attributes;
	private String synchronicity;

	/**
	 * Basic constructor that creates a function description object, 
	 * synchronicity = {@link FunctionDescription#FUNCTION_SYNC} 
	 * @param name Name of the function
	 * @param description Text description of the function
	 */
	public FunctionDescription(String name, String description) {
		this(name, description, new Attributes(), FUNCTION_SYNC);
	}

	/**
	 * Basic constructor that creates a function description object.
	 * @param name Name of the function
	 * @param description Text description of the function
	 * @param synchronicity {@link FunctionDescription#FUNCTION_ASYNC}  or {@link FunctionDescription#FUNCTION_SYNC}  
	 */
	public FunctionDescription(String name, String description, String synchronicity) {
		this(name, description, new Attributes(), synchronicity);
	}

	/**
	 * Basic constructor that creates a function description object.
	 * synchronicity = {@link FunctionDescription#FUNCTION_SYNC} 
	 * @param name Name of the function
	 * @param description Text description of the function
	 * @param attributes Attributes this function takes
	 */
	public FunctionDescription(String name, String description, Attributes attributes) {
		this(name, description, attributes, FUNCTION_SYNC);
	}

	/**
	 * Basic constructor that creates a function description object.
	 * @param name Name of the function
	 * @param description Text description of the function
	 * @param attributes Attributes this function takes
	 * @param synchronicity {@link FunctionDescription#FUNCTION_ASYNC}  or {@link FunctionDescription#FUNCTION_SYNC} 
	 */
	public FunctionDescription(String name, String description, Attributes attributes, String synchronicity) {
		this.name = name;
		this.description = description;
		this.attributes = attributes;
		this.synchronicity = synchronicity;
	}

	/**
	 * Basic constructor that creates a function description object from a DataObject.
	 * The dataObject is expected to have a <FUNCTION> tag as the top level.
	 *
	 * @param data DataObject containing function description info
	 */
	public FunctionDescription(DataObject data) {
		DataObject nameObj = data.getDataObject(FUNCTION_NAME);
		name = nameObj.getValue();
		DataObject descriptionObj = data.getDataObject(FUNCTION_DESCRIPTION);
		description = descriptionObj.getValue();
		attributes = Attributes.fromDataObject(data.getDataObject(Attributes.ATTRIBUTES));
		DataObject timingObj = data.getDataObject(FUNCTION_SYNCHRONICITY);
		synchronicity = timingObj.getValue();
	}

	/**
	 * This method converts the service function info to a DataObject
	 *
	 * @return FunctionDescription object converted to a <FUNCTION> DataObject
	 */
	public DataObject toDataObject() {
		DataObjects v = new DataObjects();
		v.addElement(new DataObject(FUNCTION_NAME,name));
		v.addElement(new DataObject(FUNCTION_DESCRIPTION,description));
		if (attributes != null) { v.addElement(attributes.toDataObject()); }
		v.addElement(new DataObject(FUNCTION_SYNCHRONICITY, synchronicity));
		return new DataObject(FUNCTION, v);
	}

	/**
	 * Sets the Name of the service function
	 *
	 * @param name Name of the service function
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the name of the service function
	 *
	 * @return Name of the service function
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the description of the service functions
	 *
	 * @param description Description of the service function
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Returns the description of the service function
	 *
	 * @return description of the service function
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the attributes for this service function
	 *
	 * @param attributes Attributes of the service function
	 */
	public void setAttributes(Attributes atts) {
		this.attributes = atts;
	}

	/**
	 * Returns the attributes of the service function
	 *
	 * @return attributes of the service function
	 */
	public Attributes getAttributes() {
		return attributes;
	}

	public static final String FUNCTION_ASYNC = "ASYNCHRONOUS";
	public static final String FUNCTION_SYNC = "SYNCHRONOUS";

	/**
	 * Sets the timing of the service function - ASYNCHRONOUS or SYNCHRONOUS
	 *
	 * @param synchronicity Timing of the service function
	 */
	public void setSynchronicity(String synchronicity) {
		this.synchronicity = synchronicity;
	}

	/**
	 * Return the timing of the service function
	 *
	 * @return timing of the service function
	 */
	public String getSynchronicity() {
		return synchronicity;
	}

}
