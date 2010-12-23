package context.apps;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import context.arch.discoverer.Discoverer;
import context.arch.enactor.Enactor;
import context.arch.enactor.EnactorException;
import context.arch.enactor.Generator;
import context.arch.widget.Widget;

/**
 * Use this class as the focal point of a context-aware application.
 * Extend it to add Widgets and Enactors to the model so that contexts
 * and logic are modeled. Then call #start() to launch the Widgets and
 * Enactors so that they automatically loads the components in their own threads.
 * 
 * @author Brian Y. Lim
 * @author Kanupriya Tavri
 *
 */
public class ContextModel {
	
	protected static Logger LOGGER = Logger.getLogger(ContextModel.class);
	static { LOGGER.setLevel(Level.INFO); }

	/** 
	 * Time delay in msec to let widgets start up before having enactors reference them. 
	 * Value is empirically derived, and may differ across applications.
	 * Make the value larger if you find the application stalling.
	 */
	protected static final long DISCOVERER_LOAD_DELAY = 3000;
	protected final long WIDGETS_LOAD_DELAY = 6000; // TODO: replace with triggers when stages are ready()
	protected final long ENACTORS_LOAD_DELAY = 12000;

	/**
	 * whether or not to start discoverer internally, or assume it is already started
	 */
	protected boolean startDiscoverer;
	
	/**
	 * Discoverer for the application. It is static as there should only be one in an environment.
	 */
	protected static Discoverer discoverer;
	protected static boolean discovererStarted;
	
	// using sets so that they don't accidentally add null
	protected Set<Widget> widgets = new HashSet<Widget>();
	protected Set<Enactor>  enactors = new HashSet<Enactor>();
		
	public ContextModel() {
		this(false);
	}
	
	/**
	 * Create a context model and also start the discoverer when this model is started.
	 * This is convenient for non-distributed applications.
	 * @param startDiscoverer
	 */
	public ContextModel(boolean startDiscoverer) {
		this.startDiscoverer = startDiscoverer;
	}
	
	/**
	 * Call this when ready to start, after adding all the widget generators as desired.
	 */
	public void start() {
		if (startDiscoverer) { startDiscoverer(); }
		
		startWidgets();
		// Enactors are called in a chain after all widgets have started
	}
	
	/**
	 * Call this to properly stop enactors and widgets, i.e. they stop communicating with the Discoverer.
	 * TODO: does not really work yet.
	 */
	public void stop() {
		// TODO
		// not well deployed 
		// also need to actually kill the threads

		// shut down enactors
		for (Enactor enactor : enactors) {
			enactor.stopXMLServer();
//			enactor. // don't know what else to shut this down
		}
		
		// shut down widgets
		for (Widget widget : widgets) {
			widget.shutdown();
		}
		
		// shutting down discoverer done separately
	}
	
	/**
	 * Convenience method to pause the thread of the caller of this method.
	 * @param delay how long to delay, in milliseconds.
	 */
	protected static void pause(long delay) {
		try { Thread.sleep(delay);
		} catch (InterruptedException e) { e.printStackTrace(); }
	}
	
	/**
	 * Add widget to the application so that it will enqueue to start when {@link #start()} is called.
	 * Do not prematurely start the widget by calling @Widget.findDiscoverer before adding.
	 * @param widget
	 */
	public void addWidget(Widget widget) {
		widgets.add(widget);
	}
	
	/**
	 * Add enactor to the application so that it will enqueue to start when {@link #start()} is called.
	 * Do not prematurely start the widget by calling @Enactor.startAll before adding.
	 * @param enactor
	 */
	public void addEnactor(Enactor enactor) {
		enactors.add(enactor);
	}
	
	/**
	 * Add generator to the application so that it will enqueue to start when {@link #start()} is called.
	 * Do not prematurely start the widget by calling @Generator.startAll before adding.
	 * @see #addGenerator(Generator)
	 * @param generator
	 */
	public void addGenerator(Generator generator) {
		/*
		 * Call #addEnactor since Generators are subclasses of enactors,
		 * and can be loaded the same way.
		 */
		addEnactor((Enactor)generator);
	}
	
	/**
	 * Use this method to start the Discoverer to this runtime.
	 * Starts the discoverer in its own thread so that it won't block any other toolkit component.
	 * This does not start the discoverer if it is already started.
	 * Not needed if context model was created to start this discoverer too.
	 * @return false if the discoverer is already started.
	 */
	public static boolean startDiscoverer() {
		return ContextModel.startDiscoverer(Discoverer.DEFAULT_PORT);
	}
	
