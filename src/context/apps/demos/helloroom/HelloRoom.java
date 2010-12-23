package context.apps.demos.helloroom;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import context.apps.ContextModel;
import context.apps.ContextModel.EnactorsReadyListener;
import context.arch.enactor.Enactor;
import context.arch.enactor.EnactorXmlParser;
import context.arch.widget.Widget;
import context.arch.widget.WidgetXmlParser;

/**
 * Main class for Hello World context-aware application of a living room smart lamp.
 * It demonstrates four steps to create a simple context-aware application:
 * <ol>
 * 	<li>Modeling context with Widgets</li>
 * 	<li>Modeling reasoning with Enactors</li>
 * 	<li>Modeling behavior with Services</li>
 * 	<li>Combining models</li>
 * </ol>
 * Running this program would launch a GUI simulation of a room sensor suite
 * with brightness and presence sensors, and lamp light level.
 * 
 * @author Brian Y. Lim
 *
 */
public class HelloRoom extends ContextModel implements EnactorsReadyListener {
	
	protected Widget roomWidget;
	protected Widget lightWidget;

	protected Enactor enactor;
	
	protected LightService lightService;
	
	protected HelloRoomUI ui;

	public HelloRoom() {
		super();
		
		/*
		 * Room sensor Widget
		 */
		roomWidget = WidgetXmlParser.getWidget("demos/HelloRoom/room-widget.xml");
		addWidget(roomWidget);
		
		/*
		 * Light actuator Widget and Service
		 */
		lightWidget = WidgetXmlParser.getWidget("demos/HelloRoom/light-widget.xml");
		lightService = new LightService(lightWidget);
		lightWidget.addService(lightService);
		addWidget(lightWidget);
		
		/*
		 * Enactor to use rules about RoomWidget to update LightWidget
		 */
		enactor = EnactorXmlParser.getEnactor("demos/HelloRoom/room-enactor.xml");
		addEnactor(enactor);
		
		// Listen for when enactors are ready, then show UI
		super.setEnactorsReadyListener(this);

		// setup UI component
		ui = new HelloRoomUI(lightService.lightLabel); // need to attach lightService before starting
		ui.setVisible(false);
	}
	
	@Override
	public void enactorsReady() {
		ui.setVisible(true);
		
		JFrame frame = new JFrame("Hello Room");
		frame.add(ui);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(new Dimension(300, 200));
		frame.setLocationRelativeTo(null); // center of screen
		frame.setVisible(true);
	}
	

	/**
	 * Class to represent the UI representation for the application.
	 * @author Brian Y. Lim
	 */
	@SuppressWarnings("serial")
	public class HelloRoomUI extends JPanel {
		
		private JSlider brightnessSlider;
		private JSpinner presenceSpinner;
		
		private float fontSize = 20f;
		
		public HelloRoomUI(JLabel lightLabel) {			
			setLayout(new GridLayout(3, 2)); // 3 rows, 2 columns
			
			/*
			 * UI for brightness
			 */
			add(new JLabel("brightness") {{ setFont(getFont().deriveFont(fontSize)); }});
			
			add(brightnessSlider = new JSlider(new DefaultBoundedRangeModel(255, 0, 0, 255)) {{
				addChangeListener(new ChangeListener() {
					@Override
					public void stateChanged(ChangeEvent evt) {
						// brightness is from 0 to 255
						short brightness = (short)brightnessSlider.getValue();
						roomWidget.updateData("brightness", brightness);
						
						// set color to represent brightness level
						setBackground(new Color(brightness, brightness, brightness));
					}
				});
				setOpaque(true); // to allow background color to show
				setMajorTickSpacing(50);
				setPaintTicks(true);
				setPaintLabels(true);
			}});
			
			/*
			 * UI for presence
			 */
			add(new JLabel("presence") {{ setFont(getFont().deriveFont(fontSize)); }});
			
			add(presenceSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 5, 1)) {{ // # people from 0 to 5
				// get text field to color text
				final JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) getEditor();
				final JFormattedTextField tf = editor.getTextField();
				tf.setForeground(Color.red);
				setFont(getFont().deriveFont(fontSize));
				
				addChangeListener(new ChangeListener() {
					@Override
					public void stateChanged(ChangeEvent evt) {
						int presence = (Integer) presenceSpinner.getValue();
						roomWidget.updateData("presence", presence);
						
						// color text red for when presence is red
						if (presence == 0) { tf.setForeground(Color.red); }
						else { tf.setForeground(Color.black); }
					}
				});
			}});
			
			// UI for light level
			add(new JLabel("light") {{ setFont(getFont().deriveFont(fontSize)); }});
			add(lightLabel);
		}
		
		@Override
		public void setVisible(boolean visible) {
			// force update if visible
			if (visible) {
				short brightness = (short)brightnessSlider.getValue();
				roomWidget.updateData("brightness", brightness);
	
				int presence = (Integer) presenceSpinner.getValue();
				roomWidget.updateData("presence", presence);
			}
			
			super.setVisible(visible);
		}
		
	}
	
	public static void main(String[] args) {
		ContextModel.startDiscoverer();
		
		HelloRoom app = new HelloRoom();
		app.start();		
	}

}
