/*
 * Created on Apr 23, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package context.arch.logging;

/**
 * @author Marti Motoyama
 *
 */
/*
 * BoxLayoutDemo.java is a 1.4 application that requires no other files.
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.hibernate.LockOptions;
import org.hibernate.Session;

import context.arch.logging.hibernate.ComponentAdded;
import context.arch.logging.hibernate.ERParameter;
import context.arch.logging.hibernate.ERReference;
import context.arch.logging.hibernate.EnactorRegistration;
import context.arch.logging.hibernate.SEInputAttribute;
import context.arch.logging.hibernate.ServiceExecution;

public class ExplanationGUI {
	private JTextArea enactorRegistrationTextArea;
	private JTextArea serviceExecutionTextArea;
	private ServiceExecutionListModel seListModel;
	private LocationFilterListModel lfListModel;
	private JCheckBox filterCheckBox;
	private JList seJList, lfJList;
	
	public ExplanationGUI(Container pane) {
		//Create Text Area, where Enactor Registration information will appear
		enactorRegistrationTextArea = new JTextArea();
		enactorRegistrationTextArea.setFont(new Font("Arial", Font.PLAIN, 12));
		enactorRegistrationTextArea.setLineWrap(true); // Lines wrapped if true. Default false.
		
		//Create Text Area, where Service Execution information will appear
		serviceExecutionTextArea = new JTextArea();
		serviceExecutionTextArea.setFont(new Font("Arial", Font.PLAIN, 12));
		serviceExecutionTextArea.setLineWrap(true); // Lines wrapped if true. Default false.
		
		//Create ServiceExecutionListModel
		seListModel = new ServiceExecutionListModel();
		seJList = new JList(seListModel);
		seJList.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
		seJList.getSelectionModel().addListSelectionListener(new ServiceExecutionListSelectionListener());
		seJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		seJList.setCellRenderer(new ServiceExecutionListCellRenderer());
		
		//Create LocationFilterListModel
		lfListModel = new LocationFilterListModel();
		lfJList = new JList(lfListModel);
		lfJList.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
		lfJList.getSelectionModel().addListSelectionListener(new LocationFilterListSelectionListener());
		lfJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		//Create Checkbox for Filter
		filterCheckBox = new JCheckBox("Filter");
		filterCheckBox.addItemListener(new LocationFilterCheckBoxListener());
		
		//Layout the above components into four distinct areas
		pane.setLayout(new BorderLayout());
		addComponentsToLeftPanel(pane);
		addComponentsToRightPanel(pane);
	}
	
	private void addComponentsToLeftPanel(Container pane){
		JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new BorderLayout());
		leftPanel.setBackground(Color.WHITE);
		leftPanel.setPreferredSize(new Dimension(350, 600));
		addComponentsToTopLeftPanel(leftPanel);
		addComponentsToBottomLeftPanel(leftPanel);
		pane.add(leftPanel, BorderLayout.WEST);
	}
	
	//Service executions list
	private void addComponentsToTopLeftPanel(Container pane){
		JScrollPane leftTopPanel = new JScrollPane(seJList);
		leftTopPanel.setBackground(Color.WHITE);
		leftTopPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Service Executions"), 
				BorderFactory.createEmptyBorder(5,5,5,5)));
				
		pane.add(leftTopPanel, BorderLayout.CENTER);
		
	}
	
	//Filters list
	private void addComponentsToBottomLeftPanel(Container pane){
		JPanel leftBottomPanel = new JPanel();
		leftBottomPanel.setLayout(new BorderLayout());
		leftBottomPanel.setBackground(Color.WHITE);
		leftBottomPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Filters"), 
				BorderFactory.createEmptyBorder(5,5,5,5)));
		
		JTabbedPane leftBottomPanelTabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);
		leftBottomPanelTabbedPane.setBackground(Color.WHITE);
		JScrollPane locFilterList = new JScrollPane(lfJList);
		leftBottomPanelTabbedPane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
		leftBottomPanelTabbedPane.addTab("Location Filter", locFilterList);
		
		filterCheckBox.setBackground(Color.WHITE);
		leftBottomPanel.add(filterCheckBox, BorderLayout.NORTH);
		leftBottomPanel.add(leftBottomPanelTabbedPane, BorderLayout.CENTER);
		pane.add(leftBottomPanel, BorderLayout.SOUTH);
		
	}
	
	private void addComponentsToRightPanel(Container pane){
		JPanel rightPanel = new JPanel();
		rightPanel.setBackground(Color.WHITE);
		rightPanel.setLayout(new BorderLayout());
		rightPanel.setPreferredSize(new Dimension(Short.MAX_VALUE,600));
		addComponentsToTopRightPanel(rightPanel);
		addComponentsToBottomRightPanel(rightPanel);
		pane.add(rightPanel, BorderLayout.CENTER);
	}

	private void addComponentsToTopRightPanel(Container pane){
		JTabbedPane rightTopPanel = new JTabbedPane(JTabbedPane.TOP);
		rightTopPanel.setBackground(Color.WHITE);
		rightTopPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Details"), 
				BorderFactory.createEmptyBorder(5,5,5,5)));
		
		JScrollPane erPanel = new JScrollPane(enactorRegistrationTextArea);
		erPanel.setBackground(Color.WHITE);

		JScrollPane sePanel = new JScrollPane(serviceExecutionTextArea);
		sePanel.setBackground(Color.WHITE);
		
		rightTopPanel.addTab("Service Execution", sePanel);
		rightTopPanel.addTab("Enactor Registration", erPanel);
		pane.add(rightTopPanel, BorderLayout.CENTER);
	}
	
	private void addComponentsToBottomRightPanel(Container pane){
		//TODO: Create Timeline
	}
	
	//This class needs reworking. According to the forums on Hibernate, in order to 
	//allow a lazily loaded, detached object to be reassociated with a session,
	//you need to use session.lock(object, LockMode.NONE). There was mention of
	//using ThreadLocal, but I wasn't able to discern in time how to use it.
	class ServiceExecutionListSelectionListener implements ListSelectionListener {
		public void valueChanged(ListSelectionEvent e){ 			
			WorkQueue.getWorkQueue().execute( new Runnable() {
				public void run(){	
					ServiceExecution se = null;
					EnactorRegistration er = null;
					Iterator<SEInputAttribute> seiaIterator = null;
					Iterator<ERReference> errIterator = null;
					Iterator<ERParameter> erpIterator= null;
					Iterator<ComponentAdded> caIterator = null;
					
					//We need to ensure that the list model does not change during a text area update.
					//Otherwise, we could retrieve an inconsistent object if a context switch occurs
					//between when we obtain the selected index and when we actually go to retrieve
					//the object. Furthermore, we don't want to associate a detached object
					//with more than 2 sessions. Make sure we atomically get all the necessary fields
					//when we reassociate an object. Probably needs to be revised to incorporate
					//session.close() in finally (but can we throw execptions from runnable?)
					synchronized(seListModel){
						ListSelectionModel lsm = (ListSelectionModel) seJList.getSelectionModel();
						int	selectedIndex = lsm.getMaxSelectionIndex();
						
						try{
							//Since we allow for strings in the list model, we must be wary not to process
							//a string object
							Object obj = seListModel.getElementAt(selectedIndex);
							if (obj instanceof String) return;
							
							Session session = HibernateUtils.getNewSession();						
							se = (ServiceExecution) obj;
							session.buildLockRequest(LockOptions.NONE).lock(se);
							seiaIterator = se.getSEInputAttributes().iterator();
							
							er = se.getEnactorRegistration();
							session.buildLockRequest(LockOptions.NONE).lock(er);
							errIterator = er.getERReferences().iterator();
							erpIterator = er.getERParameters().iterator();
							caIterator = er.getComponentsAdded().iterator();
							
							session.close();
						}catch(Exception e){
							e.printStackTrace();
						}
					}
					
					//Set up Service Execution Text Area
					serviceExecutionTextArea.selectAll();
					serviceExecutionTextArea.replaceSelection(null);
					serviceExecutionTextArea.append( "Component ID: \n\t" + se.getComponentAdded().getComponentdescriptionid() + " \n\n" 
							+ "Execution Time: \n\t" + se.getExecutiontime() + " \n\n");
						
					serviceExecutionTextArea.append("Service Execution Input Attributes: \n");		
					while(seiaIterator.hasNext()){
						SEInputAttribute seia = (SEInputAttribute) seiaIterator.next();
						serviceExecutionTextArea.append("\tName: " + seia.getAttributename() + "\n");
						serviceExecutionTextArea.append("\tType: " + seia.getAttributetype() + "\n");
						if (seia.getAttributevaluenumeric() != null) serviceExecutionTextArea.append("\tValue: " + seia.getAttributevaluenumeric() + "\n\n");
						if (seia.getAttributevaluestring() != null) serviceExecutionTextArea.append("\tValue: " + seia.getAttributevaluestring() + "\n\n");
					}
					
					//Set up Enactor Registration Text Area
					enactorRegistrationTextArea.selectAll();
					enactorRegistrationTextArea.replaceSelection(null);
					enactorRegistrationTextArea.append( "Enactor ID: \n\t" + er.getEnactorid()+ " \n\n" 
							+ "Registration Time: \n\t" + er.getRegistrationtime() + " \n\n");
					
					enactorRegistrationTextArea.append("Enactor References: \n");
					while(errIterator.hasNext()){
						ERReference err = (ERReference) errIterator.next();
						enactorRegistrationTextArea.append("\t" + err.getDescriptionquery() + "\n");
					}
					
					enactorRegistrationTextArea.append("\nEnactor Parameters: \n");
					while(erpIterator.hasNext()){
						ERParameter erp = (ERParameter) erpIterator.next();
						enactorRegistrationTextArea.append("\t" + erp.getParametername() + "\n");
					}
					
					enactorRegistrationTextArea.append("\nComponents Added: \n");
					while(caIterator.hasNext()){
						ComponentAdded ca = (ComponentAdded) caIterator.next();
						enactorRegistrationTextArea.append("\tComponent ID:" + ca.getComponentdescriptionid() + "\n");	
					}
					
				}});
		}
	}
	
	class LocationFilterListSelectionListener implements ListSelectionListener {
		public void valueChanged(ListSelectionEvent e) { 
			ListSelectionModel lsm = (ListSelectionModel) e.getSource();
			int selectedIndex = lsm.getMaxSelectionIndex();
	
			if (filterCheckBox.isSelected() == true){
				//Set up query to be passed to list runnable
				String location = (String) lfListModel.getElementAt(selectedIndex);
				seListModel.filterListByLocation(location);
			}
		}
	}
	
	class LocationFilterCheckBoxListener implements ItemListener {
		public void itemStateChanged(ItemEvent  e) { 
			if (e.getStateChange() == ItemEvent.DESELECTED) {
				seListModel.setListToDefaultQuery();
			}
			else if (e.getStateChange() == ItemEvent.SELECTED) {
				ListSelectionModel lsm = lfJList.getSelectionModel();
				int selectedIndex = lsm.getMaxSelectionIndex();
				if (selectedIndex >= 0) {
					String location = (String) lfListModel.getElementAt(selectedIndex);
					seListModel.filterListByLocation(location);
				}
			}
		}
	}

	
	/**
	 * Create the GUI and show it.  For thread safety,
	 * this method should be invoked from the
	 * event-dispatching thread.
	 */
	private static void createAndShowGUI() {
		//Make sure we have nice window decorations.
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			System.err.println("Couldn't use system look and feel.");
		}
		
		JFrame.setDefaultLookAndFeelDecorated(true);

		//Create and set up the window.
		JFrame frame = new JFrame("ExplanationGUI");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//Set up the content pane.
		@SuppressWarnings("unused")
		ExplanationGUI egui = new ExplanationGUI(frame.getContentPane());

		//Display the window.
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		//Schedule a job for the event-dispatching thread:
		//creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}
}
