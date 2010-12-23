package context.arch.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import context.arch.subscriber.Subscribers;

/**
 * This class is a utility class that reads in the contents of a file.
 */
public class FileUtil {

	private static BufferedReader reader = null;
	
	static {
		// check if subscription directory has been created
		File dir = new File(Subscribers.WIDGET_SUBSCRIPTIONS_DIR);
		// otherwise, create
		if (!dir.exists()) {
			dir.mkdir();
		}
	}

	/**
	 * This method reads in the file and returns the result in a string.
	 *
	 * @return String containing the contents of the file.
	 */
	public static String read(String filename) {
		try {
			File f = new File(filename);
			
			
			// check if file exists
			if (!f.exists()) { // if not
				return ""; // then nothing to read
			}
			
			reader = new BufferedReader(new FileReader(f));
			
			StringBuffer sb = new StringBuffer();
			String line = "\n";
			while (line != null) {
				line = reader.readLine();
				if (line != null) {
					sb.append(line+"\n");
				}
			}
			return sb.toString();
			
		} catch (IOException ioe) {
			ioe.printStackTrace();
			return new String();
		}
	}
	
	public static FileWriter getWriter(String filename) throws IOException {
		return getWriter(filename, false);
	}
	
	public static FileWriter getWriter(String filename, boolean append) throws IOException {
		// check if subscription directory has been created
		File dir = new File(Subscribers.WIDGET_SUBSCRIPTIONS_DIR);
		// otherwise, create
		if (!dir.exists()) {
			dir.mkdir();
		}
		
		// check if file exists if appending
		File f = new File(filename);
		if (!f.exists()) {
			f.createNewFile(); // need to create if non-existent
		}

		return new FileWriter(filename, append);
	}
	
	public static void closeWriter(Writer writer) {
		try {
			if (writer != null) { writer.close(); }
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
