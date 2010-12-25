/*
 * AbstractDescriptionElement.java
 *
 * Created on July 5, 2001, 3:36 PM
 */

package context.arch.discoverer.component;

import context.arch.discoverer.ComponentDescription;
import context.arch.discoverer.query.comparison.AbstractComparison;
import context.arch.storage.AttributeNameValue;
import context.arch.comm.DataObject;
import context.arch.comm.DataObjects;

/**
 * Abstract class to describe an element of a component ({@link ComponentDescription}), 
 * particularly important for the discovery subscription and query mechanism of the Context Toolkit.
 * Subclasses would typically describe elements like host name, port number, non-constant attributes, etc.
 * However, elements can also be multi-variate and complex combinations of the information in components, e.g. Euclidean distance of x and y attributes in a Widget.
 * 
 * @param <E> class of return of extractElement(ComponentDescription); i.e. the format of the element that we want to compare
 * @param <C1> class of the first comparison argument to AbstractComparison
 * @param <C2> class of the second comparison argument to AbstractComparison; this is usually set to be the same as C1.
 *
 * @author Agathe
 * @author Brian Y. Lim
 */
public abstract class AbstractElement<E, C1,C2> {

	/** Tag for DataObject version */
	public static final String ABSTRACT_DESCRIPTION_ELEMENT = "abDesElem";

	/** Tag for the E generics class parameter of AbstractDescriptionElement */
	public static final String ABSTRACT_DESCRIPTION_ELEMENT_E = "abDesElemE";
	/*
	 * eClass is a hack to be able to retain knowledge of which class C1 and C2 are at runtime, after generics erasure.
	 */
	private Class<E> e;
	public Class<?> getE() { return e; }
	protected Class<C1> c1;
	public Class<?> getC1() { return c1; }
	protected Class<C2> c2;
	public Class<?> getC2() { return c2; }

	/** Tag for DataObject version */
	public static final String NAME = "abName";

	/** Tag for DataObject version */
	public static final String VALUE = "abValue";

	/** The name of this description element */
	protected String elementName;

	/** Value of this element */
	protected C2 value;

	protected AbstractElement(String elementName, 
			Class<E> e,
			Class<C1> c1, Class<C2> c2) {
		this.elementName = elementName;
		this.e = e;
		this.c1 = c1;
		this.c2 = c2;
	}

	protected AbstractElement(String elementName, 
			Class<E> e,
			Class<C1> c1, Class<C2> c2,
			C2 value) {
		this(elementName, e, c1, c2);
		this.value = value;
	}

	public String getElementName() {
		return elementName;
	}

	public C2 getValue() {
		return value;
	}
	
	/**
	 * When passing value through the query system, it could to be converted to a string.
	 * This method is called to get the string representation.
	 * Default is to return the toString() method of the getValue().
	 * If value is a complex object, then this method should be overridden to allow sufficient conversion.
	 * @return
	 * @see #getValueDataObject()
	 */
	public String getValueCodex() {
		return value.toString();
	}
	
	/**
	 * When passing value through the query system, it could to be converted to a DataObject.
	 * This is especially useful for complex value objects. The system would check if this is set first,
	 * then check {@link #getValueCodex()}. It should be overridden by the subclass to set it if used.
	 * @return null by default, otherwise a DataObject representing the value if set.
	 * @see #getValueCodex()
	 */
	public DataObject getValueDataObject() {
		return null;
	}

	/** Sets the value of this element */
	// TODO: change to support storing value as an actual Value, and not a String
	// maybe store as setValue(value, value.class) internally
	public void setValue(C2 value){
		this.value = value;
				
//		if (value instanceof String) {
//			((String)this.value).toLowerCase();
//		}
	}

	/**
	 * Returns the element from the componentDescription corresponding to this 
	 * description element
	 *
	 * @param cd
	 * @return Object
	 */
	public abstract E extractElement(ComponentDescription component);

	/**
	 * Returns true if the comparison between the value of this object and the value
	 * of the corresponding field of the componentDescription returns true
	 *
	 * @param componentDescription The component to compare to this object
	 * @param comparison The comparison element to use
	 * @return boolean
	 */
	public abstract Boolean processQueryItem(ComponentDescription component, AbstractComparison<C1,C2> comparison);

	/** Returns a printable version */
	public String toString(){
		return getElementName() + ": " + getValue();
	}

	/**
	 * Returns the DataObject version
	 */
	@SuppressWarnings("serial")
	public DataObject toDataObject() {
		DataObjects v = new DataObjects();
		v.add(new DataObject(NAME, getElementName()));
//		v.add(new DataObject(VALUE, getValueCodex())); // TODO: why does this transmit a String and yet expects an Object (see fromDataObject)? Because it eventually needs to be XML serialized; still not generalizable though 
		
		// add value as either ValueCodex or DataObject
		final DataObject valueDobj = getValueDataObject();
		if (valueDobj != null) {
			v.add(new DataObject(VALUE, new DataObjects() {{ add(valueDobj); }}));
		}
		else {
			v.add(new DataObject(VALUE, getValueCodex()));
		}
		
		v.add(new DataObject(ABSTRACT_DESCRIPTION_ELEMENT_E, getE().getName()));
		v.add(new DataObject(AbstractComparison.ABSTRACT_COMPARISON_C1, getC1().getName()));
		v.add(new DataObject(AbstractComparison.ABSTRACT_COMPARISON_C2, getC2().getName()));
		return new DataObject(ABSTRACT_DESCRIPTION_ELEMENT, v);
	}

