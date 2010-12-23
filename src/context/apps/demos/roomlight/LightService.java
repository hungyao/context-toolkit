package context.apps.demos.roomlight;

import context.arch.comm.DataObject;
import context.arch.service.Service;
import context.arch.service.helper.FunctionDescription;
import context.arch.service.helper.FunctionDescriptions;
import context.arch.service.helper.ServiceInput;
import context.arch.widget.Widget;

/**
 * Custom service for the room application to set the light level.
 * This is attached to a LightWidget, and is coupled to RoomApplication to be able to set its light level.
 * @author Brian Y. Lim
 *
 */
public class LightService extends Service {

	public static final String LIGHT_ON = "lightOn";
	public static final String LIGHT_OFF = "lightOff";
	private RoomApplication application;

	@SuppressWarnings("serial")
	public LightService(final Widget widget, RoomApplication application) {
		super(widget, "LightService", 
				new FunctionDescriptions() {
					{ // constructor
						/*
						 * define functions for the service
						 */
						// light on and vary brightness
						add(new FunctionDescription(
								LIGHT_ON, 
								"Sets the light level of the lamp", 
								widget.getNonConstantAttributes(),
								FunctionDescription.FUNCTION_SYNC));
						// light off
						add(new FunctionDescription(
								LIGHT_OFF, 
								"Sets the light of the lamp to Off", 
								null, // no inputs
								FunctionDescription.FUNCTION_SYNC));
					}
				});
		this.application = application;
	}

	@Override
	public DataObject execute(ServiceInput serviceInput) {
		String functionName = serviceInput.getFunctionName();
		
		if (functionName.equals(LIGHT_ON)) {
			int light = serviceInput.getInput().getAttributeValue("light");
			application.setLight(light);
		}
		
		else if (functionName.equals(LIGHT_OFF)) {
			application.setLight(0);
		}
		
		return new DataObject(); // no particular info to return
	}

}
