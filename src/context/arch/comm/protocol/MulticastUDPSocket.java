package context.arch.comm.protocol;

import java.net.MulticastSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.IOException;

/**
 * This class implements a threaded server - client socket that accepts UDP
 * multicast packets.
 * It does nothing with the UDP packets themselves but can be subclassed
 * to do real work.  Whenever it receives data, it clones itself.  The original
 * class listens for more new incoming packets while the clone handles the received
 * data.
 *
 * - Information about TTL (Time To Live) -
 * You can specify the TTL for multicast packet. The TTL values are :
 * 0 : transmitter
 * 1 : local network
 * 16 : site
 * 32 : county
 * 48 : country
 * 64 : continent
 * 128 : world
 *
 * @see java.lang.Runnable
 * @see java.lang.Cloneable
 */
public class MulticastUDPSocket implements Runnable, Cloneable, MulticastConstants {
	
	private static final Logger LOGGER = Logger.getLogger(MulticastUDPSocket.class.getName());
	static {LOGGER.setLevel(Level.WARNING);}

	/**
	 * Debug flag. Set to true to see debug messages.
	 */
	public static boolean DEBUG = false;

	/**
	 * The TTL value
	 */
	//private static byte ttl; // why would it have been static?
	private int ttl;

	/**
	 * Multicast socket
	 */
	private MulticastSocket serverSocket;
	private Thread runner = null;
	/**
	 * Data received
	 */
	private DatagramPacket data = null;

	/**
	 * Number of the used multicast port
	 */
	private int portNumber = MulticastConstants.DEFAULT_MULTICAST_PORT;

	/**
	 * Address of the used multicast address
	 */
	private String groupAddress = MulticastConstants.DEFAULT_MULTICAST_ADDRESS;

	/**
	 * Group
	 */
	private InetAddress group;
	@SuppressWarnings("unused")
	private MulticastUDPSocket parentSocket = null; // what was this for??

	/**
	 *  Default constructor for MulticastUDPSocket, with the default port.
	 *  The multicast address and port must be changed in the MulticastConstants class
	 * @see ...
	 */
	public MulticastUDPSocket() {
		this(TTL_SITE);
	}

	public MulticastUDPSocket(int ttlValue){
		this.ttl = ttlValue;
	}

