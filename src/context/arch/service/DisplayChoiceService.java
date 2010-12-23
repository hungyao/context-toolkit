package context.arch.service;

import context.arch.comm.DataObject;
import context.arch.comm.CommunicationsHandler;
import context.arch.storage.Attribute;
import context.arch.storage.AttributeNameValue;
import context.arch.storage.Attributes;
import context.arch.service.helper.FunctionDescription;
import context.arch.service.helper.ServiceInput;
import context.arch.service.helper.FunctionDescriptions;

import java.util.Vector;
import java.util.Hashtable;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * This class implements the display choices service.  It accepts a list
 * of choices, a question to display and a title to display, displays them in 
 * a frame to the user and collects the user's choice.
 *
 * @see context.arch.service.Service
 * @see ActionListener
 */
public class DisplayChoiceService extends Service implements ActionListener {

	/**
	 * Tag for the DISPLAY_CHOICES service
	 */
	public static final String DISPLAY_CHOICES = "displayChoices";

	/**
	 * Tag for the DISPLAY function
	 */
	public static final String DISPLAY = "display";

	/**
	 * Tag for TITLE of the frame
	 */
	public static final String TITLE = "title";

	/**
	 * Tag for QUESTION to be asked
	 */
	public static final String QUESTION = "question";

	/**
	 * Tag for a CHOICE
	 */
	public static final String CHOICE = "choice";

	private Hashtable<String, DisplayChoiceFrame> frameHash;

	/**
	 * Basic constructor that creates the service object.  It defines the 
	 * DISPLAY_CHOICES service and the DISPLAY function.
	 *
	 * @see Service#setFunctionDescriptions(FunctionDescriptions)
	 */
	public DisplayChoiceService(CommunicationsHandler comm) {
		super(comm);
		setName(DISPLAY_CHOICES);
		FunctionDescriptions descs = new FunctionDescriptions();
		descs.addFunctionDescription(DISPLAY,"Displays a list of choices to the user", FunctionDescription.FUNCTION_ASYNC);
		setFunctionDescriptions(descs);
		frameHash = new Hashtable<String, DisplayChoiceFrame>();
	}

	/**
	 * This method is called when a remote component requests that this
	 * service be executed.  The input the service name, function name, 
	 * frame title, question to ask the user, and list of potential choices.  
	 * It creates a frame and displays it to the user and adds the input 
	 * request to the pending queue.  It returns the status of the service request.
	 *
	 * @param serviceInput Object containing the necessary information to
	 *        execute the service
	 * @return DataObject that indicates the status of the request 
	 */
	public DataObject execute(ServiceInput serviceInput) {
		if (serviceInput.getServiceName().equals(DISPLAY_CHOICES)) {
			if (serviceInput.getFunctionName().equals(DISPLAY)) {
				Vector<String> choices = new Vector<String>();
				Attributes input = serviceInput.getInput();
				String title = null;
				String question = null;
				for (Attribute<?> a : input.values()) {
					AttributeNameValue<?> att = (AttributeNameValue<?>) a;
					
					if (att.getName().equals(CHOICE)) {
						choices.addElement(att.getValue().toString());
					}
					else if (att.getName().equals(TITLE)) {
						title = (String)att.getValue();
					}
					else if (att.getName().equals(QUESTION)) {
						question = (String)att.getValue();
					}
				}
				DisplayChoiceFrame frame = new DisplayChoiceFrame(this,choices,question,serviceInput.getUniqueId());
				if (title != null) {
					frame.setTitle(title);
				}
				else {
					frame.setTitle("Choice Frame");
				}
				frame.pack();
				frame.setResizable(true);
				frame.setVisible(true);
				frameHash.put(serviceInput.getUniqueId(), frame);
				pending.addPending(serviceInput);
				return new DataObject(STATUS,EXECUTING);
			}
		}
		return new DataObject();
	}

	/**
	 * This method is called when the user interacts with the frame that
	 * displays choices.  It gets the user choice, determines which pending
	 * request the result is for, removes the request from the pending queue
	 * returns the results to the requester, and removes the frame.
	 *
	 * @param evt ActionEvent from a user interacting with the frame
	 * @see Service#sendServiceResult(ServiceInput,AttributeNameValues)
	 */
	public void actionPerformed(ActionEvent evt) {
		String uniqueId = evt.getActionCommand();
		DisplayChoiceFrame dcf = frameHash.get(uniqueId);
		Attributes atts = new Attributes();
		atts.add(AttributeNameValue.instance(CHOICE, dcf.getChoice()));
		ServiceInput si = pending.getPending(uniqueId);
		pending.removePending(uniqueId);
		dcf.dispose();
		@SuppressWarnings("unused")
		DataObject data = sendServiceResult(si,atts); 
	}

}
