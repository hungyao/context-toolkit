package context.arch.handler;

import java.util.Collection;
import java.util.Hashtable;

/**
 * This class maintains a list of asynchronous service handlers, allows 
 * additions and removals of individual handlers.
 */
public class AsyncServiceHandlers extends Hashtable<String, AsyncServiceHandlerInfo> {

	private static final long serialVersionUID = 8332352593498292769L;

	/**
	 * Basic empty constructor
	 */
	public AsyncServiceHandlers() {
		super(5);
	}

	/**
	 * Adds an asynchronous service handler to the handler list
	 *
	 * @param info Container for handler info
	 * @see context.arch.handler.AsyncServiceHandlerInfo
	 */
	public synchronized void addHandler(AsyncServiceHandlerInfo info) {
		put(info.getUniqueId(), info);
	}

	/**
	 * Removes a handler from the handler list
	 *
	 * @param info AsyncServiceHandlerInfo object to remove
	 * @see context.arch.handler.AsyncServiceHandlerInfo
	 */
	public synchronized void removeHandler(AsyncServiceHandlerInfo info) {
		remove(info.getUniqueId());
	}

	/**
	 * Returns a handler that matches the given key
	 *
	 * @param key String that matches handler info
	 * @return context widget handler that matches the given key
	 */
	public synchronized AsyncServiceHandler getHandler(String key) {
		AsyncServiceHandlerInfo info = (AsyncServiceHandlerInfo)get(key);
		if (info != null) {
			return info.getHandler();
		}
		return null;
	}

	/**
	 * Returns a collection containing all the handlers in the list
	 */
	public synchronized Collection<AsyncServiceHandlerInfo> getHandlers() {
		return values();
	}

	/**
	 * Returns the number of handlers in the list
	 */
	public synchronized int numHandlers() {
		return size();
	}

}
