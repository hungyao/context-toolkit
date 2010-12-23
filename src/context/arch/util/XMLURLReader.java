package context.arch.util;

import context.arch.comm.CommunicationsObject;
import context.arch.comm.DataObject;
import context.arch.comm.language.ParserObject;
import context.arch.comm.language.InvalidDecoderException;
import context.arch.comm.language.DecodeException;
import context.arch.comm.protocol.HTTPClientSocket;
import context.arch.comm.protocol.RequestData;
import context.arch.comm.protocol.InvalidProtocolException;
import context.arch.comm.protocol.ProtocolException;
import context.arch.comm.RequestObject; //Agathe


import java.net.URL;
import java.net.MalformedURLException;
import java.io.IOException; //Agathe

/**
 * This class takes a URL that is encoded in XML and creates a
 * DataObject from it.
 *
 * @see context.arch.comm.DataObject
 */
public class XMLURLReader {

  /**
   * The default port for HTTP access
   */
  public static final int HTTP_PORT = 80;

  private String url;                          // url for the config file
  private String host, path;	                 // result of splitting the url
  private int port;
  private ParserObject p;                      // our parser
  private CommunicationsObject c;              // our comms handler
  private DataObject d;				     // parser result
  private String type = HTTPClientSocket.GET;  // the request type we use

  /**
   * This constructor accepts a URL and creates the necessary objects to
   * parse it.
   *
   * @param aUrl URL encoded in XML to parse
   * @exception MalformedURLException if the given url can't be converted
   *            to a URL object
   * @see context.arch.comm.language.ParserObject
   */
  public XMLURLReader(String aUrl) throws MalformedURLException {
  
    url = aUrl;
    p = new ParserObject ();
    
    /* Agathe: I changed the Communication so that it starts a pool
     of threads - here I modified for it starts just one thread
     */
    c = new CommunicationsObject (null, null, null,-1 , 1);
    d = null;
  	
    URL u = null;
    u = new URL (url);	// convert to URL to split it
    host = u.getHost();
    port = u.getPort();
    if (port < 0) {
      port = HTTP_PORT;
    }
    path = u.getFile ();
  }
  
  /**
   * This method returns a DataObject containing the parsed information
   * from an XML-encoded URL.
   *
   * @return DataObject containing the parsed information
   * @exception DecodeException if the URL can't be parsed correctly
   */
  public DataObject getParsedData() throws DecodeException {
    d = null;
    try {
      RequestData replydata = c.sendRequest(new RequestObject(null, path, host, port, "", type));
      //RequestData replydata = c.sendRequest("", path, host, port, type);
      d = p.decodeData(replydata.getData());
    } catch (ProtocolException pe) {
        System.out.println ("getParsedData caught an exception: "+pe);
        throw new DecodeException ("XMLURLReader getParsedData ProtocolException");
    } catch (InvalidProtocolException ipe) {
        System.out.println ("getParsedData caught an exception: "+ipe);
        throw new DecodeException ("XMLURLReader getParsedData InvalidProtocolException");
    } catch (DecodeException de) {
        System.out.println ("getParsedData caught an exception: "+de);
        throw new DecodeException ("XMLURLReader getParsedData DecodeException");
    } catch (InvalidDecoderException ide) {
        System.out.println ("getParsedData caught an exception: "+ide);
        throw new DecodeException ("XMLURLReader getParsedData InvalidDecoderException");
    } catch (IOException io){
      System.out.println("XMLURLReader caught IOException" + io);
    }
    return d;
  }

}