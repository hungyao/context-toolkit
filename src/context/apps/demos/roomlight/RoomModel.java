package context.apps.demos.roomlight;

import context.arch.discoverer.query.AbstractQueryItem;
import context.arch.enactor.Enactor;
import context.arch.enactor.EnactorXmlParser;
import context.arch.storage.Attributes;
import context.arch.widget.Widget;
import context.arch.widget.WidgetXmlParser;

/**
 * 
 * @author Brian Y. Lim
 *
 */
public class RoomModel {
	
	Widget roomWidget;
	Widget lightWidget;

	RoomGenerator roomGenerator;
	Enactor roomEnactor;
	
	private LightService lightService;

	public static final String room = "Living Room";
	public static final String lamp = "Ceiling";
	public static final short BRIGHTNESS_MAX = 255;
	public static final int LIGHT_MAX = 10;

	@SuppressWarnings("serial")
	public RoomModel(RoomApplication application) {
		super();
		
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
