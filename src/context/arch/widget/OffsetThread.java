package context.arch.widget;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.coach.tracing.service.ntp.NtpConnection;

/**
 * This class is responsible for retrieving and updating the offset between
 * the local clock the component is running on and a "more accurate" global
 * time clock
 */
public class OffsetThread extends Thread {
	private int delay;

	/**
	 * Empty constructor
	 */
	public OffsetThread() {
	}

	/**
	 * Constructor that updates the offset between the local and global clock
	 * at a frequency of "seconds" seconds.
	 *
	 * @param seconds Number of seconds between updates
	 */
	public OffsetThread(float seconds) {
		delay = (int) seconds * 1000;
		start();
	}

	/**
	 * The run method for the Thread.
	 */
	public void run() {
		while (true) {
			try {
				Thread.sleep(delay);
				getCurrentOffset();
			} catch (InterruptedException e){
				return;
			}
		}
	}

	/**
	 * This method contacts an network time protocol (ntp) server and
	 * determines the offset between the local and global clock
	 *
	 * @return offset in milliseconds between the local and global clock
	 */
	protected long getCurrentOffset() {
		long Offset = 0;
		String ServerName = "0.pool.ntp.org"; // URL for NTP time server

		try {
			NtpConnection ntpConnection= new NtpConnection(InetAddress.getByName(ServerName));
			Offset = ntpConnection.getInfo().offset;
			// System.out.println("offset is: "+Offset);
			return Offset;
		}
		catch (SocketException se) {
			System.out.println("OffsetThread (for NtpConnection): SocketException: "+se);
		}
		catch (UnknownHostException uhe) {
			System.out.println("OffsetThread (for NtpConnection): UnknownHostException: "+uhe);
//			uhe.printStackTrace();
		}
		catch (IOException ioe) {
			System.out.println("OffsetThread (for NtpConnection): IOException: "+ioe);
//			System.out.println("Trying again...");
//			return getCurrentOffset();
			
			/*
			 * Rather than recursively/repeatedly wait for connection,
			 * just assume no offset
			 */
		}
		return Offset;
	}
}	
