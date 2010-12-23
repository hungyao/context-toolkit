/*
 * Created on Mar 4, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package context.arch.logging;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.event.TreeModelEvent;
import javax.swing.tree.TreePath;

import org.hibernate.Session;

import context.arch.logging.hibernate.WRService;
import context.arch.logging.hibernate.WRServiceFunction;
import context.arch.logging.hibernate.WidgetRegistration;


/**
 * @author Marti Motoyama
 *
 * TreeModel for the WidgetRegistration-related tables
 */

public class WidgetRegistrationTreeModel extends AbstractTreeModel implements Serializable {

	private static final long serialVersionUID = 4110792759208959622L;

	public WidgetRegistrationTreeModel() {
		rootObject = "Pending";

		//Issue a runnable to populate this particular tree model
		WorkQueue.getWorkQueue().execute( new Runnable(){
			public void run(){
				AbstractTreeModel tm = WidgetRegistrationTreeModel.this;
				Object oldRoot = tm.getRoot();
				try{
					Session session = HibernateUtils.getNewSessionToClose();
					List<?> updates = session.createQuery("from WidgetRegistration").list();
					tm.setRoot(updates);
					tm.fireTreeStructureChanged(new TreeModelEvent (this, new Object[] {oldRoot}));
				}
				catch (Exception e){
					e.printStackTrace();
				}
			}});
	}


	//////////////// TreeModel interface implementation ///////////////////////
	/**
	 * Returns the child of parent at a given index.
	 */
	public Object getChild(Object parent, int index) {
		if (parent instanceof List<?>){	
			List<?> parentList = (List<?>) parent;
			if (index < parentList.size() && index >= 0)
				return parentList.get(index);
		}

		if (parent instanceof Set<?>){
			Set<?> parentSet = (Set<?>) parent;
			Object[] setArray = parentSet.toArray();
			if (index < setArray.length && index >= 0)
				return setArray[index];
		}

		if (parent instanceof WidgetRegistration){
			WidgetRegistration parentWR = (WidgetRegistration) parent;
			switch(index){
			case 0:
				return parentWR.getWRAttributes();
			case 1:
				return parentWR.getWRCallbacks();
			case 2:
				return parentWR.getWRServices();
			default:
				return null;
			}
		}

		if (parent instanceof WRService){
			WRService parentWRService = (WRService) parent;
			Set<WRServiceFunction> parentSet = parentWRService.getWRServiceFunctions();
			Object[] setArray = parentSet.toArray();
			if (index < setArray.length && index >= 0)
				return setArray[index];
		}

		return null;

	}

	/**
	 * Returns the number of children of parent.
	 */
	public int getChildCount(Object parent) {
		int count = 0;
		if (parent instanceof List<?>){
			List<?> parentList = (List<?>) parent;
			return parentList.size();
		}

		if (parent instanceof Set<?>){
			Set<?> parentSet = (Set<?>) parent;
			return parentSet.size();
		}

		if (parent instanceof WidgetRegistration){
			return WidgetRegistration.CHILDREN_COUNT;
		}

		if (parent instanceof WRService){
			WRService parentWRService = (WRService) parent;
			Set<WRServiceFunction> parentSet = parentWRService.getWRServiceFunctions();
			return parentSet.size();
		}

		return count;
	}

	/**
	 * Returns the index of child in parent.
	 */
	public int getIndexOfChild(Object parent, Object child) {
		int index = 0;

		if (parent instanceof List<?>){
			for (Object childObject : (List<?>)parent) {
				if (childObject == child){
					return index;
				}
				index++;
			}
		}

		if (parent instanceof Set<?>){
			for (Object childObject : (Set<?>)parent) {
				if (childObject == child){
					return index;
				}
				index++;
			}	
		}

		if (parent instanceof WidgetRegistration){
			WidgetRegistration parentWR = (WidgetRegistration) parent;
			if (child == parentWR.getWRAttributes()) index = 0;
			else if(child == parentWR.getWRCallbacks()) index = 1;
			else if (child == parentWR.getWRServices()) index = 2;
		}

		if (parent instanceof WRService){
			WRService parentWRService = (WRService) parent;
			Set<WRServiceFunction> parentSet = parentWRService.getWRServiceFunctions();
			Iterator<WRServiceFunction> parentSetIterator = parentSet.iterator();
			while (parentSetIterator.hasNext()) {
				Object childObject = parentSetIterator.next();
				if (childObject == child){
					return index;
				}
				index++;
			}	
		}

		return index;
	}

	/**
	 * Returns the root of the tree.
	 */
	public Object getRoot() {
		return rootObject;
	}

	/**
	 * Returns true if node is a leaf.
	 */
	public boolean isLeaf(Object node) {
		if (node instanceof List<?>){
			if (((List<?>) node).size() == 0) return true;
			return false;
		}
		if (node instanceof Set<?>){
			if (((Set<?>) node).size() == 0) return true;
			return false;
		}
		if (node instanceof WidgetRegistration){
			return false;
		}
		if (node instanceof WRService){
			return false;
		}

		return true;
	}

	/**
	 * Messaged when the user has altered the value for the item
	 * identified by path to newValue.  Not used by this model.
	 */
	public void valueForPathChanged(TreePath path, Object newValue) {
		System.out.println("*** valueForPathChanged : "
				+ path + " --> " + newValue);
	}
}
