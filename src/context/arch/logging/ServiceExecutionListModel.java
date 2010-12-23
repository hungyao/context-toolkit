/*
 * Created on May 1, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package context.arch.logging;

import java.io.Serializable;
import java.util.ArrayList;

import org.hibernate.type.Type;

/**
 * @author Marti Motoyama
 *
 */
public class ServiceExecutionListModel extends HibernateListModel implements Serializable {

	private static final long serialVersionUID = -1231421005485104681L;

	public ServiceExecutionListModel(){
		list = new ArrayList<String>();
		list.add("Pending");
		this.defaultQuery = "from ServiceExecution se order by se.executiontime desc";
		this.setListToDefaultQuery();
	}	
	
	public void filterListByLocation(String location){
		String query = 	"FROM ServiceExecution se1 " +
						"WHERE se1.serviceexecutionid in ( " +
							"SELECT se2.serviceexecutionid " +
							"FROM ServiceExecution se2, WRAttribute wa " +
							"WHERE se2.ComponentAdded.componentdescriptionid = wa.WidgetRegistration.widgetid " +
							"AND wa.attributename = ? " +
							"AND wa.attributevaluestring = ? " +
						"GROUP BY se2.serviceexecutionid ) " +
						"ORDER by se1.executiontime desc";
		Object[] parameters = {"location", location};
		Type[] types = {HibernateUtils.STRING, HibernateUtils.STRING};
		setListToQueryResults(query, parameters, types);
		
	}
}
