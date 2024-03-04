/*
 * Basic Java skill show cases
 *
 * Copyright (c) 2024 Stephen Liu. All Rights Reserved. 
 *
 */

package stephen.db.lock;

import java.util.logging.Logger;

import stephen.common.Messages;

/**
 * This class provides a lock model to support row-level lock mechanism in
 * database. Two exclusive states, occupied or unoccupied, are maintained. When
 * the lock is occupied, the status is 'occupied' and when the lock is released,
 * the status is 'unoccupied'.
 * <p>
 * The lock is time sensitive. When lock is occupied,a internal variable will
 * record the start time. It will be used for deadlock detection.
 * <p>
 * Also a transaction environment is created inside the lock object. Transaction
 * environment is used to isolate concurrent operations on same record from
 * different database connections. It is composed by the current thread
 * object(the lock's owner who is corresponding to unique database connection)
 * and <code>TransactionContext</code> object(which is used to cache changed
 * data on the record instead of save into data file).
 * <p>
 * When the lock is released, all resource occupied by the lock will be
 * released, and all threads waiting on the lock will got notified.
 * 
 * @author Stephen Liu
 * @see stephen.db.lock.TransactionContext
 * @see stephen.db.lock.TransactionContext
 * 
 */
public class StatefulLock {
	private static Logger logger = Logger.getLogger(StatefulLock.class.getName());

	private long lockStartTime = Long.MAX_VALUE;
	private Object owner;
	private TransactionContext context;

	private boolean isOccupied;

	/**
	 * Create a StatefulLock Object; it is unoccupied by default.
	 */
	public StatefulLock() {
		isOccupied = false;
	}

	/**
	 * Set occupy flag on this object
	 */
	public synchronized void occupy(Object owner, TransactionContext context) {
		this.owner = owner;
		this.context = context;
		isOccupied = true;
		lockStartTime = System.currentTimeMillis();
	}

	/**
	 * Reset all state in lock object; All changed data in
	 * <code>TransactionContext</code> will be removed if they hasn't been saved
	 * into database.
	 * <p>
	 * At same time, all waiting threads on this lock will get notified.
	 * 
	 */
	public synchronized void release() {

		logger.finest(Messages.getString("StatefulLock.lockReleased", new Object[] { this.toString() }));

		isOccupied = false;
		this.owner = null;
		this.context = null;
		lockStartTime = Long.MAX_VALUE;

		synchronized (this) {
			this.notifyAll();
		}
	}

	/**
	 * Determine if the object is set occupied or not.
	 * 
	 * @return yes if the object doesn't set occupy flag;<br>
	 *         no if the object is set occupy flag.
	 */
	public synchronized boolean isAvailable() {
		if (isOccupied) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Get the lock's owner.
	 * 
	 * @return the lock's owner.
	 */
	public synchronized Object getOwner() {
		return owner;
	}

	/**
	 * Get transaction context in the lock object.
	 * 
	 * @return the transaction context in the lock object.
	 */
	public synchronized TransactionContext getTransactionContext() {
		return context;
	}

	/**
	 * Get the start time when the lock is occupied.
	 * 
	 * @return the start time when the lock is occupied.
	 */
	public synchronized long getLockStartTime() {
		return lockStartTime;
	}

	/**
	 * Convert internal data into a string.
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(lockStartTime);
		if (lockStartTime < Long.MAX_VALUE) {
			buffer.append("(");
			buffer.append((System.currentTimeMillis() - lockStartTime) / 1000);
			buffer.append("s)");
		}
		buffer.append("/");
		buffer.append(owner);
		if (owner instanceof Thread) {
			buffer.append("(");
			buffer.append(((Thread) owner).getState());
			buffer.append(")");
		}
		buffer.append("/");
		buffer.append(isOccupied);
		buffer.append("/");
		buffer.append(context);
		return buffer.toString();
	}

}
