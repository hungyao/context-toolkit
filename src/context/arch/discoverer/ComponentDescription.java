/*
 * ComponentDescription.java
 *
 * Created on April 16, 2001, 11:27 AM
 */

package context.arch.discoverer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import context.arch.comm.DataObject;
import context.arch.comm.DataObjects;
import context.arch.service.helper.ServiceDescription;
import context.arch.storage.Attribute;
import context.arch.storage.AttributeNameValue;
import context.arch.storage.Attributes;
import context.arch.subscriber.Callback;
import context.arch.subscriber.Subscriber;
import context.arch.subscriber.Subscribers;
import context.arch.util.Error;

/**
 * This class allows to store a component's description
 *
 * Has changed Vectors into ArrayList
 * all server and widget att merged
 * all callback / service of widget and server merged
 *
 * @author Agathe
 * @author Brian Y. Lim
 * @see context.arch.BaseObject
 * 
 */
public class ComponentDescription extends Object implements Cloneable {
	
	/*
	 * The following are name tags for elements of ComponentDescription that may be used by the query system.
	 * Note that applications may define other novel elements to deal with interesting combinations 
	 * (e.g. multidimensional, hypotenuse calculations)
	 */
	
	public static final String ID_ELEMENT = "id";
	public static final String CLASSNAME_ELEMENT = "classname";
	
	public static final String HOSTNAME_ELEMENT = "hostname";
	public static final String PORT_ELEMENT = "port";

	public static final String TYPE_ELEMENT = "type";
	
	public static final String CONST_ATT_ELEMENT = "constAtt";
	public static final String NON_CONST_ATT_ELEMENT = "nonConstAtt";
	public static final String NON_CONST_ATT_NAME_ELEMENT = "nonConstAttName";
	public static final String SERVICE_ELEMENT = "service";

	public static final String SUBSCRIBER_ELEMENT = "subscriber";
	public static final String CALLBACK_ELEMENT = "callback";

	/**
	 * The component id
	 */
	public String id;

	/**
	 * To component classname
	 */
	public String classname;

	/**
	 * The component hostname
	 */
	public String hostname;

	/**
	 * The component hostaddress
	 */
	public String hostaddress;

	/**
	 * The component type : Discoverer.APPLICATION or Discoverer.WIDGET or Discoverer.SERVER
	 * or Discoverer.INTERPRETER
	 */
	public String type;

	/**
	 * The component version
	 */
	public String version;

	/**
	 * The component port
	 */
	public int port;

	private Attributes constantAttributes;
	private Attributes nonConstantAttributes;
	private ArrayList<String> callbacks;
	private ArrayList<String> services;
	private ArrayList<String> subscribers;
	private Attributes inAttributes;
	private Attributes outAttributes;

	/**
	 * The constructor that creates a new ComponentDescription with no parameters
	 */
	public ComponentDescription() {
		constantAttributes = new Attributes();
		nonConstantAttributes = new Attributes();
		callbacks = new ArrayList<String>();
		services = new ArrayList<String>();
		subscribers = new ArrayList<String>();
		inAttributes = new Attributes();
		outAttributes = new Attributes();
	}

	/**
	 * This method allows to return the ComponentDescription version of a DataObject
	 *
	 * @param dataObject The DataObject containing the description of a context object
	 * @return ComponentDescription The ComponentDescription version
	 */
	public static ComponentDescription fromDataObject(DataObject dataObject) {
		DataObject data = dataObject;

		if (data == null) return null;

		ComponentDescription comp = new ComponentDescription();

		comp.setId(data.getDataObject(Discoverer.ID));
		comp.setClassname(data.getDataObject(Discoverer.COMPONENT_CLASSNAME));
		comp.setHostname(data.getDataObject(Discoverer.HOSTNAME));
		comp.setHostaddress(data.getDataObject(Discoverer.HOSTADDRESS));
		comp.setPort(data.getDataObject(Discoverer.PORT));
		comp.setVersion(data.getDataObject(Discoverer.VERSION));
		comp.setType(data.getDataObject(Discoverer.TYPE));

		comp.setConstantAttributes(data.getDataObject(Discoverer.CONSTANT_ATTRIBUTE_NAME_VALUES));
		comp.setNonConstantAttributes(data.getDataObject(Discoverer.NON_CONSTANT_ATTRIBUTE_NAME_VALUES));
		comp.setSubscribers(data.getDataObject(Subscribers.SUBSCRIBERS));
		comp.setWidgetCallbacks(data.getDataObject(Discoverer.WIDGET_CALLBACKS));
		comp.setWidgetServices(data.getDataObject(Discoverer.WIDGET_SERVICES));
		comp.setServerCallbacks(data.getDataObject(Discoverer.SERVER_CALLBACKS));
		comp.setServerServices(data.getDataObject(Discoverer.SERVER_SERVICES));
		comp.setInAttributes(data.getDataObject(Discoverer.INCOMING_ATTRIBUTE_NAME_VALUES));
		comp.setOutAttributes(data.getDataObject(Discoverer.OUTGOING_ATTRIBUTE_NAME_VALUES));
		
		return comp;
	}

