package context.apps.demos.roomlight;

import context.arch.storage.Attribute;
import context.arch.storage.AttributeNameValue;
import context.arch.widget.Widget;

/**
 * 
 * @author Brian Y. Lim
 *
 */
public final class RoomWidget extends Widget {

	public static final String PRESENCE = "presence"; // regards to whether there are people in the room (number of people in the room > 0)
	public static final String BRIGHTNESS = "brightness"; // regards to brightness detected (0 to 255)
	public static final String ROOM = "room"; // which room the light is in
	
	public static final short BRIGHTNESS_MAX = 255;

	private String room;

	/**
	 * 
	 * @param roomId of the room.
	 */
	public RoomWidget(String room) {
		super(RoomWidget.class.getName(), RoomWidget.class.getName());
		this.room = room;
	}
	
	@Override
	protected void init() {
		// non-constant attributes
		addAttribute(Attribute.instance(PRESENCE, Integer.class));
		addAttribute(Attribute.instance(BRIGHTNESS, Short.class));
		
		// constant attributes
		addAttribute(AttributeNameValue.instance(ROOM, room), true);
	}

}
