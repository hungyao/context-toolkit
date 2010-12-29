package context.apps.demos.accelerometer;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import weka.core.Instance;
import weka.core.Instances;
import context.arch.discoverer.component.ClassnameElement;
import context.arch.discoverer.query.RuleQueryItem;
import context.arch.enactor.Generator;
import context.arch.storage.Attributes;
import context.arch.widget.ClassifierWidget;

public class AccelerometerGenerator extends Generator {

	private Instances dataset;

	public AccelerometerGenerator() {
		super(RuleQueryItem.instance(new ClassnameElement(AccelerometerWidget.class.getName())), 
				"Accelerometer", null);
		
		/*
		 * Preset dataset
		 */
		try {
			String datasetName = "demos/accelerometer-nb/accelerometer-activity-test.arff";
			Reader arffReader = new FileReader(datasetName);
			dataset = new Instances(arffReader);
			dataset.setClassIndex(dataset.numAttributes()-1); // last attribute is class
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		start();
	}

	/**
	 * Load a scenario instance from the test ARFF file.
	 * @param instanceIndex index of the scenario index to load
	 * @return
	 */
	public void loadInstance(int instanceIndex) {
		// load instance from dataset
		Instance instance = dataset.instance(instanceIndex);

		/*
		 * Set data values from .arff instance
		 */
		Attributes data = ClassifierWidget.instanceToAttributes(instance);

		// put data into widget
		updateOutWidget(data);
	}

}
