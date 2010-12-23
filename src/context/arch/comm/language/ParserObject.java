package context.arch.comm.language;

import java.io.Reader;

import context.arch.comm.DataObject;

/**
 * This class handles the encoding and decoding for the BaseObject class.
 */
public class ParserObject {

  /**
   * Debug flag. Set to true to see debug messages.
   */
  public static boolean DEBUG = false;

  /**
   * The default decoder class to use is "context.arch.comm.language.SAX_XMLDecoder".
   */
  public static final String DEFAULT_DECODER = "context.arch.comm.language.SAX_XMLDecoder";

  /**
   * The AELFRED SAX XML decoder class is "context.arch.comm.language.SAX_XMLDecoder".
   */
  public static final String AELFRED_XML_DECODER = "context.arch.comm.language.SAX_XMLDecoder";

  /**
   * The default encoder class to use is "context.arch.comm.language.XMLEncoder".
   */
  public static final String DEFAULT_ENCODER = "context.arch.comm.language.XMLEncoder";

  /**
   * The XML encoder class is "context.arch.comm.language.XMLEncoder".
   */
  public static final String XML_ENCODER = "context.arch.comm.language.XMLEncoder";

  private String encoderClass = null;
  private String decoderClass = null;

  /**
   * Basic constructor for ParserObject using the default encoder and decoder
   *
   * @see #DEFAULT_ENCODER
   * @see #DEFAULT_DECODER
   */
  public ParserObject() {
    this(DEFAULT_ENCODER,DEFAULT_DECODER);
  }

  /**
   * Constructor for ParserObject using the given encoder and default decoder
   *
   * @param encoderClass class to use for encoding
   * @see #DEFAULT_DECODER
   */
  public ParserObject(String encoderClass) {
    this(encoderClass,DEFAULT_DECODER);
  }

  /**
   * Constructor for ParserObject using the given encoder and decoder class.
   * If either parameter is null, the DEFAULT_ENCODER or DEFAULT_DECODER
   * is used.
   *
   * @param encoderClass Encoder class to use for encoding
   * @param encoderClass Decoder class to use for decoding
   * @see #DEFAULT_ENCODER
   * @see #DEFAULT_DECODER
   */
  public ParserObject(String encoderClass, String decoderClass) {
    if (encoderClass == null) {
      this.encoderClass = DEFAULT_ENCODER;
    }
    else {
      this.encoderClass = encoderClass;
    }
    if (decoderClass == null) {
      this.decoderClass = DEFAULT_DECODER;
    }
    else {
      this.decoderClass = decoderClass;
    }
  }
 
  /** 
   * This private method creates a DecoderInterface object.
   * 
   * @param className Class to instantiate decoder with
   * @return newly created DecoderInterface object
   * @exception context.arch.comm.language.InvalidDecoderException if the given decoder
   *		class can't be instantiated
   */
  private DecoderInterface createDecoder(String className) throws InvalidDecoderException {
    DecoderInterface decoder = null;
    try {
      decoder = (DecoderInterface)Class.forName(className).newInstance();
    } catch (IllegalAccessException iae) {
        System.out.println("ParserObject IllegalAccess: "+iae);
        throw new InvalidDecoderException();
    } catch (InstantiationException ie) {
        System.out.println("ParserObject Instantiation: "+ie);
        throw new InvalidDecoderException();
    } catch (ClassNotFoundException cnfe) {
        System.out.println("ParserObject ClassNotFound: "+cnfe);
        throw new InvalidDecoderException();
    }
    return decoder;
  }

  /** 
   * This private method creates a EncoderInterface object.
   * 
   * @param className Class to instantiate encoder with
   * @return newly created EncoderInterface object
   * @exception context.arch.comm.language.InvalidEncoderException if the given encoder
   *		class can't be instantiated
   */
  private EncoderInterface createEncoder(String className) throws InvalidEncoderException {
    EncoderInterface encoder = null;
    try {
      encoder = (EncoderInterface)Class.forName(className).newInstance();
    } catch (IllegalAccessException iae) {
        System.out.println("ParserObject IllegalAccess: "+iae);
        throw new InvalidEncoderException();
    } catch (InstantiationException ie) {
        System.out.println("ParserObject Instantiation: "+ie);
        throw new InvalidEncoderException();
    } catch (ClassNotFoundException cnfe) {
        System.out.println("ParserObject ClassNotFound: "+cnfe);
        throw new InvalidEncoderException();
    }
    return encoder;
  }

  /**
   * This method tries to create a DecoderInterface object and decode the given
   * message in the Reader.
   *
   * @param message Reader containing a message to be decoded
   * @return decoded message in a DataObject
   * @exception context.arch.comm.language.DecodeException if the message can't be decoded
   * @exception context.arch.comm.language.InvalidDecoderException if the decoder can't be created
   * @see context.arch.comm.language.DecoderInterface#decodeData(Reader)
   */
  public DataObject decodeData(Reader message) throws DecodeException, InvalidDecoderException {
    return decodeData(decoderClass, message);
  }

  /**
   * This method tries to create a DecoderInterface object using the given class name
   * and decode the given message in the Reader.
   *
   * @param className Class to use to instantiate a decoder
   * @param message Reader containing a message to be decoded
   * @return decoded message in a DataObject
   * @exception context.arch.comm.language.DecodeException if the message can't be decoded
   * @exception context.arch.comm.language.InvalidDecoderException if the decoder can't be created
   * @see context.arch.comm.language.DecoderInterface#decodeData(Reader)
   */
  public DataObject decodeData(String className, Reader message) throws DecodeException, InvalidDecoderException {
    DecoderInterface decoder = createDecoder(className);
    return decoder.decodeData(message);
  }

  /**
   * This method tries to create a EncoderInterface object and encode the given
   * message in the DataObject.
   *
   * @param message DataObject containing a message to be encoded
   * @return encoded message as a String
   * @exception context.arch.comm.language.EncodeException if the message can't be encoded
   * @exception context.arch.comm.language.InvalidEncoderException if the encoder can't be created
   * @see context.arch.comm.language.EncoderInterface#encodeData(DataObject)
   */
  public String encodeData(DataObject message) throws EncodeException, InvalidEncoderException {
    return encodeData(encoderClass, message);
  }

  /**
   * This method tries to create a EncoderInterface object using the given class
   * and encode the given message in the DataObject.
   *
   * @param className Class to use to instantiate a encoder
   * @param message DataObject containing a message to be encoded
   * @return encoded message as a String
   * @exception context.arch.comm.language.EncodeException if the message can't be encoded
   * @exception context.arch.comm.language.InvalidEncoderException if the encoder can't be created
   * @see context.arch.comm.language.EncoderInterface#encodeData(DataObject)
   */
  public String encodeData(String className, DataObject message) throws EncodeException, InvalidEncoderException {
    EncoderInterface encoder = createEncoder(className);
    return encoder.encodeData(message);
  }
}

