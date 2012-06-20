package context.apps.demos.roomlight;

/**
 * This is an example of a component, which serves as a DragSource as 
 * well as Drop Target.
 * To illustrate the concept, JList has been used as a droppable target
 * and a draggable source.
 * Any component can be used instead of a JList.
 * The code also contains debugging messages which can be used for 
 * diagnostics and understanding the flow of events.
 * 
 * Ref: http://www.iut-info.univ-lille1.fr/docs/tutorial/dnd/sheetal.html
 * Modified by Brian Y. Lim
 * 
 * @version 1.0
 */

import java.awt.dnd.*;
import java.awt.datatransfer.*;

import java.io.IOException;

import javax.swing.JList;
import javax.swing.DefaultListModel;

public class DNDList extends JList implements DNDComponentInterface,
		DropTargetListener, DragSourceListener, DragGestureListener {

	private static final long serialVersionUID = 722947780163432545L;

	/**
	 * enables this component to be a dropTarget
	 */

	DropTarget dropTarget = null;

	/**
	 * enables this component to be a Drag Source
	 */
	DragSource dragSource = null;

	/**
	 * constructor - initializes the DropTarget and DragSource.
	 * @param listener notified when the list contents changes
	 */
	public DNDList() {
		dropTarget = new DropTarget(this, this);
		dragSource = new DragSource();
		dragSource.createDefaultDragGestureRecognizer(this,
				DnDConstants.ACTION_MOVE, this);
	}

	/**
	 * is invoked when you are dragging over the DropSite
	 * 
	 */
	public void dragEnter(DropTargetDragEvent event) {
		// debug messages for diagnostics
//		System.out.println("dragEnter");
		event.acceptDrag(DnDConstants.ACTION_MOVE);
	}

	/**
	 * is invoked when you are exit the DropSite without dropping
	 * 
	 */
	public void dragExit(DropTargetEvent event) {
//		System.out.println("dragExit");

	}

	/**
	 * is invoked when a drag operation is going on
	 * 
	 */
	public void dragOver(DropTargetDragEvent event) {
//		System.out.println("dragOver");
	}

	/**
	 * a drop has occurred
	 * 
	 */
	public void drop(DropTargetDropEvent event) {
		try {
			Transferable transferable = event.getTransferable();

			// TODO we accept only Strings
			if (transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
				event.acceptDrop(DnDConstants.ACTION_MOVE);
				String s = (String) transferable
						.getTransferData(DataFlavor.stringFlavor);
				addElement(s);
				event.getDropTargetContext().dropComplete(true);
			} else {
				event.rejectDrop();
			}
		} catch (IOException exception) {
			exception.printStackTrace();
			System.err.println("Exception" + exception.getMessage());
			event.rejectDrop();
		} catch (UnsupportedFlavorException ufException) {
			ufException.printStackTrace();
			System.err.println("Exception" + ufException.getMessage());
			event.rejectDrop();
		}
	}

	/**
	 * is invoked if the use modifies the current drop gesture
	 * 
	 */
	public void dropActionChanged(DropTargetDragEvent event) {
	}

	/**
	 * a drag gesture has been initiated
	 * 
	 */
	public void dragGestureRecognized(DragGestureEvent event) {

		Object selected = getSelectedValue();
		if (selected != null) {
			StringSelection text = new StringSelection(selected.toString());

			// as the name suggests, starts the dragging
			dragSource.startDrag(event, DragSource.DefaultMoveDrop, text, this);
		} else {
			System.out.println("nothing was selected");
		}
	}

	/**
	 * this message goes to DragSourceListener, informing it that the dragging
	 * has ended
	 * 
	 */
	public void dragDropEnd(DragSourceDropEvent event) {
		if (event.getDropSuccess()) {
			removeElement();
		}
	}

	/**
	 * this message goes to DragSourceListener, informing it that the dragging
	 * has entered the DropSite
	 * 
	 */
	public void dragEnter(DragSourceDragEvent event) {
//		System.out.println(" dragEnter");
	}

	/**
	 * this message goes to DragSourceListener, informing it that the dragging
	 * has exited the DropSite
	 * 
	 */
	public void dragExit(DragSourceEvent event) {
//		System.out.println("dragExit");
	}

	/**
	 * this message goes to DragSourceListener, informing it that the dragging
	 * is currently occurring over the DropSite
	 * 
	 */
	public void dragOver(DragSourceDragEvent event) {
//		System.out.println("dragExit");

	}

	/**
	 * is invoked when the user changes the dropAction
	 * 
	 */
	public void dropActionChanged(DragSourceDragEvent event) {
//		System.out.println("dropActionChanged");
	}

	/**
	 * adds elements to itself
	 * 
	 */
	public void addElement(Object s) {
		((DefaultListModel) getModel()).addElement(s.toString());
	}

	/**
	 * removes an element from itself
	 */
	public void removeElement() {
		((DefaultListModel) getModel()).removeElement(getSelectedValue());
	}

}
