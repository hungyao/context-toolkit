package context.apps.demos.homeactivity;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.ToolTipManager;

import context.arch.intelligibility.Explanation;
import context.arch.intelligibility.expression.Reason;
import context.arch.intelligibility.hmm.HmmExplainer;
import context.arch.intelligibility.presenters.Presenter;
import context.arch.intelligibility.query.AltQuery;
import context.arch.intelligibility.query.Query;
import context.arch.widget.SequenceWidget;

/**
 * Supports rendering of and interaction for several explanation types:
 * <ul>
 * 	<li>Why (by Input and by Time)</li>
 * 	<li>Why Not (by Input and by Time)</li>
 * 	<li>How To?</li>
 * 	<li>What If: by clicking on sensors to toggle their state</li>
 * 	<li>Inputs: by virtue of showing the sensors</li>
 * 	<li>Outputs?</li>
 * 	<li>Certainty?</li>
 * </ul>
 * @author Brian Y. Lim
 *
 */
public class FloorplanPanel extends JPanel implements MouseListener, MouseMotionListener {

	private static final long serialVersionUID = 2481900797033338083L;

	public static final Image floorplanImg = new ImageIcon("resources/icons/floorplan.png").getImage();

	public static int NUM_SENSORS = 14;
	private SensorBubble[] sensorBubbles = new SensorBubble[] {
			new SensorBubble("Microwave", 614,97),
			new SensorBubble("HallToiletDoor", 283,353),
			new SensorBubble("HallBathroomDoor", 283,263),
			new SensorBubble("CupsCupboard", 443,51),
			new SensorBubble("Fridge", 510,91),
			new SensorBubble("PlatesCupboard", 482,51),
			new SensorBubble("FrontDoor", 345,474),
			new SensorBubble("Dishwasher", 469,90),
			new SensorBubble("ToiletFlush", 171,371),
			new SensorBubble("Freezer", 551,93),
			new SensorBubble("PansCupboard", 543,51),
			new SensorBubble("WashingMachine", 596,89),
			new SensorBubble("GroceriesCupboard", 618,52),
			new SensorBubble("HallBedroomDoor", 340,311)
	};
	
	private static final Map<String, SensorBarsPanel> sensorBarsPanels = new LinkedHashMap<String, SensorBarsPanel>(); // <name, panel>
	static {
		for (String sensorName : SensorsWidget.FEATURES) {			
			sensorBarsPanels.put(sensorName, new SensorBarsPanel(sensorName));
		}
	}

	private PopupFactory popupFactory;
	private Popup popup;

	private HmmExplainer explainer;

	Presenter<int[]> inputsPresenter;
	Presenter<double[]> presenter;
	
	private boolean editable;

	public FloorplanPanel(HomeModel contextModel, boolean editable) {
		this(contextModel);
		this.editable = editable;
	}
	
	public int[] getObservations() {
		int[] observations = new int[sensorBubbles.length];
		
		for (int t = 0; t < sensorBubbles.length; t++) {
			observations[t] = sensorBubbles[t].on ? 1 : 0;
		}
		
		return observations;
	}

