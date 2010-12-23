package context.arch.comm.protocol;

/**
 * This class implements a simple client socket that sends TCP packets.
 * It does nothing with the TCP packets themselves but can be subclassed
 * to do real work.
 *
 */
public class TCPClientSocket {

	/**
	 * The default port number to use is 5555
	 */
	public static final int DEFAULT_PORT = 5555;

	/**
	 * The default hostname of the remote server
	 */
	public static final String DEFAULT_SERVER = "127.0.0.1";

//	private Socket data = null;
	protected int portNumber;
	protected String remoteServer;

	/**
	 *  Default constructor for TCPClientSocket, with the default port and remote server hostname
	 *
	 * @see #DEFAULT_PORT
	 * @see #DEFAULT_SERVER
	 */
	public TCPClientSocket() {
		remoteServer = DEFAULT_SERVER;
		portNumber = DEFAULT_PORT;
	}

	/**
	 * TCPClientSocket constructor with user-specified port and remote server hostname
	 *
	 * @param port Port number to use
	 * @see #DEFAULT_SERVER
	 */
	public TCPClientSocket(int port) {
		remoteServer = DEFAULT_SERVER;
		portNumber = port;
	}

	/**
	 * TCPClientSocket constructor with user-specified port and remote server hostname
	 *
	 * @param server Hostname of remote server to connect to
	 * @param port Port number to use
	 */
	public TCPClientSocket(String server, int port) {
		remoteServer = server;
		portNumber = port;
	}

	/**
	 * Sets the remote server's hostname
	 *
	 * @param server Name of the remote server to connect to
	 */
	public void setServer(String server) {
		this.remoteServer = server;
	}

	/**
	 * Returns the remote server's hostname
	 *
	 * @return Name of the remote server to connect to
	 */
	public String getServer() {
		return remoteServer;
	}

	/**
	 * Sets the port number on the remote host to connect to
	 * 
	 * @param port Port number on the remote host to connect to
	 */
	public void setPort(int port) {
		this.portNumber = port;
	}

	/**
	 * Returns the port number on the remote host to connect to
	 * 
	 * @return Port number on the remote host to connect to
	 */
	public int getPort() {
		return portNumber;
	}
}