	/**
	 * Start the discoverer at a specified port.
	 * It does not check if the port is available, and in such a case, it would fail.
	 * @param port of which to start the discoverer at.
	 * @return false if discoverer is already started
	 */
	public static boolean startDiscoverer(int port) {
		if (discovererStarted) { return false; }
		
		discoverer = new Discoverer(port, false);
		
		new Thread(Discoverer.class.getName()) {
			@Override
			public void run() {
				LOGGER.info("Discoverer starting");
				discoverer.start(null);
				LOGGER.info("Discoverer started (port = " + discoverer.getPort() + ")");
			}
		}.start();

		discovererStarted = true;
		return true;
	}
	
	/**
	 * Stops the discoverer.
	 * This should be called after stopping this model, or any other model depending on the discoverer.
	 * TODO: it does not really work.
	 * @return true if the stopping was successful
	 */
	public static boolean stopDiscoverer() {
		if (!discovererStarted) { return false; }
		
		discoverer.shutdown();

		discovererStarted = false;
		return true;
	}
	
	/**
	 * Checks if the Discoverer has already been started by this model.
	 * @return
	 */
	public static boolean isDiscovererStarted() {
		return discovererStarted;
	}
	
	/**
	 * Starts widgets, each in its own thread.
	 * This is necessary so that they don't block any other toolkit component.
	 */
	protected void startWidgets() {
		start = System.currentTimeMillis();
		countsToGo = widgets.size();

		LOGGER.info("Widgets starting");
		
		for (final Widget widget : widgets) {
			final String threadName = widget.getId();
			new Thread(threadName)  {
				@Override
				public void run() {
					widget.start(true);
					LOGGER.info(threadName + " started (port = " + widget.getPort() + ")");
					doneAndCheckAllWidgetsStarted();
				}
			}.start();
		}
	}

	/**
	 * Starts enactors, each in its own thread.
	 * This is necessary so that they don't block any other toolkit component.
	 */
	protected void startEnactors() {
		start = System.currentTimeMillis();
		countsToGo = enactors.size();		

		LOGGER.info("Enactors starting");
		pause(5000); // wait some more
		
		for (final Enactor enactor : enactors) {
			final String threadName = enactor.getId();
			new Thread(threadName)  {
				@Override
				public void run() {
					try {
						enactor.startAll();
						LOGGER.info(threadName + " started (port = " + enactor.getPort() + ")");
						doneAndCheckAllEnactorsStarted();
					} catch (EnactorException e) {
						e.printStackTrace();
					}
				}
			}.start();
		}
	}

	/*
	 * The following are to calculate loading times
	 */
	private long start, end, duration;
	/*
	 * Used to determine if all enactors and/or widgets have been started
	 */
	private int countsToGo;
	
	// no way to measure for discoverer since it is called statically
//	private void doneDiscovererStarted() {
//		// assumes start time was set
//		end = System.currentTimeMillis();
//		duration = end - start;
//		System.out.println("ContextApplication: all Discoverer started after " + duration + "msec");
//
//		// rest for widgets
//		start = System.currentTimeMillis();
//		countsToGo = widgets.size();		
//		
//		startWidgets(); // then it starts widgets
//	}
	
	/**
	 * Checks if all widgets have finished initializing
	 */
	private void doneAndCheckAllWidgetsStarted() {
		// assumes countsToGo initialized to a non-zero value
		// then decrements each time this is called (should be when one component has started)
		countsToGo--;
		
		// checks whether all done
		if (countsToGo == 0) {
			// calculate duration
			end = System.currentTimeMillis();
			duration = end - start;
			System.out.println("ContextApplication: all Widgets started after " + duration + "msec");
			
			startEnactors(); // then it starts enactors
		}
	}
	private void doneAndCheckAllEnactorsStarted() {
		countsToGo--;
		if (countsToGo == 0) {
			// calculate duration
			end = System.currentTimeMillis();
			duration = end - start;
			System.out.println("ContextApplication: all Enactors started after " + duration + "msec");
			
			if (enactorsReadyListener != null) {
				enactorsReadyListener.enactorsReady();
			}
		}
	}
	
	/**
	 * Used to listen to when the initialization of widgets, and then enactors are done, so that the context model is ready.
	 * @author Brian Y. Lim
	 */
	public interface EnactorsReadyListener {
		public void enactorsReady();
	}	
	protected EnactorsReadyListener enactorsReadyListener;
	public void setEnactorsReadyListener(EnactorsReadyListener listener) {
		this.enactorsReadyListener = listener;
	}

}
