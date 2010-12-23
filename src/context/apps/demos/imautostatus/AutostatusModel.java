package context.apps.demos.imautostatus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import context.apps.ContextModel;
import context.arch.comm.DataObject;
import context.arch.enactor.EnactorComponentInfo;
import context.arch.enactor.EnactorListener;
import context.arch.enactor.EnactorParameter;
import context.arch.storage.Attributes;

/**
 * Combines widgets, generators, and enactors for the IM Autostatus application.
 * @author Brian Y. Lim
 *
 */
public class AutostatusModel extends ContextModel {
	
	AutostatusWidget autostatusWidget;
	ResponsivenessWidget responsivenessWidget;

	AutostatusGenerator autostatusGenerator;
	AutostatusEnactor autostatusEnactor;

	/**
	 * 
	 * @param userId pertains to the buddy to predict responsiveness about. This allows for multiple models to be instantiated, one per buddy.
	 */
	public AutostatusModel(String userId) {
		super();
		
		/*
		 * Widgets
		 */
		autostatusWidget = new AutostatusWidget(userId);
		responsivenessWidget = new ResponsivenessWidget(userId);
		addWidget(autostatusWidget);
		addWidget(responsivenessWidget);
		
		/*
		 * Enactors
		 */
		autostatusGenerator = new AutostatusGenerator(userId);
		autostatusEnactor = new AutostatusEnactor(userId);
		addEnactor(autostatusGenerator);
		addEnactor(autostatusEnactor);
		
		start();
	}
	
	/**
	 * Provides a rudimentary UI to simulate a chat program.
	 * Reads from the command line (System.in) as message inputs to the buddy.
	 * @author Brian Y. Lim
	 *
	 */
	public static class ConsoleReader implements EnactorListener {
		
		AutostatusModel model;
		
		ConsoleReader(final AutostatusModel model) {
			this.model = model;
			
			/*
			 * Thread to continually read command prompt
			 */
			new Thread() {
				@Override
				public void run() {
					BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
					boolean running = true;
					
					// initial prompt
					System.out.print("\nLoad Scenario> ");
					
					try {
						while (running) {
							String line = in.readLine();
							
							if (line.equals("quit")) { // command to stop application
								System.exit(0);
							}
							
							int instanceIndex = Integer.parseInt(line);
							model.autostatusGenerator.loadInstance(instanceIndex);
						}
					} catch (IOException e) { e.printStackTrace(); }
				}
			}.start();
		}
		
		/**
		 * Would be called when AutostatusEnactor has its EnactorReference triggered
		 */
		@Override
		public void componentEvaluated(EnactorComponentInfo eci) {
			// get responsiveness prediction from enactor
			String outcome = model.autostatusEnactor.getOutcomeValue();
			String responsiveness = "Within 1 minute";
			if (outcome.equals("1")) { responsiveness = "After 1 minute"; }
			
			// update prompts
			System.out.println("IM-Autostatus> " + responsiveness);
			System.out.print("Load Scenario> ");
		}

		@Override public void componentAdded(EnactorComponentInfo eci, Attributes paramAtts) {}
		@Override public void componentRemoved(EnactorComponentInfo eci, Attributes paramAtts) {}
		@Override public void parameterValueChanged(EnactorParameter parameter, Attributes validAtts, Object value) {}
		@Override public void serviceExecuted(EnactorComponentInfo eci, String serviceName, String functionName, Attributes input, DataObject returnDataObject) {}
		
	}
	
	/**
	 * Main method to run to start the rudimentary chat interface to test the IM Autostatus application.
	 * Only enter a number from 1 to 6 to load an instance scenario at the Load Scenario prompt.
	 * To stop the application, type "quit" without the quotes.
	 * @param args
	 */
	public static void main(String[] args) {
		ContextModel.startDiscoverer(); // need to start discoverer
		
		final AutostatusModel model = new AutostatusModel("Bob");
		model.setEnactorsReadyListener(new EnactorsReadyListener() {			
			@Override
			public void enactorsReady() {
				ConsoleReader reader = new ConsoleReader(model);
				model.autostatusEnactor.addListener(reader);
			}
		});
	}

}
