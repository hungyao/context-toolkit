package context.arch.enactor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * A registry used by the EnactorSubscriptionManager to track references to widgets.
 * 
 * @author newbergr
 */
public class WidgetReferenceRegistry extends HashMap<EnactorReference, WidgetReferenceRegistry.WidgetReferenceRegEntry> {

	private static final long serialVersionUID = 154392374190086012L;

//	private HashMap map = new HashMap(); // use subclass instead of delegate design pattern
	
//	public WidgetReferenceRegEntry get(EnactorReference rwr) {
//		return (WidgetReferenceRegEntry) map.get(rwr);
//	}
//
//	public WidgetReferenceRegEntry remove(EnactorReference rwr) {
//		return (WidgetReferenceRegEntry) map.remove(rwr);
//	}
//
//	public WidgetReferenceRegEntry put(EnactorReference rwr, WidgetReferenceRegEntry re) {
//		return (WidgetReferenceRegEntry) map.put(rwr, re);
//	}

	public Set<EnactorReference> getWidgetReferences() {
//		return map.keySet();
		return keySet();
	}

	static class WidgetReferenceRegEntry {
		
		public boolean addWidgetSubscription(String subId) {
			return widgetSubscriptions.add(subId);
		}

		public boolean removeWidgetSubscription(String subId) {
			return widgetSubscriptions.remove(subId);
		}

		public List<String> getWidgetSubscriptions() {
//			return retWR; 
			return Collections.unmodifiableList(widgetSubscriptions); 
		}

		private ArrayList<String> widgetSubscriptions = new ArrayList<String>();
//		private List retWR = Collections.unmodifiableList(widgetSubscriptions);    
		
	}
}
