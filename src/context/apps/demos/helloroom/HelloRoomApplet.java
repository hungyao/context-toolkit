package context.apps.demos.helloroom;

import javax.swing.JApplet;

import context.apps.ContextModel;

/**
 * This is the applet version of the Hello World tutorial application.
 * However, packaging this for deploying on the web would require the jar file to be too big (>15mb).
 * @author Brian Y. Lim
 *
 */
public class HelloRoomApplet extends JApplet {

	private static final long serialVersionUID = -6693741131988858384L;
	
	@Override
	public void init() {
		ContextModel.startDiscoverer();
		
		HelloRoom app = new HelloRoom();
		app.start();
		
		add(app.ui);
	}

}
