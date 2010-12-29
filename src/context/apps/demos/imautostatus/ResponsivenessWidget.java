package context.apps.demos.imautostatus;

import context.arch.storage.Attribute;
import context.arch.storage.AttributeNameValue;
import context.arch.widget.Widget;

/**
 * Widget to represent the responsiveness status of a buddy.
 * This represents the output context state of the application.
 * @author Brian Y. Lim
 *
 */
public class ResponsivenessWidget extends Widget {
	
	public static final String CLASSNAME = ResponsivenessWidget.class.getName();

	public static final String USER_ID = "USER_ID"; // regards to individual user ID
	private String userId;
	
	public static final String RESPONSIVENESS = "RESPONSIVENESS";

	public ResponsivenessWidget(String userId) {
		super(CLASSNAME + '_' + userId, CLASSNAME);
		this.userId = userId;
		
		super.start(true);
	}

	@Override
	protected void init() {
		// non-constant attributes
		addAttribute(Attribute.instance(RESPONSIVENESS, String.class));

		// constant attributes
		addAttribute(AttributeNameValue.instance(USER_ID, userId), true);
	}

}
