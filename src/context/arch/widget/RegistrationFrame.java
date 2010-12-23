package context.arch.widget;

import java.awt.Label;
import java.awt.Color;
import java.awt.Checkbox;
import java.awt.TextField;
import java.awt.List;
import java.awt.Button;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.Insets;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * This class implements a frame that runs in its own thread.  It contains
 * a GUI that collects registration information for a tour guide.  This
 * information includes user name, affiliation, email, interests and name
 * of handheld device, if any.
 *
 * @see Frame
 * @see Runnable
 * @see ActionListener
 */
public class RegistrationFrame extends Frame implements Runnable, ActionListener {

	private static final long serialVersionUID = -2856326914853939259L;

/**
   * Tag for the submit button
   */
  public static final String SUBMIT = "submit";

  /**
   * Tag for the cancel button
   */
  public static final String CANCEL = "cancel";

  private ActionListener listener;
  private Checkbox cb1,cb2,cb3,cb4,cb5,cb6,cb7,cb8;
  private TextField name,affiliation,email,interest1,interest2,interest3;
  private List device;
  private String separator;
  private Label message;

  /**
   * Constructor that creates the frame and GUI for displaying to the user.
   *  
   * @param listener ActionListener to pass events to
   * @param separator String to use to separate user interests
   */
  public RegistrationFrame(ActionListener listener, String separator) {
    this.listener = listener;
    this.separator = separator;
    setLayout(new BorderLayout());

    Panel p4 = new Panel();
    p4.setLayout(new BorderLayout());

    GridBagLayout gridbag = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();

    Panel p1 = new Panel();
    p1.setLayout(gridbag);
    c.fill = GridBagConstraints.BOTH;
    c.weightx = 1.0;

    c.gridwidth = GridBagConstraints.RELATIVE;
    c.insets = new Insets(10,10,10,10);
    Label lName = new Label("Name: ");
    gridbag.setConstraints(lName,c);
    p1.add(lName);

    c.gridwidth = GridBagConstraints.REMAINDER;
    name = new TextField(40);
    gridbag.setConstraints(name,c);
    p1.add(name);

    c.gridwidth = GridBagConstraints.RELATIVE;
    Label lAffiliation = new Label("Affiliation: ");
    gridbag.setConstraints(lAffiliation,c);
    p1.add(lAffiliation);

    c.gridwidth = GridBagConstraints.REMAINDER;
    affiliation = new TextField(40);
    gridbag.setConstraints(affiliation,c);
    p1.add(affiliation);
    
    c.gridwidth = GridBagConstraints.RELATIVE;
    Label lEmail = new Label("Email Address: ");
    gridbag.setConstraints(lEmail,c);
    p1.add(lEmail);

    c.gridwidth = GridBagConstraints.REMAINDER;
    email = new TextField(40);
    gridbag.setConstraints(email,c);
    p1.add(email);

    p4.add("North",p1);

    Panel p2 = new Panel();
    p2.setLayout(gridbag);
    c.fill = GridBagConstraints.BOTH;
    c.weightx = 1.0;

    c.gridwidth = GridBagConstraints.REMAINDER;
    Label lInterests = new Label("Interests: ");
    gridbag.setConstraints(lInterests,c);
    p2.add(lInterests);

    c.gridwidth = GridBagConstraints.RELATIVE;
    c.insets = new Insets(0,30,0,0);
    cb1 = new Checkbox("context");
    gridbag.setConstraints(cb1,c);
    p2.add(cb1);

    c.gridwidth = GridBagConstraints.REMAINDER;
    c.insets = new Insets(0,0,0,0);
    cb2 = new Checkbox("applications");
    gridbag.setConstraints(cb2,c);
    p2.add(cb2);

    c.gridwidth = GridBagConstraints.RELATIVE;
    c.insets = new Insets(0,30,0,0);
    cb3 = new Checkbox("virtual environments");
    gridbag.setConstraints(cb3,c);
    p2.add(cb3);

    c.gridwidth = GridBagConstraints.REMAINDER;
    c.insets = new Insets(0,0,0,0);
    cb4 = new Checkbox("demos");
    gridbag.setConstraints(cb4,c);
    p2.add(cb4);

    c.gridwidth = GridBagConstraints.RELATIVE;
    c.insets = new Insets(0,30,0,0);
    cb5 = new Checkbox("capture");
    gridbag.setConstraints(cb5,c);
    p2.add(cb5);

    c.gridwidth = GridBagConstraints.REMAINDER;
    c.insets = new Insets(0,0,0,0);
    cb6 = new Checkbox("ubiquitous");
    gridbag.setConstraints(cb6,c);
    p2.add(cb6);

    c.gridwidth = GridBagConstraints.RELATIVE;
    c.insets = new Insets(0,30,0,0);
    cb7 = new Checkbox("perception");
    gridbag.setConstraints(cb7,c);
    p2.add(cb7);

    c.gridwidth = GridBagConstraints.REMAINDER;
    c.insets = new Insets(0,0,0,0);
    cb8 = new Checkbox("errors");
    gridbag.setConstraints(cb8,c);
    p2.add(cb8);

    c.gridwidth = GridBagConstraints.REMAINDER;
    c.gridheight = 2;
    c.anchor = GridBagConstraints.SOUTH;
    c.insets = new Insets(10,10,10,10);
    Label lInterestsFree = new Label("Interests (free-form):");
    gridbag.setConstraints(lInterestsFree,c);
    p2.add(lInterestsFree);

    c.gridwidth = GridBagConstraints.RELATIVE;
    Label lInterest1 = new Label("Interest #1: ");
    gridbag.setConstraints(lInterest1,c);
    p2.add(lInterest1);

    c.gridwidth = GridBagConstraints.REMAINDER;
    interest1 = new TextField(40);
    gridbag.setConstraints(interest1,c);
    p2.add(interest1);

    c.gridwidth = GridBagConstraints.RELATIVE;
    Label lInterest2 = new Label("Interest #2: ");
    gridbag.setConstraints(lInterest2,c);
    p2.add(lInterest2);

    c.gridwidth = GridBagConstraints.REMAINDER;
    interest2 = new TextField(40);
    gridbag.setConstraints(interest2,c);
    p2.add(interest2);

    c.gridwidth = GridBagConstraints.RELATIVE;
    Label lInterest3 = new Label("Interest #3: ");
    gridbag.setConstraints(lInterest3,c);
    p2.add(lInterest3);

    c.gridwidth = GridBagConstraints.REMAINDER;
    interest3 = new TextField(40);
    gridbag.setConstraints(interest3,c);
    p2.add(interest3);

    p4.add("Center",p2);

    Panel p3 = new Panel();
    p3.setLayout(gridbag);
    c.fill = GridBagConstraints.BOTH;
    c.weightx = 1.0;

    c.gridwidth = GridBagConstraints.RELATIVE;
    c.gridheight = 2;
    c.anchor = GridBagConstraints.SOUTH;
    c.insets = new Insets(10,10,10,10);
    Label lDevice = new Label("Handheld device:");
    gridbag.setConstraints(lDevice,c);
    p3.add(lDevice);

    c.gridwidth = GridBagConstraints.REMAINDER;
    c.gridheight = 1;
    device = new List();
    device.add("None");
    device.add("WinCE #1");
    device.add("WinCE #2");
    device.add("WinCE #3");
    device.add("WinCE #4");
    device.add("WinCE #5");
    device.select(0);
    gridbag.setConstraints(device,c);
    p3.add(device);

    p4.add("South",p3);
    add("North",p4);

    Panel p5 = new Panel();
    p5.setLayout(gridbag);
    c.fill = GridBagConstraints.BOTH;
    c.weightx = 1.0;
    
    c.gridwidth = GridBagConstraints.RELATIVE;
    Button submit = new Button("Submit");
    submit.addActionListener(this);
    submit.setActionCommand(SUBMIT);
    gridbag.setConstraints(submit,c);
    p5.add(submit);

    c.gridwidth = GridBagConstraints.REMAINDER;
    Button cancel = new Button("Cancel");
    cancel.addActionListener(this);
    cancel.setActionCommand(CANCEL);
    gridbag.setConstraints(cancel,c);
    p5.add(cancel);

    message = new Label("");
    message.setForeground(Color.red);
    gridbag.setConstraints(message,c);
    p5.add(message);

    add("Center",p5);
  }

