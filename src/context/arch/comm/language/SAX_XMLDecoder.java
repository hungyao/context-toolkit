package context.arch.comm.language;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.Reader;
import java.util.Hashtable;

import javax.xml.parsers.SAXParserFactory;

import context.arch.comm.DataObject;

/**
 * This class provides access to the SAX XML parsing code using the specified
 * XML parser class/driver.  It implements ParserInterface and extends 
 * HandlerBase
 *
 * @see context.arch.comm.language.DecoderInterface
 * @see org.xml.sax.HandlerBase
 * 
 * Updated to adhere to SAX2
 * 
 * @author Anind Dey
 * @author Brian Y. Lim
 * 
 */
public class SAX_XMLDecoder extends DefaultHandler implements DecoderInterface {

	/**
	 * Debug flag. Set to true to see debug messages.
	 */
	public static boolean DEBUG = false;

	public static final String AELFRED_SAX_XML_DECODER = "com.microstar.xml.SAXDriver"; // this parser hasn't been updated since 2002, so I think let's upgrade to Xerces
	public static final String XERCES_SAX_XML_DECODER = "org.apache.xerces.parsers.SAXParser"; // the Xerces parser is bundled with JDK 1.5 onwards, so we should use this	

	/**
	 * The language for this class is XML
	 */
	public static final String LANGUAGE = "XML";

	private XMLReader parser;
	private String decoderDriver = XERCES_SAX_XML_DECODER;
	private DataObject data;

	/**
	 * Basic constructor which uses the default XML parser and sets the
	 * document handler to this class
	 *
	 * @exception context.arch.comm.language.InvalidDecoderException when the
	 *		given decoder can not be created
	 * @see #DEFAULT_SAX_XML_DECODER
	 */
	public SAX_XMLDecoder() throws InvalidDecoderException {
		try {
			parser = XMLReaderFactory.createXMLReader(decoderDriver);
		} catch (SAXException e) {
			try {
				// If unable to create an instance, let's try to use
				// the XMLReader from JAXP
				SAXParserFactory m_parserFactory = SAXParserFactory.newInstance();
				m_parserFactory.setNamespaceAware(true);

				parser = m_parserFactory.newSAXParser().getXMLReader();
			} catch (Exception e1) {
				e1.printStackTrace();
				return;
			}
		}

		ContentHandler handler = this;
		parser.setContentHandler(handler);
	}

	/**
	 * This method decodes the given XML data and returns the result in
	 * a DataObject.  It calls the parser created in the constructor
	 *
	 * @param XMLdata XML data to be decoded
	 * @return the DataObject containing the results of the decoded XML data
	 * @exception context.arch.comm.language.DecodeException when the
	 *		given XML data can not be decoded
	 * @see org.xml.sax.Parser#parse(InputSource)
	 */
	public DataObject decodeData(Reader XMLdata) throws DecodeException {
		try {
			parser.parse(new InputSource(XMLdata));
			return data;
		} catch (IOException ioe) {
			System.out.println("SAX_XMLParser parse IOException: "+ioe);
			throw new DecodeException();
		} catch (SAXException se) {
			System.out.println("SAX_XMLParser parse SAXException: "+se);
			throw new DecodeException();
		}
	}

	/**
	 * Returns the language being used in encoding and decoding
	 *
	 * @return the language being used in encoding and decoding
	 * @see #LANGUAGE
	 */
	public String getLanguage() {
		return LANGUAGE;
	}

	/**
	 * Returns the name of the parser driver being used for encoding and decoding
	 *
	 * @return the name of the parser driver being used for encoding and decoding
	 */
	public String getClassName() {
		return this.getClass().getName();
	}

	/**
	 * Receive notification of the beginning of the document.
	 * Creates a new DataObject
	 *
	 * @see context.arch.comm.DataObject
	 */
	@Override
	public void startDocument() {
		data = new DataObject();
	}

	/**
	 * Receive notification of the end of the document.  Empty method.
	 */
	@Override
	public void endDocument() {
	}

	/**
	 * Receive notification of the start of a new element.
	 * Adds attributes to the DataObject
	 *
	 * @param name String name of new element
	 * @param attributes Attributes object containing CTK attributes for new element
	 * @see context.arch.comm.DataObject#addElement(String,Hashtable)
	 */
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		Hashtable<String, String> hash = new Hashtable<String, String>();
		for (int i = 0; i < attributes.getLength(); i++) {
			hash.put(attributes.getQName(i).trim(), attributes.getValue(i).trim());
		}
		data.addElement(qName.trim(), hash);
	}

	/**
	 * Receive notification of the end of an element.  Closes the DataObject.
	 *
	 * @param name String name of ended element
	 * @see context.arch.comm.DataObject#closeElement(String)
	 */
	@Override
	public void endElement(String uri, String localName, String qName) {
		data.closeElement(qName.trim());
	}

	/**
	 * Receive notification of non-element and non-attribute characters ignoring whitespace.
	 * Adds the value to the current element
	 *
	 * @param ch array of characters read in
	 * @param start start position in the array
	 * @param length number of characters to read in from the array
	 * @see context.arch.comm.DataObject#addValue(String)
	 */
	@Override
	public void characters(char ch[], int start, int length) {
		String chars = new String(ch, start, length).trim();
		if (chars.length() > 0) {
			data.addValue(chars);
		}
	}
}
