package context.apps.demos.roomlight;

import context.arch.storage.Attribute;
import context.arch.storage.AttributeNameValue;
import context.arch.widget.Widget;

public final class LightWidget extends Widget {
	
	public static final String CLASSNAME = LightWidget.class.getName();

	public static final String LIGHT = "light"; // regards to output light level (0 to 10)
	public static final int LIGHT_MAX = 10;

	public static final String LIGHT_ON = "On";
	public static final String LIGHT_OFF = "Off";

	public static final String ROOM = "ROOM"; // which room the light is in

	private String room;

	/**
	 * 
	 * @param roomId of the room.
	 */
	public LightWidget(String room) {
		super(CLASSNAME, CLASSNAME);
		this.room = room;
	}

	@Override
	protected void init() {
		// non-constant attributes
		addAttribute(Attribute.instance(LIGHT, Integer.class));

		// constant attributes
		addAttribute(AttributeNameValue.instance(ROOM, room));
	}

}
