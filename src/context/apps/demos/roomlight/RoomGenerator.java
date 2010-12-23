package context.apps.demos.roomlight;

import context.arch.discoverer.query.AbstractQueryItem;
import context.arch.enactor.Generator;

public class RoomGenerator extends Generator {

	public RoomGenerator(AbstractQueryItem<?,?> outWidgetQuery, String id) {
		super(outWidgetQuery, "RoomSensors", id);
	}
	
	/*
	 * The following are assessor methods for convenience.
	 */
	
//	public void setPresence(int presence) {
//		super.setAttributeValue("presence", presence);
//	}
//	
//	public void setBrightness(short brightness) {
//		super.setAttributeValue("brightness", brightness);
//	}

}
