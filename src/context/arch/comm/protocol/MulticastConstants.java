/*
 * MulticastConstants.java
 *
 * Created on 1 avril 2001, 17:36
 */

package context.arch.comm.protocol;

/**
 *
 * @author  Agathe
 * @version 
 */
public interface MulticastConstants {
  
  /**
   * Multicast Address : between 224.0.0.0 and 239.255.255.255
   * Must be used for all components
   */
  public static final String DEFAULT_MULTICAST_ADDRESS = "239.0.0.2";
  
  /**
   * Multicast Port
   * Must be used for all components
   */
  public static final int DEFAULT_MULTICAST_PORT = 6655;
  
  /**
   * Values for the multicast packet TTL
   */
  public static final int TTL_WORLDWIDE = 128;
  public static final int TTL_CONTINENT = 64;
  public static final int TTL_COUNTRY = 48;
  public static final int TTL_COUNTY = 32;
  public static final int TTL_SITE = 16;
  public static final int TTL_LOCAL_NETWORK = 1;
  public static final int TTL_TRANSMITTER = 0;

}

