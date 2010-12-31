package context.apps.demos.roomlight;

import context.arch.discoverer.query.AbstractQueryItem;
import context.arch.enactor.Enactor;
import context.arch.widget.Widget;
import context.arch.widget.WidgetXmlParser;

/**
 * Model class to contain widget and enactor models for the smart room demo application. 
 * @author Brian Y. Lim
 *
 */
public class RoomModel {
	
	protected Widget roomWidget;
	protected Widget lightWidget;

	protected RoomGenerator roomGenerator;
	protected Enactor roomEnactor;
	
	protected LightService lightService;

	public static final String room = "Living Room";
	public static final String lamp = "Ceiling";
	public static final short BRIGHTNESS_MAX = 255;
	public static final int LIGHT_MAX = 10;

	public RoomModel(RoomApplication application) {
		/*
		 * Room sensor Widget
		 */
		roomWidget = new RoomWidget(room);
		
		/*
		 * Light actuator Widget and Service
		 */
		lightWidget = new LightWidget(room);
		lightService = new LightService(lightWidget, application);
		lightWidget.addService(lightService);
		
		/*
		 * Generator for RoomWidget.
		 * Sets its attribute values via method invocation
		 */
		AbstractQueryItem<?,?> roomWidgetQuery = WidgetXmlParser.createWidgetSubscriptionQuery(roomWidget);
		roomGenerator = new RoomGenerator(
				roomWidgetQuery,
				room); // generatorId
		
		/*
		 * Enactor to use rules about RoomWidget to update LightWidget
		 */
		AbstractQueryItem<?,?> lightWidgetQuery = WidgetXmlParser.createWidgetSubscriptionQuery(lightWidget);
		roomEnactor = new RoomEnactor(roomWidgetQuery, lightWidgetQuery); // for targeting out widget
	}
	
	/**
	 * Empty constructor for subclassing
	 */
	public RoomModel() {}

}
