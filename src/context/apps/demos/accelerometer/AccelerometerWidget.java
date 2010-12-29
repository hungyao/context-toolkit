package context.apps.demos.accelerometer;

import context.arch.widget.ClassifierWidget;

public final class AccelerometerWidget extends ClassifierWidget {

	public AccelerometerWidget() {
		super(
				"userId", // id
				"AccelerometerWidget", // widget classname
				"demos/accelerometer-nb/accelerometer-activity-test.arff" // path to .arff file
				);		
		super.start(true);
	}
	
	@Override
	protected void init() {
		// nothing
	}

}
