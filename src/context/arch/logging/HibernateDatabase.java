/*
 * Created on Feb 16, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package context.arch.logging;

//import java.io.FileNotFoundException;
//import java.io.FileReader;
//import java.io.IOException;
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.SQLException;
//import java.util.Properties;

/**
 * @author Marti Motoyama
 *
 * This class is no longer used, since we are using the hibernate.properties file in the classpath --Brian
 */
@Deprecated
public class HibernateDatabase {
	
	/*
	 * We reference an external properties file for the DB properties 
	 * so that application sensitive configuration data is not exposed
	 * when the toolkit is distributed.
	 * Applications should each have their own Hibernate DB.
	 */
//	public static final String DRIVER = "org.postgresql.Driver";
//	public static final String URL = "jdbc:postgresql://localhost:5432/ctk_explanation";
//	public static final String USERNAME = "ctk_user";
//	public static final String PASSWORD = "";

//	private static Connection dbConnection;
//	public static Connection getDBConnection() {
//		if (dbConnection == null) {
//			try{
//				//implicitly loads the driver using the Class.forName() method
//				Class.forName(HibernateDatabase.DRIVER);
//
//				try{
//					dbConnection = DriverManager.getConnection(HibernateDatabase.URL,
//							HibernateDatabase.USERNAME, 
//							HibernateDatabase.PASSWORD);
//					dbConnection.setAutoCommit(false);
//				}catch(SQLException e){
//					System.out.println(e.toString());
//				}
//			}
//			catch(ClassNotFoundException e){
//				System.out.println("Driver loading unsuccessful");
//			}
//		}
//		return dbConnection;
//	}

	/**
	 * Not sure if this particular function should exist, as it could
	 * potentially cause problems for classes that are using this connection at
	 * the same time as another class is attempting to close it
	 */
//	public static void closeDBConnection() {
//		if (dbConnection != null) {
//			synchronized (HibernateDatabase.class) {
//				if (dbConnection != null) {
//					try {
//						dbConnection.close();
//					} catch (SQLException e) {
//						System.out.println(e.toString());
//					}
//				}
//			}
//		}
//	}

}
