package context.apps.demos.accelerometer;

import context.arch.discoverer.component.ClassnameElement;
import context.arch.discoverer.query.ClassifierWrapper;
import context.arch.discoverer.query.RuleQueryItem;
import context.arch.enactor.ClassifierEnactor;

public class AccelerometerEnactor extends ClassifierEnactor {

	public AccelerometerEnactor() {
		super(
				RuleQueryItem.instance(new ClassnameElement(AccelerometerWidget.class)), 
				RuleQueryItem.instance(new ClassnameElement(MotionWidget.class)), 
				
				MotionWidget.MOTION, 
				
				new ClassifierWrapper(
						"demos/accelerometer-nb/nb.model", // file path to Weka classifier model
						"demos/accelerometer-nb/accelerometer-activity-train.arff"), // file path to Weka ARFF file with relevant header
				""); // no ID
	}

}
