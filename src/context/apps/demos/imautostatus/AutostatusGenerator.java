package context.apps.demos.imautostatus;

import weka.core.Instance;
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

	public AutostatusGenerator(String userId) {
		super(
				// subscription query for specific AutostatusWidget with USER_ID = userId
				new ANDQueryItem(
						RuleQueryItem.instance(new ClassnameElement(AutostatusWidget.class)),
						RuleQueryItem.instance(new ConstantAttributeElement(AttributeNameValue.instance(ResponsivenessWidget.USER_ID, userId)))
						), 
				"Autostatus", // outcome name
				userId); // enactor id
		
		start();
	}
	
	/**
	 * Load a scenario instance from the test ARFF file.
	 * @param instanceIndex index of the scenario index to load
	 * @return
	 */
	public void setInstance(Instance instance) {
		// Set data values from .arff instance
		Attributes data = ClassifierWidget.instanceToAttributes(instance);

		// put data into widget
		super.updateOutWidget(data);
	}

}
