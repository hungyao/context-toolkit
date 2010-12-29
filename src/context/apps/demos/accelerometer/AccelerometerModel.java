package context.apps.demos.accelerometer;

public class AccelerometerModel {
	
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
		
		/*
		 * Enactors
		 */
		accelGenerator = new AccelerometerGenerator();
		accelEnactor = new AccelerometerEnactor();
	}
	
	protected void update(int instanceIndex) {
		accelGenerator.loadInstance(instanceIndex);
	}

}
