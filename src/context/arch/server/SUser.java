package context.arch.server;

import context.arch.subscriber.Callbacks;
import context.arch.service.Services;
import context.arch.storage.Attributes;
import context.arch.storage.Conditions;
import context.arch.storage.Storage;
import context.arch.widget.WidgetHandle;
import context.arch.widget.WidgetHandles;
import context.arch.util.ContextTypes;

/**
 * This class implements a user server.  It should subscribe to all
 * the widgets that could provide information about its user.
 *
 * @see Server
 */
public class SUser extends Server {

	/**
	 * Debug flag. Set to true to see debug messages.
	 */
	private static final boolean DEBUG = true;

	/**
	 * Widget version number
	 */
	public String VERSION_NUMBER = "1.0.0";

	/**
	 * Name of server
	 */
	public static final String CLASSNAME = "User";

	/**
	 * Tag to indicate user name (our key)
	 */ 
	public static final String USERNAME = ContextTypes.USERNAME;

	private String username;

	/**
	 * Constructor that creates a user server for the user name
	 * on the specified port. This server monitors the set of widgets
	 * in widgets with storage enabled.
	 *  
	 * @param name User name this server is attached to
	 * @param port Port this server is listening on
	 * @param widgets Set of widgets this server monitors
	 *
	 */
	public SUser (int port, String name, WidgetHandles widgets) {
		this(port, name, true, widgets);
	}

	/**
	 * Constructor that creates a user server for the user name
	 * on the specified port. This server monitors the set of widgets
	 * in widgets with storage functionality set to storageFlag
	 *  
	 * @param name User name this server is attached to
	 * @param port Port this server is listening on
	 * @param widgets Set of widgets this server monitors
	 * @param storageFlag Flag to indicate whether storage is enabled or not
	 *
	 */
	public SUser (int port, String name, boolean storageFlag, WidgetHandles widgets) {
		super(port, CLASSNAME+SPACER+name, storageFlag, CLASSNAME, widgets);
		username = name;
		setVersion(VERSION_NUMBER);
		startSubscriptions();
	}

	/**
	 * Constructor that creates a user server for user name
	 * on the DEFAULT_PORT. This server monitors the set of widgets
	 * in widgets with storage enabled.
	 *  
	 * @param name User name this server is attached to
	 * @param widgets Set of widgets this server monitors
	 *
	 */
	public SUser (String name, WidgetHandles widgets) {
		this(DEFAULT_PORT, name, true, widgets);
	}

	/**
	 * Constructor that creates a user server for user name
	 * on the DEFAULT_PORT. This server monitors the set of widgets
	 * in widgets with storage functionality set to storageFlag.
	 *  
	 * @param name User name this server is attached to
	 * @param widgets Set of widgets this server monitors
	 * @param storageFlag Flag to indicate whether storage is enabled or not
	 *
	 */
	public SUser (String name, boolean storageFlag, WidgetHandles widgets) {
		this(DEFAULT_PORT, name, storageFlag, widgets);
	}

	/**
	 * This abstract method is called when the widget wants to get the latest generator
	 * info.
	 * Maybe we should query our widgets here?
	 * 
	 * @return Attributes containing the latest generator information
	 * 
	 * Changes by Brian Y. Lim: return type changed to Attributes from AttributeNameValues
	 */
	protected Attributes queryGenerator() {
		return new Attributes();
	}

	/**
	 * This method sets the attributes for the server - those
	 * that are specific to the server, and not contained in the widgets
	 * it subscribes to.  Currently, there are none.
	 *
	 * @return Attributes object containing the server-specific attributes
	 */
	protected Attributes setServerAttributes() {
		return new Attributes();
	}

	/**
	 * This method set the callbacks for a server - those
	 * that are specific to the server, and not contained in the widgets
	 * it subscribes to.  Currently, there are none.
	 *
	 * @return Callbacks object containing the server-specific callbacks
	 */
	protected Callbacks setServerCallbacks() {
		return new Callbacks();
	}

	/**
	 * This method set the services for a server - those
	 * that are specific to the server, and not contained in the widgets
	 * it subscribes to.  Currently, there are none.
	 *
	 * @return Services object containing the server-specific services
	 */
	protected Services setServerServices() {
		return new Services();
	}

	/**
	 * This method sets the conditions to apply to the
	 * server's subscriptions.  The condition for the User Server is
	 * USERNAME='username'
	 *
	 * @return Conditions containing condition info for server subscriptions
	 */
	protected Conditions setConditions() {
		Conditions conds = new Conditions();
		conds.addCondition(USERNAME,Storage.EQUAL,username);
		return conds;
	}

	/**
	 * Main method to create a server with name, port and widgets specified by 
	 * command line arguments. Each widget is specified by: host, port, id
	 */
	public static void main(String argv[]) {
		if (argv.length == 1) {
			if (DEBUG) {
				System.out.println("Attempting to create a SUser on "+DEFAULT_PORT+" for " +argv[0]+" with storage enabled");
			}
			@SuppressWarnings("unused")
			SUser su = new SUser(argv[0], new WidgetHandles());
		}
		else if ((argv.length - 2) % 3 == 0) {
			WidgetHandles widgets = new WidgetHandles ();
			for (int i=0; i<(argv.length-2)/3; i++) {
				String rhost = argv[2+3*i];
				int rport = Integer.parseInt(argv[2+3*i+1]);
				String rid = argv[2+3*i+2];

				@SuppressWarnings("unused")
				WidgetHandle w = new WidgetHandle(rid, rhost, rport);
				widgets.addWidgetHandle(rid,rhost,rport);
			}

			if ((argv[1].equals("true")) || (argv[1].equals("false"))) {      		
				if (DEBUG) {
					System.out.println("Attempting to create a SUser on "+argv[1]+" for " +argv[0]+" with storage set to "+argv[1]+" with widgets "+widgets);
				}
				@SuppressWarnings("unused")
				SUser su = new SUser(argv[0], Boolean.valueOf(argv[1]).booleanValue(), widgets);
			}
			else {
				if (DEBUG) {
					System.out.println("Attempting to create a SUser on "+argv[1]+" for " +argv[0]+" with storage enabled, with widgets "+widgets);
				}
				@SuppressWarnings("unused")
				SUser su = new SUser(Integer.parseInt(argv[1]), argv[0], widgets);
			}
		}
		else if ((argv.length != 0) && ((argv.length - 3) % 3 == 0)) {
			WidgetHandles widgets = new WidgetHandles ();
			for (int i=0; i<(argv.length-3)/3; i++) {
				String rhost = argv[3+3*i];
				int rport = Integer.parseInt(argv[3+3*i+1]);
				String rid = argv[3+3*i+2];

				@SuppressWarnings("unused")
				WidgetHandle w = new WidgetHandle(rid, rhost, rport);
				widgets.addWidgetHandle(rid,rhost,rport);
			}

			if (DEBUG) {
				System.out.println("Attempting to create a SUser on "+argv[1]+" for " +argv[0]+" with storage set to "+argv[2]+" with widgets "+widgets);
			}
			@SuppressWarnings("unused")
			SUser su = new SUser(Integer.parseInt(argv[1]), argv[0], Boolean.valueOf(argv[2]).booleanValue(), widgets);
		}
		else {
			System.out.println("USAGE: java context.arch.server.SUser <username> [port] [storageFlag] {widget_host widget_port, widget_id}");
		}
	}

	@Override
	protected Attributes setServerConstantAttributes() {
		return new Attributes();
	}

}