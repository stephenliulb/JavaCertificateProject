/*
 * Basic Java skill show cases
 *
 * Copyright (c) 2024 Stephen Liu. All Rights Reserved. 
 *
 */

package stephen.db.lock;

import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import stephen.common.Constant;
import stephen.common.Messages;

/**
 * This class manages a pool of lock objects. One lock object will correspond to
 * only one record at any time; Different records will have different lock
 * objects
 * <p>
 * When a lock object is requested for a specific record, the cached lock object
 * will be return if it is available in the pool, otherwise a new lock object
 * will be created for the record and cached into the pool.
 * <p>
 * The maximum number of cached lock objects has a top limitation controlled by
 * the constant <code>MAX_CACHED_LOCK</code>. <br>
 * When the pool is full,a spare lock object will be removed to make room for a
 * new lock object.
 * <p>
 * To prevent deadlock, a background thread will periodically check the occupied
 * lock status. if occupied lock is expired based on the parameter value<code>
 * Constant.LOCK_EXPIRED_PERIOD</code>, the lock will be released and all
 * changed data in transaction context will be removed instead of save into data
 * store; at same time, all threads waiting for the lock will be notified.
 * 
 * @author Stephen Liu
 * @see stephen.common.Constant
 * 
 */
public class LockManager {
	private static Logger logger = Logger.getLogger(LockManager.class.getName());

	private final ConcurrentHashMap<Integer, StatefulLock> locks = new ConcurrentHashMap<Integer, StatefulLock>();

	private Thread lockEvictingDetector;

	private static volatile LockManager mgr;

	private LockManager() {
		lockEvictingDetector = new Thread(new LockEvictingDetector(Constant.LOCK_EXPIRED_PERIOD));
		lockEvictingDetector.start();
	}

	public static LockManager getInstance() {
		if (mgr == null) {
			synchronized (LockManager.class) {
				if (mgr == null) {
					mgr = new LockManager();
				}
			}

		}

		return mgr;
	}

	/**
	 * Get the lock object for a specified record number. If no lock object exists
	 * for the specific record number, a new lock object will be created for it and
	 * cached into the pool.
	 * 
	 * @param recNo record number
	 * @return lock object corresponding to the record number.
	 */
	public synchronized StatefulLock getLock(int recNo) {
		StatefulLock obj = locks.get(recNo);

		if (obj == null) {

			// maintain the pool to prevent from the unlimited increase.
			if (locks.size() >= Constant.MAX_CACHED_LOCK) {

				logger.log(Level.FINE, Messages.getString("LockManager.cacheFull",
						new Object[] { locks.size(), Constant.MAX_CACHED_LOCK }));
				// look for one spare lock object
				int idleKey = -1;
				for (Integer key : locks.keySet()) {
					StatefulLock value = locks.get(key);
					if (value.isAvailable()) {
						idleKey = key;
						break;
					}
				}

				// clean up the spare lock object
				if (idleKey >= 0) {
					locks.remove(idleKey);
				} else {
					String errMsg = Messages.getString("LockManager.noSpareLock");
					logger.severe(errMsg);
					throw new RuntimeException(errMsg);
				}
			}

			obj = new StatefulLock();
			locks.put(recNo, obj);
		}
		return obj;
	}

	/**
	 * Release all locks belong to one specific owner. The use case is that when
	 * database connection is closed, all the transactions on this connection should
	 * be rolled back and all resource should be released.
	 * 
	 * @param owner the owner.
	 */
	public synchronized void releaseLocks(Object owner) {
		for (int recNo : locks.keySet()) {
			StatefulLock lock = locks.get(recNo);

			if (lock.isAvailable()) {
				continue;
			}

			if (lock.getOwner().equals(owner)) {
				lock.release();

				logger.finer(Messages.getString("LockManager.lockReleasedForOwner",
						new Object[] { lock.getOwner(), recNo }));

			}
		}
	}

	/**
	 * Determine if the lock object for the specific record number exists or not.
	 * 
	 * @param recNo record number
	 * @return true if the corresponding lock object has been created and is
	 *         existing in the pool;<br>
	 *         false if the corresponding lock object is not existing in the pool.
	 */
	public synchronized boolean isLockExist(int recNo) {
		StatefulLock obj = locks.get(recNo);

		if (obj == null) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Output all locks' status in cached lock pool.
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		for (int recNo : locks.keySet()) {
			StatefulLock lock = locks.get(recNo);
			buffer.append(lock.toString());
			buffer.append("\n");
		}
		return buffer.toString();
	}

	private class LockEvictingDetector implements Runnable {
		private long lockExpiredPeriod;

		public LockEvictingDetector(long lockExpiredPeriod) {
			this.lockExpiredPeriod = lockExpiredPeriod;
		}

		public void run() {

			while (true) {

				for (int recNo : locks.keySet()) {

					StatefulLock lock = locks.get(recNo);

					if (lock.isAvailable()) {
						continue;
					}

					if ((System.currentTimeMillis() - lock.getLockStartTime()) >= lockExpiredPeriod) {

						logger.finer(
								Messages.getString("LockManager.lockExpired", new Object[] { lock.getOwner(), recNo }));

						lock.release();

					}

				}

				// print out locks status
				logger.finer(
						Messages.getString("LockManager.locksStatus", new Object[] { LockManager.this.toString() }));

				try {
					synchronized (this) {
						this.wait(lockExpiredPeriod);
					}
				} catch (InterruptedException e) {
					//
				}

				logger.finer(Messages.getString("LockManager.deadLockDetectorWakeup"));
			}

		}
	}

}
