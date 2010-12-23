package context.arch.widget;

import java.util.Vector;

/**
 * This class maintains a list of widget handles, allows additions, removals and
 * updates to individual handles.
 *
 * @see context.arch.widget.WidgetHandle
 */
public class WidgetHandles extends Vector<WidgetHandle> {

	private static final long serialVersionUID = 1773558607106338778L;

//	protected Hashtable hash; // not being used

	/**
	 * Basic empty constructor
	 */
	public WidgetHandles() {
		super();
	}

	/**
	 * Adds a widgethandle to the widgethandle list
	 *
	 * @param id ID of the widget being subscribed to 
	 * @param hostname Name of the widget's host computer
	 * @param port Port number of the widget
	 */
	public synchronized void addWidgetHandle(String id, String hostname, int port) {
		addElement(new WidgetHandle(id, hostname, port));
	}

	/**
	 * Adds a widgethandle to the widgethandle list
	 *
	 * @param handle WidgetHandle to add
	 */
	public synchronized void addWidgetHandle(WidgetHandle handle) {
		addElement(handle);
	}

	/**
	 * Adds a set of widgethandles to the widgethandle list
	 *
	 * @param handles WidgetHandles to add to the list
	 */
	public synchronized void addWidgetHandles(WidgetHandles handles) {
		if (handles != null) {
			for (int i=0; i<handles.numWidgetHandles(); i++) {
				addElement(handles.getWidgetHandleAt(i));
			}
		}
	}

	/**
	 * Returns the WidgetHandle at the given index.  Do not assume that a given
	 * WidgetHandle's index will stay constant throughout its lifetime.  When
	 * other WidgetHandles are added and removed, a given widgetHandle's index
	 * may change.
	 *
	 * @param index index value of the WidgetHandle object to retrieve
	 */
	public synchronized WidgetHandle getWidgetHandleAt(int index) {
		return (WidgetHandle)(elementAt(index));
	}

	/**
	 * Returns the number of widgetHandles in the list
	 */
	public synchronized int numWidgetHandles() {
		return size();
	}
}