	public DataObject toDataObject() {
		DataObjects v1 = new DataObjects();
		v1.addElement(new DataObject(Discoverer.ID, id));
		v1.addElement(new DataObject(Discoverer.COMPONENT_CLASSNAME, classname));
		v1.addElement(new DataObject(Discoverer.HOSTNAME, hostname));
		v1.addElement(new DataObject(Discoverer.HOSTADDRESS, hostaddress));
		v1.addElement(new DataObject(Discoverer.PORT, String.valueOf(port)));
		v1.addElement(new DataObject(Discoverer.VERSION, version));
		v1.addElement(new DataObject(Discoverer.TYPE, type));
		
		// constant attributes
		Attributes atts = new Attributes(this.constantAttributes);
		DataObjects v = new DataObjects();
		v.addElement(atts.toDataObject ());
		v1.addElement(new DataObject(Discoverer.CONSTANT_ATTRIBUTE_NAME_VALUES, v));  

		// non-constant attributes
		atts = new Attributes(this.nonConstantAttributes);
		v = new DataObjects();
		v.addElement(atts.toDataObject ());
		v1.addElement(new DataObject (Discoverer.NON_CONSTANT_ATTRIBUTE_NAME_VALUES, v));  
		
		// subscribers
		v = new DataObjects();
		for (String subscriber : this.subscribers) {
			v.addElement(new DataObject(Subscriber.SUBSCRIBER, subscriber));
		}
		v1.addElement(new DataObject(Subscribers.SUBSCRIBERS, v));  

		// callbacks
		v = new DataObjects();
		for (String callback : this.callbacks) {
			v.addElement(new DataObject(Callback.CALLBACK_NAME, callback));
		}
		v1.addElement(new DataObject(Discoverer.WIDGET_CALLBACKS, v));  

		// services
		v = new DataObjects();
		for (String service : this.services) {
			v.addElement(new DataObject(ServiceDescription.SERVICE_NAME, service));
		}
		v1.addElement (new DataObject (Discoverer.WIDGET_SERVICES, v));  

		v = new DataObjects();
		for (Attribute<?> att : this.inAttributes.values()) {
			v.addElement(att.toDataObject());
		}
		v1.addElement (new DataObject(Discoverer.INCOMING_ATTRIBUTE_NAME_VALUES, v));  

		v = new DataObjects();
		for (Attribute<?> att : this.outAttributes.values()) {
			v.addElement (att.toDataObject ());
		}
		v1.addElement (new DataObject(Discoverer.OUTGOING_ATTRIBUTE_NAME_VALUES, v));  

		DataObject res = new DataObject(Discoverer.REGISTERER, v1);
		return res;

	}
	/**
	 * Returns a printable version of ComponentDescription
	 *
	 * @return String The string version of the ComponentDescription
	 */
	public String toString(){
		StringBuffer s = new StringBuffer();
		s.append("Id : "+id);
		s.append(" - Classname : " + classname);
		s.append(" - Hostname : " + hostname + " " + hostaddress);
		s.append(" - Port : " + port);
		s.append(" - Type : " + type);

		s.append(" - Constant Attribute : " + constantAttributes);
		//s.append(" - Constant Attribute Values : " + constantAttributeValues);
		//s.append(" - Constant Attribute NamesValues : " + constantAttributeNamesValues);
		s.append(" - Non Constant Attribute : " + nonConstantAttributes);
		s.append(" - Incoming attributes : " + inAttributes);
		s.append(" - Outgoing attributes : " + outAttributes);
		s.append(" - Callbacks : " + callbacks);
		s.append(" - Services : " + services);
		s.append(" - Subscribers : " + subscribers);

		return s.toString();
	}

	public String toSmallString(){  
		StringBuffer s = new StringBuffer();
		s.append("Id : "+id);
		s.append(" - Classname : " + classname);
		s.append(" - Hostname : " + hostname + " " + hostaddress);
		s.append(" - Port : " + port);
		s.append(" - Type : " + type);
		return s.toString();
	}
	public String toSmallStringNL(){  
		StringBuffer s = new StringBuffer();
		s.append("Id : " + id);
		s.append("\n - Hostname : " + hostname + " " + hostaddress);
		s.append("\n - Port : " + port);
		s.append("\n - Type : " + type);
		return s.toString();
	}

	/**
	 * Returns a printable version of ComponentDescription with new lines
	 *
	 * @return String The string version of the ComponentDescription
	 */
	public String toStringNL(){
		StringBuffer s = new StringBuffer();
		s.append("\n- Id : " + id);
		s.append("\n- Classname : " + classname);
		s.append("\n- Hostname : " + hostname + " " + hostaddress);
		s.append("\n- Port : " + port);
		s.append("\n- Type : " + type);

		s.append("\n- Constant Attribute : " + constantAttributes);
		s.append("\n- Non Constant Attribute : " + nonConstantAttributes);
		s.append("\n - Incoming attributes : " + inAttributes);
		s.append("\n - Outgoing attributes : " + outAttributes);
		s.append("\n - Widget callbacks : " + callbacks);
		s.append("\n - Widget services : " + services);
		s.append("\n - Subscribers : " + subscribers);

		return s.toString();
	}

