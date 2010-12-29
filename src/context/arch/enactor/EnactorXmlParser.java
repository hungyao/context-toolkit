package context.arch.enactor;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;

import context.arch.discoverer.ComponentDescription;
import context.arch.discoverer.query.AbstractQueryItem;
import context.arch.service.helper.ServiceInput;
import context.arch.storage.Attribute;
import context.arch.storage.Attributes;
import context.arch.widget.Widget;
import context.arch.widget.WidgetXmlParser;

/**
 * Utility class to generate rule-based Enactors from XML declarations.
 * 
 * @author Brian Y. Lim
 * @apiviz.uses context.arch.enactor.Enactor
 * @see WidgetXmlParser
 */
public class EnactorXmlParser {
	
	/**
	 * Stub class to generate rule-based Enactors from XML declarations.
	 * @author Brian Y. Lim
	 *
	 */
	private static class EnactorXml {

		private URL baseUrl;
	
		private Element rootNode;
	
		private String enactorId;
	
		/**
		 * Create an Enactor stub defined in an XML file.
		 * @param filename
		 * @param enactorId
		 */
		public EnactorXml(String filename, String enactorId) {
			Document document = WidgetXmlParser.getDocument(filename);
			rootNode = document.getRootElement(); // <Enactor>
	
			this.enactorId = enactorId;
	
			try {
				baseUrl = new File(filename).getParentFile().toURI().toURL(); // for relative HREF
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
	
		/**
		 * @return the enactor Id in URL encoded form.
		 */
		private String getFullId() {
			return WidgetXmlParser.urlEncode(enactorId);
		}
		
	}

	/* ------------------------------------------------------------------------------ */

	/**
	 * Very basic enactor with no constant attribute values of in and out widgets set.
	 * Id set as timestamp of creation.
	 */
	public static Enactor createEnactor(String filename) {
		return createEnactor(filename,
				String.valueOf(System.currentTimeMillis()),
				new Attributes(), // empty
				new Attributes()
				);
	}

	/**
	 * Convenience method to get an enactor with minimal arguments.
	 * Id of enactor is automatically set by appending the Ids of inWidget and outWidget
	 * @param filename of XML file containing enactor definition
	 * @param inWidget to extract constant attribute values from, regarding in-widget
	 * @param outWidget to extract constant attribute values from, regarding out-widget
	 * @return Enactor specified in XML file.
	 */
	public static Enactor createEnactor(String filename,
			Widget inWidget, Widget outWidget) {
		String enactorId = inWidget.getId() + "_" + outWidget.getId();

		return createEnactor(new EnactorXml(filename, enactorId), 
				inWidget.getConstantAttributes(), outWidget.getConstantAttributes());
	}

	/**
	 * Convenience method to get an enactor with minimal arguments.
	 * @param filename of XML file containing enactor definition
	 * @param enactorId to set enactor to
	 * @param inConstAttValues constant attribute values regarding in-widget
	 * @param outConstAttValues constant attribute values regarding out-widget
	 * @return Enactor specified in XML file.
	 */
	public static Enactor createEnactor(String filename, String enactorId,
			Attributes inConstAtts, Attributes outConstAtts) {
		return createEnactor(new EnactorXml(filename, enactorId), inConstAtts, outConstAtts);
	}

	private static Enactor createEnactor(final EnactorXml exml, 
			Attributes inConstAtts, Attributes outConstAtts) {

		final Namespace ns = exml.rootNode.getNamespace();

		final String enactorName = exml.rootNode.getAttributeValue("name");

		try {
			/*
			 * get stubs in and out widgets
			 */
			String inHref = exml.rootNode.getChild("InWidget", ns).getAttributeValue("href");
			final ComponentDescription inWidgetStub = WidgetXmlParser.createWidgetStub(new URL(exml.baseUrl, inHref), "", inConstAtts); // doesn't set id
			String outHref = exml.rootNode.getChild("OutWidget", ns).getAttributeValue("href");
			final ComponentDescription outWidgetStub = WidgetXmlParser.createWidgetStub(new URL(exml.baseUrl, outHref), "", outConstAtts);

			/*
			 * extract subscription queries for widgets
			 */
			AbstractQueryItem<?, ?> inWidgetQuery = WidgetXmlParser.createWidgetSubscriptionQuery(inWidgetStub);
			AbstractQueryItem<?, ?> outWidgetQuery = WidgetXmlParser.createWidgetSubscriptionQuery(outWidgetStub);

			// get outcome name
			String outcomeName = exml.rootNode.getChildText("OutcomeName", ns);

			/*
			 * parse and store constant vars used in the XML notation
			 */
			final Map<String, Comparable<?>> constVars = new HashMap<String, Comparable<?>>(); // <name, stringValue>
			for (Object child : exml.rootNode.getChildren("const", ns)) {
				Element constElement = (Element) child;
				String name = constElement.getAttributeValue("name");
				try {
					Class<?> constType = WidgetXmlParser.toClass(constElement.getAttributeValue("type"));
					String strValue = constElement.getText();
					Comparable<?> value = (Comparable<?>) constType.getMethod("valueOf", String.class)
																   .invoke(null, strValue);
					constVars.put(name, value);
				} catch (Exception e) { e.printStackTrace(); }
			}

			/*
			 * construct enactor with details for EnactorReferences in its constructor
			 */
			Enactor enactor = new Enactor(inWidgetQuery, outWidgetQuery, outcomeName, exml.getFullId()) {

				{ // Constructor
					// to store queries by name to be reusable (e.g. for ElseQueryItem)
					Map<String, AbstractQueryItem<?, ?>> queries = new HashMap<String, AbstractQueryItem<?,?>>();

					// iterate enactor references
					for (Object refChild : exml.rootNode.getChildren("Reference", ns)) {
						Element refElement = (Element) refChild;				
						String refName = refElement.getAttributeValue("name");

						/*
						 * Text for query
						 */
						Element queryElement = refElement.getChild("Query", ns);
						String queryName = queryElement.getAttributeValue("name");
						String queryStr = queryElement.getText().trim();

						// parse query into Abstract Syntax Tree
						QueryParser parser = new QueryParser(queryStr, constVars, queries, inWidgetStub);
						AbstractQueryItem<?, ?> query = parser.parseQuery();
						queries.put(queryName, query); // query may be referenced, so store it

						/*
						 * Parse outcome assignments
						 * Can have multiple
						 */
						List<AttributeEvalParser<?>> assnParsers = new ArrayList<AttributeEvalParser<?>>();

						for (Object outcomeChild : refElement.getChildren("Outcome", ns)) {
							Element outcomeElement = (Element) outcomeChild;

							String outAttName = outcomeElement.getAttributeValue("outAttribute");
							Attribute<?> outAtt = outWidgetStub.getNonConstantAttribute(outAttName);

							String assnStr = outcomeElement.getText().trim();

							// parse assignment
							AttributeEvalParser<?> assnParser = AttributeEvalParser.instance(outAtt, assnStr, constVars);
							assnParsers.add(assnParser);
						}

						/*
						 * ServiceInputs to trigger with enactor reference.
						 * May have more than one
						 */
						List<ServiceInput> serviceInputs = new ArrayList<ServiceInput>();

						for (Object serviceChild : refElement.getChildren("ServiceInput", ns)) {
							Element serviceElement = (Element) serviceChild;

							String serviceName = serviceElement.getAttributeValue("service");
							String functionName = serviceElement.getAttributeValue("function");

							// get attributes
							Attributes allAtts = outWidgetStub.getAllAttributes();
							Attributes inputAtts = new Attributes();
							for (Object attChild : serviceElement.getChildren("Attribute", ns)) {
								Element attElement = (Element) attChild;
								String attName = attElement.getAttributeValue("name");

								// get attribute from outWidget's non-constant attributes
								Attribute<?> att = Attribute.instance(
										attName, 
										allAtts.get(attName).getType());
								inputAtts.add(att);
							}						

							ServiceInput input = new ServiceInput(serviceName, functionName, inputAtts);
							serviceInputs.add(input);
						}					

						/*
						 * Create enactor reference with accumulated parameters
						 */
						EnactorReference er = new EnactorReference(this, query, refName, assnParsers, serviceInputs);
						addReference(er);
					}
				}

				@Override				
				public String getClassname() {
					return enactorName;
				}
			};
			
			// start the enactor
			enactor.start();

			return enactor;
		} catch (MalformedURLException e) { e.printStackTrace(); }

		return null;
	}

	/* ------------------------------------------------------------------------------ */

	/**
	 * Very basic generator with no constant attribute values of out widget set.
	 * Id set as timestamp of creation.
	 */
	public static Generator createGenerator(String filename) {
		return createGenerator(
				new EnactorXml(
						filename, 
						String.valueOf(System.currentTimeMillis())),
				new Attributes() // empty
				);
	}

	public static Generator createGenerator(String filename, Widget outWidget) {
		String enactorId = "_" + outWidget.getId();
		EnactorXml exml = new EnactorXml(filename, enactorId);

		return createGenerator(exml, outWidget.getConstantAttributes());
	}

	private static Generator createGenerator(final EnactorXml exml, Attributes outConstAtts) {

		final Namespace ns = exml.rootNode.getNamespace();

		final String enactorName = exml.rootNode.getAttributeValue("name");

		try {
			/*
			 * get stubs for out widget
			 */
			String outHref = exml.rootNode.getChild("OutWidget", ns).getAttributeValue("href");
			final ComponentDescription outWidgetStub = WidgetXmlParser.createWidgetStub(new URL(exml.baseUrl, outHref), "", outConstAtts);

			/*
			 * extract subscription query for widget
			 */
			AbstractQueryItem<?, ?> outWidgetQuery = WidgetXmlParser.createWidgetSubscriptionQuery(outWidgetStub);

			// get outcome name
			String outcomeName = exml.rootNode.getChildText("OutcomeName", ns);

			/*
			 * parse and store constant vars used in the XML notation
			 */
			final Map<String, Object> constVars = new HashMap<String, Object>(); // <name, stringValue>
			for (Object child : exml.rootNode.getChildren("const", ns)) {
				Element constElement = (Element) child;
				String name = constElement.getAttributeValue("name");
				try {
					Class<?> constType = WidgetXmlParser.toClass(constElement.getAttributeValue("type"));
					String strValue = constElement.getText();
					Object value = constType.getMethod("valueOf", String.class)
					.invoke(null, strValue);
					constVars.put(name, value);
				} catch (Exception e) { e.printStackTrace(); }
			}

			/*
			 * construct generator with details for EnactorReferences in its constructor
			 */
			Generator generator = new Generator(outWidgetQuery, outcomeName, exml.getFullId()) {				
				@Override				
				public String getClassname() {
					return enactorName;
				}
			};			
			return generator;

		} catch (MalformedURLException e) { e.printStackTrace(); }

		return null;
	}
}
