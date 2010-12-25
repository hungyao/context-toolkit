package context.apps.demos.roomlight;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import context.apps.ContextModel;
import context.apps.ContextModel.EnactorsReadyListener;
import context.arch.enactor.Enactor;
import context.arch.enactor.Generator;
import context.arch.intelligibility.Explainer;
import context.arch.intelligibility.Explanation;
import context.arch.intelligibility.presenters.ContextIcons;
import context.arch.intelligibility.presenters.QueryPanel;
import context.arch.intelligibility.presenters.StringPresenter;
import context.arch.intelligibility.query.Query;
import context.arch.intelligibility.query.QueryListener;

/**
 * Main application with GUI to display the smart room application.
 * This also demonstrates some intelligibility features.
 * @author Brian Y. Lim
 *
 */
public class RoomApplication extends JFrame implements ChangeListener, ListDataListener, EnactorsReadyListener, QueryListener {

	private static final long serialVersionUID = -8804998219675878102L;
	private RoomPanel roomPanel;
	private JSlider brightnessSlider;
	private RoomModel contextModel;
	private Generator generator;
	private Enactor enactor;
	private Explainer explainer;
	private StringPresenter presenter;
	private DNDList outsideList;
	private DNDList insideList;
	private DefaultListModel insideModel;
	private QueryPanel queryPanel;
	private JLabel explanationLabel;
	private JPanel intelligibilityPanel;
	
	public RoomApplication() {
		super("Intelligibility - Room");
		
		roomPanel = new RoomPanel();
		add(roomPanel, BorderLayout.CENTER);
		
		/*
		 * Context modeling
		 */
		contextModel = new RoomModel(this);	
		contextModel.setEnactorsReadyListener(this);
		generator = contextModel.roomGenerator;
		
		/*
		 * Side panel
		 */
		JPanel sidePanel = new JPanel();
		sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
		
		brightnessSlider = new JSlider(SwingConstants.VERTICAL, 0, RoomModel.BRIGHTNESS_MAX, 0);
		brightnessSlider.setEnabled(false);
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
		outsideList.setEnabled(false);
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
		insideList.setEnabled(false);
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
		 * Bottom panel for explanations
		 */
		intelligibilityPanel = new JPanel();		
		intelligibilityPanel.setPreferredSize(new Dimension(10, 80));
		add(intelligibilityPanel, BorderLayout.SOUTH);
//		intelligibilityPanel.setLayout(new BoxLayout(intelligibilityPanel, BoxLayout.X_AXIS));

		/*
		 * Window properties
		 */		
		pack();
		setLocationRelativeTo(null);
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

	@Override
	public void enactorsReady() {
		brightnessSlider.setEnabled(true);
		outsideList.setEnabled(true);
		insideList.setEnabled(true);

		enactor = contextModel.roomEnactor;

		/*
		 * Context intelligibility
		 */
		explainer = enactor.getExplainer();
//		presenter = new TypePanelPresenter(enactor);
		presenter = new StringPresenter(enactor);

		queryPanel = new QueryPanel(enactor);
		intelligibilityPanel.add(queryPanel);
		explanationLabel = new JLabel();
		intelligibilityPanel.add(explanationLabel);

		queryPanel.addQueryListener(this);

		updateBrightness();
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
		queryPanel.update();
	}

	/**
	 * Called when the user selects a query.
	 * It generates the corresponding explanation, and has it rendered.
	 */
	@Override
	public void queryInvoked(Query query) {
		Explanation explanation = explainer.getExplanation(query);
		System.out.println("explanation: " + explanation);
		
		String text = presenter.render(explanation);
		explanationLabel.setText("<html>" + text + "</html>");
	}
	
	public static void main(String[] args) {
		ContextModel.startDiscoverer();		
		RoomApplication f = new RoomApplication();
		f.setVisible(true);
	}

}