	/**
	 * Adds the data parameter to the constant attributes
	 *
	 * @param data The name of the constant attribute
	 * @see context.arch.storage.Attributes
	 */
	public void addConstantAttribute(AttributeNameValue<?> att) {
		constantAttributes.put(att.getName(), att); // TODO should match both name and value
	}

	/**
	 * Adds a set of attribute names to the constant attributes. Gets a 
	 * DISCOVERER_CONSTANT_ATTRIBUTE_NAME_VALUES DataObject
	 *
	 * @param data The DataObject containing a set of constant attribute names
	 * @return Error The error code
	 * @see context.arch.storage.Attributes
	 */
	public Error setConstantAttributes(DataObject data) {
		Error err = new Error();
		if (data != null) {
			// Gets the ATTRIBUTE_NAME_VALUES data object's content
			DataObject doAtt = data.getChildren().firstElement();
			
			//Retrieve the attributeNameValues object
			Attributes atts = Attributes.fromDataObject(doAtt);
			setConstantAttributes(atts);

			err.setError(Error.NO_ERROR);
		}
		else {
			err.setError(Error.INVALID_DATA_ERROR);
		}
		return err;
	}
	
	public void setConstantAttributes(Attributes atts) {
		this.constantAttributes = atts;
	}

	/**
	 * Adds the data parameter to the non constant attributes.
	 * May actually replace if already existent.
	 *
	 * @param att The non constant attribute
	 * @see context.arch.storage.Attribute
	 */
	public void addNonConstantAttribute(Attribute<?> att) {
		nonConstantAttributes.put(att.getName(), att);
	}

	/**
	 * May actually replace if already existent.
	 * @param atts
	 */
	public void addNonConstantAttributes(Attributes atts) {
		nonConstantAttributes.putAll(atts);
	}

	/**
	 * Adds a set of attribute names to the non constant attributes. Gets a 
	 * DISCOVERER_NON_CONSTANT_ATTRIBUTE_NAME_VALUES DataObject
	 *
	 * @param data The DataObject containing a set of non constant attribute names
	 * @return Error The error code
	 * @see context.arch.storage.Attribute
	 */
	public Error setNonConstantAttributes(DataObject data) {
		Error err = new Error();
		if (data != null) {
			DataObjects vTemp = data.getChildren();
			if (!vTemp.isEmpty()) {
				DataObject doNCAtt = (DataObject) vTemp.firstElement();
				Attributes NCatts = Attributes.fromDataObject(doNCAtt);
				setNonConstantAttributes(NCatts);
			}
			// There is no error if the field is empty
			err.setError(Error.NO_ERROR);
		}
		else
			err.setError(Error.INVALID_DATA_ERROR);
		return err;
	}
	
	public void setNonConstantAttributes(Attributes atts) {
		this.nonConstantAttributes = atts;
	}

	/**
	 * Adds the data parameter to the incoming attributes
	 *
	 * @param data The incoming attribute
	 * @see context.arch.storage.Attribute
	 */
	public void setInAttribute(Attribute<?> data){
		if (data != null){
			inAttributes.add(data);
		}
	}

	/**
	 * Adds a set of incoming attribute names to the cincoming attributes. Gets a 
	 * INCOMING_DISCOVERER_ATTRIBUTE_NAME_VALUES DataObject
	 *
	 * @param data The DataObject containing a set of incoming attribute names
	 * @return Error The error code
	 * @see context.arch.storage.Attribute
	 */
	public Error setInAttributes(DataObject data){
		Error err = new Error();
		if (data != null) {
			DataObjects vTemp = data.getChildren();
			if ( ! vTemp.isEmpty()) {
				DataObject doInAtt = vTemp.firstElement();
				for (DataObject InattValue : doInAtt.getChildren()) {
					setInAttribute(Attribute.fromDataObject(InattValue));
				}   
			}
			// There is no error if the field is empty
			err.setError(Error.NO_ERROR);
		}
		else
			err.setError(Error.INVALID_DATA_ERROR);
		return err;
	}

	/**
	 * Adds the data parameter to the outgoing attributes
	 *
	 * @param data The outgoing attribute
	 * @see context.arch.storage.Attribute
	 */
	public void setOutAttribute (Attribute<?> data){
		if (data != null ){
			outAttributes.add(data);
		}
	}

