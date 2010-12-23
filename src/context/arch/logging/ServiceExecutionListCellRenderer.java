/*
 * Created on May 2, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package context.arch.logging;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import context.arch.logging.hibernate.ServiceExecution;

/**
 * @author Marti Motoyama
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ServiceExecutionListCellRenderer extends DefaultListCellRenderer {
	
	private static final long serialVersionUID = -7500607100445374116L;

	public ServiceExecutionListCellRenderer(){
	}
	
	public Component getListCellRendererComponent(
			JList list,
			Object value,
			int index,
			boolean isSelected,
			boolean cellHasFocus){
		
		
		super.getListCellRendererComponent(
				list, value, index,
				isSelected, cellHasFocus);
		
		if (value instanceof ServiceExecution){
			ServiceExecution se = (ServiceExecution) value;
			setText(index + ". [Time: " + se.getExecutiontime() + "]"
					+ "[Component ID: " + se.getComponentAdded().getComponentdescriptionid()  
					+ "] [Function Name: " + se.getFunctionname() 
					+ "]");
		}
		
		return this;
	}
	

}
