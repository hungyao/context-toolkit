package context.apps.demos.roomlight;

import java.util.HashMap;
import java.util.Map;

import context.apps.ContextModel;
import context.arch.discoverer.query.AbstractQueryItem;
import context.arch.enactor.Enactor;
import context.arch.enactor.EnactorXmlParser;
import context.arch.widget.Widget;
import context.arch.widget.WidgetXmlParser;

/**
 * 
 * @author Brian Y. Lim
 *
 */
public class RoomModel extends ContextModel {
	
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
		
		// 
		Map<String, Comparable<?>> roomConstAttValues = new HashMap<String, Comparable<?>>() {{
			put("room", room);
		}};
		Map<String, Comparable<?>> lampConstAttValues = new HashMap<String, Comparable<?>>() {{
			put("lamp", lamp);
		}};
		
		/*
		 * Room sensor Widget
		 */
		roomWidget = WidgetXmlParser.getWidget(
				"demos/room-rules/room-widget.xml", 
				room, // widgetId
				roomConstAttValues);
//		roomWidget.start(true);
		addWidget(roomWidget);
		
		/*
		 * Light actuator Widget and Service
		 */
		lightWidget = WidgetXmlParser.getWidget(
				"demos/room-rules/light-widget.xml", 
				lamp, // widgetId
				lampConstAttValues);
		lightService = new LightService(lightWidget, application);
		lightWidget.addService(lightService);
//		lightWidget.start(true);
		addWidget(lightWidget);
		
		/*
		 * Generator for RoomWidget.
		 * Sets its attribute values via method invocation
		 */
		AbstractQueryItem<?,?> roomWidgetQuery = WidgetXmlParser.getWidgetSubscriptionQuery(
				"demos/room-rules/room-widget.xml", 
				room, // widgetId
				roomConstAttValues);
		roomGenerator = new RoomGenerator(
				roomWidgetQuery,
				room); // generatorId
		addEnactor(roomGenerator);
		
		/*
		 * Enactor to use rules about RoomWidget to update LightWidget
		 */
		roomEnactor = EnactorXmlParser.getEnactor(
				"demos/room-rules/room-enactor.xml",
				room + '_' + lamp, // enactorId
				roomConstAttValues,  // for targeting in widget
				lampConstAttValues); // for targeting out widget
		addEnactor(roomEnactor);
		
		/*
		 * Start widgets and enactors
		 */
		start();
	}

}
