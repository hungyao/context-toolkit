package context.arch.logging;

import java.util.Enumeration;
import java.util.Vector;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;

public class TreeModelSupport {
   private Vector<TreeModelListener> vector = new Vector<TreeModelListener>();

   public void addTreeModelListener( TreeModelListener listener ) {
      if ( listener != null && !vector.contains( listener ) ) {
         vector.addElement( listener );
      }
   }

   public void removeTreeModelListener( TreeModelListener listener ) {
      if ( listener != null ) {
         vector.removeElement( listener );
      }
   }

   public void fireTreeNodesChanged( TreeModelEvent e ) {
      Enumeration<TreeModelListener> listeners = vector.elements();
      while ( listeners.hasMoreElements() ) {
         TreeModelListener listener = listeners.nextElement();
         listener.treeNodesChanged( e );
      }
   }

   public void fireTreeNodesInserted( TreeModelEvent e ) {
      Enumeration<TreeModelListener> listeners = vector.elements();
      while ( listeners.hasMoreElements() ) {
         TreeModelListener listener = listeners.nextElement();
         listener.treeNodesInserted( e );
      }
   }

   public void fireTreeNodesRemoved( TreeModelEvent e ) {
      Enumeration<TreeModelListener> listeners = vector.elements();
      while ( listeners.hasMoreElements() ) {
         TreeModelListener listener = listeners.nextElement();
         listener.treeNodesRemoved( e );
      }
   }

   public void fireTreeStructureChanged( TreeModelEvent e ) {
      Enumeration<TreeModelListener> listeners = vector.elements();
      while ( listeners.hasMoreElements() ) {
         TreeModelListener listener = listeners.nextElement();
         listener.treeStructureChanged( e );
      }
   }
}