  /**
   * Method required by the Runnable interface.  It just makes the frame
   * visible for the user
   */
  public void run() {
    setVisible(true);
  }

  /**
   * Method required by the ActionListener interface.  It handles the
   * user interaction with the submit and cancel buttons.  When the 
   * submit button is pressed, the user input is checked to see that
   * a name, affiliation and email address have been entered.  If
   * the input is okay, it passes the event to the listener given in
   * the constructor.
   *
   * @param evt ActionEvent caused by interaction with a button
   */
  public void actionPerformed(ActionEvent evt) {
    if (evt.getActionCommand().equals(SUBMIT)) {
      if (name.getText().trim().length() == 0) {
        message.setText("Please enter your name before submitting");
      }
      else if (affiliation.getText().trim().length() == 0) {
        message.setText("Please enter your affiliation before submitting");
      }
      else if (email.getText().trim().length() == 0) {
        message.setText("Please enter your email address before submitting");
      }
      else {
        listener.actionPerformed(evt);
      }
    }
    else if (evt.getActionCommand().equals(CANCEL)) {
      listener.actionPerformed(evt);
    }
  }

  /**
   * Returns the name of the user
   *
   * @return Name of the user
   */
  public String getName() {
    return name.getText();
  }

  /**
   * Returns the user's affiliation
   *
   * @return Affiliation of the user
   */
  public String getAffiliation() {
    return affiliation.getText();
  }

