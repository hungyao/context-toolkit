package context.arch.widget;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

import context.arch.discoverer.ComponentDescription;
import context.arch.discoverer.component.ClassnameElement;
import context.arch.discoverer.component.ConstantAttributeElement;
import context.arch.discoverer.query.ANDQueryItem;
import context.arch.discoverer.query.AbstractQueryItem;
import context.arch.discoverer.query.RuleQueryItem;
import context.arch.storage.Attribute;
import context.arch.storage.AttributeNameValue;
import context.arch.storage.Attributes;

/**
 * Utility class to generate Widget from an XML declaration. 
 * Can also generate widget stubs in the form of ComponentDescription to be used
 * to generate QueryItems for subscribing to the widget.
 * @author Brian Y. Lim
 * @apiviz.uses context.arch.widget.Widget
 *
 */
public class WidgetXmlParser {
	
	/**
	 * Stub class to generate Widget from an XML declaration. 
	 * @author Brian Y. Lim
	 *
	 */
	private static class WidgetXml {

		private String classname;
		private String id;
		
		/** 
		 * Whether to register to the Discoverer.
		 * Usually true, but null if just creating a stub
		 */
		private Boolean register = true;
		
		private Element rootNode;
		
		private WidgetXml(Document document, String id) {
			rootNode = document.getRootElement(); // <Widget>
			
			// widget name
			this.classname = rootNode.getAttributeValue("name");
	//		setWidgetClassName(name);
			
			// quality ID with widget name
	//		setId(widgetName + '_' + id);
			this.id = id;
		}
		
		private WidgetXml(String filename, String id) {
			this(getDocument(filename), id);
		}
		
		public String getFullId() {
			return classname + "_" + urlEncode(id);
		}
		
	}
	
	public static AbstractQueryItem<?,?> createWidgetSubscriptionQuery(String filename, String id, Attributes constAtts) {
		return createWidgetSubscriptionQuery(
				WidgetXmlParser.createWidgetStub(
						new WidgetXml(filename, id),
						constAtts
				));
	}
	
	/**
	 * Convenience method to create a subscription query from a widget object. 
	 * @param widget
	 * @return
	 */
	public static AbstractQueryItem<?,?> createWidgetSubscriptionQuery(Widget widget) {
		return createWidgetSubscriptionQuery(
				WidgetXmlParser.createWidgetStub(widget)
				);
	}
	
	/**
	 * Gets a subscription query based on the name of the widget, and 
	 * constant attribute values. The specified values help select the widget of interest
	 * @param widgetStub a minimal description of the widget containing its name, id (maybe; but buggy now), and limited constant attributes with values.
	 * @return an ANDQueryItem of multiple conditions to be satisfied to find this widget to subscribe to
	 */
	public static AbstractQueryItem<?,?> createWidgetSubscriptionQuery(ComponentDescription widgetStub) {
		String widgetName = widgetStub.classname;
//		String id = widgetStub.id; // may not have been set
		Collection<AttributeNameValue<?>> constAtts = widgetStub.getConstantAttributes();
		
		// start with matching widget name
		ANDQueryItem query = new ANDQueryItem(
				RuleQueryItem.instance(new ClassnameElement(widgetName)));
		
		// add id if it was set
//		if (id != null) {
//			query.add(new RuleQueryItem<String, String>(new IdElement(id)));
//		}
		// still buggy
		
		// add constant attributes with values that were set
		for (AttributeNameValue<?> att : constAtts) {
			query.add(RuleQueryItem.instance(new ConstantAttributeElement(att)));
		}
		
		// don't add non-constant attributes since tracking them would be for condition queries instead of subscription
//		for (String attName : widgetStub.getNonConstantAttributeNames()) {
//			query.add(RuleQueryItem.instance(new NonConstantAttributeElement(attName)));
//		}
		
		//System.out.println("getWidgetSubscriptionQuery query = " + query);
		return query;
	}

	public static ComponentDescription createWidgetStub(Widget widget) {		
		ComponentDescription comp = new ComponentDescription();
		comp.classname = widget.getClassname();
		comp.id = widget.getId();
		comp.setConstantAttributes(widget.getConstantAttributes());
		comp.setNonConstantAttributes(widget.getNonConstantAttributes());
		
		return comp;
	}