	/**
	 * Takes a DataObject and return an AbstractDescriptionElement object
	 *
	 * @param data
	 * @return AbstractDescriptionElement
	 */
	public static AbstractElement<?,?,?> fromDataObject(DataObject data) {		
		String name = data.getDataObject(NAME).getValue();
		String valueCodex =  data.getDataObject(VALUE).getValue();

//		return AbstractDescriptionElement.fromDataObject(name, value, 
//				Class.forName(eClassName),
//				Class.forName(c1), Class.forName(c2));
				
		if (name.equals(ComponentDescription.ID_ELEMENT)) {
			return new IdElement(valueCodex);
		}
		else if (name.equals(ComponentDescription.PORT_ELEMENT)) { 
			return new PortElement(Integer.valueOf(valueCodex));
		}
		else if (name.equals(ComponentDescription.TYPE_ELEMENT) ){ 
			return new TypeElement(valueCodex);
		}
		else if (name.equals(ComponentDescription.CLASSNAME_ELEMENT)) {
			return new ClassnameElement(valueCodex);
		}
		else if (name.equals(ComponentDescription.HOSTNAME_ELEMENT)) {
			return new HostnameElement(valueCodex);
		}
		
		/*
		 * Value for these are AttributeNameValue instead of string
		 */
		else if (name.equals(ComponentDescription.CONST_ATT_ELEMENT)) {
			AttributeNameValue<?> valueAtt = AttributeNameValue.fromDataObject(data.getDataObject(VALUE));
			return new ConstantAttributeElement(valueAtt);
		}
		else if (name.equals(ComponentDescription.NON_CONST_ATT_ELEMENT)) { 
			AttributeNameValue<?> valueAtt = AttributeNameValue.fromDataObject(data.getDataObject(VALUE));
			return new NonConstantAttributeElement(valueAtt);
		}
		
		else if (name.equals(ComponentDescription.NON_CONST_ATT_NAME_ELEMENT)) { // just matching for name, and not value
			return new NonConstantAttributeNameElement(valueCodex); // value would be name of Attribute
		}
		else if (name.equals(ComponentDescription.SERVICE_ELEMENT)) {
			return new ServiceElement(valueCodex);
		}
		else if (name.equals(ComponentDescription.SUBSCRIBER_ELEMENT)) {
			return new SubscriberElement(valueCodex);
		} 
		else if (name.equals(ComponentDescription.CALLBACK_ELEMENT)) {
			return new CallbackElement(valueCodex);
		}
		
		// some other element type not defined by the existing library
		else {
			// use reflections to initialize
			String eClassName = data.getDataObject(ABSTRACT_DESCRIPTION_ELEMENT_E).getValue();
			String c1 = data.getDataObject(AbstractComparison.ABSTRACT_COMPARISON_C1).getValue();
			String c2 = data.getDataObject(AbstractComparison.ABSTRACT_COMPARISON_C2).getValue();

			try {
				return AbstractElement.fromDataObject(name, valueCodex, 
						Class.forName(eClassName),
						Class.forName(c1), Class.forName(c2));
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				return null;
			}	
//			System.out.println("AbstractDescriptionElement.name = " + name);
//			System.out.println("AbstractDescriptionElement.value = " + value);
//			return null;
		}
	}

	/**
	 * Returns the AbstracDescriptionElement corresponding to the specified name
	 * TODO: still need to work out an extensible way to retrieve a concrete
	 * AbstractDescriptionElement
	 *
	 * @param
	 * @param
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected static <E,C1,C2> AbstractElement<E,C1,C2> fromDataObject(String name, Object value, 
			Class<E> eClass,
			Class<C1> c1, Class<C2> c2) {
//		System.err.println("AbstractDescriptionElement.factory");
//		System.out.println("\tname = " + name + ", value = " + value + " (" + value.getClass().getSimpleName() + ")");
		
		try {
			AbstractElement<E,C1,C2> instance = 
				(AbstractElement<E,C1,C2>) Class.forName(name)
														   .getConstructor(
																   String.class,
																   Class.class,
																   Class.class, Class.class,
																   Object.class)
														   .newInstance(
																   name,
																   eClass, 
																   c1, c2,
																   value); // TODO: need to parse value from string to actual class
			// TODO: may want to support some mapping instead of assuming name is the fully qualified class name; also may not handle classes that need to be remotely loaded
			return instance;			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public abstract AbstractComparison<C1,C2> getDefaultComparison();

}
