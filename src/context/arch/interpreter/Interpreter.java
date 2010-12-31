package context.arch.interpreter;

import context.arch.BaseObject;
import context.arch.comm.DataObject;
import context.arch.comm.DataObjects;
import context.arch.discoverer.Discoverer;
import context.arch.storage.Attribute;
import context.arch.storage.Attributes;
import context.arch.util.Error;

/**
 * This class is the basic interpreter.
 * 
 * TODO: use-cases? May be useful e.g. to represent temperature as Celsius when input represents it in Fahrenheit
 *
 * @see context.arch.BaseObject
 */
public abstract class Interpreter extends BaseObject {

	/** Debug flag */ 
	public static boolean DEBUG = false;

	/**
	 * Tag for interpreter
	 */
	public static final String INTERPRET = "interpret";

	/**
	 * The tag for the type of this object
	 */
	public static final String INTERPRETER_TYPE = "interpreter";

	/**
	 * Tag for interpreterReply
	 */
	public static final String INTERPRET_REPLY = "interpretReply";

	/**
	 * Default port to use for interpreters
	 */
	public static final int DEFAULT_PORT = 7000;

	protected Attributes inAttributes;
	protected Attributes outAttributes;

	/**
	 * Constructor that creates a BaseObject with the given port and sets the
	 * incoming and outgoing attributes.
	 *
	 * @param port Port number to create the BaseObject on
	 * @see context.arch.BaseObject
	 */
	public Interpreter(int port) {
		super(port);
		debugprintln(DEBUG, "Interpreter construction after super () port=" + port);
		inAttributes = setInAttributes();
		outAttributes = setOutAttributes();
	}

	/**
	 * Returns the type of the object
	 * This method should be overridden
	 *
	 * @return String
	 */
	public String getType(){
		return Interpreter.INTERPRETER_TYPE;
	}

	/**
	 * This method is meant to handle any internal methods that the baseObject doesn't
	 * handle.  In particular, this method handles interpret requests.  It ensures
	 * that the ID of the incoming request matches this interpreter.  If the 
	 * method is an INTERPRET method, it sends it to the interpreter.  Otherwise
	 * runInterpreterMethod() is called.
	 *
	 * @param data DataObject containing the method to run and parameters
	 * @return DataObject containing the results of running the method 
	 * @see #runInterpreterMethod(DataObject,String)
	 */
	public DataObject runUserMethod(DataObject data) {
		debugprintln (DEBUG, "Interpreter <runUserMethod>");
		DataObject interpreter = data.getDataObject(ID);
		String error = null;

		// Test the id
		if (interpreter == null) {
			error = Error.INVALID_ID_ERROR;
			return (new Error(error)).toDataObject();
		}
		else {
			String queryId = (String)(interpreter.getValue());
			if (!queryId.equals(getId())) {
				error = Error.INVALID_ID_ERROR;
				return (new Error(error)).toDataObject();
			}
		}

		String methodType = data.getName();

		if (methodType.equals(INTERPRET)) {
			return callInterpreter(data,error);
		}
		else {
			return runInterpreterMethod(data,error);
		}
	}

	/**
	 * This method ensures that the incoming attributes are correct and calls
	 * interpretData().  It returns the interpreted results.
	 *
	 * @param data Incoming interpret request
	 * @param error Incoming error, if any
	 * @return interpreted results
	 * @see #interpretData(AttributeNameValues)
	 */
	public DataObject callInterpreter(DataObject data, String error) {
		DataObjects v = new DataObjects();
		DataObject result = new DataObject(INTERPRET_REPLY,v);
		Attributes dataToInterpret = null;
		Error err = new Error(error);
		if (err.getError() == null) {
			dataToInterpret = Attributes.fromDataObject(data);
			if (dataToInterpret == null) {
				err.setError(Error.MISSING_PARAMETER_ERROR);
			}
			else if (!canHandle(dataToInterpret)) {
				err.setError(Error.INVALID_ATTRIBUTE_ERROR);
			}
		}

		if (err.getError() == null) {
			Attributes interpreted = interpretData(dataToInterpret);
			if (interpreted != null) {
				v.addElement(interpreted.toDataObject());
				err.setError(Error.NO_ERROR);
			}
			else {
				err.setError(Error.INVALID_DATA_ERROR);
			}
		}
		v.addElement(err.toDataObject());
		return result;
	}