	public static ComponentDescription createWidgetStub(final WidgetXml wxml, Attributes constAtts) {
		Widget widget = WidgetXmlParser.createWidget(wxml, constAtts);
		return WidgetXmlParser.createWidgetStub(widget);
	}
	
	public static ComponentDescription createWidgetStub(URL href, String widgetId, Attributes constAtts) {
		WidgetXml wxml = new WidgetXml(getDocument(href), widgetId);
		wxml.register = null; // so that no widget not registered
		return WidgetXmlParser.createWidgetStub(wxml, constAtts);
	}
	
	public static Widget createWidget(String filename, String widgetId, Attributes constAtts) {
		return createWidget(new WidgetXml(filename, widgetId), constAtts);
	}
	
	/**
	 * Very basic widget with no explicit ID constant attribute values being set.
	 * The ID is set by the timestamp of creation.
	 * @param filename
	 * @param widgetId
	 * @return
	 */
	public static Widget createWidget(String filename) {
		return createWidget(new WidgetXml(
				filename, 
				String.valueOf(System.currentTimeMillis())), 
				new Attributes()); // empty constantAttVars map
	}
	
	/**
	 * 
	 * @param wxml
	 * @param constAttValues <attName, value>
	 * @return
	 */
	public static Widget createWidget(final WidgetXml wxml, final Attributes constAtts) {
		Widget widget = new Widget(
				wxml.getFullId(), 
				wxml.classname) {
			
			@SuppressWarnings("unchecked")
			@Override
			protected void init() {
				Namespace ns = wxml.rootNode.getNamespace();
				
				// add attributes
				for (Element attrElem : (List<Element>) wxml.rootNode
															.getChild("Attributes", ns)
															.getChildren("Attribute", ns)) {
					
					// XML: could have specified as child element, but using attributes enforces string format
					String attName = attrElem.getAttributeValue("name");
					String typeStr = attrElem.getAttributeValue("type");
					boolean constant = Boolean.parseBoolean(attrElem.getAttributeValue("constant"));
					
					Attribute<?> att = createAttribute(attName, typeStr, constAtts.getAttributeValue(attName), constant);
					addAttribute(att, constant);
				}
			}

			
			@Override
			public String getClassname() {
				return wxml.classname;
			}
		};
		
		widget.start(wxml.register); // to init and register to Discoverer after construction
		return widget;
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends Comparable<? super T>> Attribute<T> createAttribute(String attName, String typeStr, T attValue, boolean constant) {
		Attribute<T> att;		
		
		if (constant) {
			att = AttributeNameValue.instance(
					attName,
					attValue
					);
		}
		else {
			Class<T> attType = (Class<T>) toClass(typeStr);
			att = Attribute.instance(attName, attType);
		}
		
		return att;
	}
	
	public static Class<?> toClass(String typeName) {
		if (typeName.equalsIgnoreCase("int")) {
			return Integer.class;
		}
		else if (typeName.equalsIgnoreCase("float")) {
			return Float.class;
		}
		else if (typeName.equalsIgnoreCase("double")) {
			return Double.class;
		}
		else if (typeName.equalsIgnoreCase("byte")) {
			return Byte.class;
		}
		else if (typeName.equalsIgnoreCase("short")) {
			return Short.class;
		}
		else if (typeName.equalsIgnoreCase("long")) {
			return Long.class;
		}
		else if (typeName.equalsIgnoreCase("boolean")) {
			return Boolean.class;
		}
		else if (typeName.equalsIgnoreCase("string")) {
			return String.class;
		}
		else { // use reflection to get class
			try {
				return Class.forName(typeName);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				return null;
			}		
		}
	}
	
	/* ================================================================================== */

	private static SAXBuilder builder = new SAXBuilder();
	
	/**
	 * Convenience method to get an XML document from a file name.
	 * @param filename
	 * @return XML document
	 */
	public static Document getDocument(String filename) {
		try {
			return (Document) builder.build(new File(filename));
		} catch (JDOMException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Convenience method to get an XML document from a URL.
	 * @param url
	 * @return XML document
	 */
	public static Document getDocument(URL url) {
		try {
			return (Document) builder.build(url);
		} catch (JDOMException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static String urlEncode(String s) {
		try {
			return URLEncoder.encode(s, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

}
