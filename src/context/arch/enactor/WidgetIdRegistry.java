package context.arch.enactor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * A registry used by the EnactorSubscriptionManager to track widgets.
 * 
 * @author newbergr
 */
public class WidgetIdRegistry {
	
	public WidgetIdRegEntry get(String widgetId) {
		return map.get(widgetId);
	}

	public WidgetIdRegEntry remove(String widgetId) {
		return map.remove(widgetId);
	}

	public WidgetIdRegEntry put(String widgetId, WidgetIdRegEntry re) {
		return map.put(widgetId, re);
	}

	private HashMap<String, WidgetIdRegEntry> map = new HashMap<String, WidgetIdRegEntry>();

	static class WidgetIdRegEntry {
		
		public boolean addWidgetSubscription(String subId) {
			return widgetSubscriptions.add(subId);
		}

		public boolean removeWidgetSubscription(String subId) {
			return widgetSubscriptions.remove(subId);
		}

		public List<String> getWidgetSubscriptions() {
			return Collections.unmodifiableList(widgetSubscriptions); 
		}

		private ArrayList<String> widgetSubscriptions = new ArrayList<String>();
		
	}
	
}