	/**
	 * This abstract method interprets the given data and returns it. 
	 *
	 * @param data AttributeNameValues containing data to be interpreted
	 * @return AttributeNameValues object containing the interpreted data
	 */
	protected abstract Attributes interpretData(Attributes data);

	/**
	 * This is an empty method that should be overridden by objects
	 * that subclass from this class.  It is called when another component
	 * tries to run a method on the interpreter, but it's not an interpret
	 * request.
	 *
	 * @param data DataObject containing the data for the method
	 * @param error String containing the incoming error value
	 * @return DataObject containing the method results
	 */
	protected DataObject runInterpreterMethod(DataObject data, String error) {
		debugprintln (DEBUG, "Interpreter <runInterpreterMethod>");
		Error err = new Error(Error.UNKNOWN_METHOD_ERROR);
		return err.toDataObject ();
	}

	/**
	 * This method checks the list of incoming attributes to ensure that the
	 * interpreter can handle these attributes.  
	 * 
	 * @param inAtts List of incoming attributes to check
	 * @return whether the list of attributes is valid
	 */
	private boolean canHandle(Attributes inAtts) {
		for (int i = 0; i < inAtts.size(); i++) {
			Attribute<?> inAtt = inAtts.get(i);
			if (!isInAttribute(inAtt.getName())) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns the attribute type with the given name for incoming attributes
	 *
	 * @param name Name of the attribute to get
	 */   
	protected Class<?> getInAttributeType(String name) {
		return inAttributes.get(name).getType();
	}

	/**
	 * Adds an incoming attribute
	 *
	 * @param name Name of the attribute to set
	 * @param type Type of the attribute
	 */   
	protected <T extends Comparable<? super T>> void addInAttribute(String name, Class<T> type) {
		inAttributes.add(new Attribute<T>(name, type));
	}

	/**
	 * Checks if the given incoming attribute is an attribute of this interpreter
	 *
	 * @param name Name of the attribute to check
	 */
	protected boolean isInAttribute(String name) {
		return inAttributes.containsKey(name);
	}

	/**
	 * Returns the attribute type with the given name for outgoing attributes
	 *
	 * @param name Name of the attribute to get
	 */   
	protected Class<?> getOutAttributeType(String name) {
		return outAttributes.get(name).getClass();
	}

	/**
	 * Adds an outgoing attribute
	 *
	 * @param name Name of the attribute to set
	 * @param type Type of the attribute
	 */   
	protected <T extends Comparable<? super T>> void addOutAttribute(String name, Class<T> type) {
		outAttributes.add(new Attribute<T>(name, type));
	}

	/**
	 * Checks if the given outgoing attribute is an attribute of this interpreter
	 *
	 * @param name Name of the attribute to check
	 */
	protected boolean isOutAttribute(String name) {
		return outAttributes.containsKey(name);
	}

	/**
	 * Sets the incoming attributes for the interpreter
	 */
	protected abstract Attributes setInAttributes();

	/**
	 * Sets the outgoing attributes for the interpreter
	 */
	protected abstract Attributes setOutAttributes();

	/** 
	 * Overloads the BaseObject method
	 */ 
	public DataObject getUserDataObject(){
		DataObject result;

		// Get the incoming attributes
		DataObject doInAtt_ = setInAttributes().toDataObject();
		DataObjects vInAtt = new DataObjects();
		vInAtt.addElement(doInAtt_);
		DataObject doInAtt = new DataObject(Discoverer.INCOMING_ATTRIBUTE_NAME_VALUES, vInAtt);

		// Get the outgoing attributes
		DataObject doOutAtt_ = setOutAttributes().toDataObject();
		DataObjects vOutAtt = new DataObjects();
		vOutAtt.addElement(doOutAtt_);
		DataObject doOutAtt = new DataObject(Discoverer.OUTGOING_ATTRIBUTE_NAME_VALUES, vOutAtt);

		DataObjects v = new DataObjects();
		v.addElement(doInAtt);
		v.addElement(doOutAtt);

		// Get getInterpreterDescription
		DataObject doDescrip = getInterpreterDescription();
		if (doDescrip != null) {
			for (DataObject child : doDescrip.getChildren()){
				v.addElement(child);
			}
		}

		result = new DataObject(Discoverer.TEMP_DEST, v);

		return result;
	}

	/**
	 * Returns the interpreter description that should be overloaded
	 */
	public DataObject getInterpreterDescription(){
		return null;
	}


}
