/*
 * Basic Java skill show cases
 *
 * Copyright (c) 2024 Stephen Liu. All Rights Reserved. 
 *
 */

package stephen.server;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 *  Provides a thread pool to execute the requests
 *  
 *  @author Stephen Liu
 */
public class TaskExecutor {
	public static final int corePoolSize = 5;
	public static final int maximumPoolSize = 10;
	public static final int keepAliveTime = 60;
	public static final TimeUnit unit = TimeUnit.SECONDS;

	private static volatile TaskExecutor instance;

	private ThreadPoolExecutor executor = null;

	private TaskExecutor() {
	}

	/**
	 * Get the singleton instance of TaskExecutor
	 * 
	 * @param handler  exception handler during incoming request handling
	 * @return
	 */
	public static final TaskExecutor getInstance(UncaughtExceptionHandler handler) {
		if (instance == null) {
			synchronized (TaskExecutor.class) {
				if (instance == null) {
					instance = new TaskExecutor();
					instance.init(handler);
				}
			}
		}

		return instance;
	}

	private void init(UncaughtExceptionHandler handler) {
		ArrayBlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(maximumPoolSize);
		executor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
				new ThreadFactory() {
					@Override
					public Thread newThread(Runnable r) {
						Thread t = new Thread(r);
						if (handler != null) {
							t.setUncaughtExceptionHandler(handler);
						}
						return t;
					}
				});
	}

	/**
	 * Execute the request task
	 * 
	 * @param task -  request task
	 */
	public void exec(Runnable task) {
		executor.execute(task);
	}

	
	/**
	 * Shutdown the thread pool inside executor
	 */
	public void shutdown() {
		executor.shutdown();
	}	
}
