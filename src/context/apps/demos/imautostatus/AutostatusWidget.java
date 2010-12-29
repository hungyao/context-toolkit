package context.apps.demos.imautostatus;

import context.arch.storage.AttributeNameValue;
import context.arch.widget.ClassifierWidget;

/**
 * Widget to represent the state of the input features of the IM Autostatus application.
 * @author Brian Y. Lim
 *
 */
public class AutostatusWidget extends ClassifierWidget {

	public static final String USER_ID = "USER_ID"; // regards to individual user ID
	private String userId;

	/**
	 * 
	 * @param userId of the buddy of the instant message window.
	 */
	public AutostatusWidget(String userId) {
		super(
				userId, // widget id
				"AutostatusWidget", // widget classname
				"demos/imautostatus-dtree/imautostatus-test.arff" // path to .arff file
				);
		this.userId = userId;
		
		super.start(true);
	}

	@Override
	protected void init() {
		// non-constant attributes already set by superclass

		// constant attributes
		addAttribute(AttributeNameValue.instance(USER_ID, userId), true);
	}

}
