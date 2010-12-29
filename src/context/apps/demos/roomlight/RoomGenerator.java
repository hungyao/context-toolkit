package context.apps.demos.roomlight;

import context.arch.discoverer.query.AbstractQueryItem;
import context.arch.enactor.Generator;

public class RoomGenerator extends Generator {

	public RoomGenerator(AbstractQueryItem<?,?> outWidgetQuery, String id) {
		super(outWidgetQuery, "RoomSensors", id);
		start();
	}

}
