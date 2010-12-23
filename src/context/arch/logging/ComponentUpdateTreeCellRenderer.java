/*
 * Created on Mar 15, 2004
 *
 */
package context.arch.logging;

import java.awt.Component;
import java.util.List;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import context.arch.logging.hibernate.CUAttribute;
import context.arch.logging.hibernate.CUDestination;
import context.arch.logging.hibernate.ComponentUpdate;

/**
 * @author Marti Motoyama
 *
 * Class to "pretty print" the ComponentUpdateTreeModel
 */
public class ComponentUpdateTreeCellRenderer extends DefaultTreeCellRenderer{
	
	private static final long serialVersionUID = -1279485485374696785L;

		public ComponentUpdateTreeCellRenderer() {
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
			
			if (value instanceof List<?>){	
				setText("ComponentUpdate List");
			}
			
			if (value instanceof ComponentUpdate){
				ComponentUpdate cu = (ComponentUpdate) value;
				setText("Component Update: [ComponentID = " + cu.getComponentid() 
						+ "] [UpdateName = " + cu.getUpdatename()
						+ "] [UpdateTime = " + cu.getUpdatetime() + "]");
			}
			
			if (value instanceof CUDestination){
				CUDestination cud = (CUDestination) value;
				setText("CUDestination: [Destination ComponentID = " + cud.getDestinationcomponentid() + "]");
			}

			if (value instanceof CUAttribute){
				CUAttribute cua = (CUAttribute) value;
				setText("CUAttribute: [attribute name = " + cua.getAttributename() 
						+ "] [attribute type = " + cua.getAttributetype()
						+ "] [attribute value string = " + cua.getAttributevaluestring()
						+ "] [attribute value numeric = " + cua.getAttributevaluenumeric() 
						+ "] [constant = " + cua.isConstant() + "]");
			}
			
			return this;
		}
		
}
