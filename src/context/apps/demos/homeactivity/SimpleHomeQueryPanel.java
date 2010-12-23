package context.apps.demos.homeactivity;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import context.apps.demos.homeactivity.SensorsWidget.SensorsData;
import context.arch.intelligibility.Explanation;
import context.arch.intelligibility.expression.Reason;
import context.arch.intelligibility.hmm.HmmExplainer;
import context.arch.intelligibility.query.AltQuery;
import context.arch.intelligibility.query.Query;
import context.arch.intelligibility.query.WhatIfQuery;

public class SimpleHomeQueryPanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = 6633309991441110778L;
	
	private HomeApplication viewer;
	private ActivityEnactor enactor;
	private HmmExplainer explainer;
	
	private JButton whyButton;
	private JButton whatButton;
	private JButton whyNotButton;
	private JButton whatIfButton;
	private TimelineStatesPanel whyNotTimelinePanel;
	private FloorplanPanel whatIfFloorplanPanel;

	public SimpleHomeQueryPanel(HomeApplication viewer) {
		this.viewer = viewer;
		this.enactor = viewer.contextModel.activityEnactor;
		this.explainer = (HmmExplainer)enactor.getExplainer();
		
//		enactor.addListener(this);
		
//		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		whatButton = new JButton("What & Certainty?");
		whatButton.addActionListener(this);
		this.add(whatButton);
		whyButton = new JButton("Why?");
		whyButton.addActionListener(this);
		this.add(whyButton);
		whyNotButton = new JButton("Why Not? ...");
		whyNotButton.addActionListener(this);
		this.add(whyNotButton);
		whatIfButton = new JButton("What If? ...");
		whatIfButton.addActionListener(this);
		this.add(whatIfButton);
		
		viewer.evidencePanel.setVisible(false);
		
		whyNotTimelinePanel = new TimelineStatesPanel(enactor, true);
		whatIfFloorplanPanel = new FloorplanPanel(viewer.contextModel, true);
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		viewer.evidencePanel.setVisible(true);
		
		Query query = null;
		
		Object src = evt.getSource();
		if (src == whatButton) {
			query = new Query(Query.QUESTION_WHAT, ActivityWidget.ACTIVITY, System.currentTimeMillis());	
		}
		else if (src == whyButton) {
			query = new Query(Query.QUESTION_WHY, ActivityWidget.ACTIVITY, System.currentTimeMillis());			
		}
		else if (src == whyNotButton) {
			List<String> states = viewer.evidencePanel.statesPanel.getStates();
			if (states != null) {
				whyNotTimelinePanel.setStates(states);
			}
			
			JOptionPane.showMessageDialog(this, 
					whyNotTimelinePanel, 
					"Why Not?", JOptionPane.QUESTION_MESSAGE);
			String altOutcome = whyNotTimelinePanel.getStates().toString();
			altOutcome = altOutcome.substring(1, altOutcome.length() - 1) // remove '[' and ']'
								   .replace(",", "");
			System.out.println("altOutcome = " + altOutcome);
			
			query = new AltQuery(AltQuery.QUESTION_WHY_NOT, ActivityWidget.ACTIVITY, 
					altOutcome,
					System.currentTimeMillis());			
		}
		else if (src == whatIfButton) {
			Explanation inputsExplanation = explainer.getExplanation(
					new Query(Query.QUESTION_INPUTS, ActivityWidget.ACTIVITY, System.currentTimeMillis()));
			whatIfFloorplanPanel.presenter.render(inputsExplanation); // don't need to set?
			
			JOptionPane.showMessageDialog(this, 
					whatIfFloorplanPanel, 
					"What If?", JOptionPane.QUESTION_MESSAGE);
			
			int[] obs = whatIfFloorplanPanel.getObservations();	
			SensorsData data = new SensorsData(System.currentTimeMillis());			
			System.arraycopy(obs, 0, data.featureValues, 0, obs.length);
			
//			ComponentDescription widgetState = enactor.getInWidgetState();
//			widgetState.addNonConstantAttributes(data.toAttributes(HomeModel.SEQUENCE_LENGTH-1)); // to replace some attributes
			
			Reason inputs = explainer.getInputsExplanation();
//			
			query = new WhatIfQuery(WhatIfQuery.QUESTION_WHAT_IF, ActivityWidget.ACTIVITY, 
					inputs);

//			viewer.evidencePanel.setVisible(false);
			
		}
		
		Explanation explanation = explainer.getExplanation(query);
		System.out.println("Explanation: " + explanation);
		viewer.presenter.render(explanation);

		viewer.pack();
	}

}
