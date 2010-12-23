package context.arch.comm.protocol;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;

/**
 * This class implements a threaded serversocket that accepts TCP packets.
 * It does nothing with the TCP packets themselves but can be subclassed
 * to do real work.  Whenever it receives data, it clones itself.  The original
 * class listens for more new connections while the clone handles the received
 * data.
 *
 * @see java.lang.Runnable
 * @see java.lang.Cloneable
 */
public class TCPServerSocket implements Runnable, Cloneable {

	/**
	 * Debug flag. Set to true to see debug messages.
	 */
	public static boolean DEBUG = false;

	/**
	 * The default port number to use is 5555
	 */
	public static final int DEFAULT_PORT = 5555;

	private ServerSocket serverSocket;
	private Thread runner = null;
	private Socket data = null;
	private int portNumber;

	/**
	 *  Default constructor for TCPServerSocket, with the default port.
	 *
	 * @see #DEFAULT_PORT
	 */
	public TCPServerSocket() {
		portNumber = DEFAULT_PORT;
	}

	/**
	 * TCPServerSocket constructor with user-specified port.
	 *
	 * @param port Port number to use
	 */
	public TCPServerSocket(int port) {
		portNumber = port;
	}

	/**
	 * Starts a ServerSocket and a thread with this TCPServerSocket as the
	 * Runnable.
	 *
	 */
	public void start() {
		if (runner == null) {
			try {
				serverSocket = new ServerSocket(portNumber);
				runner = new Thread(this);
				runner.start();
			} catch (IOException ioe) {
				System.out.println("TCPServerSocket init error: "+ioe + " on port number "+portNumber);
			}
		}
	}

	/**
	 * Stops the original thread (just the original?) running and closes the socket.
	 */
	public void stopServer() {
		if (serverSocket != null) {
			runner = null;
			try {
				serverSocket.close();
			} catch (IOException ioe) {
				System.out.println("TCPServerSocket stopServer error: "+ioe);
			}
		}
	}  

	/**
	 * This method loops forever waiting for data on the socket.  When data
	 * arrives, it clones a new instance of TCPServerSocket so the new
	 * thread can deal with the data, while the current instance looks for new
	 * data.  The new thread deals with the data by calling handleIncomingRequest().
	 *
	 * @see #handleIncomingRequest(java.net.Socket)
	 */
	public void run() {
		if (serverSocket != null) {
			while (true) {
				try {
					Socket dataSocket = serverSocket.accept();
					TCPServerSocket newSocket = (TCPServerSocket) clone();
					newSocket.serverSocket = null;
					newSocket.data = dataSocket;
					newSocket.runner = new Thread(newSocket);
					newSocket.runner.start();
				} catch (IOException ioe) {
					System.out.println("TCPServerSocket run IOexception: "+ioe);
				} catch (CloneNotSupportedException cnse) {
					System.out.println("TCPServerSocket run CloningException: "+cnse);
				}           
			}
		}
		else {
			handleIncomingRequest(data);
		}
	}

	/**
	 * This method handles data received on a given TCPServerSocket.
	 * Could be abstract since the method does nothing.  A subclass is
	 * expected to override this method to handle the incoming data as
	 * necessary.
	 *
	 * @param data Socket the data is arriving on
	 */
	public void handleIncomingRequest(Socket data) {
		if (DEBUG) {
			System.out.println("in TCPServerSocket handleIncomingRequest(data)");
		}
	}

}
