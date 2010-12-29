package context.apps.demos.roomlight;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import context.arch.discoverer.Discoverer;
import context.arch.enactor.Enactor;
import context.arch.enactor.Generator;
import context.arch.intelligibility.Explainer;
import context.arch.intelligibility.presenters.ContextIcons;
import context.arch.intelligibility.presenters.StringPresenter;

/**
 * Main application with GUI to display the smart room application.
 * @author Brian Y. Lim
 *
 */
public class RoomApplication extends JFrame implements ChangeListener, ListDataListener {

	private static final long serialVersionUID = -8804998219675878102L;
	
	protected RoomPanel roomPanel;
	protected JSlider brightnessSlider;
	protected RoomModel contextModel;
	protected Generator generator;
	protected Enactor enactor;
	protected Explainer explainer;
	protected StringPresenter presenter;
	protected DNDList outsideList;
	protected DNDList insideList;
	protected DefaultListModel insideModel;
	
	public RoomApplication() {
		super("Intelligibility - Room");
		initLayout();
		
		/*
		 * Context modeling
		 */
		contextModel = new RoomModel(this);	
		generator = contextModel.roomGenerator;
		enactor = contextModel.roomEnactor;
	}
	
	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			updateBrightness();
			updatePresence();

			pack();
			setLocationRelativeTo(null);
		}
		super.setVisible(visible);
	}
	
	protected void initLayout() {		
		roomPanel = new RoomPanel();
		add(roomPanel, BorderLayout.CENTER);
		
		/*
		 * Side panel
		 */
		JPanel sidePanel = new JPanel();
		sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
		
		brightnessSlider = new JSlider(SwingConstants.VERTICAL, 0, RoomModel.BRIGHTNESS_MAX, 0);
		brightnessSlider.setPreferredSize(new Dimension(50, 100));
		brightnessSlider.setMajorTickSpacing(RoomModel.BRIGHTNESS_MAX);
		brightnessSlider.setPaintLabels(true);
		brightnessSlider.addChangeListener(this);
		JPanel sliderWrapper = new JPanel();
		sliderWrapper.setPreferredSize(new Dimension(120, 100));
		sliderWrapper.setBorder(BorderFactory.createTitledBorder("Brightness"));
		sliderWrapper.add(brightnessSlider);
		sidePanel.add(sliderWrapper);

		add(sidePanel, BorderLayout.EAST);		

		outsideList = new DNDList();
		DefaultListModel outsideModel = new DefaultListModel();
		// source for names: http://babynamesworld.parentsconnect.com/top-100-baby-names.php?p=top&s_top_year3=2009&s_top_nr3=100&s_gender4=1
	    outsideModel.addElement("Isabella");
	    outsideModel.addElement("Emma");
	    outsideModel.addElement("Jacob");
		outsideList.setModel(outsideModel);
		//outsideModel.addListDataListener(this);  // just listen to one list, instead of both
		JScrollPane scrollpane = new JScrollPane(outsideList);
		scrollpane.setBorder(BorderFactory.createTitledBorder("Outside Room"));
		scrollpane.setPreferredSize(new Dimension(120, 50));
		sidePanel.add(scrollpane);
		
		insideList = new DNDList();
		insideModel = new DefaultListModel();
		insideModel.addElement("Ethan");
		insideList.setModel(insideModel);
		insideModel.addListDataListener(this);
		scrollpane = new JScrollPane(insideList);
		scrollpane.setBorder(BorderFactory.createTitledBorder("Inside Room"));
		scrollpane.setPreferredSize(new Dimension(120, 50));
		sidePanel.add(scrollpane);

		roomPanel.setListModels(insideModel, outsideModel);

		/*
		 * Window properties
		 */		
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		/*
		 * Icons
		 */
		ContextIcons.set("On", new ImageIcon("demos/room-rules/img/lightbulb.png"));
		ContextIcons.set("Off", new ImageIcon("demos/room-rules/img/lightbulb_off.png"));
	}
	
	private void updateBrightness() {
		int brightness = brightnessSlider.getValue();
		roomPanel.setBrightness(brightness);		
		generator.setAttributeValue("brightness", (short)brightness);
	}
	
	private void updatePresence() {
		int presence = insideModel.getSize();
		generator.setAttributeValue("presence", presence);
	}

	@Override
	public void stateChanged(ChangeEvent evt) {
		updateBrightness();
	}

	@Override public void contentsChanged(ListDataEvent evt) {}

	@Override 
	public void intervalAdded(ListDataEvent evt) {
		updatePresence();
	}
	@Override 
	public void intervalRemoved(ListDataEvent evt) {
		updatePresence();
	}
	
	/**
	 * Call this to change the light level of the lamp in the application.
	 * Updates the value, and the GUI paint.
	 * @param light
	 */
	public void setLight(int light) {
		roomPanel.setLight(light);						
		roomPanel.repaint();
	}
	
	public static void main(String[] args) {
		Discoverer.start();		
		new RoomApplication().setVisible(true);
	}

}
