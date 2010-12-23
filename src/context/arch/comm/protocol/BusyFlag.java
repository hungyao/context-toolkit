/*
 * BusyFlag.java
 *
 * Created on June 13, 2001, 5:30 PM
 */

package context.arch.comm.protocol;

/**
 * This object is a lock on a shared object
 *
 * @author  Agathe
 */

public class BusyFlag {
  /**
   * The owner of the lock
   */
  private Thread busyFlag = null;
  
  /**
   * The number of waiting threads
   */
  private int busycount = 0;

  /**
   * To get the lock. If the lock is busy, we wait.
   */
  public synchronized void getBusyFlag(){
    while (tryGetBusyFlag () == false) {
      try {
        wait();
      }
      catch (Exception e){
        System.out.println("BusyFlag <getBusyFlag> exception " + e);
      }
    }
  }
  
  /**
   * Try to get the lock
   *
   * @return boolean True if the lock is not busy. Otherwise false
   */
  public synchronized boolean tryGetBusyFlag(){
    if (busyFlag == null) {
      busyFlag = new Thread("flag");
      busyFlag.start ();
      busycount = 1;
      return true;
    }
    if (busyFlag.getName ().equals ("flag")){
      busycount ++;
      return true;
    }
    return false;
  }
  
  /**
   * Release the lock.
   */
  public synchronized void freeBusyFlag(){
    if (getBusyFlagOwner().getName ().equals("flag")){
      busycount --;
      if (busycount == 0){
        busyFlag = null;
        notify();
      }
    }
  }
  
  /**
   * Returns the Thread owning the lock
   *
   * @return Thread The lock's current owner
   */
  public synchronized Thread getBusyFlagOwner (){
    return busyFlag;
  }
}
