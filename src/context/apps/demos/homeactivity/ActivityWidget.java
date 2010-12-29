package context.apps.demos.homeactivity;

import java.util.List;

import context.arch.storage.Attribute;
import context.arch.storage.Attributes;
import context.arch.widget.SequenceWidget;

public final class ActivityWidget extends SequenceWidget {
	
	public static final String CLASSNAME = ActivityWidget.class.getName();
	
	public static final String ACTIVITY = "ACTIVITY";

	public ActivityWidget() {
		super(CLASSNAME, CLASSNAME, 5); // sequence length 5
		super.start(true);
	}

	@Override
	protected void init() {
		// non-constant attributes
		addAttribute(Attribute.instance(ACTIVITY, String.class));
	}
	
	public static class ActivityData extends WidgetData {

		public List<String> activities;

		public ActivityData(long timestamp) {
			super(ActivityWidget.class.getName(), timestamp);
		}

		@Override
		public Attributes toAttributes() {
			Attributes atts = new Attributes();
			for (String activity : activities) {
				atts.addAttribute(ACTIVITY, activity);
			}
			return atts;
		}
		
	}

}
