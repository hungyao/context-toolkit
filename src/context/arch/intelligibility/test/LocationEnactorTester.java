package context.arch.intelligibility.test;

//import java.io.File;
//
//import java.io.IOException;
//import java.net.URI;
//import java.net.URISyntaxException;
//
//import context.arch.discoverer.Discoverer;
//import edu.cmu.laksa.apps.StreamGobbler;
//import edu.cmu.laksa.context.ContactLauncher;
//import edu.cmu.laksa.context.enactors.LocationEnactor;
//import edu.cmu.laksa.context.widgets.LocationMySqlGenerator;
//import edu.cmu.laksa.context.widgets.LocationSemanticGenerator;
//import edu.cmu.laksa.context.widgets.WLocation;
//import edu.cmu.laksa.context.widgets.WLocationSemantic;

/**
 * TODO: move this class to the Laksa project, or create an equivalent
 * @author Brian Y. Lim
 *
 */
public class LocationEnactorTester {
	
//	private String dirPath;
//	private File dir;
//	private String classpath = System.getProperty("java.class.path");
//	private String runtimePre;
//	
//	private String contactId;
//	
//	private Discoverer discover;
//	
//	private WLocation locWidget;
//	private LocationMySqlGenerator locGen;
//	private WLocationSemantic locationSemanticWidget;
//	private LocationSemanticGenerator locationSemanticGen;
//	private LocationEnactor locEnactor;
//	
//	private Process discovererProcess;
//	private Process locationProcess;
//	private Process locationSemanticProcess;
//	
//	/**
//	 * 
//	 * @param dirName for Runtime.exec
//	 * @param contactId
//	 */
//	public LocationEnactorTester(String dirPath, String contactId) {
//		this.dirPath = dirPath;
//		this.contactId = contactId;
//		prepareRuntimes();
//	}
//	
//	public void start() {
//		startDiscoverer();
//		pause(5000);
//		startLocationComponents();
//	}
//	public void stop() {
//		stopDiscoverer();
//		stopLocationComponents();
//	}
//	
//	private void prepareRuntimes() {
//		dirPath = dirPath.replace("%20", " ");
//		dir = new File(dirPath);
//		runtimePre = "java -cp \"" + classpath + "\" ";		
////		runtimePre = "java -Dmyprocessname Laksa -cp \"" + classpath + "\" "; // doesn't work to rename the process		
//	}
//	private Process executeRuntimes(Class _class) {
//		return executeRuntimes(_class, false, contactId);
//	}	
//	private Process executeRuntimes(Class _class, boolean print) {
//		return executeRuntimes(_class, print, contactId);
//	}	
//	private Process executeRuntimes(Class _class, boolean print, String contactId) {
////		String runtime = runtimePre + _class.getName() + " " + contactId; // contactId as args[0]
//		String[] runtime = {"java", "-cp", classpath, _class.getName(), contactId};
//		System.out.println();
////		System.out.println(runtime);
//		try {
//			Process process = Runtime.getRuntime().exec(runtime, null, dir);
//			
//			// allow streams to flush
//			StreamGobbler s1 = new StreamGobbler(_class.getSimpleName() + " stdin", process.getInputStream(), print);
//			StreamGobbler s2 = new StreamGobbler(_class.getSimpleName() + " stderr", process.getErrorStream(), print);
//			s1.start ();
//			s2.start ();
//			
//			System.out.println(_class.getName() + " started");
//			return process;
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return null;
//	}
//	
//	public void stopRuntimes(Process process) {
//		try {
//			process.destroy();
//			System.out.println(process + " stopped");
//		} catch (NullPointerException e) {
//			e.printStackTrace();
//		}
//	}
//	
//	public static void pause(long time) {
//		try {
//			Thread.sleep(time);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//	}
//	
//	/**
//	 * Singleton pattern. Only one discoverer
//	 */
//	private void startDiscoverer() {
//		if (discovererProcess != null) { return; } 
//		// Somehow, this the infrastructure only works if the Discoverer is in another Runtime, not just a thread
////		discovererProcess = executeRuntimes(Discoverer.class, false, "");
//		discovererProcess = executeRuntimes(Discoverer.class, true, "");
//		pause(3000); // wait for a while for Discoverer to start
//	}
//	private void stopDiscoverer() {
//		stopRuntimes(discovererProcess);	
//	}
//	
//	private void startLocationComponents() {
//		locationProcess = executeRuntimes(LocationMySqlGenerator.class);
//		locationSemanticProcess = executeRuntimes(LocationSemanticGenerator.class, true);
//	}
//	private void stopLocationComponents() {
//		stopRuntimes(locationProcess);
//		stopRuntimes(locationSemanticProcess);	
//	}
//	
//	public static void main(String[] args) {
//		System.out.println(new File(".").getAbsolutePath());
//		
//		
//		String dirPath;
////		dirPath = System.getProperty("user.dir");
//		dirPath = LocationEnactorTester.class.getProtectionDomain().getCodeSource().getLocation().getPath();
//		if (dirPath.startsWith("/") && dirPath.charAt(2)==':') { // if Windows, then it will start with /<drive>:/<path>
//			dirPath = dirPath.substring(1); // so need to remove it
//		} // not sure about Mac, but can see: http://lists.apple.com/archives/java-dev/2004/Jul/msg00544.html
//    	System.out.println("dirPath = " + dirPath);
//		
//    	String contactId = "evelyn.laksa@gmail.com";
//    	System.out.println("contactId = " + contactId);
//    	
//    	LocationEnactorTester launcher = new LocationEnactorTester(dirPath, contactId);
//		
//		launcher.start();
//		try {
//			System.out.println();
//			System.out.println("Enter 'c' to stop runtimes.");
//			if (System.in.read() == 'c') {
//				launcher.stop();
//			}
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}		
//	}

}