	/**
	 * Adds a set of outgoing attributes to the outgoing attributes. Gets a 
	 * OUTGOING_DISCOVERER_CONSTANT_ATTRIBUTE_NAME_VALUES DataObject
	 *
	 * @param data The DataObject containing a set of outgoing attribute names
	 * @return Error The error code
	 * @see context.arch.storage.Attribute
	 */
	public Error setOutAttributes(DataObject data){
		Error err = new Error();
		if (data != null) {
			DataObjects vTemp = data.getChildren();
			if ( ! vTemp.isEmpty()) {
				DataObject doAtt = vTemp.firstElement();
				for (DataObject attValue : doAtt.getChildren()) {
					setOutAttribute(Attribute.fromDataObject(attValue));
				}   
			}
			// There is no error if the field is empty
			err.setError(Error.NO_ERROR);
		}
		else
			err.setError(Error.INVALID_DATA_ERROR);
		return err;
	}

	/**
	 * Adds a set of server non constant attributes to the server attributes. Gets a 
	 * Discoverer.SERVER_NON_CONSTANT_ATTRIBUTES DataObject
	 *
	 * @param data The DataObject containing a set of server attribute names
	 * @return Error The error code
	 * @see context.arch.storage.Attribute
	 */
	public Error setServerNonConstantAttributes(DataObject data) {
		Error err = new Error();
		DataObjects vTemp;
		if ( (data != null) && ((vTemp = data.getChildren()) != null)) {
			DataObject doAtt = (DataObject) vTemp.firstElement();
			for (DataObject attValue : doAtt.getChildren()) {
//				System.out.println("ComponentDescription.setServerNonConstantAttributes");
//				System.out.println("\tattValue = " + attValue);
				setServerNonConstantAttribute(Attribute.fromDataObject(attValue)); // TODO: note that NonConstants are only Attribute? Why no value?
			}  
			err.setError(Error.NO_ERROR);
		}
		else
			err.setError(Error.INVALID_DATA_ERROR);
		return err;
	}

	/**
	 * Adds the data parameter to the server non constant attributes
	 *
	 * @param data The servernopn constant attribute
	 * @see context.arch.storage.Attribute
	 */ 
	public void setServerNonConstantAttribute(Attribute<?> data) {
		if (data != null) {
			nonConstantAttributes.put(data.getName(), data);
		}
	}

	/**
	 * Adds the data parameter to the subscriber attributes
	 *
	 * @param data The name of the subscriber attribute
	 * @see context.arch.storage.Attribute
	 */
	public void setSubscriber (String data){
		if (data != null && ! data.trim().equals("")){
			subscribers.add(data);
		}
	}

	/**
	 * Adds a set of subscriber names to the subscriber attributes. Gets a 
	 * DISCOVERER_CONSTANT_ATTRIBUTE_NAME_VALUES DataObject
	 *
	 * @param data The DataObject containing a set of subscriber attribute names
	 * @return Error The error code
	 * @see context.arch.storage.Attribute
	 */  
	public Error setSubscribers(DataObject data){
		Error err = new Error();
		DataObjects vTemp;
		if (data != null) {
			vTemp = data.getChildren();
			if (! vTemp.isEmpty()) {
				for (DataObject subValue : vTemp) {
					if (! subValue.getValue().isEmpty()){
						setSubscriber(subValue.getValue());
					}
				}
			}
			err.setError(Error.NO_ERROR);
		}
		else
			err.setError(Error.INVALID_DATA_ERROR);
		return err;
	}

	/**
	 * Adds the data parameter to the server services
	 *
	 * @param data The name of the server services
	 * @see context.arch.storage.Attribute
	 */
	public void setServerService (String data){
		if (data != null && ! data.trim().equals("")){
			services.add(data);
		}
	}

	/**
	 * Adds a set of server service names. Gets a 
	 * SERVER_SERVICES DataObject
	 *
	 * @param data The DataObject containing a set of server services names
	 * @return Error The error code
	 * @see context.arch.storage.Attribute
	 */
	public Error setServerServices(DataObject data){
		Error err = new Error();
		DataObjects vTemp;
		if (data != null) {
			vTemp = data.getChildren();
			if ( ! vTemp.isEmpty() ) {
				for (DataObject value : vTemp) {
					if (! value.getValue().isEmpty()){
						setServerService(value.getValue());
					}
				}  
			}
			err.setError(Error.NO_ERROR);
		}
		else
			err.setError(Error.INVALID_DATA_ERROR);
		return err;
	}

	/**
	 * Adds the data parameter to the widget callbacks
	 *
	 * @param data The name of the widget callbacks
	 * @see context.arch.storage.Attribute
	 */
	public void setWidgetCallback (String data){
		if (data != null && ! data.trim().equals("")){
			callbacks.add(data);
		}
	}

