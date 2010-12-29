package context.apps.demos.imautostatus;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import weka.core.Instance;
import context.arch.enactor.Generator;
import context.arch.storage.Attributes;
import context.arch.widget.ClassifierWidget;

/**
 * Window to display GUI for the instant messaging chat simulation.
 * It has a combo box on top to choose a scenario to load, a chat history in the middle,
 * and an input field at the bottom to type messages.
 * @author Brian Y. Lim
 *
 */
public class ChatWindow extends JFrame implements KeyListener {

	private static final long serialVersionUID = 7559239744697886505L;
	
	public static final Icon alicePic = new ImageIcon("demos/imautostatus-dtree/img/alice.png");
	public static final Icon bobPic = new ImageIcon("demos/imautostatus-dtree/img/bob.png");
	
	private JComboBox scenarioComboBox;
	private JPanel instanceChoicePanel;
	private JTextPane outputPane;
	private JTextPane inputField;
	private JButton inputButton;
	
	/**
	 * 
	 * @param userId
	 * @param sendListener to listen to when a new message is sent by the user in the inputField
	 * @param scenarioListener to listen to when the user selects a scenario from a combo box
	 */
	public ChatWindow(String userId, ActionListener sendListener, Generator generator) {
		super("IM Autostatus - " + userId);
		setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		
		/*
		 * Scenario chooser
		 */
		instanceChoicePanel = initInstanceChoicePanel(generator);
		add(instanceChoicePanel);
		
		/*
		 * Outcome Pane
		 */
		JPanel outputPanel = new JPanel();
		outputPanel.setBorder(BorderFactory.createLineBorder(Color.gray));
//		outputPanel.setMinimumSize(new Dimension(20,0));
		outputPanel.setLayout(new BoxLayout(outputPanel, BoxLayout.X_AXIS));
		outputPane = new JTextPane();
		outputPane.setEditable(false);
		JScrollPane scrollpane = new JScrollPane(outputPane);
		scrollpane.setPreferredSize(new Dimension(480, 300));
		outputPanel.add(scrollpane);
		JPanel sidePanel = new JPanel();
		sidePanel.setLayout(new BorderLayout());
		JLabel avatar = new JLabel(bobPic);
		avatar.setBorder(BorderFactory.createLineBorder(Color.darkGray));
		sidePanel.add(avatar, BorderLayout.NORTH);
		sidePanel.add(new JLabel(), BorderLayout.CENTER);
		outputPanel.add(sidePanel);
		add(outputPanel);
		
		/*
		 * Input field
		 */
		JPanel inputPanel = new JPanel();
		inputPanel.setBorder(BorderFactory.createLineBorder(Color.gray));
		inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.X_AXIS));
		inputField = new JTextPane();
		inputField.addKeyListener(this);
		scrollpane = new JScrollPane(inputField);
		scrollpane.setPreferredSize(new Dimension(480, 100));
		inputPanel.add(scrollpane);
		sidePanel = new JPanel();
		sidePanel.setLayout(new BorderLayout());
		avatar = new JLabel(alicePic);
		avatar.setBorder(BorderFactory.createLineBorder(Color.darkGray));
		sidePanel.add(avatar, BorderLayout.NORTH);
		inputButton = new JButton("Send");
		inputButton.setEnabled(false);
		inputButton.addActionListener(sendListener);
		sidePanel.add(inputButton, BorderLayout.SOUTH);
		inputPanel.add(sidePanel);
		add(inputPanel);

		/* ------------------------------------------------
		 * Window properties
		 */		
		pack();
		setLocationRelativeTo(null); // positions frame in center of screen
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);

		scenarioComboBox.setSelectedIndex(0);
	}
	
	private JPanel initInstanceChoicePanel(final Generator generator) {
		JPanel panel = new JPanel();
		
		JLabel label = new JLabel("Choose scenario:");
		panel.add(label);
		
		// combo box of six choices for choosing instance scenarios
		scenarioComboBox = new JComboBox(new Object[] {1, 2, 3, 4, 5, 6});
		panel.add(scenarioComboBox);
		
		scenarioComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				Instance instance = AutostatusApplication.scenarios.get(scenarioComboBox.getSelectedIndex());
				Attributes atts = ClassifierWidget.instanceToAttributes(instance);
				generator.updateOutWidget(atts);
			}
		});
		
		/*
		 * Instructions button and dialog
		 */
		JButton instructionsButton = new JButton("Instructions");
		instructionsButton.addActionListener(new ActionListener() {
			private JDialog dialog = new JDialog(ChatWindow.this, "Instructions") {
				private static final long serialVersionUID = 1L;
				
				{ // constructor
					JEditorPane instructionsLabel = new JEditorPane("text/html", "<p style=\"font-family:sans-serif\">" +
							// instructions on how to use the UI
							"Type one of the following commands to ask for explanations:<br/><table border=1 cellspacing=0>" +
							"<tr valign=top><td><b>Command</b></td><td><b>Description</b></td><td><b>Example</b></td></tr>" +
							"<tr valign=top><td><b>im-why</b></td><td>Why the responsiveness was predicted as such</td><td></td></tr>" +
							"<tr valign=top><td><b>im-certainty</b></td><td>How certain the IM Autostatus plugin is of the responsiveness prediction</td><td></td></tr>" +
							"<tr valign=top><td><b>im-whynot x</b></td><td>Why the responsiveness was predicted as x, where x may be 'within' or 'after'</td><td>im-whynot within</td></tr>" +
							"<tr valign=top><td><b>im-howto</b></td><td>Under what circumstances (when would) the responsiveness be predicted as x, where x may be 'within' or 'after'</td><td>im-howto after</td></tr>" +
							"<tr valign=top><td><b>im-whatif a=#[,a2=#]</b></td><td>What if the inputs are different, what would the predicted responsiveness be? Choices for inputs: " + ConsoleStringPresenter.inputsDesc.toString().substring(1, ConsoleStringPresenter.inputsDesc.toString().length()-1) + "</td><td>im-whatif Focus=in focus</td></tr>" +
							"<tr valign=top><td><b>im-inputs</b></td><td>What the inputs to predict responsiveness are</td><td></td></tr>" +
							"<tr valign=top><td><b>im-outputs</b></td><td>What output values the predicted responsiveness can take</td><td></td></tr>" +
							"</table></p>");
					instructionsLabel.setEditable(false);
					JScrollPane scrollpane = new JScrollPane(instructionsLabel);
					setSize(new Dimension(500, 320));
					setLocationRelativeTo(null);
					add(scrollpane);
				}
			};
			@Override
			public void actionPerformed(ActionEvent arg0) {
				dialog.setVisible(true);
			}
		});
		panel.add(instructionsButton);
		
		return panel;
	}
	
	/**
	 * Date formatter for printing a timestamp to the IM chat window.
	 */
	private static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
	
	/**
	 * Encapsulated method to print a message from a sender to the history text box (outputPane).
	 * @param sender
	 * @param msg
	 */
	public void writeMessage(String sender, String msg) {
		String prevText = outputPane.getText();
		if (prevText.trim().length() > 0) { prevText += "\n"; }		
		
		String text = sender + " [" + df.format(new Date()) + "]> " + msg;
		outputPane.setText(prevText + text);
	}

	@Override 
	public void keyReleased(KeyEvent evt) {
		// enable or disable Send button depending on whether there is text in the input box
		if (inputField.getText().length() > 0) {
			inputButton.setEnabled(true);
		}
		else {
			inputButton.setEnabled(false);
		}
	}

	@Override public void keyTyped(KeyEvent evt) {}

	@Override
	public void keyPressed(KeyEvent evt) {
		// parse when enter key is pressed
		if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
			if (evt.isAltDown()) { // Alt+Enter gives new line
				inputField.setText(inputField.getText() + "\n");
			}
			else { // Enter sends message
				evt.consume();
				inputButton.doClick();
			}
		}
	}
	
	public String getMessage() {
		return inputField.getText();		
	}
	
	public void clearInput() {
		inputField.setText("");
		inputButton.setEnabled(false);
	}

}
