package context.arch.comm.language;

import java.lang.StringBuffer;

import context.arch.comm.DataObject;
import context.arch.comm.DataObjects;

public class XMLEncoder implements EncoderInterface {

	/**
	 * Language used to encode
	 */ 
	public static final String LANGUAGE = "XML";

	/**
	 * Tag to mark end of line
	 */
	private static final String END_OF_LINE = "\r\n";

	/**
	 * XML version number is 1.0
	 */
	private static final String XML_VERSION = "1.0";

	/**
	 * Header to use for XML message
	 */

	private static final String	XML_HEADER = "<?xml version=\"" + XML_VERSION + "\"?>" + END_OF_LINE;

	private StringBuffer xmlText = new StringBuffer();

	/**
	 * Constructor for XMLEncoder
	 */
	public XMLEncoder() {
		xmlText.append(XML_HEADER);
	}

	/**
	 * Method used to encode data
	 *
	 * @param data Data to be encoded
	 * @see #addElement(context.arch.comm.DataObject)
	 * @see #getXML()
	 * @exception context.arch.comm.language.EncodeException if the data can't be encoded
	 */
	public String encodeData(DataObject data) throws EncodeException {
		addElement(data);
		return getXML();
	}

	/**
	 * Method to start XML element - creates opening tag
	 *
	 * @param tag XML element being started
	 */
	public String beginTag (String tag) {
		return ("<" + tag + ">"/* + END_OF_LINE*/);
	}

	/**
	 * Method to start XML element - creates opening tag and sets attributes
	 *
	 * @param tag XML element being started
	 * @param attributes attributes for XML element
	 */
	public String beginTag (String tag, String attributes) {
		if (attributes.length() > 0) {
			return ("<" + tag + " " + attributes + ">"/* + END_OF_LINE*/);
		} 
		else {
			return beginTag (tag);
		}
	}

	/**
	 * Method to add single XML tag
	 *
	 * @param tag single XML element 
	 */
	public String singleTag (String tag) {
		return ("<" + tag + "/>" + END_OF_LINE);
	}

	/**
	 * Method to end XML element - creates closing tag
	 *
	 * @param tag XML element being closed
	 */
	public String endTag (String tag) {
		return ("</" + tag + ">" + END_OF_LINE);
	}

	/**
	 * Method to add XML element
	 *
	 * @param elt DataObject element being added 
	 * @exception context.arch.comm.language.EncodeException if the element can't be added
	 */
	public void addElement(DataObject elt) throws EncodeException {
		xmlText.append(addXMLElement(elt));
	}

	/**
	 * Private method to add XML element
	 *
	 * @param elt DataObject element being added 
	 * @return String that contains encoding for element
	 * @exception context.arch.comm.language.EncodeException if the element can't be added
	 */
	private String addXMLElement(DataObject elt) throws EncodeException {
		String name = elt.getName ();
//		Map<String, String> atts = elt.getAttributes();
//		Vector val = elt.getValue();
		String val = elt.getValue();
		DataObjects children = elt.getChildren();		
		String xmlElement = new String ();
		String xmlAttributes = new String ();

		// throw exception if no name
		if (name == null) { 
			//System.out.println("elt = " + elt);
			// may be null if one part of the DataObject is empty, i.e.: new DataObject()
			// so should return null instead, if returning nothing
			throw new EncodeException("tag is null");
		} // null tag
		
		// no value or children
		if (val == null && children.isEmpty()) {
			xmlElement = singleTag(name);
		}
		
		else {
			xmlElement = beginTag(name, xmlAttributes); // start tag
	
			// handle value; assume only 1
			if (val != null) {
				xmlElement += val;
			}
			
			// handle children
			for (DataObject child : children) {
				xmlElement += addXMLElement(child);
			}

			xmlElement += endTag (name); // end tag
		}

		return xmlElement;
	}

	/**
	 * Returns XML encoding as a string
	 *
	 * @return XML encoding
	 */		
	public String getXML() {
		return xmlText.toString();
	}

	/**
	 * Returns language used to encode XML
	 *
	 * @return language used to encode XML
	 * @see #LANGUAGE
	 */		
	public String getLanguage() {
		return LANGUAGE;
	}

	/**
	 * Returns name of class used to encode XML
	 *
	 * @return name of class used to encode XML
	 */		
	public String getClassName() {
		return this.getClass().getName();
	}
}