	public FloorplanPanel(HomeModel contextModel) {
		this.explainer = (HmmExplainer) contextModel.activityEnactor.getExplainer();
		
		this.inputsPresenter = new Presenter<int[]>(contextModel.activityEnactor) {

			@Override
			public int[] render(Explanation explanation) {
				/*
				 * Get only the latest state
				 */
				Reason inputExpl = explanation.getContent().get(0);
//				System.out.println("whyExpl = " + whyExpl);
				String seqMarker = SequenceWidget.getTPrepend(HomeModel.SEQUENCE_LENGTH - 1); // marker for the last time step

				int[] observations = new int[SensorsWidget.FEATURES.length];
				
				for (int i = 0; i < observations.length; i++) {
					String fullName = seqMarker + SensorsWidget.FEATURES[i];

//					System.out.println("fullName = " + fullName);
//					System.out.println("inputExpl = " + inputExpl);
//					System.out.println("inputExpl.getValue(fullName) = " + inputExpl.getValue(fullName));
					Object value = inputExpl.getValue(fullName);
					if (value instanceof String) {
						observations[i] = Integer.parseInt((String) value); // TODO: not sure why it was stored as a String
					}
					else {
						observations[i] = (Integer) value;
					}
				}
				
				return observations;
			}			
		};

		this.presenter = new Presenter<double[]>(contextModel.activityEnactor) {
			@Override
			public double[] render(Explanation explanation) {
				Query query = explanation.getQuery();
				String question = query.getQuestion();

				// render inputs (or What If)
				int[] observations;
//				if (question == WhatIfQuery.QUESTION_WHAT_IF) {
//					// observations = ((WhatIfQuery)query).getWidgetState(); TODO
//				}
//				else {
					observations = inputsPresenter.render(
							explainer.getExplanation(
									new Query(Query.QUESTION_INPUTS, ActivityWidget.ACTIVITY, System.currentTimeMillis())));
//				}				
				
				double[] evidences = new double[SensorsWidget.FEATURES.length];
				
				if (question == Query.QUESTION_WHY || 
						 question == AltQuery.QUESTION_WHY_NOT){ // why or why not
					/*
					 * Get evidences for all time steps
					 */
					Reason whyExpl = explanation.getContent().get(0);
					
					for (int i = 0; i < evidences.length; i++) {
						evidences[i] = 0;
						
						for (int t = 0; t < HomeModel.SEQUENCE_LENGTH; t++) {
							String seqMarker = SequenceWidget.getTPrepend(t);
							String fullName = seqMarker + SensorsWidget.FEATURES[i];
				
	//						System.out.println("whyExpl.getValue("+fullName+") = " + whyExpl.getValue(fullName));
							// expect value to be Value<Double>
							evidences[i] += (Double) whyExpl.getValue(fullName);
						}
					}					
				}
				
				updateSensors(evidences, observations);
				
				return evidences;
			}	
		};
		
		setPreferredSize(new Dimension(floorplanImg.getWidth(this), floorplanImg.getHeight(this)));
		setBorder(BorderFactory.createEtchedBorder());

		addMouseListener(this);
		addMouseMotionListener(this);
		
		ToolTipManager.sharedInstance().registerComponent(this);
		ToolTipManager.sharedInstance().setInitialDelay(0);

		popupFactory = PopupFactory.getSharedInstance();

		updateSensors(getRandomEvidences(), getRandomStates());
	}

	/*
	 * For testing
	 */
	private int[] getRandomEvidences() {
		int[] evidences = new int[NUM_SENSORS];
		for (int i = 0; i < evidences.length; i++) {
			evidences[i] = (int)(50*Math.random() - 15);
		}
		return evidences;
	}
	private boolean[] getRandomStates() {
		boolean[] states = new boolean[NUM_SENSORS];
		for (int i = 0; i < states.length; i++) {
			states[i] = Math.random() > 0.5;
		}
		return states;
	}

	public static double EVIDENCE_RADIUS_SCALE = 1e-1;

	public void updateSensors(double[] evidences, int[] states) {
		// update radii
		for (int i = 0; i < sensorBubbles.length; i++) {
			sensorBubbles[i].setEvidence(evidences[i], (states[i] == 1));
		}
		repaint();
	}

	public void updateSensors(int[] evidences, boolean[] states) {
		// update radii
		for (int i = 0; i < sensorBubbles.length; i++) {
			sensorBubbles[i].setEvidence(evidences[i], states[i]);
		}
		repaint();
	}

	public static final Color SENSOR_GRAY = new Color(120, 120, 120, 180);
	public static final Color SENSOR_GRAY_LESS = new Color(120, 120, 120, 80);
	public static final Color SENSOR_BLUE = new Color(60, 140, 230, 180);
	public static final Color SENSOR_BLUE_LESS = new Color(60, 140, 230, 80);
	public static final Color SENSOR_RED = new Color(220, 90, 105, 180);
	public static final Color SENSOR_RED_LESS = new Color(220, 90, 105, 80);
	public static final Color TRANSPARENT = new Color(255, 255, 255, 0);

