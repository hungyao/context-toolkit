package context.apps.demos.imautostatus;

import java.util.HashMap;
import java.util.Map;

import context.arch.intelligibility.DescriptiveExplainerDelegate;


/**
 * Utility class to convert feature names to user-meaningful descriptive text.
 * @author Brian Y. Lim
 *
 */
public class AutostatusDescriptiveExplainerDelegate extends DescriptiveExplainerDelegate {
	
	public AutostatusDescriptiveExplainerDelegate() {
		super();

		prettyNames.put("UserInputCountFeature(120)", "Number of inputs in the past 2 min");
		prettyNames.put("timeSinceLastOMsg", "Time since last incoming message");
		prettyNames.put("Focus", "Whether your message window is in focus");
		prettyNames.put("KBCountFeature(30)", "Number of keypresses in the past 1/2 min");
		prettyNames.put("KBCountFeature(60)", "Number of keypresses in the past min");

		units.put("UserInputCountFeature(120)", "");
		units.put("timeSinceLastOMsg", "sec"); // TODO need to pre-process
		units.put("Focus", "");
		units.put("KBCountFeature(30)", "");
		units.put("KBCountFeature(60)", "");

//		definitions.put("UserInputCountFeature(120)", "");
//		definitions.put("timeSinceLastOMsg", "");
//		definitions.put("Focus", "");
//		definitions.put("KBCountFeature(30)", "");
//		definitions.put("KBCountFeature(60)", "");
		
		Map<Object, String> classPrettyValues = new HashMap<Object, String>();
		classPrettyValues.put("0", "Within 1 min");
		classPrettyValues.put("1", "After 1 min");
		prettyValues.put(ResponsivenessWidget.RESPONSIVENESS, classPrettyValues);
	}

}