	/**
	 * Starts a ServerSocket and a thread with this MulticastUDPSocket 
	 * as the Runnable.
	 */
	public void start() {
		if (runner == null) {
			try {
				LOGGER.info("Multicast service : port " + portNumber + " group "+ groupAddress );
				group = InetAddress.getByName(groupAddress);
				serverSocket = new MulticastSocket(portNumber);
				//at least under Windows with multiple network interfaces, the multicast socket
				//sometimes gets confused and doesn't receive packets. Explicitly binding
				//to the network interface of InetAddress.getLocalHost seems to resolve the problem
				serverSocket.setInterface(InetAddress.getLocalHost());

				// Joins the group
				serverSocket.joinGroup(group);
				//serverSocket.setTTL(ttl); // Deprecated
				serverSocket.setTimeToLive(ttl);

				runner = new Thread(this);
				runner.start();

			} catch (IOException ioe) {
				System.out.println("MulticastUDPSocket init error: "+ioe
						+ " on port number "+portNumber
						+ " with group address " + groupAddress);
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
				//Leave the group
				serverSocket.leaveGroup(group);
				serverSocket.close();
				serverSocket = null;


				try {
					Thread.sleep(5000l);
				}
				catch (InterruptedException ie) {
					System.out.println("" + ie);
				}


			}
			catch (IOException ioe){
				System.out.println("MulticastUDPSocket run IOexception: " +ioe);
			}
		}
		else {
			System.out.println("stopServer : in a child thread");
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
			boolean condition = true;
			while (condition) {
				try {

					byte buf [] = new byte[1000];
					DatagramPacket dataUDP = new DatagramPacket(buf, buf.length);

					// Wait until data are received
					serverSocket.receive(dataUDP);

					MulticastUDPSocket newSocket = (MulticastUDPSocket) clone();
					newSocket.serverSocket = null;
					newSocket.data = dataUDP;
					newSocket.ttl = ttl; // copy this, since it is no longer class static
					newSocket.parentSocket = this; // what was this for??
					newSocket.runner = new Thread(newSocket);
					newSocket.runner.start();       
				} 
				catch (IOException ioe) {
					if (serverSocket == null){
						System.out.println("IOException in the main serverSocket : "
								+ " serverSocket is null");
						condition = false;
					}
					else {
						System.out.println("MulticastUDPSocket run IOexception: "
								+ioe);
					}
				}
				catch (CloneNotSupportedException cnse) {
					System.out.println("MulticastUDPSocket run CloningException: "
							+cnse);
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
	public void handleIncomingRequest(DatagramPacket data) {
		if (DEBUG) {
			System.out.println("in MulticastUDPSocket handleIncomingRequest(data)");
		}

		/*// temp
String info="";
info = info + " getAddress " + data.getAddress() + " length "+data.getLength();
info = info + " toString "+ data.toString();
//System.out.println("a recu info " + info);
String resp = new String(data.getData());
resp = resp.trim();
System.out.println("reponse = " + resp+"/");
System.out.println("\nlength=" + resp.length());

if (resp.equalsIgnoreCase("end")){
  System.out.println("veut arreter... fait arreter le pere");
parentSocket.stopServer();
}
// end temp */
	}

	public void sendPacket(String msg){
		//if(msg.equals("") || msg==null)
		//  msg = "empty message";
		//System.out.println("msg = "+msg);
		DatagramPacket dp = new DatagramPacket(msg.getBytes(), msg.getBytes().length, group, portNumber);
		//System.out.println("ok dp " +dp.toString() + "port " +this.portNumber+ " group " + this.groupAddress);

		try {
			if (serverSocket!= null){
				//System.out.println("current TTL : " + serverSocket.getTimeToLive ());
				serverSocket.send(dp);

			}
			else
				System.out.println("serverSocket null " + serverSocket.toString());
		}
		catch (IOException ioe){
			System.out.println("in MulticastUDPSocket send" + ioe);
		}
	}

	/**
	 * Returns the group address
	 *
	 * @return Address of the multicast group
	 */
	public String getMulticastGroup() {
		return groupAddress;
	}

	/**
	 * Returns the port number of the multicast group
	 * 
	 * @return Port number of the multicast group
	 */
	public int getMulticastPort() {
		return portNumber;
	}

	/**
	 *
	 */
	/*
  public void setTTL (byte valueTTL) {
    this.ttl = valueTTL;
  }

  public static byte getTTL(){
    return ttl;
  }
	 */

	/**
	 * This method allows to set the TTL
	 *
	 * @param valueTTL The TTL
	 */
	public void setTTL (int ttl) {
		this.ttl = ttl;
	}

	/**
	 * This method returns the Time To Live of the multicast socket
	 *
	 * @return int The TTL
	 */
	public int getTTL(){
		return ttl;
	}


	// java context.arch.comm.protocol.MulticastUDPSocket
	/*public static void main (String arg[]){
    System.out.println("ok");
    MulticastUDPSocket mu = new MulticastUDPSocket();
    //System.out.println("ok instanciation arg" + arg[0]);

    // To test the length of packets = 576 theorically
    StringBuffer s = new StringBuffer("");
    String temp = "-abcd"; // 5 char
    int lg=0;
    for (int i=0; lg <2000 ; i++){
      s.append(temp);
      s.append(i);
      lg = s.length();
    }

    try {
        Thread.sleep(3000l);
      }
      catch (InterruptedException ie) {
        System.out.println("" + ie);
      }

      System.out.println("before starting mu");
      System.out.println("s=" + s);
      System.out.println("length = "+s.length()+"\n\n");

        try {
        Thread.sleep(3000l);
      }
      catch (InterruptedException ie) {
        System.out.println("" + ie);
      }

    mu.start();

    /*for (int k=0 ; k<arg.length ; k++ ) {
      mu.sendPacket(arg[k]);
      System.out.println("main : k = "+k);
      try {
        Thread.sleep(3000l);
      }
      catch (InterruptedException ie) {
        System.out.println("" + ie);
      }

    }

    mu.sendPacket(s.toString());
    //mu.stopServer();
  }
	 */
}
