package context.apps.demos.imautostatus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import context.arch.comm.DataObject;
import context.arch.discoverer.Discoverer;
import context.arch.enactor.EnactorComponentInfo;
import context.arch.enactor.EnactorListener;
import context.arch.enactor.EnactorParameter;
import context.arch.storage.Attributes;

/**
 * IM Autostatus demo application to illustrate how to combine widgets, generators, and enactors
 * for an application that uses a decision tree classifier to predict buddy response time. 
 * Run the class as an application to start the rudimentary chat interface to test the 
 * User interaction is via the command prompt.
 * Only enter a number from 1 to 6 to load an instance scenario at the Load Scenario prompt.
 * For instructions on how to use, type "help" without the quotes.
 * To stop the application, type "quit" without the quotes.
 * Note that this is a non-intelligible application that does not support question asking
 * @author Brian Y. Lim
 *
 */
public class AutostatusApplication {
	
	protected AutostatusWidget autostatusWidget;
	protected ResponsivenessWidget responsivenessWidget;

	protected AutostatusGenerator generator;
	protected AutostatusEnactor enactor;

	/**
	 * 
	 * @param userId pertains to the buddy to predict responsiveness about. This allows for multiple models to be instantiated, one per buddy.
	 */
	public AutostatusApplication(String userId) {
		super();
		
		/*
		 * Widgets
		 */
		autostatusWidget = new AutostatusWidget(userId);
		responsivenessWidget = new ResponsivenessWidget(userId);
		
		/*
		 * Enactors
		 */
		generator = new AutostatusGenerator(userId);
		enactor = new AutostatusEnactor(userId);
	}
	
	/**
	 * Provides a rudimentary UI to simulate a chat program.
	 * Reads from the command line (System.in) as message inputs to the buddy.
	 * @author Brian Y. Lim
	 *
	 */
	public static class ConsoleReader implements EnactorListener {
		
		AutostatusApplication model;
		
		ConsoleReader(final AutostatusApplication model) {
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
							else if (line.equals("help")) {
								printUserInstructions();
							}
							else {							
								int instanceIndex = Integer.parseInt(line);
								model.generator.loadInstance(instanceIndex);
							}
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
			String outcome = model.enactor.getOutcomeValue();
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
	
	private static void printUserInstructions() {
		System.out.println("/*-------------------------*");
		System.out.println(" * IM Autostatus Simulator *");
		System.out.println(" *-------------------------*/");
		System.out.println("Only enter a number from 1 to 6 to load an instance scenario at the Load Scenario prompt.");
		System.out.println("To stop the application, type 'quit' without the quotes.");
	}
	
	/**
	 * Main method to run to start the rudimentary chat interface to test the IM Autostatus application.
	 * User interaction is via the command prompt.
	 * Only enter a number from 1 to 6 to load an instance scenario at the Load Scenario prompt.
	 * For instructions on how to use, type "help" without the quotes.
	 * To stop the application, type "quit" without the quotes.
	 * Note that this is a non-intelligible application that does not support question asking
	 * and explanation generation.
	 * @param args
	 */
	public static void main(String[] args) {		
		Discoverer.start(); // need to start discoverer
				
		AutostatusApplication model = new AutostatusApplication("Bob");
		ConsoleReader reader = new ConsoleReader(model);		
		model.enactor.addListener(reader);
	}

}
