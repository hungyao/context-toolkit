package context.arch.handler;

import java.util.Collection;
import java.util.Hashtable;

/**
 * This class maintains a list of context widget handlers, allows additions and 
 * removals of individual handlers.
 */
public class Handlers extends Hashtable<String, HandlerInfo> {

	private static final long serialVersionUID = -4065761541076150381L;

	/**
	 * Basic empty constructor
	 */
	public Handlers() {
		super(5); // why 5?
	}

	/**
	 * Adds a handler to the handler list
	 *
	 * @param handlerInfo container for handler info
	 * @see context.arch.handler.HandlerInfo
	 */
	public void addHandler(HandlerInfo info) {
		put(info.getSubId(), info);
	}

	/**
	 * Removes a handler from the handler list
	 *
	 * @param handlerInfo HandlerInfo object to remove
	 * @see context.arch.handler.HandlerInfo
	 */
	public void removeHandler(HandlerInfo info) {
		remove(info.getSubId());
	}

	/**
	 * Removes a handler from the handler list
	 *
	 * @param handlerInfo HandlerInfo object to remove
	 * @see context.arch.handler.HandlerInfo
	 */
	public void removeHandler(String subId) {
		remove(subId);
	}


	/**
	 * Returns a handler that matches the given key
	 *
	 * @param key String that matches handler info
	 * @return context widget handler that matches the given key
	 */
	public synchronized Handler getHandler(String key) {
		HandlerInfo info = (HandlerInfo)get(key);
		if (info != null) {
			return info.getHandler();
		}
		return null;
	}

	/**
	 * Return the HandlerInfo corresponding to a subscription
	 */
	public HandlerInfo getHandlerInfo(String key){
		return (HandlerInfo) get(key);
	}

	/**
	 * Returns an enumeration containing all the handlers in the list
	 */
	public Collection<HandlerInfo> getHandlers() {	  
		return super.values();
	}

	/**
	 * Returns the number of handlers in the list
	 */
	public int numHandlers() {
		return size();
	}

}
