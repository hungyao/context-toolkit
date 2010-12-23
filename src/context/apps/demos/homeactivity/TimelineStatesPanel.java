package context.apps.demos.homeactivity;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import context.arch.intelligibility.Explanation;
import context.arch.intelligibility.expression.DNF;
import context.arch.intelligibility.expression.Parameter;
import context.arch.intelligibility.expression.Reason;
import context.arch.intelligibility.hmm.HmmExplainer;
import context.arch.intelligibility.presenters.Presenter;
import context.arch.intelligibility.query.AltQuery;
import context.arch.intelligibility.query.Query;
import context.arch.intelligibility.query.WhatIfQuery;


public class TimelineStatesPanel extends JPanel {
	
	private static final long serialVersionUID = 8300845514081563499L;
	
	public final DecimalFormat nf = (DecimalFormat)DecimalFormat.getInstance();
	{
		nf.applyPattern("###########0");
	}
	
	private StatePanel[] statePanels;

	public static int NUM_ACTIVITIES = 7;
	public static int SEQUENCE_LENGTH = 5; // T

	private ActivityEnactor enactor;
	private HmmExplainer explainer;

	private boolean editable;

	private JLabel averageLabel;
	
	Presenter<Void> presenter;
	
	@SuppressWarnings("serial")
	public TimelineStatesPanel(ActivityEnactor enactor, boolean editable) {
		this.enactor = enactor;
		this.explainer = (HmmExplainer)enactor.getExplainer();
		this.editable = editable;		
		
		setBorder(BorderFactory.createTitledBorder("Activities Timeline"));
		
		statePanels = new StatePanel[SEQUENCE_LENGTH];
		
		for (int t = 0; t < SEQUENCE_LENGTH; t++) {
			statePanels[t] = new StatePanel(t);
			add(statePanels[t]);			
		}

		setStates(
				new ArrayList<String>() {{
					add("Undefined");
					add("Undefined");
					add("Undefined");
					add("Undefined");
					add("Undefined");
				}});
		setEvidences(
				new double[] {
						0, 0, 0, 0, 0
				}
				);
		
		averageLabel = new JLabel("0.00%");
		add(averageLabel);
		
		presenter = new Presenter<Void>(enactor) {	
			public final DecimalFormat nf = (DecimalFormat)DecimalFormat.getInstance();
			{
				nf.applyPattern("##0.000########");
			}
			
			@SuppressWarnings("unchecked")
			@Override
			public Void render(Explanation explanation) {	
				Query query = explanation.getQuery();
				String question = query.getQuestion();
				
				List<String> stateSeq = TimelineStatesPanel.this.enactor.getOutcomeValueSequence();
				if (question == AltQuery.QUESTION_WHY_NOT) {
					String altOutcome = ((AltQuery)query).getAltOutcomeValue();
					stateSeq = Arrays.asList(altOutcome.split(" "));
				}
				else if (question == WhatIfQuery.QUESTION_WHAT_IF) {
					String whatifOutcome = (String) explanation.getContent().getFirstLiteral().getValue();
					stateSeq = Arrays.asList(whatifOutcome.substring(1, whatifOutcome.length()-1).split(", "));
				}
				System.out.println("stateSeq = " + stateSeq);
				setStates(stateSeq); // always show this
				setEvidences(new double[HomeModel.SEQUENCE_LENGTH]);
								
				if (question == Query.QUESTION_WHAT) {					
					// really just add certainty
					double certainty = (Double) explainer.getCertaintyExplanation().getFirstLiteral().getValue();
					System.out.println("Certainty = " + (certainty*100) + "%");
					averageLabel.setText("Certainty = " + nf.format(certainty*100) + "%");
				}	
				
				else if (question == Query.QUESTION_WHY || 
						 question == AltQuery.QUESTION_WHY_NOT) { // why or why not

					DNF exp = explanation.getContent();
					Reason whyExpl = exp.get(0);
					double[] transEvidences = new double[stateSeq.size()];
//					System.out.println("whyExp = " + whyExp);
					for (int i = 0; i < transEvidences.length; i++) {
						transEvidences[i] = ((Parameter<Double>)whyExpl.get(i + 1)).getValue(); // skip first which is average
					}
					
					setEvidences(transEvidences);

					double average = ((Parameter<Double>)whyExpl.get(0)).getValue(); // first element is average
					averageLabel.setText("Average = " + nf.format(average));	
				}
				
				return null;
			}
		};
	}

	public void setEvidences(double[] transitionEvidences) {
		for (int t = 0; t < statePanels.length; t++) {
			statePanels[t].setEvidence(transitionEvidences[t]);
		}
	}
	
	private List<String> states;

	public void setStates(List<String> states) {
		this.states = states;
		
		for (int t = 0; t < statePanels.length; t++) {
			String state = states.get(t);
//			System.out.println("state = " + state);
			
			statePanels[t].setState(state);
		}
		
		repaint();
	}
	
	public List<String> getStates() {
		if (states == null) { // if not yet set, then generate What
			presenter.render(
					explainer.getExplanation(
					new Query(Query.QUESTION_WHAT, ActivityWidget.ACTIVITY, System.currentTimeMillis())));
		}
		repaint();
		
		return states;
	}
	
