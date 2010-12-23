/*
 * Created on Mar 15, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package context.arch.logging;

import java.awt.Component;
import java.util.List;
import java.util.Set;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import context.arch.logging.hibernate.WRAttribute;
import context.arch.logging.hibernate.WRCallback;
import context.arch.logging.hibernate.WRService;
import context.arch.logging.hibernate.WRServiceFunction;
import context.arch.logging.hibernate.WidgetRegistration;

/**
 * @author Marti Motoyama
 *
 * Class to "pretty print" the WidgetRegistrationTreeModel
 */
public class WidgetRegistrationTreeCellRenderer extends DefaultTreeCellRenderer{

	private static final long serialVersionUID = 3741746881724386325L;

	public WidgetRegistrationTreeCellRenderer() {
	}

	public Component getTreeCellRendererComponent(
			JTree tree,
			Object value,
			boolean sel,
			boolean expanded,
			boolean leaf,
			int row,
			boolean hasFocus) {

		super.getTreeCellRendererComponent(
				tree, value, sel,
				expanded, leaf, row,
				hasFocus);

		if (value == null){
			setText("N/A");
		}

		else if (value instanceof List<?>){	
			setText("WidgetRegistration List");
		}
		else if (value instanceof Set<?>){	
			Set<?> vs = (Set<?>) value;
			if (vs.size() == 0){
				setText("N/A");
			}
			else{
				Object o = vs.toArray()[0];

				if (o instanceof WRService){
					setText("WRService List");
				}
				else if (o instanceof WRServiceFunction){
					setText("WRServiceFunction List");
				}
				else if (o instanceof WRCallback){
					setText("WRCallback List");
				}
				else if (o instanceof WRAttribute){
					setText("WRAttribute List");
				}

			}		
		}

		else if (value instanceof WidgetRegistration){
			WidgetRegistration wr = (WidgetRegistration) value;
			setText("Widget Registration: [WidgetID = " + wr.getWidgetid() 
					+ "] [UpdateTime = " + wr.getRegistrationtime()
					+ "]");
		}

		else if (value instanceof WRService){
			WRService wrs = (WRService) value;
			setText("WRService: [Service Name = " + wrs.getServicename() 
					+ "]");
		}

		else if (value instanceof WRServiceFunction){
			WRServiceFunction wrsf = (WRServiceFunction) value;
			setText("WRServiceFunction: [Function Name = " + wrsf.getFunctionname() 
					+ "] [Function Description = " + wrsf.getFunctiondescription() + "]");
		}			

		else if (value instanceof WRCallback){
			WRCallback wrcb = (WRCallback) value;
			setText("WRCallback: [Callback Name = " + wrcb.getCallbackname() + "]");
		}

		else if (value instanceof WRAttribute){
			WRAttribute wr = (WRAttribute) value;
			setText("WRAttribute: [attribute name = " + wr.getAttributename() 
					+ "] [attribute type = " + wr.getAttributetype()
					+ "] [attribute value string = " + wr.getAttributevaluestring()
					+ "] [attribute value numeric = " + wr.getAttributevaluenumeric() 
					+ "] [constant = " + wr.isConstant() + "]");
		}

		return this;
	}

}
