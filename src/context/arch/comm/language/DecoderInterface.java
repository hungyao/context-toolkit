package context.arch.comm.language;

import java.io.Reader;
import context.arch.comm.DataObject;

/**
 * This interface specifies all the methods an DecoderInterface object must support.  This
 * allows the details of the decoding to be abstracted away.
 */
public interface DecoderInterface {

  /** 
   * Method to decode the incoming data
   *
   * @param message Message to be decoded
   * @return the decoded message in a DataObject
   * @exception context.arch.comm.language.DecodeException thrown when the given
   *		data can not be decoded successfully
   */
  public abstract DataObject decodeData(Reader message) throws DecodeException;

  /** 
   * Method to get the class being used for decoding
   *
   * @return the class used for decoding
   */
  public abstract String getClassName();

  /** 
   * Method to get the language being used for decoding
   *
   * @return the language used for decoding
   */
  public abstract String getLanguage();

}
