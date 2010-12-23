package context.apps.demos.accelerometer;

import context.apps.ContextModel;

public class AccelerometerModel extends ContextModel {
	
	AccelerometerWidget accelWidget;
	MotionWidget motionWidget;

	AccelerometerGenerator accelGenerator;
	AccelerometerEnactor accelEnactor;

	public AccelerometerModel() {
		super();
		
		/*
		 * Widgets
		 */
		accelWidget = new AccelerometerWidget();
		motionWidget = new MotionWidget();
		addWidget(accelWidget);
		addWidget(motionWidget);
		
		/*
		 * Enactors
		 */
		accelGenerator = new AccelerometerGenerator();
		accelEnactor = new AccelerometerEnactor();
		addEnactor(accelGenerator);
		addEnactor(accelEnactor);
		
		start();
	}
	
	protected void update(int instanceIndex) {
		accelGenerator.loadInstance(instanceIndex);
	}

}
