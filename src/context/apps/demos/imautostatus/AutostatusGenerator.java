package context.apps.demos.imautostatus;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import weka.core.Instance;
import weka.core.Instances;
import context.arch.discoverer.component.ClassnameElement;
import context.arch.discoverer.component.ConstantAttributeElement;
import context.arch.discoverer.query.ANDQueryItem;
import context.arch.discoverer.query.RuleQueryItem;
import context.arch.enactor.Generator;
import context.arch.storage.AttributeNameValue;
import context.arch.storage.Attributes;
import context.arch.widget.ClassifierWidget;

/**
 * Class to populate and update a {@link AutostatusWidget} from a WEKA dataset.
 * @author Brian Y. Lim
 *
 */
public class AutostatusGenerator extends Generator {
	
	private Instances dataset;

	public AutostatusGenerator(String userId) {
		super(
				// subscription query for specific AutostatusWidget with USER_ID = userId
				new ANDQueryItem(
						RuleQueryItem.instance(new ClassnameElement(AutostatusWidget.class)),
						RuleQueryItem.instance(new ConstantAttributeElement(AttributeNameValue.instance(ResponsivenessWidget.USER_ID, userId)))
						), 
				"Autostatus", // outcome name
				userId); // enactor id
		
		/*
		 * Preset dataset for loading each Instance
		 */
		try {
			String datasetName = "demos/imautostatus-dtree/imautostatus-test.arff";
			Reader arffReader = new FileReader(datasetName);
			dataset = new Instances(arffReader);
			dataset.setClassIndex(dataset.numAttributes()-1); // last attribute is class
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Load a scenario instance from the test ARFF file.
	 * @param instanceIndex index of the scenario index to load
	 * @return
	 */
	public void loadInstance(int instanceIndex) {
		// load instance from dataset
		Instance instance = dataset.instance(instanceIndex);

		// Set data values from .arff instance
		Attributes data = ClassifierWidget.instanceToAttributes(instance);

		// put data into widget
		super.updateOutWidget(data);
	}

}
