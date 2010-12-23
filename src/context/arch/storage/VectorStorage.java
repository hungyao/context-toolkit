package context.arch.storage;

//import java.util.Vector;
//import java.util.Hashtable;
//import java.util.Enumeration;
//import java.util.Date;
//import java.math.BigInteger;
//import java.sql.DriverManager;
//import java.sql.Connection;
//import java.sql.Statement;
//import java.sql.ResultSet;
//import java.sql.SQLException;

/**
 * This class allows storage and retrieval of data in String, Integer, Long, Float,
 * Double, or Short format. It implements the Storage interface, using a Vector to
 * store data temporarily. It can flush locally stored data to persistent data upon 
 * request.
 * 
 * TODO: database design needs to be normalized to be more scalable
 * TODO: actually needs a severe overhaul --Brian
 * 
 * @author Anind Dey
 * @author Brian Y. Lim
 */
public class VectorStorage 
//implements Storage 
{

////	private Hashtable<String, String> attributeTypes;
//	private Attributes attributes;
//	@SuppressWarnings("unused")
//	private String storageClass = "";
//	private long lastFlush = 0;
//	private long numStored = 0;
//	private int flushType;
//	private long flushCondition;
//	private String table;
//	private boolean firstTime = true;
//	private Vector<Attributes> data;
//
//	/**
//	 * Debug flag. Set to true to see debug messages.
//	 */
//	private static final boolean DEBUG = false;
//
//	/**
//	 * Default flush type is by number of stores
//	 */
//	public static final int DEFAULT_FLUSH_TYPE = DATA;
//
//	/**
//	 * Default flush condition is 2 (i.e. flush after 2 stores)
//	 */
//	public static final long DEFAULT_FLUSH_CONDITION = 2;
//
//	/**
//	 * Separator used in structured info
//	 */
//	public static final char OLD_SEPARATOR = Attributes.SEPARATOR;
//
//	/**
//	 * Separator used in structured info - String
//	 */
//	public static final String OLD_SEPARATOR_STRING = new Character(Attributes.SEPARATOR).toString();
//
//	/**
//	 * Separator used by database in structured info
//	 */
//	public static final char NEW_SEPARATOR = '_';
//
//	/**
//	 * Separator used by database in structured info - String
//	 */
//	public static final String NEW_SEPARATOR_STRING = new Character(NEW_SEPARATOR).toString();
//
//	/**
//	 * Basic constructor that uses the default flush condition
//	 *
//	 * @param table Name of table to use
//	 * @exception SQLException if errors in accessing table info 
//	 */
//	public VectorStorage(String table) throws SQLException {
//		this(table, new Integer(DEFAULT_FLUSH_TYPE),new Long(DEFAULT_FLUSH_CONDITION));
//	}
//
//	/**
//	 * Basic constructor that uses the given flush type and condition
//	 *
//	 * @param table Name of table to use
//	 * @param flushType Flush to database based on TIME or DATA
//	 * @param flushCondition Condition to flush local storage to database
//	 * @exception SQLException if errors in accessing table info 
//	 */
//	public VectorStorage(String tableName, Integer flushType, Long flushCondition) throws SQLException {
//		try {
//			Class.forName("gwe.sql.gweMysqlDriver");
//		} catch (ClassNotFoundException cnfe) {
//			System.out.println("VectorStorage constructor ClassNotFound: "+cnfe);
//		}
//		table = tableName.replace(' ','_');
//		data = new Vector<Attributes>();
//
//		this.flushType = flushType.intValue();
//		this.flushCondition = flushCondition.longValue();
//		if (this.flushType == TIME) {
//			lastFlush = new Date().getTime();
//		}
//	}
//
//	/**
//	 * This method stores the given AttributeNameValues object
//	 *
//	 * @param atts AttributeNameValues to store
//	 */
//	public void store(Attributes atts) {
//		data.addElement(atts);
//		numStored++;
//	}
//
//	/**
//	 * This method returns a Vector containing AttributeNameValue objects that match
//	 * the given conditions in the Retrieval object. It takes in the accessorId of the
//	 * "user" requesting the information, but does nothing with it currently.
//	 * 
//	 * @param accessorId Id of the "user" trying to retrieve the data
//	 * @param retrieval Retrievals object containing conditions for data retrieval
//	 * @return RetrievalResults containing AttributeNameValues objects that match the given conditions
//	 */
//	public RetrievalResults retrieveAttributes(String accessorId, Retrieval retrieval) {
//		return retrieveAttributes (retrieval);
//	}
//
//	/**
//	 * This method returns a Vector containing AttributeNameValue objects that match
//	 * the given conditions in the Retrieval object.
//	 * 
//	 * @param retrieval Retrievals object containing conditions for data retrieval
//	 * @return RetrievalResults containing AttributeNameValues objects that match the given conditions
//	 */
//	public RetrievalResults retrieveAttributes(Retrieval retrieval) {
//		flushStorage();
//		StringBuffer statement = new StringBuffer("SELECT ");
//		AttributeFunctions atts = retrieval.getAttributeFunctions();
//		if (atts.isEmpty()) { return null; }
//		
//		AttributeFunctions newAtts = new AttributeFunctions();
//		
//		// for each attribute, fix name for database use and check if struct data
//		for (AttributeFunction<?> att : atts.values()) {
//			String name = att.getName().replace(OLD_SEPARATOR,NEW_SEPARATOR);
//			Attribute<?> attr = attributes.get(name);
//			
//			// if struct data, flatten struct data and put into newAtts
//			if (attr.hasSubAttributes()) {
//				Attributes flatAtts = attr.getSubAttributes();
//				for (Attribute<?> subAtt : flatAtts.values()) {
//					if (!subAtt.hasSubAttributes()) {
//						newAtts.add(AttributeFunction.instance(
//								name.replace(OLD_SEPARATOR,NEW_SEPARATOR),
//								AttributeFunction.DEFAULT_TYPE,
//								AttributeFunction.FUNCTION_NONE));
//					}
//				}
//			}
//			// els if not struct data, put attribute into newAtts
//			else {
//				newAtts.add(AttributeFunction.instance(
//						name,
//						AttributeFunction.DEFAULT_TYPE,
//						att.getFunction()));
//			}
//		}
//
//		boolean special = false;
//		AttributeFunctions specialAtts = new AttributeFunctions();
//		for (AttributeFunction<?> af : newAtts.values()) {
//			if ((af.getFunction().equals(AttributeFunction.FUNCTION_MAX)) || (af.getFunction().equals(AttributeFunction.FUNCTION_MIN))) {
//				if (!special) {
//					specialAtts.add(AttributeFunction.instance(
//							af.getName(),
//							af.getFunction()));
//					statement.append(af.getFunction()+"("+af.getName()+")");
//					special = true;
//				}
//			}
//		}
//
//		if (special) {
//			statement.append(" FROM " + table);
//
//			@SuppressWarnings("unused")
//			boolean where = false;
//			Conditions conditions = retrieval.getConditions();
//			for (int i=0; i<conditions.numConditions(); i++) {
//				if (i == 0) { 
//					statement.append(" WHERE ");
//					where = true;
//				}
//				else {
//					statement.append(" AND ");
//				}
//				Condition condition = conditions.getConditionAt(i);
//				statement.append (condition.getAttribute().replace(OLD_SEPARATOR,NEW_SEPARATOR));
//				switch (condition.getCompare()) {
//				case LESSTHAN:         statement.append("<");  break;
//				case LESSTHANEQUAL:    statement.append("<="); break;
//				case GREATERTHAN:      statement.append(">");  break;
//				case GREATERTHANEQUAL: statement.append(">="); break;
//				case EQUAL:            statement.append("=");  break;
//				}
//				String attributeName = condition.getAttribute().replace(OLD_SEPARATOR, NEW_SEPARATOR);
//				if (attributes.get(attributeName).isType(String.class)) {
//					statement.append("'" + condition.getValue().toString() + "'");
//				}
//				else {
//					statement.append(condition.getValue().toString());
//				}   
//			}
//
//			statement.append("\n");
//
//			Vector<Hashtable<String, AttributeNameValue<?>>> preResults = executeRetrieveQuery(specialAtts, statement.toString());
//
//			if (preResults == null) {
//				return null;
//			}
//
//			RetrievalResults results = new RetrievalResults();
//			AttributeFunctions attNames = specialAtts;
//			for (int j=0; j<preResults.size(); j++) {
//				Hashtable<String, AttributeNameValue> resultAtts = preResults.elementAt(j);
//				Attributes newAttValues = new Attributes();
//				for (int k=0; k<attNames.numAttributeFunctions(); k++) {
//					String attName = attNames.getAttributeFunctionAt(k).getName();
//					AttributeNameValue<?> newAttValue = resultAtts.get(attName);
//					if (newAttValue.getValue() != null) {
//						newAttValues.addAttributeNameValue(newAttValue);
//					}
//				}
//				if (!newAttValues.isEmpty()) {
//					results.addAttributes(newAttValues);
//				}
//			}
//			if (results.numAttributeNameValues() != 0) {
//				Attributes anvs = results.getAttributesAt(0);
//				Attribute<?> anv = anvs.getAttributeAt(0);
//				if (anv instanceof AttributeNameValue<?>) {
//					conditions.addCondition(anv.getName(),EQUAL,((AttributeNameValue)anv).getValue());
//				}
//			}
//			else {
//				return null;
//			}
//			statement = new StringBuffer("SELECT ");
//		}
//
//		AttributeFunction af = newAtts.getAttributeFunctionAt(0);
//		String func = af.getFunction();
//		if (func == null) {
//			statement.append(af.getName());
//		}
//		else {
//			if ((func.equals(AttributeFunction.FUNCTION_NONE)) || 
//					(func.equals(AttributeFunction.FUNCTION_MAX)) || 
//					(func.equals(AttributeFunction.FUNCTION_MIN))) {
//				statement.append(af.getName());
//			}
//			else {
//				statement.append(func+"("+af.getName()+")");
//			}
//		}
//		for (int i=1; i<newAtts.numAttributeFunctions(); i++) {
//			af = newAtts.getAttributeFunctionAt(i);
//			func = af.getFunction();
//			if (func == null) {
//				statement.append(","+af.getName());
//			}
//			if ((func.equals(AttributeFunction.FUNCTION_NONE)) || 
//					(func.equals(AttributeFunction.FUNCTION_MAX)) || 
//					(func.equals(AttributeFunction.FUNCTION_MIN))) {
//				statement.append(","+af.getName());
//			}
//			else {
//				statement.append(","+func+"("+af.getName()+")");
//			}
//		}
//		statement.append(" FROM "+table);
//
//		@SuppressWarnings("unused")
//		boolean where = false;
//		Conditions conditions = retrieval.getConditions();
//		for (int i=0; i<conditions.numConditions(); i++) {
//			if (i == 0) { 
//				statement.append(" WHERE ");
//				where = true;
//			}
//			else {
//				statement.append(" AND ");
//			}
//			Condition condition = conditions.getConditionAt(i);
//			statement.append (condition.getAttribute().replace(OLD_SEPARATOR,NEW_SEPARATOR));
//			switch (condition.getCompare()) {
//				case LESSTHAN:         statement.append("<");  break;
//				case LESSTHANEQUAL:    statement.append("<="); break;
//				case GREATERTHAN:      statement.append(">");  break;
//				case GREATERTHANEQUAL: statement.append(">="); break;
//				case EQUAL:            statement.append("=");  break;
//			}
//			String name = condition.getAttribute().replace(OLD_SEPARATOR, NEW_SEPARATOR);
//			if (attributes.get(name).isType(String.class)) {
//				statement.append("'" + condition.getValue().toString() + "'");
//			}
//			else {
//				statement.append(condition.getValue().toString());
//			}   
//		}
//
//		statement.append("\n");
//
//		Vector<Hashtable<String, AttributeNameValue>> preResults = executeRetrieveQuery(newAtts, statement.toString());
//
//		if (preResults == null) {
//			return null;
//		}
//
//		RetrievalResults results = new RetrievalResults();
//		AttributeFunctions attNames = retrieval.getAttributeFunctions();
//		if (attNames.getAttributeFunctionAt(0).getName().equals(Attributes.ALL)) {
//			attNames = new AttributeFunctions();
//			for (Attribute<?> att : attributes.values()) {
//				attNames.addAttributeFunction(att.getName());
//			}
//		}
//		for (int j=0; j<preResults.size(); j++) {
//			Hashtable<String, AttributeNameValue> resultAtts = preResults.elementAt(j);
//			Attributes newAttValues = new Attributes();
//			for (int k=0; k<attNames.numAttributeFunctions(); k++) {
//				String attName = attNames.getAttributeFunctionAt(k).getName();
//				if (attributes.get(attName.replace(OLD_SEPARATOR, NEW_SEPARATOR)).hasSubAttributes()) {
//					AttributeNameValue<?> newAttValue = getAttributeNameValue(attName,resultAtts);
//					if (newAttValue.getValue() != null) {
//						newAttValues.add(newAttValue);
//					}
//				}
//				else {
//					AttributeNameValue<?> newAttValue = resultAtts.get(attName);
//					if (newAttValue.getValue() != null) {
//						newAttValues.add(newAttValue);
//					}
//				}
//			}
//			if (!newAttValues.isEmpty()) {
//				results.addAttributes(newAttValues);
//			}
//		}
//		return results;
//	}
//
//	/**
//	 * This method takes an attribute name (whose attribute type is STRUCT)
//	 * and returns the complete attribute information for it.
//	 *
//	 * @param name Name of the attribute
//	 * @param values Hashtable containing all the values retrieved from a line
//	 *        in the database table
//	 * @return AttributeNameValue containing all the STRUCT's values in a hierarchical
//	 *         format
//	 */
//	private AttributeNameValue getAttributeNameValue(String name, Hashtable<String, AttributeNameValue> values) {
//		return getAttributeNameValue(name,values,"");
//	}
//
//	/**
//	 * This method takes an attribute name (whose attribute type is STRUCT)
//	 * and returns the complete attribute information for it.
//	 *
//	 * @param name Name of the attribute
//	 * @param values Hashtable containing all the values retrieved from a line
//	 *        in the database table
//	 * @param prefix to use for structure info
//	 * @return AttributeNameValue containing all the STRUCT's values in a hierarchical
//	 *         format
//	 */
//	private AttributeNameValue<?> getAttributeNameValue(String name, Hashtable<String, AttributeNameValue> values, String prefix) {
//		prefix = prefix.trim();
//		if ((prefix.length() != 0) && !(prefix.endsWith(OLD_SEPARATOR_STRING))) {
//			prefix = prefix + OLD_SEPARATOR_STRING;
//		}
//
//		Attribute<?> att = attributes.get(prefix + name);
//		if (att.hasSubAttributes()) {
//			Attributes subAtts = att.getSubAttributes();
//			Attributes newSubAtts = new Attributes();
//			for (int i=0; i<subAtts.numAttributes(); i++) {
//				Attribute subAtt = subAtts.getAttributeAt(i);
//				newSubAtts.addAttributeNameValue(getAttributeNameValue(prefix + name + OLD_SEPARATOR_STRING + subAtt.getName(), values));
//			}
//			return new AttributeNameValue(prefix + name, newSubAtts, Attribute.STRUCT);
//		}
//		else {
//			return values.get(prefix+name);
//		}
//	}
//
//	/**
//	 * Checks condition under which local data is sent to persistent storage.
//	 */
//	public boolean checkFlushCondition() {
//		if (flushType == TIME) {
//			long tmp = new Date().getTime();
//			if (lastFlush + flushCondition <= tmp) {
//				return true;
//			}
//		}
//		else if (flushType == DATA) {
//			if (flushCondition <= numStored) {
//				if (DEBUG) {
//					System.out.println("flush is true");
//				}
//				return true;
//			}
//		}   
//		return false;
//	}
//
//	/**
//	 * Flushes local data to persistent storage
//	 */
//	public void flushStorage() {
//		if (data.size() == 0) {
//			return;
//		}
//
//		if (DEBUG) {
//			System.out.println("flushing");
//		}
//
//		try {
//			if (firstTime) {
//				firstTime = false;
//				if (!tableExists(table)) {
//					createTable();
//				}
//			} 
//
//			Connection conn = StorageObject.getConnection();
//			Statement statement = conn.createStatement();
//			
//			for (Attributes atts : data) {
//				String s = createInsertStatement(atts);
//				statement.executeUpdate(s);
//			}
//			
//			statement.close();
//			conn.close();
//
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//		resetForFlush();
//	}
//
//	/**
//	 * This private method creates and returns a SQL statement for inserting
//	 * data into a database.
//	 *
//	 * @param atts AttributeNameValues to put in the database
//	 * @return SQL statement for inserting the attributes into a database
//	 */
//	private String createInsertStatement(Attributes atts) {
//		StringBuffer statement = new StringBuffer("insert into "+ table +" (");
//		StringBuffer values = new StringBuffer(" values (");
//		
//		for (Attribute<?> attr : atts.values()) {
//			if (!(attr instanceof AttributeNameValue<?>)) {
//				continue;
//			}
//			
//			AttributeNameValue<?> att = (AttributeNameValue<?>) attr;
//
//			statement.append(att.getName());
//			if (att.isType(String.class)) {
//				values.append("'"+att.getValue()+"'");
//			}
//			else if (att.hasSubAttributes()) {
//				Attributes subAtts = att.getSubAttributes();
//				values.append(subAtts.size());
//				String subResult = createSubInsertStatement(subAtts, att.getName() + NEW_SEPARATOR);
//				int index = subResult.indexOf("()");
//				statement.append(subResult.substring(0,index));
//				values.append(subResult.substring(index + 2));
//			}
//			else {
//				values.append(att.getValue());
//			}
//
//			statement.append(",");
//			values.append(",");
//		}
//		values.append(")");
//		statement.append(")"+values.toString());
//		return statement.toString();
//	}
//
//	/**
//	 * This private method creates and returns a SQL statement for inserting
//	 * data into a database.  It uses prefix information for structures.
//	 *
//	 * @param atts AttributeNameValues to put in the database
//	 * @return SQL statement for inserting the attributes into a database
//	 */
//	private String createSubInsertStatement(Attributes atts, String prefix) {
//		String statement = "";
//		String values = "";
//		
//		for (Attribute<?> attr : atts.values()) {
//			if (!(attr instanceof AttributeNameValue<?>)) { continue; }	
//			AttributeNameValue<?> att = (AttributeNameValue<?>) attr;
//			
//			statement += "," + prefix + att.getName();
//			
//			if (att.isType(String.class)) {
//				values += ",'" + att.getValue() + "'";
//			}
//			else if (att.hasSubAttributes()) {
//				Attributes subAtts = (Attributes)att.getValue();
//				values += "," + subAtts.size();
//				
//				String subResult = createSubInsertStatement(subAtts, prefix
//						+ NEW_SEPARATOR_STRING + att.getName() + NEW_SEPARATOR);
//				int index = subResult.indexOf("()");
//				
//				statement += subResult.substring(0,index);
//				values += subResult.substring(index + 2);
//			}
//			else {
//				values += "," + att.getValue();
//			}
//		}
//		statement += "()" + values.toString();
//		return statement;
//	}
//
//	/** 
//	 * This private method resets the data for local storage.  It should be called
//	 * after data is flushed to persistent storage.
//	 */
//	private void resetForFlush() {
//		numStored = 0;
//		lastFlush = new Date().getTime();
//		data = new Vector<Attributes>();
//	}
//
//	/**
//	 * This method contacts the database with the given query and returns a Vector
//	 * of AttributeNameValues objects that match the query, if any.  If the query fails for
//	 * any reason, null is returned
//	 *
//	 * @param attnames Vector of attribute names to return
//	 * @param query SELECT query to execute on the database
//	 * @return Vector of AttributeNameValues objects that the query returns 
//	 */
//	private Vector<Hashtable<String, AttributeNameValue<?>>> executeRetrieveQuery(AttributeFunctions atts, String query) {
//		try {
//			Connection con = StorageObject.getConnection();
//			Statement stmt = con.createStatement();
//			ResultSet rs = stmt.executeQuery(query);
//
//			Vector<Hashtable<String, AttributeNameValue<?>>> v = new Vector<Hashtable<String, AttributeNameValue>>();
//			while (rs.next()) {
//				Hashtable<String, AttributeNameValue<?>> returnAtts = new Hashtable<String, AttributeNameValue>();
//				for (int i=0; i<atts.numAttributeFunctions(); i++) {
//					AttributeFunction att = atts.getAttributeFunctionAt(i);
//					String name = att.getName();
//					Class<?> type = att.getType();
//					String value = (String)rs.getString(i + 1); // TODO: how to extract native format?
//					name = name.replace(NEW_SEPARATOR, OLD_SEPARATOR);
//					returnAtts.put(name, AttributeNameValue.instance(name, value, type));
//				}
//				v.addElement(returnAtts);
//			}
//			rs.close();
//			stmt.close();
//			con.close();
//			return v;       
//		} catch(SQLException sqle) {
//			System.out.println("VectorStorage executeRetrieveQuery SQL: "+sqle);
//		}
//		return null;  
//	}
//
//	/**
//	 * Checks to see if the given table exists
//	 *
//	 * @param stmt SQL statement to use
//	 * @param tablename Name of the table to check on
//	 * @return whether the table exists or not
//	 * @throws SQLException when problems with check occur
//	 */
//	private boolean tableExists(String tablename) throws SQLException {
//		Connection con = StorageObject.getConnection();
//		Statement stmt = con.createStatement();
//
//		ResultSet rs = stmt.executeQuery("SHOW TABLES");
//		while(rs.next()) {
//			String result = rs.getString(1);
//			if (result.equals(tablename)) {
//				return true;
//			}
//		}
//		rs.close();
//		stmt.close();
//		con.close();
//		return false;
//	}
//
//	/**
//	 * This private method returns attribute type information from the database.
//	 *
//	 * @param stmt SQL statement to use
//	 * @param tablename Name of table to get type information from
//	 * @return hashtable containing type information, with attribute names as keys
//	 * @throws SQLException when problems with retrieving the type info occur
//	 */
//	@SuppressWarnings("unused")
//	private static Hashtable<String, String> getTypeInfo(String tablename) throws SQLException {
//		Hashtable<String, String> hash = new Hashtable<String, String>();
//		Connection con = StorageObject.getConnection();
//		Statement stmt = con.createStatement();
//		ResultSet rs = stmt.executeQuery("SELECT * FROM " + tablename);
//		while (rs.next()) {
//			hash.put(rs.getString(1), rs.getString(2));
//		}
//		rs.close();
//		stmt.close();
//		con.close();
//		return hash;
//	}
//
//	/**
//	 * This private method creates a database table for storing attribute values.
//	 *
//	 * @param stmt SQL statement to use
//	 * @throws SQLException when problems creating the table occur
//	 */
//	private void createTable() throws SQLException {
//		String s = "CREATE TABLE " + table + " (";
//		
//		for (Attribute<?> att : attributes.values()) {
//			s += att + " ";
//			
//			/*
//			 * Data type of attribute
//			 */
//			if (att.isType(Integer.class)) {
//				s += "INT";
//			}
//			else if (att.isType(Short.class)) {
//				s += "SMALLINT";
//			}
//			else if (att.isType(Double.class)) {
//				s += "DOUBLE";
//			}
//			else if (att.isType(Float.class)) {
//				s += "FLOAT";
//			}
//			else if (att.isType(BigInteger.class)) {
//				s += "BIGINT";
//			}
//			else if (att.isType(String.class)) {
//				s += "TEXT"; // TODO: consider using VARCHAR instead of TEXT --Brian
//			}
//			else if (att.hasSubAttributes()) {
//				s += "INT"; // for ID of struct? --Brian
//			}
//			
//			s += ", ";
//		}
//		s = s.substring(0, s.length()-2); // truncate last ", " // TODO: use String.join instead
//		s += ")";
//
//		Connection conn = StorageObject.getConnection();
//		Statement stmt = conn.createStatement();
//		stmt.executeUpdate(s);
//		stmt.close();
//		conn.close();
//	}
//
//	/**
//	 * This method sets the attributes to use for storage.  The attributes
//	 * are used to set up the columns in a database table.
//	 *
//	 * @param attributes Attributes object containing  attributes and type info
//	 * @param attTypes Flattened hashtable version of Attributes
//	 */
//	@Override
//	public void setAttributes(Attributes attributes) {
//		attributeTypes = new Hashtable<String, String>();
//		for (String name : attTypes.keySet()) {
//			attributeTypes.put(name.replace(OLD_SEPARATOR,NEW_SEPARATOR), attTypes.get(name));
//		}
//		this.attributes = attributes;
//	}

}
