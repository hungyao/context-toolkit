package context.arch.storage;

import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ResourceBundle;

import context.arch.comm.DataObject;
//import context.arch.util.Constants;

/**
 * This class allows storage and retrieval of data in String, Integer, Long, Float,
 * Double, or Short format.  It uses a default storage class 
 * (context.arch.storage.VectorStorage), but can use any given storage class that
 * implements the Storage interface.
 * 
 * This expects to connect to a database, and grabs connection configuration from widgets-storage-db.properties
 * which should be in the classpath.
 * 
 * TODO: this seems to be some delegate class; it should be renamed. What is its true relation to Storage?
 * 
 * @see context.arch.storage.Storage
 * 
 * @author Anind Dey
 * @author Brian Y. Lim
 */
public class StorageObject {

	private Attributes lastStored = null;
	private String storageClass = "";
	private Storage storage = null;

	/**
	 * Tag for debugging.
	 */
	private static final boolean DEBUG = false;

	/**
	 * Tag for retrieving data
	 */
	public static final String RETRIEVE_DATA = "retrieveData";

	/**
	 * Tag for reply to retrieve data request
	 */
	public static final String RETRIEVE_DATA_REPLY = "retrieveDataReply";

	/**
	 * The default storage class is context.arch.storage.VectorStorage
	 */
	public static final String DEFAULT_STORAGE_CLASS = VectorStorage.class.getName();

	/**
	 * Basic constructor that uses the default storage class
	 *
	 * @param table Name of the table to use - should be the id of the calling object
	 * @exception InvalidStorageException when the default storage class can't be created  
	 * @see #DEFAULT_STORAGE_CLASS
	 */
	public StorageObject(String table) throws InvalidStorageException {
		this(DEFAULT_STORAGE_CLASS, table);
	}
	
	public StorageObject() {
		// TODO
	}

	/**
	 * Constructor that sets the storage class to use and creates the 
	 * storage class.  If the class is null, the default class is used.
	 * If the table is null, an exception is thrown.
	 *
	 * @param storageClass Class to use for storage
	 * @param table Name of the table to use - should be the id of the calling object
	 * @exception InvalidStorageException thrown when there are any errors creating the 
	 *    storage class object or connecting to the persistent storage
	 * @exception InvalidStorageException when the given storage class can't be created  
	 * @see #DEFAULT_STORAGE_CLASS
	 */
	public StorageObject(String storeClass, String table) throws InvalidStorageException {
		if (storeClass == null) {
			storageClass = DEFAULT_STORAGE_CLASS;
		}
		else {
			storageClass = storeClass;
		}
		if (table == null) {
			throw new InvalidStorageException(table);
		}

		if (DEBUG) {
			System.out.println("Storage table is: " + table + storageClass);
		}

		try {
			
//			if (System.getProperty("os.name").equals(Constants.WINCE)) { // this is an obsolete OS
//				storage = new VectorStorage(table);
//			}
//			else {
				Constructor<?> constructor = Class.forName(storageClass).getConstructor(String.class);  
				storage = (Storage) constructor.newInstance(table);
//			}
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new InvalidStorageException(storageClass);
		}
	}

	/**
	 * Constructor that sets the storage class to use
	 *
	 * @param storageClass Class to use for storage
	 * @param table Name of the table to use - should be the id of the calling object
	 * @param flushType Flush to database based on TIME or DATA
	 * @param flushCondition Condition to flush local storage to database
	 * @exception InvalidStorageException when the given storage class can't be created  
	 */
	public StorageObject(String storageClass, String table, Integer flushType, Long flushCondition) throws InvalidStorageException {
		try {

			Constructor<?> constructor = Class.forName(storageClass).getConstructor(String.class, Integer.class, Long.class);  
			storage = (Storage)constructor.newInstance(table, flushType, flushCondition);
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new InvalidStorageException(storageClass);
		}
	}
	
	/**
	 * For getting a connection from the database.
	 * The application needs the property file storage-db.properties in the classpath with properties:<pre>
	 * Driver=com.mysql.jdbc.Driver
	 * URL=jdbc:mysql://host:port/dbname
	 * Username=
	 * Password= 
	 * </pre>
	 * @return
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public static Connection getConnection() {
		ResourceBundle bundle = ResourceBundle.getBundle("widgets-storage-db");
		String driver = bundle.getString("Driver");
		String url = bundle.getString("URL");
		String username = bundle.getString("Username");
		String password = bundle.getString("Password");

		try {
			Class.forName(driver);
			return DriverManager.getConnection(url, username, password);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * This method stores the given AttributeNameValues object and checks whether the locally
	 * stored data should be flushed to persistent storage.  It is a stub method that
	 * simply calls the store(), checkFlushCondition() and flushStorage() methods in the 
	 * Storage interface.
	 *
	 * @param atts AttributeNameValues to store
	 * @see context.arch.storage.Storage#store(context.arch.storage.AttributeNameValues)
	 * @see context.arch.storage.Storage#flushStorage()
	 * @see context.arch.storage.Storage#checkFlushCondition()
	 */
	public void store(Attributes atts) {
		storage.store(atts);
		if (storage.checkFlushCondition()) {
			storage.flushStorage();
		}
		lastStored = atts;
	}

	/**
	 * This method stores the attributes in the given DataObject
	 * and checks whether the locally stored data should be flushed to persistent storage.
	 * It assumes that the DataObject has the starting tag <ATTRIBUTENAMEVALUES>.  It is 
	 * a stub method that converts the DataObject to an AttributeNameValues object and calls
	 * the store(AttributeNameValues) method in this class.
	 *
	 * @param data DataObject containing the attributes to store
	 * @see #store(context.arch.storage.AttributeNameValues)
	 */
	public void store(DataObject data) {
		store(Attributes.fromDataObject(data));
	}

	/**
	 * Returns the last AttributeNameValues object stored
	 *
	 * @return the last AttributeNameValues object stored
	 */
	public Attributes retrieveLastAttributes() {
		return lastStored;
	}

	/**
	 * Flushes the locally stored data to persistent storage
	 */
	public void flushStorage() {
		storage.flushStorage();
	}

	/**
	 * This method returns a vector containing AttributeNameValues objects that matches
	 * the given conditions in the Retrieval object  
	 * 
	 * @param retrieval Retrieval object that contains conditions for retrievalcompare Flag that dictates the type of comparison
	 * @return RetrievalResults containing AttributeNameValues objects that matches the given compare
	 *         flag and value
	 */
	public RetrievalResults retrieveAttributes(Retrieval retrieval) {
		return storage.retrieveAttributes(retrieval);
	}

	/**
	 * This method returns a vector containing AttributeNameValues objects that matches
	 * the given conditions in the Retrieval object, and that the given requestorId
	 * is allowed to have access to
	 * 
	 * @param accessorId Id of the "user" trying to retrieve data
	 * @param retrieval Retrieval object that contains conditions for retrievalcompare Flag that dictates the type of comparison
	 * @return RetrievalResults containing AttributeNameValues objects that matches the given compare
	 *         flag and value
	 */
	public RetrievalResults retrieveAttributes(String requestorId, Retrieval retrieval) {
		return storage.retrieveAttributes(requestorId, retrieval);
	}

	/**
	 * This method sets the attributes to use for storage.  
	 *
	 * @param attributes AttributeNameValues containing attribute info for this object
	 */
	public void setAttributes(Attributes attributes) {
		storage.setAttributes(attributes);
	}

}