	public static final Color SENSOR_ON = new Color(150, 255, 100);

	public static final Color SENSOR_HIGHLIGHT = new Color(255, 210, 140, 180);
	public static final Stroke SENSOR_HIGHLIGHT_STROKE = new BasicStroke(4);

	private class SensorBubble {

		Ellipse2D shape;
		
		double evidence;
		boolean on = true; // on or off

		String name;
		int x, y; 

		public SensorBubble(String name, int x, int y) {
			this.name = name;
			this.x = x;
			this.y = y;
			int radius = 1;
			this.shape = new Ellipse2D.Float(x - radius, y - radius, 2*radius, 2*radius);
		}

		public void setEvidence(double evidence, boolean on) {
			// scale radius; arbitrarily determined
			int radius = (int)(Math.log(Math.abs(evidence) * EVIDENCE_RADIUS_SCALE))*4 + 8;
//			System.out.println("radius = " + radius + ", evidence = " + evidence);
			shape = new Ellipse2D.Float(x - radius, y - radius, 2*radius, 2*radius);
			
			this.evidence = evidence;
			this.on = on;
		}
	}

	private void paintBubble(Graphics2D g2, SensorBubble sensor) {
		paintBubble(g2, sensor.shape, sensor.evidence, sensor.on);
	}

	float[] dist = {.2f, .8f, 1.0f};
	Color[] zero_colors = {TRANSPARENT, SENSOR_GRAY_LESS, SENSOR_GRAY};
	Color[] pos_colors = {TRANSPARENT, SENSOR_BLUE_LESS, SENSOR_BLUE};
	Color[] neg_colors = {TRANSPARENT, SENSOR_RED_LESS, SENSOR_RED};

	private SensorBubble currentBubble;

	private void paintBubble(Graphics2D g2, Ellipse2D shape, double evidence, boolean on) {
		g2 = (Graphics2D)g2.create(); // create 'copy' and manipulate on that

		int x = (int)shape.getCenterX();
		int y = (int)shape.getCenterY();
		int radius = (int)(shape.getWidth()/2);
		
		/*
		 * Color whether on or off
		 */
		if (on) {
			g2.setColor(SENSOR_ON);
			g2.setComposite(BlendComposite.Hue);
			g2.fillRect(x - 5, y - 5, 10, 10);
			g2.setComposite(AlphaComposite.SrcAtop); // reset
		}
		
		/*
		 * Fill gradient
		 */
		Point2D center = new Point2D.Float(x, y);
		Point2D focus = new Point2D.Float(x, y);
		Color[] colors;
		if (evidence == 0) { colors = zero_colors; }
		else if (evidence > 0) { colors = pos_colors; }
		else { colors = neg_colors; }
		
		if (radius <= 0) { // not sure why this happens
//			System.out.println("radius = " + radius); 
			radius = 1;
		}
		
		RadialGradientPaint paint = new RadialGradientPaint(
				center, radius, 
				focus,
				dist, colors,
				CycleMethod.NO_CYCLE);
		g2.setPaint(paint);
		g2.fill(shape);
	}

	public void getSelectedAt(int x, int y) {
		if (currentBubble != null && editable) {
			currentBubble.on = !currentBubble.on; // flip
			repaint();
		}
	}

	public void paint(Graphics g) {
		super.paint(g);

		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		//		g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE); // so that we don't get small gaps between strokes due to anti-aliasing
		//		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR); // to smooth images when rotated

		g.drawRect(50, 50, 100, 100);

		g.drawImage(floorplanImg, 0, 0, this);

		for (SensorBubble bubble : sensorBubbles) {
			paintBubble(g2, bubble);
		}

		if (currentBubble != null) {
			g2.setStroke(SENSOR_HIGHLIGHT_STROKE);
			if (currentBubble.evidence == 0) {
				g.setColor(SENSOR_GRAY);
			}
			else if (currentBubble.evidence > 0) {
				g.setColor(SENSOR_BLUE);
			}
			else {
				g.setColor(SENSOR_RED);
			}
			g2.draw(currentBubble.shape);
		}
	}

