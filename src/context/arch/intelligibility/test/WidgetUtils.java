package context.arch.intelligibility.test;

import java.io.IOException;
import java.net.ServerSocket;

public class WidgetUtils {
	
	public static int findFreePort() {
		try {
			ServerSocket server = new ServerSocket(0);
			int port = server.getLocalPort();
			server.close();
			return port;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}

}
