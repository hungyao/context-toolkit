/*
 * Created on Feb 29, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package context.arch.logging;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import org.hibernate.Session;

/**
 * @author Marti Motoyama
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class LoggingGUI extends JPanel implements TreeSelectionListener {

	private static final long serialVersionUID = -8130243119312055213L;

	private JTree cuJTree, wrJTree;
	protected JButton refreshButton;
	@SuppressWarnings("unused")
	private static boolean DEBUG = false;

	//Optionally play with line styles.  Possible values are
	//"Angled" (the default), "Horizontal", and "None".
	@SuppressWarnings("unused")
	private static boolean playWithLineStyle = false;
	@SuppressWarnings("unused")
	private static String lineStyle = "Horizontal";

	//Optionally set the look and feel.
	private static boolean useSystemLookAndFeel = true;

	public LoggingGUI() {
		super(new GridLayout(1,0));

		//Create the TreeModels and their corresponding JTrees
		ComponentUpdateTreeModel cuTreeModel = new ComponentUpdateTreeModel();
		WidgetRegistrationTreeModel wrTreeModel = new WidgetRegistrationTreeModel();

		cuJTree = new JTree(cuTreeModel);
		cuJTree.setRootVisible(true);
		cuJTree.setCellRenderer(new ComponentUpdateTreeCellRenderer());

		wrJTree = new JTree(wrTreeModel);
		wrJTree.setRootVisible(true);
		wrJTree.setCellRenderer(new WidgetRegistrationTreeCellRenderer());

		Dimension minimumSize = new Dimension(500, 600);

		//Create the scroll pane and add the tree to it.
		JScrollPane CULtreeView = new JScrollPane(cuJTree);
		JScrollPane WRLtreeView = new JScrollPane(wrJTree);

		CULtreeView.setMinimumSize(minimumSize);
		WRLtreeView.setMinimumSize(minimumSize);

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);

		tabbedPane.addTab("Component Update", CULtreeView);
		tabbedPane.addTab("Widget Registration", WRLtreeView);
		tabbedPane.setMnemonicAt(0, KeyEvent.VK_2);


		tabbedPane.setMinimumSize(minimumSize);

		//Add the pane to this panel.
		add(tabbedPane);

	}


	public void valueChanged(TreeSelectionEvent e) {
		return;
	}

	private static void createAndShowGUI() {
		if (useSystemLookAndFeel) {
			try {
				UIManager.setLookAndFeel(
						UIManager.getSystemLookAndFeelClassName());
			} catch (Exception e) {
				System.err.println("Couldn't use system look and feel.");
			}
		}

		//Make sure we have nice window decorations.
		JFrame.setDefaultLookAndFeelDecorated(true);

		//Create and set up the window.
		JFrame frame = new JFrame("Logging");

		//The sessions must be closed prior to the exiting of the JFrame
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				try{
					HibernateUtils.closeSessions();
				}catch (Exception e){
					e.printStackTrace();
				}
				System.exit(0);
			}
		});

		LoggingGUI newContentPane = new LoggingGUI();		
		newContentPane.setOpaque(true); //content panes must be opaque
		frame.setContentPane(newContentPane);

		//Display the window.
		frame.setSize(500,600);
		frame.setVisible(true);
	}


	public static void main(String[] args) throws Exception {
		//Schedule a job for the event-dispatching thread:
		//creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				System.out.println("creating GUI...");
				createAndShowGUI();
			}
		});

		Thread.sleep(1000);
		//initialize SessionFactory by getting it
		System.out.println("initializing factory...");
		@SuppressWarnings("unused")
		Session session = HibernateUtils.getNewSession();	
	}

}


