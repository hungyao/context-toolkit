package context.apps.demos.helloroom;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JLabel;

import context.arch.comm.DataObject;
import context.arch.service.Service;
import context.arch.service.helper.FunctionDescription;
import context.arch.service.helper.FunctionDescriptions;
import context.arch.service.helper.ServiceInput;
import context.arch.widget.Widget;

/**
 * Custom service for the room application to set the light level.
 * @author Brian Y. Lim
 *
 */
public class LightService extends Service {
	
	// package protected to be accessible to UI of HelloRoom app
	JLabel lightLabel;

	@SuppressWarnings("serial")
	public LightService(final Widget widget) {
		super(widget, "LightService", 
				new FunctionDescriptions() {
					{ // constructor
						// define function for the service
						add(new FunctionDescription(
								"lightControl", 
								"Sets the light level of the lamp", 
								widget.getNonConstantAttributes()));
					}
				});
		
		/*
		 * set up light label (for use in a UI)
		 */
		lightLabel = new JLabel("0") {{
			setHorizontalAlignment(JLabel.RIGHT);
			setBorder(BorderFactory.createEtchedBorder());
			
			setOpaque(true); // to allow background color to show
			// set color to represent light level
			setBackground(Color.black); // initially dark
		}};
	}

	@Override
	public DataObject execute(ServiceInput serviceInput) {
		int light = serviceInput.getInput().getAttributeValue("light");

		// light is from 0 to 10
		lightLabel.setText(String.valueOf(light));
		lightLabel.setBackground(new Color(light*25, light*23, light*16));
		
		return new DataObject(); // no particular info to return
	}

}
