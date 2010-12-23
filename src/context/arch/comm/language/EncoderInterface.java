package context.arch.comm.language;

import context.arch.comm.DataObject;

/**
 * This interface specifies all the methods an EncoderInterface object must support.  This
 * allows the details of the encoding to be abstracted away.
 */
public interface EncoderInterface {

  /** 
   * Method to encode the incoming data
   *
   * @param data Data to be encoded
   * @return the encoded message
   * @exception context.arch.comm.language.EncodeException thrown when the given
   *		data can not be encoded successfully
   */
  public abstract String encodeData(DataObject data) throws EncodeException;

  /** 
   * Method to get the class being used for encoding
   *
   * @return the class used for encoding
   */
  public abstract String getClassName();

  /** 
   * Method to get the language being used for encoding
   *
   * @return the language used for encoding
   */
  public abstract String getLanguage();

}
