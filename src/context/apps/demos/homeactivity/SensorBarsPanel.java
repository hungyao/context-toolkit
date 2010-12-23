package context.apps.demos.homeactivity;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.layout.SpringUtilities;

public class SensorBarsPanel extends JPanel {

	private static final long serialVersionUID = 6199944913790962578L;

	private int sequenceLength;

	private JLabel[] stateLabels;
	private BarLabel[] evidenceBars;

	public SensorBarsPanel(String sensorName) {
		this.sequenceLength = HomeModel.SEQUENCE_LENGTH;

		this.setOpaque(false);
		this.setBorder(BorderFactory.createLineBorder(Color.gray));
		this.setLayout(new BorderLayout());
		this.setBackground(new Color(255, 200, 100, 100));
		
		JLabel nameLabel = new JLabel(sensorName);
		this.add(nameLabel, BorderLayout.NORTH);
		
		JPanel barsPanel = new JPanel();
		barsPanel.setOpaque(false);
		barsPanel.setLayout(new SpringLayout());
		this.add(barsPanel, BorderLayout.CENTER);

		stateLabels = new JLabel[sequenceLength];
		evidenceBars = new BarLabel[sequenceLength];

		for (int t = sequenceLength - 1; t >= 0; t--) {
			String timeStr;
			if (t == sequenceLength - 1) {
				timeStr = "Now";
			} else {
				timeStr = (t + 1 - sequenceLength) + " min";
			}
			JLabel timeLabel = new JLabel("<html><b>" + timeStr + ":</b></html>");
			barsPanel.add(timeLabel);

			stateLabels[t] = new JLabel("On/Off");
			stateLabels[t].setFont(stateLabels[t].getFont().deriveFont(Font.PLAIN));
			barsPanel.add(stateLabels[t]);

			evidenceBars[t] = new BarLabel();
			barsPanel.add(evidenceBars[t]);
		}

		SpringUtilities.makeCompactGrid(barsPanel, 
				sequenceLength, 3, // rows, cols
				6, 6, // initX, initY
				6, 6); // xPad, yPad
	}
	
	public void setEvidences(double[] evidences) {
		for (int t = 0; t < sequenceLength; t++) {
			evidenceBars[t].setEvidence(evidences[t]);
		}
	}
	
	public void setStates(int[] states) {
		for (int t = 0; t < sequenceLength; t++) {
			if (states[t] == 1) {
				stateLabels[t].setText("On");
				stateLabels[t].setForeground(Color.green);
			}
			else {
				stateLabels[t].setText("Off");
				stateLabels[t].setForeground(Color.red);
			}
		}
	}

	public class BarLabel extends JLabel {

		private static final long serialVersionUID = -3355017981073149492L;

		private double evidence;

		public BarLabel() {
			setPreferredSize(new Dimension(100, 20));
			setBorder(BorderFactory.createLineBorder(Color.gray));
		}

		public void setEvidence(double evidence) {
			this.evidence = evidence;
		}

		public void paint(Graphics g) {
			super.paint(g);

			/*
			 * Calculate and calibrate length of bar
			 */
			int length = (int) (Math.log(Math.abs(evidence) / 1e3) * 2);
//			System.out.println("evidence = " + evidence + ", length = " + length);

			int w_2 = getWidth() / 2;
			int h = getHeight();

			if (evidence >= 0) {
				g.setColor(FloorplanPanel.SENSOR_BLUE);
				g.fillRect(w_2, 0, length, h - 1);
				g.setColor(Color.black);
				g.drawRect(w_2, 0, length, h - 1);
			} else {
				g.setColor(FloorplanPanel.SENSOR_RED);
				g.fillRect(w_2 - length, 0, length, h - 1);
				g.setColor(Color.black);
				g.drawRect(w_2 - length, 0, length, h - 1);
			}
		}

	}
	
}
