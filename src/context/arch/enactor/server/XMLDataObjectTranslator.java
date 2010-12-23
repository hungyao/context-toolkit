package context.arch.enactor.server;

import java.io.IOException;
import java.io.StringWriter;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

import context.arch.comm.DataObject;
import context.arch.comm.DataObjects;

/**
 * Class used by the EnactorXMLServer to translate DataObjects into an XML format.
 * 
 * @author newbergr
 */
public class XMLDataObjectTranslator {
	private XMLDataObjectTranslator() {}

	public static String toXML(DataObject d) {
		return toXML(toJDOMDocument(d));
	}

	public static String toXML(Document d) {
		StringWriter sw = new StringWriter();

		try {
			XMLOutputter xmlOutputter = new XMLOutputter();
			xmlOutputter.setOmitDeclaration(true);
			xmlOutputter.setNewlines(true);
			xmlOutputter.setIndent(true);
			xmlOutputter.output(d,sw);
		} catch (IOException ioe) {}

		return sw.toString();
	}

	public static Document toJDOMDocument(DataObject data) {
		Element topElement = new Element("DataObject");
		topElement.setAttribute("name",data.getName());
		processChildrenToXML(data,topElement);
		return new Document(topElement);
	}

	public static DataObject fromXML(Document d) {
		Element rootElement = d.getRootElement();
		
		if ("DataObject".equals(rootElement.getName())) {
			return processChildrenFromXML(rootElement);
		} else {
			return null;
		}
	}

	private static void processChildrenToXML(DataObject d, Element element) {
		String value = d.getValue();
		DataObjects children = d.getChildren();
//		Vector v = d.getValue();
//		Iterator i = v.iterator();
		
		// process value; assuming only one
		if (value != null) {
			Element newElement = new Element("Data");
			newElement.setAttribute("value", value);
			element.addContent(newElement);
		}
		
		// process children
		for (DataObject childDataObject : children) {
			Element newElement = new Element("DataObject");
			newElement.setAttribute("name",childDataObject.getName());
			element.addContent(newElement);
			processChildrenToXML(childDataObject,newElement);
		}

		// deprecated
//		while (i.hasNext()) {
//			Object o = i.next();
//
//			if (o instanceof DataObject) {
//				DataObject childDataObject = (DataObject) o;
//				Element newElement = new Element("DataObject");
//				newElement.setAttribute("name",childDataObject.getName());
//				element.addContent(newElement);
//				processChildrenToXML(childDataObject,newElement);
//			} else {
//				Element newElement = new Element("Data");
//				newElement.setAttribute("value", String.valueOf(o));
//				element.addContent(newElement);
//			}
//		}
	}

	private static DataObject processChildrenFromXML(Element e) {
		DataObjects v = new DataObjects();
		
		String value = null; // if assuming only one value

		for (Object o : e.getChildren()) {
			Element child = (Element)o;
			String childName = child.getName();
			
			if ("DataObject".equals(childName)) {
				v.add(processChildrenFromXML(child));
			} else if ("Data".equals(childName)) {
				value = child.getAttributeValue("value");
			}
		}

		return new DataObject(e.getAttributeValue("name"), value, v);
	}

}