	/**
	 * Adds a set of widget callback names to the callback. Gets a 
	 * WIDGET_CALLBACKS DataObject
	 *
	 * @param data The DataObject containing a set of widget callback names
	 * @return Error The error code
	 * @see context.arch.storage.Attribute
	 */
	public Error setWidgetCallbacks(DataObject data){
		Error err = new Error();
		DataObjects vTemp;
		if (data != null) {
			vTemp = data.getChildren();
			if ( ! vTemp.isEmpty() ) {
				for (DataObject callValue : vTemp) {
					if (! callValue.getValue().isEmpty()){
						setWidgetCallback(callValue.getValue());
					}
				}  
			}
			err.setError(Error.NO_ERROR);
		}
		else
			err.setError(Error.INVALID_DATA_ERROR);
		return err;
	}

	/**
	 * Adds the data parameter to the widget services
	 *
	 * @param data The name of the widget services
	 * @see context.arch.storage.Attribute
	 */
	public void setWidgetService (String data){
		if (data != null && ! data.trim().equals("")){
			services.add(data);
		}
	}

	/**
	 * Adds a set of service names. Gets a 
	 * WIDGET_SERVICES DataObject
	 *
	 * @param data The DataObject containing a set of widget services names
	 * @return Error The error code
	 * @see context.arch.storage.Attribute
	 */
	public Error setWidgetServices(DataObject data){
		Error err = new Error();
		DataObjects vTemp;
		if (data != null) {
			vTemp = data.getChildren();
			if ( ! vTemp.isEmpty() ) {
				Enumeration<DataObject> list = vTemp.elements();
				DataObject value;
				while (list.hasMoreElements()){
					value = list.nextElement();
					if (!value.getValue().isEmpty()){
						setWidgetService(value.getValue());
					}
				}  
			}
			err.setError(Error.NO_ERROR);
		}
		else
			err.setError(Error.INVALID_DATA_ERROR);
		return err;
	}

	/**
	 * Adds the data parameter to the server callbacks
	 *
	 * @param data The name of the server callbacks
	 * @see context.arch.storage.Attribute
	 */
	public void setServerCallback(String data) {
		if (data != null && ! data.trim().equals("")){
			callbacks.add(data);
		}
	}

	/**
	 * Adds a set of server callback names. Gets a 
	 * SERVER_CALLBACKS DataObject
	 *
	 * @param data The DataObject containing a set of server callback names
	 * @return Error The error code
	 * @see context.arch.storage.Attribute
	 */
	public Error setServerCallbacks(DataObject data){
		Error err = new Error();
		DataObjects vTemp;
		if (data != null) {
			vTemp = data.getChildren();
			if ( ! vTemp.isEmpty() ) {
				Enumeration<DataObject> list = vTemp.elements();
				DataObject value;
				while (list.hasMoreElements()){
					value = list.nextElement();
					if (! value.getValue().isEmpty()) {
						setServerCallback(value.getValue());
					}
				}  
			}
			err.setError(Error.NO_ERROR);
		}
		else { err.setError(Error.INVALID_DATA_ERROR); }
		return err;
	}

	/**
	 * Sets the id. Gets an ID DataObject
	 *
	 * @param data The DataObject containing the id
	 * @return Error The error code
	 * @see context.arch.BaseObject#ID
	 */
	public Error setId(DataObject data) {
		Error err = new Error();  
		if (data != null && ! data.getValue().isEmpty()) {
			id = data.getValue();
			err.setError(Error.NO_ERROR);
		}
		else { err.setError(Error.INVALID_DATA_ERROR); }
		return err;
	}

	/**
	 * Sets the classname. Gets a 
	 * COMPONENT_CLASSNAME DataObject
	 *
	 * @param data The DataObject containing the classname
	 * @return Error The error code
	 * @see context.arch.discoverer.Discoverer
	 */  
	public Error setClassname(DataObject data) {
		Error err = new Error();  
		if (data != null && ! data.getValue().isEmpty()) {
			classname = data.getValue();
			err.setError(Error.NO_ERROR);
		}
		else { err.setError(Error.INVALID_DATA_ERROR); }
		return err;
	}

	/**
	 * Sets the component type. Gets a 
	 * TYPE DataObject
	 *
	 * @param data The DataObject containing the type
	 * @return Error The error code
	 * @see context.arch.discoverer.Discoverer
	 */ 
	public Error setType(DataObject data){
		Error err = new Error();  
		if (data != null && ! data.getValue().isEmpty()) {
			type = data.getValue();
			err.setError(Error.NO_ERROR);
		}
		else { err.setError(Error.INVALID_DATA_ERROR); }
		return err;
	}

	/**
	 * Sets the component hostname. Gets a 
	 * HOSTNAME DataObject
	 *
	 * @param data The DataObject containing the hostname
	 * @return Error The error code
	 * @see context.arch.discoverer.Discoverer
	 */ 
	public Error setHostname(DataObject data) {
		Error err = new Error();  
		if (data != null && ! data.getValue().isEmpty()) {
			hostname = data.getValue();
			err.setError(Error.NO_ERROR);
		}
		else { err.setError(Error.INVALID_DATA_ERROR); }
		return err;
	}

