/*
 * Created on May 1, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package context.arch.logging;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;

import org.hibernate.Session;
import org.hibernate.type.Type;

/**
 * @author Marti Motoyama
 *
 */
public class HibernateListModel extends ListModelSupport implements ListModel {
	
	public List<String> list;
	public String defaultQuery;
	public Type[] defaultTypes;
	public Object[] defaultParams;
	
	public HibernateListModel(){
		list = new ArrayList<String>();
		list.add("Pending");
	}
	
	public HibernateListModel(String query, Object[] params, Type[] types){
		list = new ArrayList<String>();
		list.add("Pending");
		
		this.defaultQuery = query;
		this.defaultParams = params;
		this.defaultTypes = types;
		setListToDefaultQuery();
	}
	
	public HibernateListModel(String query){
		list = new ArrayList<String>();
		list.add("Pending");

		this.defaultQuery = query;
		setListToDefaultQuery();
	}
	
	//Returns the value at the specified index. 
	public Object getElementAt(int index) {
		return list.get(index);
	}
	
	//Returns the length of the list. 
	public int getSize() {
		return list.size();
	}
	
	//Set the list
	public void setList(List<String> l) {
		list  = l;
	}
	
	//Get the List
	public List<String> getList(){
		return list;
	}
	
	//Populate list with all the entries possible
	public void setListToDefaultQuery(){
		setListToQueryResults(defaultQuery, defaultParams, defaultTypes);
	}	
	
	
	//Set the session, this is necessary to avoid problems that arise when sessions remain unclosed
	//in hibernate. Isn't there a finite number of sessions in the pool? Shouldn't we avoid
	//keeping potentially an infinite set of sessions open? However, closing sessions could cause problems.
	//For example, if the session is closed in middle of updating a Text Area
	//then we lose access to the service execution object and all the fields
	//associated with that object.
	public void setListToQueryResults(String query, Object[] parameters, Type[] types){
		//Issue a runnable to repopulate the services executed list model
		WorkQueue.getWorkQueue().execute( new QueryRunnable(query, parameters, types){
			@SuppressWarnings("unchecked")
			public void run(){						
				HibernateListModel lm = HibernateListModel.this;
				try{						
					Session session = HibernateUtils.getNewSession();
					List<String> updates = session.createQuery(getQuery()).setParameters(getParameters(), getTypes()).list();//.find(getQuery(), getParameters(), getTypes());			
					session.close();
						
					//no entries matched the query
					if (updates == null || updates.isEmpty()){
						updates = new ArrayList<String>();
						updates.add("No matching entries");
					}
					
					//We need to ensure that no other runnable is modifying or accessing the list
					synchronized(lm){
						lm.setList(updates);
						lm.fireContentsChanged(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, updates.size())); 
					}
					
				}
				catch (Exception e){
					e.printStackTrace();
				}	
			}});
	}	
}