  /**
   * Returns the email address of the user
   *
   * @return Email address of the user
   */
  public String getEmail() {
    return email.getText();
  }

  /**
   * Returns the interests of the user, separated by the separator
   * string passed into the constructor
   *
   * @return Interests of the user
   */
  public String getInterests() {
    StringBuffer interests = new StringBuffer();
    if (cb1.getState()) {
      interests.append(cb1.getLabel()+separator);
    }
    if (cb2.getState()) {
      interests.append(cb2.getLabel()+separator);
    }
    if (cb3.getState()) {
      interests.append(cb3.getLabel()+separator);
    }
    if (cb4.getState()) {
      interests.append(cb4.getLabel()+separator);
    }
    if (cb5.getState()) {
      interests.append(cb5.getLabel()+separator);
    }
    if (cb6.getState()) {
      interests.append(cb6.getLabel()+separator);
    }
    if (cb7.getState()) {
      interests.append(cb7.getLabel()+separator);
    }
    if (cb8.getState()) {
      interests.append(cb8.getLabel()+separator);
    }

    if (interest1.getText().length() != 0) {
      interests.append(interest1.getText().trim()+separator);
    }
    if (interest2.getText().length() != 0) {
      interests.append(interest2.getText().trim()+separator);
    }
    if (interest3.getText().length() != 0) {
      interests.append(interest3.getText().trim()+separator);
    }

    String result = interests.toString();
    if (result.length() != 0) {
      return result.substring(0,result.length()-separator.length());
    }
    return result;
  }

  /**
   * Returns the handheld device carried by the user
   *
   * @return handheld device carried by the user
   */
  public String getDevice() {
    String result = device.getSelectedItem();
    if (result == null) {
      return new String("None");
    } 
    if (result.equals("WinCE #1")) {
      return new String("199.77.129.187");
    }
    if (result.equals("WinCE #2")) {
      return new String("127.0.0.1");
    }
    if (result.equals("WinCE #3")) {
      return new String("127.0.0.1");
    }
    if (result.equals("WinCE #4")) {
      return new String("127.0.0.1");
    }
    if (result.equals("WinCE #5")) {
      return new String("127.0.0.1");
    }

    return result;
  }

}
