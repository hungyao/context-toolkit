package context.apps.demos;

import context.apps.ContextModel;
import context.apps.demos.accelerometer.AccelerometerApplication;
import context.apps.demos.homeactivity.HomeApplication;
import context.apps.demos.imautostatus.AutostatusApplication;
import context.apps.demos.roomlight.RoomApplication;

public class IntelligibilityDemos {
	
	public static void main(String[] args) {
		ContextModel.startDiscoverer();
		
		/*
		 * Start multiple apps
		 */

		RoomApplication roomDemo = new RoomApplication();
		roomDemo.setVisible(true);
		
		AccelerometerApplication accelDemo = new AccelerometerApplication();
		accelDemo.setVisible(true);

		AutostatusApplication autostatusDemo = new AutostatusApplication("Bob");
		autostatusDemo.setVisible(true);

		HomeApplication homeDemo = new HomeApplication();
		homeDemo.setVisible(true);
	}

}
