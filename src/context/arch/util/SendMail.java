package context.arch.util;

import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.IOException;

/**
 * This class contacts a machine's POP port and uses it to send an email
 * message.
 */
public class SendMail {

  /**
   * Basic empty constructor
   */
  public SendMail() {
  }
	
  /**
   * This method sends an email message.
   *
   * @param sendAddress The address of the machine requesting the send
   * @param mailServer The machine to contact to use for sending the message
   * @param sender The email addres of the sending person
   * @param recipient The email address of the person the message is being sent to
   * @param subject The subject of the email message
   * @param message The actual content of the mail message to send
   */
  public void sendMail(String sendAddress, String mailServer, String sender, String recipient, String subject, String message) {
    try {   
      Socket mailsocket = new Socket(mailServer, 25);
      BufferedReader read = new BufferedReader (new InputStreamReader(mailsocket.getInputStream(), "8859_1")); // 8859_1 character encoding
      BufferedWriter write = new BufferedWriter (new OutputStreamWriter(mailsocket.getOutputStream(), "8859_1"));

      // start conversation with mailserver
      send(read, write, "HELO "+sendAddress);
      send(read, write, "MAIL FROM: <"+sender+">");
      send(read, write, "RCPT TO: " + recipient);
      send(read, write, "DATA");
      send(write, "Subject: "+subject);
      send(write, "From: Your Name <"+sender+">");
      send(write, "\n");      
			
      // send message
      send(write, message);
      send(write, ".\n");
      send(read, write, "QUIT");
      mailsocket.close();
    } catch (IOException ioe) {
        System.out.println("error: "+ioe);
    }
  }

  /**
   * This method writes data to a BufferedWriter and reads the
   * response in a BufferedReader.
   *
   * @param in BufferedReader to read the response from
   * @param out BufferedWriter to write data to
   * @param s The data to write out
   */
  public void send(BufferedReader in, BufferedWriter out, String s) {
    try {
      out.write(s + "\n");
      out.flush();
      s = in.readLine();
    } catch (IOException ioe) {
        System.out.println("error: "+ioe);
    }
  }

  /**
   * This method writes data to a BufferedWriter.
   *
   * @param out BufferedWriter to write data to
   * @param s The data to write out
   */
  public void send(BufferedWriter out, String s) {
    try {
      out.write(s + "\n");
      out.flush();
    } catch (IOException ioe) {
        System.out.println("error: "+ioe);
    }
  }

  /**
   * Simple main method that creates the SendMail class and
   * sends a mail using parameters from the command line
   */
  public static void main(String args[]) {
    SendMail mail = new SendMail();
    mail.sendMail(args[0], args[1], args[2], args[3], args[4], args[5]);
  }

} 
