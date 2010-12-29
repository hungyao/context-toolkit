package context.apps.demos;

import context.apps.demos.accelerometer.AccelerometerApplication;
import context.apps.demos.homeactivity.HomeApplication;
import context.apps.demos.imautostatus.AutostatusIntelligibleApplication;
import context.apps.demos.roomlight.RoomApplication;
import context.arch.discoverer.Discoverer;

public class IntelligibilityDemos {
	
	public static void main(String[] args) {
		Discoverer.start();
		
		/*
		 * Start multiple apps
		 */

		RoomApplication roomDemo = new RoomApplication();
		roomDemo.setVisible(true);
		
		AccelerometerApplication accelDemo = new AccelerometerApplication();
		accelDemo.setVisible(true);

		new AutostatusIntelligibleApplication("Bob");

		HomeApplication homeDemo = new HomeApplication();
		homeDemo.setVisible(true);
	}

}
