/*
 * Created on Mar 19, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package context.arch.logging;

import java.util.LinkedList;

/**
 * @author Marti Motoyama
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class WorkQueue {
	private final int nThreads = 2;
	public PoolWorker[] threads;
	public LinkedList<Runnable> queue;
	private static WorkQueue workQueue;
	
	public static WorkQueue getWorkQueue()
	{
		if (workQueue == null){
			synchronized (WorkQueue.class) {
				if (workQueue == null) {
					workQueue = new WorkQueue();
					workQueue.initialize();
				}
			}
		}
		return workQueue;
	}
	
	private void initialize(){
		queue = new LinkedList<Runnable>();
		threads = new PoolWorker[nThreads];

		for (int i=0; i<nThreads; i++) {
			threads[i] = new PoolWorker();
			threads[i].start();
		}
	}

	public void execute(Runnable r) {
		synchronized(queue) {
			queue.addLast(r);
			queue.notifyAll();
		}
	}

	private class PoolWorker extends Thread {
		public void run() {
			Runnable r;

			while (true) {
				synchronized(queue) {
					while (queue.isEmpty()) {
						try{					
							queue.wait();
						}catch (InterruptedException ignored){
						}
					}
					r = queue.removeFirst();
				}
				
				// If we don't catch RuntimeException, the pool could leak threads
				try {
					r.run();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
