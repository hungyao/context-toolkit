package context.apps.demos.accelerometer;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import context.arch.comm.DataObject;
import context.arch.discoverer.Discoverer;
import context.arch.enactor.EnactorComponentInfo;
import context.arch.enactor.EnactorListener;
import context.arch.enactor.EnactorParameter;
import context.arch.intelligibility.Explainer;
import context.arch.intelligibility.Explanation;
import context.arch.intelligibility.presenters.ContextIcons;
import context.arch.intelligibility.presenters.QueryPanel;
import context.arch.intelligibility.query.Query;
import context.arch.intelligibility.query.QueryListener;
import context.arch.storage.Attributes;

/**
 * <p>
 * Main class for running the Mobile Phone Activity Recognition application. It loads a GUI window simulating a phone UI.
 * The user can select instance scenarios that load input features that are interpreted by the context-aware application to infer
 * the motion that the user could be doing.
 * </p>
 * <p>
 * The application also demonstrates intelligibility features to allow users to ask for explanations for each context value.
 * </p>
 * 
 * @author Brian Y. Lim
 *
 */
public class AccelerometerApplication extends JFrame implements EnactorListener, QueryListener {

	private static final long serialVersionUID = -5633392149457720659L;

	/*
	 * Icons for physical activities
	 */
	private static final Icon iconSitting = new ImageIcon("demos/accelerometer-nb/img/motion_sitting.png");
	private static final Icon iconStanding = new ImageIcon("demos/accelerometer-nb/img/motion_standing.png");
	private static final Icon iconWalking = new ImageIcon("demos/accelerometer-nb/img/motion_walking.png");
	static {
		ContextIcons.icons.put("Sit", iconSitting);
		ContextIcons.icons.put("Stand", iconStanding);
		ContextIcons.icons.put("Walk", iconWalking);
	};
	
	private AccelerometerModel contextModel;
	private MotionPresenter presenter;
	private JLabel outcomeLabel;
	private JPanel instanceChoicePanel;
	private AccelerometerEnactor accelEnactor;
	private Explainer explainer;

	private JComboBox scenarioComboBox;

	private QueryPanel queryPanel;
	
	public AccelerometerApplication() {
		super("Intelligibility - Motion");
		setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

		/* ------------------------------------------------
		 * Context model, Enactor, Explainer
		 */		
		contextModel = new AccelerometerModel();
		
		accelEnactor = contextModel.accelEnactor;
		accelEnactor.addListener(this);
		
		explainer = accelEnactor.getExplainer();
		
		// Scenario chooser
		instanceChoicePanel = initInstanceChoicePanel();
		this.add(instanceChoicePanel);
		
		// Outcome label
		outcomeLabel = new JLabel("");
		outcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(outcomeLabel);
		
		/* ------------------------------------------------
		 * For intelligibility features
		 */
		// Query chooser
		queryPanel = new QueryPanel(accelEnactor);
		this.add(queryPanel);

		// Explanation panel
		presenter = new MotionPresenter(contextModel.accelEnactor);
		this.add(presenter.getPanel());

		/* ------------------------------------------------
		 * Window properties
		 */		
		setMinimumSize(new Dimension(320, 420));
		setLocationRelativeTo(null); // positions frame in center of screen
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		scenarioComboBox.setSelectedIndex(0);
		queryPanel.setVisible(false);
	}
	
	/**
	 * Create top panel for choosing instance scenario; only 6 choices
	 * @return
	 */
	private JPanel initInstanceChoicePanel() {
		JPanel panel = new JPanel();
		
		JLabel label = new JLabel("Choose scenario:");
		panel.add(label);
		
		scenarioComboBox = new JComboBox(new Object[] {1, 2, 3, 4, 5, 6});
		panel.add(scenarioComboBox);
		
		scenarioComboBox.addActionListener(new ActionListener() {			
			@Override public void actionPerformed(ActionEvent arg0) {
				contextModel.update(scenarioComboBox.getSelectedIndex());
			}
		});
		
		return panel;
	}

	@Override public void componentAdded(EnactorComponentInfo eci, Attributes paramAtts) {}
	@Override public void componentRemoved(EnactorComponentInfo eci, Attributes paramAtts) {}
	@Override public void parameterValueChanged(EnactorParameter parameter, Attributes validAtts, Object value) {}
	@Override public void serviceExecuted(EnactorComponentInfo eci, String serviceName, String functionName, Attributes input, DataObject returnDataObject) {}

	/**
	 * Would be called when AutostatusEnactor has its EnactorReference triggered
	 */
	@Override
	public void componentEvaluated(EnactorComponentInfo eci) {
		// set outcome
		String outcome = contextModel.accelEnactor.getOutcomeValue();
		outcomeLabel.setIcon(ContextIcons.icons.get(outcome));
		outcomeLabel.setText(outcome);
				
		/*
		 * Update intelligibility query and presenter
		 */
		
		remove(queryPanel);
		queryPanel = new QueryPanel(accelEnactor);
		queryPanel.addQueryListener(this);
		this.add(queryPanel);
		
		remove(presenter.getPanel());
		this.add(presenter.getPanel());
	}

	@Override
	public void queryInvoked(Query query) {
		Explanation explanation = explainer.getExplanation(query);
		presenter.render(explanation);
	}
	
	public static void main(String[] args) {
		Discoverer.start();
		
		AccelerometerApplication f = new AccelerometerApplication();
		f.setVisible(true);
	}

}
