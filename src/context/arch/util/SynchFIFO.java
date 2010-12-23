/*
 * FIFOObject.java
 *
 * Created on June 15, 2001, 2:25 PM
 */

package context.arch.util;

import java.util.Vector;

/**
 * This method provides a synchronized FIFO pipe.
 *
 * @author  Agathe
 */
public class SynchFIFO extends Vector<Object> {

	private static final long serialVersionUID = 1339709490236135379L;

	/**
	 * Returns the next object or waits for the next if the FIFO does'nt 
	 * contain any, and removes it from the pipe.
	 * 
	 * @return Object The next object
	 */
	public synchronized Object getNext() { 
		while(0 == size()) {
			try {
				wait(); 
			}
			catch (InterruptedException e){
				System.out.println("SynchFIFO <getNext> " + e);
			}
		}
		Object o = elementAt(0); 
		removeElementAt(0); 
		return o; 
	} 

	/**
	 * Put a new Object in the pipe. If the getNext() method had been previously
	 * called, the notifyAll allows to send a message to the callers to end the wait.
	 * 
	 * @param O The object to add
	 */
	public synchronized void put(Object o) { 
		//System.out.println("SynchFIFO <put> " + o);
		addElement(o); 
		notifyAll(); 
	} 

	/**
	 * Removes all elements from this object and returns an array of the
	 * removed objects.
	 *
	 * @return Object[] The removed objects
	 */
	public synchronized Object[] removeAll(){
		int length = this.size ();
		Object [] obj = new Object [length];
		this.copyInto (obj);
		this.removeAllElements ();
		return obj;
	}

}