	/**
	 * Sets the component hostaddress. Gets a 
	 * HOSTADDRESS DataObject
	 *
	 * @param data The DataObject containing the hostaddress
	 * @return Error The error code
	 * @see context.arch.discoverer.Discoverer
	 */ 
	public Error setHostaddress(DataObject data){
		Error err = new Error();  
		if (data != null && ! data.getValue().isEmpty()) {
			hostaddress = data.getValue();
			err.setError(Error.NO_ERROR);
		}
		else { err.setError(Error.INVALID_DATA_ERROR); }
		return err;
	}

	/**
	 * Sets the component port. Gets a 
	 * PORT DataObject
	 *
	 * @param data The DataObject containing the port
	 * @return Error The error code
	 * @see context.arch.discoverer.Discoverer
	 */ 
	public Error setPort(DataObject data) {
		Error err = new Error();  
		if (data != null && ! data.getValue().isEmpty()){
			try {
				this.port = Integer.valueOf(data.getValue());
				err.setError(Error.NO_ERROR);
			}catch (NumberFormatException nfe) {
				System.out.println("Discoverer - addComponent - NumberFormatException " + nfe); 
				err.setError(Error.INVALID_DATA_ERROR);
			}
		}
		else { err.setError(Error.INVALID_DATA_ERROR); }
		return err;
	}

	/**
	 * Sets the component version. Gets a 
	 * VERSION DataObject
	 *
	 * @param data The DataObject containing the version
	 * @return Error The error code
	 * @see context.arch.discoverer.Discoverer
	 */ 
	public Error setVersion(DataObject data){
		Error err = new Error();  
		if (data != null && ! data.getValue().isEmpty()) {
			version = data.getValue();
			err.setError(Error.NO_ERROR);
		}
		else { err.setError(Error.INVALID_DATA_ERROR); }
		return err;
	}

	/**
	 * This method allows to get the modified descriptions fields and update the component
	 * Fields that can be modified : the non constant attributes (widget, server) and the subscribers, 
	 * It gets a NON_CONSTANT_ATTRIBUTE_NAME_VALUES
	 * The default update type is the add type (Discoverer.UPDATE_ADD_TYPE)
	 *  
	 * @param data The DataObject containing the modified fields
	 * @return Error The error code
	 * @see context.arch.comm.DataObject
	 * @see context.arch.discoverer.Discoverer
	 */
	public Error updateDescription(DataObject data) {
		Error err = updateDescription(data, Discoverer.UPDATE_ADD_TYPE);
		return err;
	}

	/**
	 * This method allows to get the modified descriptions fields and update the component
	 * Fields that can be modified : the non constant attributes (widget, server) and the subscribers, 
	 * It gets a NON_CONSTANT_ATTRIBUTE_NAME_VALUES
	 *  
	 * @param data The DataObject containing the modified fields
	 * @return Error The error code
	 * @see context.arch.comm.DataObject
	 * @see context.arch.discoverer.Discoverer
	 */
	public Error updateDescription(DataObject data, String updateType) {
		Error error = new Error();

		if (data != null) { 
			if (updateType == null){
				updateType = Discoverer.UPDATE_ADD_TYPE;
			}
			//Test the widget non constant attributes
			DataObject nonCAtt = data.getDataObject(Discoverer.NON_CONSTANT_ATTRIBUTE_NAME_VALUES);
			if (updateType.equalsIgnoreCase(Discoverer.UPDATE_REPLACE_TYPE) && nonCAtt != null) {
//				nonConstantAttributes = null; // don't need to nullify when assigning to new instance
				nonConstantAttributes = new Attributes();
			}
			setNonConstantAttributes(nonCAtt);

			// Test the subscribers
			DataObject subs = data.getDataObject(Subscribers.SUBSCRIBERS);
			if (updateType.equalsIgnoreCase(Discoverer.UPDATE_REPLACE_TYPE) && subs != null){
				subscribers = new ArrayList<String>();
			}
			setSubscribers(subs);

			error.setError(Error.NO_ERROR);
		}
		else
			error.setError(Error.INVALID_DATA_ERROR);
		return error ;
	}

	/**
	 * Returns an enumeration of the constant attribute names
	 *
	 * @return Enumeration The list of constant attribute names
	 *
  public Enumeration getConstantAttributeNames(){
    return constantAttributeNames.elements();
  }
	 */

	/**
	 * Returns an enumeration of the constant attribute values
	 *
	 * @return Enumeration The list of constant attribute values
	 *
  public Enumeration getConstantAttributeValues(){
    return constantAttributeValues.elements();
  }
	 */

	/**
	 * Returns an enumeration of the constant attribute names&values
	 *
	 * @return Enumeration The list of constant attribute objects, as a list of Attribute objects
	 */ 
	public Collection<AttributeNameValue<?>> getConstantAttributes() {
		Collection<AttributeNameValue<?>> c = new ArrayList<AttributeNameValue<?>>();
		for (Attribute<?> att : constantAttributes.values()) {
			c.add((AttributeNameValue<?>)att);
		}
		// TODO: alternative method: just natively store list of AttributeNameValue<?>
		
		return c;
	}
	