	@Override public void mouseEntered(MouseEvent evt) {}
	@Override public void mouseExited(MouseEvent evt) {}
	@Override public void mouseReleased(MouseEvent evt) {}
	@Override public void mouseClicked(MouseEvent evt) {}

	@Override
	public void mousePressed(MouseEvent evt) {
		getSelectedAt(evt.getX(), evt.getY());
	}

	@Override public void mouseDragged(MouseEvent evt) {}

	@Override
	public void mouseMoved(MouseEvent evt) {
		int x = evt.getX();
		int y = evt.getY();

		SensorBubble tempBubble = null;
		for (SensorBubble bubble : sensorBubbles) {
			if (bubble.shape.contains(x, y)) {
				if (tempBubble == null || 
						tempBubble.shape.getWidth() > bubble.shape.getWidth()) { // smaller bubble gets priority
					tempBubble = bubble;
				}
			}
		}

		/*
		 * No change
		 */
		if (currentBubble == tempBubble) { // no change
			return;
		}

		/*
		 * New current bubble
		 */
		if (tempBubble != null) {
			if (popup != null) { popup.hide(); }
			currentBubble = tempBubble;
			
			/*
			 * Set time bars evidence and statuses
			 */
			String sensorName = currentBubble.name;
			SensorBarsPanel barsPanel = sensorBarsPanels.get(sensorName);
			double[] sensorEvidences = new double[HomeModel.SEQUENCE_LENGTH];
			int[] sensorStates = new int[HomeModel.SEQUENCE_LENGTH];
			Reason why = explainer.getWhyExplanation().get(0);
			Reason inputs = explainer.getInputsExplanation();
			for (int t = 0; t < HomeModel.SEQUENCE_LENGTH; t++) {
				sensorEvidences[t] = (Double) why.getValue(SequenceWidget.getTPrepend(t) + sensorName);
				
				if (inputs.getValue(SequenceWidget.getTPrepend(t) + sensorName) == null) {
					System.out.println("SequenceWidget.getTPrepend(t) + sensorName = " + (SequenceWidget.getTPrepend(t) + sensorName));
				}
				Object val = inputs.getValue(SequenceWidget.getTPrepend(t) + sensorName);
				if (val instanceof String) {
					sensorStates[t] = Integer.parseInt((String)val);	
				}		
				else if (val instanceof Integer) {
					sensorStates[t] = (Integer)val;
				}
//				System.out.println("sensorEvidences[t] = " + sensorEvidences[t]);
			}
			barsPanel.setEvidences(sensorEvidences);
			barsPanel.setStates(sensorStates);

			if (!editable) {
				Point p = this.getLocationOnScreen();
				popup = popupFactory.getPopup(this, 
	//					new DetailsPanel(currentBubble.name, new String[] {}, new double[] {}),
						barsPanel,
						p.x + x, p.y + y + 20);
				popup.show();
			}
			else {
				setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			}
				
			repaint();
			return;
		}
		
		/*
		 * No bubble
		 */
		// reset
		if (popup != null) { popup.hide(); }
		currentBubble = null;
		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		repaint();
	}
	
	public static class DetailsPanel extends JPanel {

		private static final long serialVersionUID = 173492125733883290L;
		
		private static final Color bg = new Color(255, 255, 220);
		
		public DetailsPanel(String title, String[] labels, double[] evidences) {
			super();
			setLayout(new BorderLayout());
			setBackground(bg);
			setBorder(BorderFactory.createLineBorder(Color.black));
			
			add(new JLabel(title), BorderLayout.NORTH);
		}
		
	}

}
