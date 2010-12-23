package context.apps.demos.imautostatus;

import context.arch.discoverer.component.ClassnameElement;
import context.arch.discoverer.component.ConstantAttributeElement;
import context.arch.discoverer.query.ANDQueryItem;
import context.arch.discoverer.query.ClassifierWrapper;
import context.arch.discoverer.query.RuleQueryItem;
import context.arch.enactor.ClassifierEnactor;
import context.arch.storage.AttributeNameValue;

/**
 * Enactor to handle classifier reasoning for IM Autostatus application.
 * It takes input from AutostatusWidget and outputs changes to ResponsivenessWidget.
 * @author Brian Y. Lim
 *
 */
public class AutostatusEnactor extends ClassifierEnactor {

	public AutostatusEnactor(String userId) {
		super(
				// in-widget query
				new ANDQueryItem(
						RuleQueryItem.instance(new ClassnameElement(AutostatusWidget.class)),
						RuleQueryItem.instance(new ConstantAttributeElement(AttributeNameValue.instance(ResponsivenessWidget.USER_ID, userId)))
						),
				// out-widget query
				new ANDQueryItem(
						RuleQueryItem.instance(new ClassnameElement(ResponsivenessWidget.class)),
						RuleQueryItem.instance(new ConstantAttributeElement(AttributeNameValue.instance(ResponsivenessWidget.USER_ID, userId)))
						),
					
				ResponsivenessWidget.RESPONSIVENESS, // outcome name
				
				// delegate for classifier
				new ClassifierWrapper(
						"demos/imautostatus-dtree/imautostatus.model", 	 // file path to Weka classifier model
						"demos/imautostatus-dtree/imautostatus-test.arff" // file path to Weka ARFF file with relevant header),
						),  
						
				userId); // enactor id
	}

}