	public AttributeNameValue<?> getConstantAttribute(String name) {
		return (AttributeNameValue<?>)constantAttributes.get(name);
	}

	/**
	 * Returns an enumeration of the non constant attributes
	 *
	 * @return Collection The list of non constant attributes, as a list of Attribute objects
	 */ 
	public Attributes getNonConstantAttributes() {
		return nonConstantAttributes;
	}
	
	/**
	 * Return non-constant attributes as a collection of AttributeNameValue<?>,
	 * including only those with values (i.e. AttributeNameValue<?> not just Attribute<?>).
	 * So if a non-constant value had not had its value set, it would be omitted.
	 * @return
	 */
	public Collection<AttributeNameValue<?>> getNonConstantAttributeNameValues() {
		Collection<AttributeNameValue<?>> atts = new ArrayList<AttributeNameValue<?>>();
		
		for (Attribute<?> att : nonConstantAttributes.values()) {
			if (att instanceof AttributeNameValue<?>) {
				atts.add((AttributeNameValue<?>) att);
			}
		}
		
		return atts;
	}

	public AttributeNameValue<?> getNonConstantAttributeNameValue(String name) {
		return (AttributeNameValue<?>) nonConstantAttributes.get(name);
	}
	public Attribute<?> getNonConstantAttribute(String name) {
		return nonConstantAttributes.get(name);
	}

	/**
	 * Returns all non constant attributes (non constant and constant)
	 *
	 * @return 
	 */ 
	public Attributes getAllAttributes() {
		Attributes all = new Attributes();
		all.putAll(nonConstantAttributes);
		all.putAll(constantAttributes);
		return all;
	}
	
	/**
	 * Search for attribute with name from all attributes
	 * @param name
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends Comparable<? super T>> Attribute<T> getAttribute(String name) {
		// try searching non-constant attributes first
		Attribute<T> att = (Attribute<T>) nonConstantAttributes.get(name);
		
		// then try constants if not yet found
		if (att == null) { 
			att = (Attribute<T>) constantAttributes.get(name);
		}
		return att;
	}
	
	public <T extends Comparable<? super T>> T getAttributeValue(String name) {
		T value = nonConstantAttributes.getAttributeValue(name);
		if (value == null) {
			value = constantAttributes.getAttributeValue(name);
		}
		return value;
	}

	/**
	 * Returns an enumeration of the incoming attributes
	 *
	 * @return Enumeration The list of incoming attributes, as a list of Attribute objects
	 */ 
	public Collection<Attribute<?>> getInAttributes() {
		return inAttributes.values();
	}
	
	public Attribute<?> getInAttribute(String name) {
		return inAttributes.get(name);
	}

	/**
	 * Returns an enumeration of the outgoing attributes
	 *
	 * @return Enumeration The list of outgoing attributes, as a list of Attribute objects
	 */ 
	public Collection<Attribute<?>> getOutAttributes() {
		return outAttributes.values();
	}
	
	public Attribute<?> getOutAttribute(String name) {
		return outAttributes.get(name);
	}

	/**
	 * To retrieve just the names of constant attributes for matching.
	 *
	 * @return Enumeration The list of widget/server constant attributes names
	 */
	public Collection<String> getConstantAttributeNames() {
		return constantAttributes.keySet();
	}

	/**
	 * To retrieve just the values of constant attributes for matching.
	 */
	public Collection<String> getConstantAttributeValues() {
		Collection<String> result = new ArrayList<String>();
		for (AttributeNameValue<?> att : this.getConstantAttributes()) {
			result.add(att.getValue().toString()); // TODO storing value as string is a waste of bytes and may have problems if a custom object does not have a proper toString method
			// is the reason for using String to support data portability through the network protocol?
//			result.add(att.getValue()); // changed value from String to Object; not sure if this breaks anything
		}
		return result;
	}

	/**
	 * To retrieve the names and values of constant attributes for matching.
	 */
	public Collection<String> getConstantAttributeNameValues() {
		Collection<String> result = new ArrayList<String>();
		for (AttributeNameValue<?> att : this.getConstantAttributes()) {
			result.add(att.getName() + Discoverer.FIELD_SEPARATOR + att.getValue().toString());
		}
		return result;
	}

	public Collection<String> getNonConstantAttributeNames() {
		Collection<String> result = new ArrayList<String>();
		for (Attribute<?> att : nonConstantAttributes.values()) {
			result.add(att.getName());
		}
		return result;
	}

	public Collection<Object> getNonConstantAttributeValues() {
		Collection<Object> result = new ArrayList<Object>();
		for (Attribute<?> a : nonConstantAttributes.values()) {
			if (a instanceof AttributeNameValue<?>) { // TODO: would there be a case when these are not AttributeNameValue? 
				AttributeNameValue<?> att = (AttributeNameValue<?>) a;
				result.add(att.getValue());
			}
		}
		return result;
	}

