package context.arch.service;

import java.awt.Checkbox;
import java.awt.Button;
import java.awt.Label;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.CheckboxGroup;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Vector;

/**
 * This class is a frame that runs in it's own thread.  It's able to display
 * a title, question to the user, choices to the user in the form of radio
 * buttons, and to make the user choice available, if any.
 */
public class DisplayChoiceFrame extends Frame implements Runnable, ActionListener {

	private static final long serialVersionUID = -6038071883157249221L;

	/**
	 * Tag for the submit button
	 */
	public static final String SUBMIT = "submit";

	/**
	 * Tag for the cancel button
	 */
	public static final String CANCEL = "cancel";

	/**
	 * Tag if the user does not select any choice
	 */
	public static final String NO_CHOICE = "noChoice";

	private ActionListener listener;
	private String requestId;
	private CheckboxGroup cbg;
	private String choice;
	private Button submit, cancel;

	/**
	 * This constructor lays out and creates the frame for the user to view.
	 *
	 * @param listener ActionListener object to pass events to
	 * @param choices List of choices to display to the user
	 * @param question Question to pose to the user
	 * @param requestId Unique id to identify the result
	 */
	public DisplayChoiceFrame(ActionListener listener, Vector<String> choices, String question, String requestId) {
		this.listener = listener;
		this.requestId = requestId;
		setLayout(new BorderLayout());
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();

		Panel p1 = new Panel();
		p1.setLayout(gridbag);
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0;

		Label lQuestion = new Label(question);
		c.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints(lQuestion,c);
		p1.add(lQuestion);

		cbg = new CheckboxGroup();
		for (int i = 0; i < choices.size(); i++) {
			Checkbox cb = new Checkbox(choices.elementAt(i), false, cbg);
			c.gridwidth = GridBagConstraints.REMAINDER;
			gridbag.setConstraints(cb,c);
			p1.add(cb);
			if (i == 0) {
				cbg.setSelectedCheckbox(cb);
			}
		}

		add("North",p1);

		Panel p2 = new Panel();
		p2.setLayout(gridbag);
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0;

		c.gridwidth = GridBagConstraints.RELATIVE;
		submit = new Button("Submit");
		submit.addActionListener(this);
		submit.setActionCommand(SUBMIT);
		gridbag.setConstraints(submit,c);
		p2.add(submit);

		c.gridwidth = GridBagConstraints.REMAINDER;
		cancel = new Button("Cancel");
		cancel.addActionListener(this);
		cancel.setActionCommand(CANCEL);
		gridbag.setConstraints(cancel,c);
		p2.add(cancel);

		add("South",p2);
	}

	/**
	 * This method implements the necessary method for the ActionListener interface.
	 * If a user clicks on either button, it collects this information and notifies
	 * the listener passed into this class with an ActionEvent containing the
	 * object that created the event (submit or cancel button), and the unique
	 * id passed into this class.
	 *
	 * @see ActionEvent
	 */
	public void actionPerformed(ActionEvent evt) {
		Object obj;
		if (evt.getActionCommand().equals(SUBMIT)) {
			choice = cbg.getSelectedCheckbox().getLabel();
			obj = submit;
		}
		else {
			choice = NO_CHOICE;
			obj = cancel;
		}
		listener.actionPerformed(new ActionEvent(obj,0,requestId));
	}

	/**
	 * This method implements the necessary method for the Runnable interface.
	 * It simply makes the frame visible to the user.
	 *
	 * #setVisible(boolean);
	 */
	public void run() {
		setVisible(true);
	}

	/**
	 * This method returns the user's choice from the radio buttons.  This 
	 * will be one of the input choices, or, if none selected, it will be
	 * NO_CHOICE.
	 *
	 * @return the user's choice
	 * @see #NO_CHOICE
	 */ 
	public String getChoice() {
		return choice;
	}

}