	private class StatePanel extends JPanel {

		private static final long serialVersionUID = -5318260705701330914L;
		
		public final Map<String, Icon> stateIcons = new LinkedHashMap<String, Icon>();
		{
			stateIcons.put("LeaveHouse", new ImageIcon("resources/demos/home-hmm/exit.png"));
			stateIcons.put("UseToilet", new ImageIcon("resources/demos/home-hmm/toilet.png"));
			stateIcons.put("TakeShower", new ImageIcon("resources/demos/home-hmm/shower.png"));
			stateIcons.put("GotoBed", new ImageIcon("resources/demos/home-hmm/sleep.png"));
			stateIcons.put("Breakfast", new ImageIcon("resources/demos/home-hmm/breakfast.png"));
			stateIcons.put("Dinner", new ImageIcon("resources/demos/home-hmm/dinner.png"));
			stateIcons.put("GetDrink", new ImageIcon("resources/demos/home-hmm/cup.png"));
			stateIcons.put("Undefined", new ImageIcon("resources/demos/home-hmm/unknown.png"));
		};
		public final Map<Icon, String> iconStates = new LinkedHashMap<Icon, String>();
		{
			for (String key : stateIcons.keySet()) {
				iconStates.put(stateIcons.get(key), key);
			}
		}
		
		private JLabel transitionArrow;		
		private JLabel stateLabel;
		private JComboBox stateComboBox;
		
		@SuppressWarnings("unused")
		Image arrowImg = new ImageIcon("resources/demos/home-hmm/arrow-prob.png").getImage();
		
		private double evidence = .4;
		
//		private String state;
		private boolean first; // if first, then it represents transition from start; i.e. prior probability

		private int t;
				
		public StatePanel(int t) {
			this.t = t;
			this.first = t == 0;
			
			transitionArrow = new JLabel() {
				private static final long serialVersionUID = -6006798250322379965L;
				public void paint(Graphics g) {
					int w = getWidth(), h = getHeight();
					int h_2 = h/2;
					int thickness;
					int arrowThickness;
					
					if (evidence == 0) {
						arrowThickness = thickness = 0;
					}
					else {
						thickness = (int)(Math.log(evidence/3e7)*20 * h_2 * 2/3); // manually calibrated
						
						if (thickness <= 0) { 
							arrowThickness = thickness = 1; 
						}
						arrowThickness = thickness + 4;
					}
					
					Shape arrow = new Polygon(
							new int[] {
									0,
									w*2/3,
									w*2/3,
									w,
									w*2/3,
									w*2/3,
									0
							}, 
							new int[] {
									h_2 - thickness,
									h_2 - thickness,
									h_2 - arrowThickness,
									h_2,
									h_2 + arrowThickness,
									h_2 + thickness,
									h_2 + thickness,
							}, 
							7);
					
					Graphics2D g2 = (Graphics2D)g;
					g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//					g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR); // to smooth images when scaled
					
					if (first) {
						g.setColor(FloorplanPanel.SENSOR_GRAY_LESS);
					}
					else {
						g.setColor(FloorplanPanel.SENSOR_BLUE_LESS);
					}
					g2.fill(arrow);
					g.setColor(FloorplanPanel.SENSOR_BLUE);
					g2.draw(arrow);
					
//					g2.drawImage(arrowImg, 0, 0, w, (int)(probability * h), this);
				}
			};
			transitionArrow.setPreferredSize(new Dimension(40, 30));
			
			stateLabel = new JLabel();
			if (editable) {
				stateLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			}
			stateComboBox = new JComboBox(stateIcons.values().toArray());
			stateComboBox.setVisible(false);
			
			stateLabel.addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent evt) {
					if (editable) {
						stateLabel.setVisible(false);					
						stateComboBox.setVisible(true);
					}
				}
			});
			stateComboBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (editable) {
						stateLabel.setVisible(true);
						stateLabel.setIcon((Icon)stateComboBox.getSelectedItem());					
						stateComboBox.setVisible(false);
						
						// also set for global array
//						System.out.println("stateComboBox.getSelectedIndex() = " + stateComboBox.getSelectedIndex());
						states.set(StatePanel.this.t, ""+stateComboBox.getSelectedIndex());
					}
				}
			});

			add(transitionArrow);
			add(stateLabel);
			add(stateComboBox);
		}

		public void setEvidence(double transitionEvidence) {
			this.evidence = transitionEvidence;	
			transitionArrow.setToolTipText(nf.format(evidence));			
			transitionArrow.repaint();
		}

		public void setState(String activity) {
			try {
				int i = Integer.parseInt(activity); // sometimes it is numeric
				activity = enactor.getOutcomeValues().get(i);
			} catch (NumberFormatException e) {
				// leave activity = activity
			}
			
			stateLabel.setIcon(stateIcons.get(activity));
			stateComboBox.setSelectedItem(stateLabel.getIcon());
			stateLabel.setToolTipText(activity);
//			this.state = activity;
		}
		
//		public String getState() {
//			return state;
//		}
		
	}

}
