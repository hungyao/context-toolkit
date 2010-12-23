package context.arch.storage;

/**
 * This interface specifies all the methods a Storage object must support
 * allowing the details of the specific storage techniques used to be abstracted away.
 *
 * Note: currently, the constructors are expected to have the following
 * format:
 *        1) empty constructor (no parameters)
 *        2) constructor(Integer i, Long l) where i is the type
 *             of flushing to use (TIME/DATA) and l is either the
 *             number of milliseconds between flushes or number of
 *             stores between flushes.
 *
 * @see context.arch.storage.StorageObject
 */
public interface Storage {

  /**
   * Tag to indicate data to retrieve should be equal to that specified 
   */
  public static final int EQUAL = 0;

  /**
   * Tag to indicate data to retrieve should be less than or equal to that specified 
   */
  public static final int LESSTHANEQUAL = 1;

  /**
   * Tag to indicate data to retrieve should be less than that specified 
   */
  public static final int LESSTHAN = 2;

  /**
   * Tag to indicate data to retrieve should be greater than or equal to that specified 
   */
  public static final int GREATERTHANEQUAL = 3;

  /**
   * Tag to indicate data to retrieve should be greater than that specified 
   */
  public static final int GREATERTHAN = 4;

  /**
   * Tag to indicate flushing to persistent storage is time-based
   */
  public static final int TIME = 0;

  /**
   * Tag to indicate flushing to persistent storage is based on the amount of data stored
   */
  public static final int DATA = 1;

  /**
   * Tag to indicate that there will be no storage in this component
   */
  public static final String NO_STORAGE = "noStorage";

  /** 
   * Abstract method that stores the attributes in the AttributeNameValues object
   *
   * @param atts Attributes object containing attributes to be stored
   */
  public abstract void store(Attributes atts);

  /** 
   * Abstract method that returns a vector containing AttributeNameValue objects that 
   * match the given retrieval conditions.  
   * 
   * @param retrieval Retrieval object containing retrieval conditions
   * @return RetrievalResults containing AttributeNameValue objects that match the given retrieval conditions
   */
  public abstract RetrievalResults retrieveAttributes(Retrieval retrieval);

  /** 
   * Abstract method that returns a vector containing AttributeNameValue objects that 
   * match the given retrieval conditions, and that are allowed to be given to the object
   * with the given accessorId
   * 
   * @param accessorId Id of the "user" wanting to retrieve data
   * @param retrieval Retrieval object containing retrieval conditions
   * @return RetrievalResults containing AttributeNameValue objects that match the given retrieval conditions
   */
  public abstract RetrievalResults retrieveAttributes(String accessorId, Retrieval retrieval);

  /**
   * Abstract method that checks whether the locally stored data should be flushed
   * to persistent storage.
   *
   * @return Boolean value which indicates flushing is necessary or not
   */
  public abstract boolean checkFlushCondition();

  /**
   * Abstract method that flushes the locally stored data to persistent storage
   */
  public abstract void flushStorage();

  /**
   * Abstract method that sets the attributes to use for storage
   *
   * @param attributes to use for storage
   * @param types Hashtable containing name-type pairs
   */
  public abstract void setAttributes(Attributes attributes);

}