	/**
	 * Returns an enumeration of the callbacks
	 *
	 * @return Enumeration The list of callbacks
	 */ 
	public Collection<String> getCallbacks(){
		return callbacks;
	}

	/**
	 * Returns an enumeration of the widget callbacks,
	 * but if the callback name contains an underscore,
	 * then it returns just the last word.
	 * For example : for widgetName_callbackName we just
	 * return callbackName
	 *
	 * @return Enumeration The list of callbacks
	 */ 
	public SortedSet<String> getJustNameOfWidgetCallbacks() {
		TreeSet<String> v = new TreeSet<String>();
		Iterator<String> list = getCallbacks().iterator();
		while (list.hasNext ()){
			String call = (String) list.next();
//			String underscore = "_";
			int lastIndex = call.lastIndexOf ("_");
			try {
				String res = call.substring (lastIndex + 1, call.length ());
				v.add (res);
			}
			catch(IndexOutOfBoundsException ioobe) {
				System.out.println("ComponentDescription - getJustNameOfWidgetCallbacks - no underscore");
				v.add (call);
			}
		}
		return v;
	}

	/**
	 * Returns an enumeration of the widget services
	 *
	 * @return Enumeration The list of widget services
	 */ 
	public Collection<String> getServices() {
		return services;
	}


	/**
	 * Returns an enumeration of the subscribers
	 *
	 * @return Enumeration The list of subscribers
	 */ 
	public Collection<String> getSubscribers() {
		return subscribers;
	}

	/**
	 * This method allows to return a DataObject containing information that should be constant to the original component:
	 * <ul>
	 *    <li>Discoverer.ID</li>
	 *    <li>Discoverer.HOSTNAME</li>
	 *    <li>Discoverer.PORT</li>
	 *    <li>Discoverer.TYPE</li>
	 *    <li>Discoverer.CONSTANT_ATTRIBUTE_NAME_VALUES</li>
	 * </ul>
	 *
	 * @return DataObject The data object containing the information
	 */
	public DataObject getBasicDataObject() {
		DataObjects v1 = new DataObjects();
		v1.add(new DataObject(Discoverer.ID, this.id));
		v1.add(new DataObject(Discoverer.HOSTNAME, this.hostname));
		v1.add(new DataObject(Discoverer.PORT, Integer.toString(this.port)));
		v1.add(new DataObject(Discoverer.TYPE, this.type));
		
		// constant attributes
		Attributes atts = new Attributes(this.constantAttributes);
		DataObjects v = new DataObjects();
		v.addElement(atts.toDataObject());
		v1.addElement(new DataObject(Discoverer.CONSTANT_ATTRIBUTE_NAME_VALUES, v));  

		return new DataObject(Discoverer.DISCOVERER_QUERY_REPLY_CONTENT, v1);
		
		
//		ArrayList<DataObject> v1 = new ArrayList<DataObject>(); // TODO: why is this an ArrayList?
//		v1.add(new DataObject(Discoverer.ID, this.id));
//		v1.add(new DataObject(Discoverer.HOSTNAME, this.hostname));
//		v1.add(new DataObject(Discoverer.PORT, Integer.toString(this.port)));
//		v1.add(new DataObject(Discoverer.TYPE, this.type));
//		
//		DataObjects v = new DataObjects();
//		v.addAll(v1);		
//		return new DataObject(Discoverer.DISCOVERER_QUERY_REPLY_CONTENT, v);
	}

	/**
	 * This method allows to compare 2 ComponentDescription objects.
	 * TODO: note that this equals comparison is very shallow and would not be good enough for comparing the data content
	 *
	 * @param otherComponent The ComponentDescription to compare to
	 * @return boolean The result of the comparison
	 */
	public boolean equals(ComponentDescription otherComponent) {
		ComponentDescription c1 = this;
		ComponentDescription c2 = otherComponent;

		if (	!c1.id.equals(c2.id) || !c1.classname.equals(c2.classname) || !c1.hostaddress.equals(c2.hostaddress) ||
				!c1.hostname.equals(c2.hostname) ||  !c1.type.equals(c2.type) || !c1.version.equals(c2.version) ||
				c1.port != c2.port) {
			return false;
		}
		return true;
	}

	@Override
	public ComponentDescription clone() {
		ComponentDescription comp = new ComponentDescription();
		
		comp.id = id;
		comp.classname = classname;
		comp.hostname = hostname;
		comp.hostaddress = hostaddress;
		comp.port = port;
		comp.version = version;
		comp.type = type;

		comp.constantAttributes.putAll(constantAttributes);
		comp.nonConstantAttributes.putAll(nonConstantAttributes);
		comp.subscribers.addAll(subscribers);
		comp.callbacks.addAll(callbacks);
		comp.services.addAll(services);
		comp.inAttributes.putAll(inAttributes);
		comp.outAttributes.putAll(outAttributes);
		
		return comp;
	}

}