package context.apps.demos.roomlight;

import context.arch.discoverer.query.AbstractQueryItem;
import context.arch.enactor.EnactorXmlParser;
import context.arch.storage.Attributes;
import context.arch.widget.WidgetXmlParser;

/**
 * Model class to contain widget and enactor models for the smart room demo application. 
 * This version uses XML to describe them.
 * @author Brian Y. Lim
 *
 */
public class RoomModelXML extends RoomModel {

	@SuppressWarnings("serial")
	public RoomModelXML(RoomApplication application) {
		super();
		
		/*
		 * Constant attribute values to fully describe widgets to instantiate
		 */
		Attributes roomConstAttValues = new Attributes() {{
			addAttribute("room", room);
		}};
		Attributes lampConstAttValues = new Attributes() {{
			addAttribute("lamp", lamp);
		}};
		
		/*
		 * Room sensor Widget
		 */
		roomWidget = WidgetXmlParser.createWidget(
				"demos/room-rules/room-widget.xml", 
				room, // widgetId
				roomConstAttValues);
		
		/*
		 * Light actuator Widget and Service
		 */
		lightWidget = WidgetXmlParser.createWidget(
				"demos/room-rules/light-widget.xml", 
				lamp, // widgetId
				lampConstAttValues);
		lightService = new LightService(lightWidget, application);
		lightWidget.addService(lightService);
		
		/*
		 * Generator for RoomWidget.
		 * Sets its attribute values via method invocation
		 */
		AbstractQueryItem<?,?> roomWidgetQuery = WidgetXmlParser.createWidgetSubscriptionQuery(
				"demos/room-rules/room-widget.xml", 
				room, // widgetId
				roomConstAttValues);
		roomGenerator = new RoomGenerator(
				roomWidgetQuery,
				room); // generatorId
		
		/*
		 * Enactor to use rules about RoomWidget to update LightWidget
		 */
		roomEnactor = EnactorXmlParser.createEnactor(
				"demos/room-rules/room-enactor.xml",
				room + '_' + lamp, // enactorId
				roomConstAttValues,  // for targeting in widget
				lampConstAttValues); // for targeting out widget
	}

}
