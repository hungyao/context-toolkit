package context.apps.demos.imautostatus;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import context.arch.comm.DataObject;
import context.arch.discoverer.Discoverer;
import context.arch.enactor.EnactorComponentInfo;
import context.arch.enactor.EnactorListener;
import context.arch.enactor.EnactorParameter;
import context.arch.intelligibility.Explainer;
import context.arch.intelligibility.Explanation;
import context.arch.intelligibility.query.AltQuery;
import context.arch.intelligibility.query.Query;
import context.arch.storage.Attributes;

/**
 * Main application with GUI to display the IM Autostatus chat window.
 * @author Brian Y. Lim
 *
 */
public class AutostatusIntelligibleApplication extends AutostatusApplication implements EnactorListener, ActionListener {

	private static final long serialVersionUID = -6334725541707657993L;
	private Explainer explainer;
	private ConsoleStringPresenter presenter;
	private String outcome;

	private ChatWindow chatWindow;
	
	public AutostatusIntelligibleApplication(String userId) {
		super(userId);
		
		enactor.addListener(this);
		
		explainer = enactor.getExplainer();
		explainer.setDescriptionExplainer(new AutostatusDescriptiveExplainerDelegate());
		presenter = new ConsoleStringPresenter(enactor);
		
		chatWindow = new ChatWindow(userId, this, generator);		
	}

	@Override public void componentAdded(EnactorComponentInfo eci, Attributes paramAtts) {}
	@Override public void componentRemoved(EnactorComponentInfo eci, Attributes paramAtts) {}
	@Override public void parameterValueChanged(EnactorParameter parameter, Attributes validAtts, Object value) {}
	@Override public void serviceExecuted(EnactorComponentInfo eci, String serviceName, String functionName, Attributes input, DataObject returnDataObject) {}
	
	/**
	 * Would be called when AutostatusEnactor has its EnactorReference triggered
	 */
	@Override
	public void componentEvaluated(EnactorComponentInfo eci) {
		outcome = enactor.getOutcomeValue();
		outcome = outcome.equals("0") ? "within 1 min" : "after 1 min";
		
		//outputPane.setText(""); // clear previous messages
		
		// responsiveness prediction
		chatWindow.writeMessage("imautostatus", "I am likely to respond to you " + outcome);
	}

	/**
	 * Called when the Send button is pressed
	 */
	@Override
	public void actionPerformed(ActionEvent evt) {
		String message = chatWindow.getMessage();
		chatWindow.writeMessage("Alice", message);
		
		// intelligibility query prompt
		if (message.startsWith("im-")) { 
			String[] command = message.split(" ");
			String question = command[0].substring(3);
			String arguments = null;
			if (command.length > 1) { arguments = command[1]; }
			Query query = QueryParser.getQuery(question, arguments);
						
			if (query == null) {
				chatWindow.writeMessage("imautostatus", "Invalid command: " + message);
			}
			else if (query instanceof AltQuery && command.length != 2) {
				chatWindow.writeMessage("imautostatus", "Invalid number of arguments: " + (command.length - 1));
			}
			else {
				Explanation explanation = explainer.getExplanation(query);
				System.out.println("explanation: " + explanation);
				chatWindow.writeMessage("imautostatus", presenter.render(explanation));
			}			
		}
		
		// just a normal message, so just provide responsiveness prediction
		else {
			chatWindow.writeMessage("imautostatus", "I am likely to respond to you " + outcome);
		}
		
		// reset input field
		chatWindow.clearInput();
	}
	
	public static void main(String[] args) {
		Discoverer.start();		
		new AutostatusIntelligibleApplication("Bob");
	}

}
