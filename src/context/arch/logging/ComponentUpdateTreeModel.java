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

import context.arch.logging.hibernate.CUAttribute;
import context.arch.logging.hibernate.CUDestination;
import context.arch.logging.hibernate.ComponentUpdate;


/**
 * @author Marti Motoyama
 *
 * TreeModel for the ComponentUpdate-related tables
 */

public class ComponentUpdateTreeModel extends AbstractTreeModel implements Serializable {

	private static final long serialVersionUID = -2001836010686773661L;

	public ComponentUpdateTreeModel() {
		rootObject = "Pending";
		
		//Issue a runnable to populate this particular tree model
		WorkQueue.getWorkQueue().execute( new Runnable(){
			public void run(){
				AbstractTreeModel tm = ComponentUpdateTreeModel.this;
				Object oldRoot = tm.getRoot();
				try{
					Session session = HibernateUtils.getNewSession();
					List<?> updates = session.createQuery("from ComponentUpdate").list();
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
		
		if (parent instanceof ComponentUpdate){
			ComponentUpdate parentCU = (ComponentUpdate) parent;
			Set<?> cuDestinations = parentCU.getCUDestinations();
			Object[] cuDestinationsArray = cuDestinations.toArray();
			if (index < cuDestinationsArray.length && index >= 0)
				return cuDestinationsArray[index];
			
		}
		if (parent instanceof CUDestination){
			CUDestination parentCUDestination = (CUDestination) parent;
			Set<?> cuAttributes = parentCUDestination.getCUAttributes();
			Object[] cuAttributesArray = cuAttributes.toArray();
			if (index < cuAttributesArray.length && index >= 0)
				return cuAttributesArray[index];
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
		if (parent instanceof ComponentUpdate){
			ComponentUpdate parentCU = (ComponentUpdate) parent;
			Set<?> cuDestinations = parentCU.getCUDestinations();
			return cuDestinations.size();
		}
		if (parent instanceof CUDestination){
			CUDestination parentCUDestination = (CUDestination) parent;
			Set<?> cuAttributes = parentCUDestination.getCUAttributes();
			return cuAttributes.size();
		}
		
		return count;
	}

	/**
	 * Returns the index of child in parent.
	 */
	public int getIndexOfChild(Object parent, Object child) {
		int index = 0;
		
		if (parent instanceof List<?>){
			Iterator<?> parentIterator = ((List<?>) parent).iterator();
			while (parentIterator.hasNext()){
				Object childObject = parentIterator.next();
				if (childObject == child){
					return index;
				}
				index++;
			}
			
		}
		if (parent instanceof ComponentUpdate){
			ComponentUpdate parentCU = (ComponentUpdate) parent;
			Set<?> cuDestinations = parentCU.getCUDestinations();
			Iterator<?> cuDestinationsIterator = cuDestinations.iterator();
			while (cuDestinationsIterator.hasNext()){
				Object childObject = cuDestinationsIterator.next();
				if (childObject == child){
					return index;
				}
				index++;
			}	
		}
		if (parent instanceof CUDestination){
			CUDestination parentCUDestination = (CUDestination) parent;
			Set<?> cuAttributes = parentCUDestination.getCUAttributes();
			Iterator<?> cuAttributesIterator = cuAttributes.iterator();
			while (cuAttributesIterator.hasNext()){
				Object childObject = cuAttributesIterator.next();
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
		if (node instanceof CUAttribute){
			return true;
		}
		
		if (node instanceof String){
			return true;
		}
		return false;
		
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
