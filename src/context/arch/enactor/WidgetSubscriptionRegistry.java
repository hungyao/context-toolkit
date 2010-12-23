package context.arch.enactor;

import java.util.HashMap;
import java.util.Set;

/**
 * A registry used by the EnactorSubscriptionManager to track subscriptions to widgets.
 * 
 * Made type safe with generics.
 * Also made it into a subclass of HashMap instead of containing a map delegate, since it doesn't really do anything else.
 * 
 * Actually, this is really just a HashMap with no extra functionality, so just use the superclass Collection.
 * @deprecated
 * 
 * @author alann
 * @author Brian Y. Lim
 */
@Deprecated
class WidgetSubscriptionRegistry extends HashMap<String, EnactorComponentInfo> {

	private static final long serialVersionUID = 6923505813023476630L;

//	private Map map = new HashMap(); // don't use delegate anymore
	
//	public EnactorComponentInfo get(String subId) {
//		return (EnactorComponentInfo) map.get(subId);
//	}
//
//	public EnactorComponentInfo remove(String subId) {
//		return (EnactorComponentInfo) map.remove(subId);
//	}
//
//	public EnactorComponentInfo put(String subId, EnactorComponentInfo eci) {
//		return (EnactorComponentInfo) map.put(subId, eci);
//	}

	/**
	 * Really just a wrapper method for keySet()
	 */
	@Deprecated
	public Set<String> getSubscriptionIds() {
//		return map.keySet();
		return keySet();
	}

}
