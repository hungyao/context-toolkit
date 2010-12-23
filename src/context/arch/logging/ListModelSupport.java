/*
 * Created on May 1, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package context.arch.logging;

import java.util.Enumeration;
import java.util.Vector;

import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/**
 * @author Marti Motoyama
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ListModelSupport {
	private Vector<ListDataListener> vector = new Vector<ListDataListener>();
	
	//Adds a listener to the list that's notified each time a change to the data model occurs. 
	public void addListDataListener(ListDataListener listener){
		if ( listener != null && !vector.contains( listener ) ) {
			vector.addElement( listener );
		}
		
	}
	//Removes a listener from the list that's notified each time a change to the data model occurs. 
	public void removeListDataListener(ListDataListener listener){
		if ( listener != null ) {
			vector.removeElement( listener );
		}
	}
	
	public void fireContentsChanged( ListDataEvent e ) {
		Enumeration<ListDataListener> listeners = vector.elements();
		while ( listeners.hasMoreElements() ) {
			ListDataListener listener = listeners.nextElement();
			listener.contentsChanged( e );
		}
	}
}
