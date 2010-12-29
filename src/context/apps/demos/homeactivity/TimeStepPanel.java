package context.apps.demos.homeactivity;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

import context.arch.comm.DataObject;
import context.arch.enactor.EnactorComponentInfo;
import context.arch.enactor.EnactorListener;
import context.arch.enactor.EnactorParameter;
import context.arch.intelligibility.Explanation;
import context.arch.intelligibility.hmm.HmmExplainer;
import context.arch.intelligibility.query.Query;
import context.arch.storage.Attributes;

public class TimeStepPanel extends JPanel implements ActionListener, EnactorListener {

	private static final long serialVersionUID = 3516157109058541075L;

	private JButton backButton;
	private JButton forwardButton;

	private int MAX_STEP = 5;
	private int step;

	private HomeModel contextModel;

	private HomeApplication viewer;

	private HmmExplainer explainer;
	
	public TimeStepPanel(HomeApplication viewer) {
		this.viewer = viewer;
		this.contextModel = viewer.contextModel;
		this.explainer = (HmmExplainer) contextModel.activityEnactor.getExplainer();
		
		backButton = new JButton(new ImageIcon("demos/home-hmm/img/arrow-left.png"));
		backButton.setToolTipText("Move back one step in time");
		backButton.setPreferredSize(new Dimension(40, 40));
		backButton.setEnabled(false);
		backButton.addActionListener(this);
		add(backButton);
		contextModel.activityEnactor.addListener(this);

		forwardButton = new JButton(new ImageIcon("demos/home-hmm/img/arrow-right.png"));
		forwardButton.setToolTipText("Move forward one step in time");
		forwardButton.setPreferredSize(new Dimension(40, 40));
		forwardButton.setEnabled(false);
		forwardButton.addActionListener(this);
		add(forwardButton);
		
		MAX_STEP = contextModel.numTimeSteps() - 1;
		
		initSequence();
	}
	
	/**
	 * Whether to step forward or backwards and by how many steps.
	 * @param step
	 */
	public void setTimeStep(int step) {
		this.step = step;
		
		if (step <= HomeModel.SEQUENCE_LENGTH - 1) {
			backButton.setEnabled(false);
		}
		else if (step >= MAX_STEP) {
			forwardButton.setEnabled(false);
		}
		else {
			backButton.setEnabled(true);
			forwardButton.setEnabled(true);
		}
		
		contextModel.setTimeStep(step);
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		Object src = evt.getSource();
		if (src == backButton) {
			setTimeStep(step - 1);
		}
		else if (src == forwardButton) {
			setTimeStep(step + 1);
		}
		
//		viewer.evidencePanel.setVisible(false);
		
		Query query = new Query(Query.QUESTION_WHAT, ActivityWidget.ACTIVITY, System.currentTimeMillis());
		Explanation explanation = explainer.getExplanation(query);
		System.out.println("Explanation: " + explanation);
		viewer.presenter.render(explanation);
	}

	public void initSequence() {
		forwardButton.setEnabled(true);

		// step forward multiple times to fill full sequence
		for (int i = 0; i < HomeModel.SEQUENCE_LENGTH; i++) {
			forwardButton.doClick();
		}

//		forwardButton.doClick(); // do one more, since otherwise evidences are NaN
	}

	@Override
	public void componentEvaluated(EnactorComponentInfo eci) {
	}

	@Override public void componentAdded(EnactorComponentInfo eci, Attributes paramAtts) {}
	@Override public void componentRemoved(EnactorComponentInfo eci, Attributes paramAtts) {}
	@Override public void parameterValueChanged(EnactorParameter parameter, Attributes validAtts, Object value) {}
	@Override public void serviceExecuted(EnactorComponentInfo eci, String serviceName, String functionName, Attributes input, DataObject returnDataObject) {}

}
