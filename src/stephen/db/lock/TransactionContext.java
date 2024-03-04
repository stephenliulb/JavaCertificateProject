/*
 * Basic Java skill show cases
 *
 * Copyright (c) 2024 Stephen Liu. All Rights Reserved. 
 *
 */
package stephen.db.lock;

/**
 * This class is used to cache changed data for one record instead of save into
 * data store during record data manipulation; It is designed to roll back
 * changed data in case any exception occurred during manipulating records in
 * data store.
 * <p>
 * To save cached record changes into data store will defer to the point when
 * the lock on this record is released; Any exception occurs before this point
 * will result in these changes are completely removed instead of save into data
 * store.
 * <p>
 * It is part of transaction environment which is built on database lock
 * mechanism.
 * 
 * @author Stephen Liu
 * @see stephen.db.DBMainImpl#lock(int)
 */
public class TransactionContext {
	private int transactionID;
	private String[] data;
	private boolean isDeleted;

	/**
	 * Create an object with specific ID.
	 * 
	 * @param transactionID
	 */
	public TransactionContext(int transactionID) {
		this.transactionID = transactionID;
		isDeleted = false;
	}

	/**
	 * Update the record cached in this object. If this record has been deleted,just
	 * skip and return.
	 * 
	 * @param newData new record data.
	 */
	public void update(String[] newData) {
		if (!isDeleted) {
			this.data = newData;
		}
	}

	/**
	 * Delete the record cached in this object.
	 */
	public void delete() {
		isDeleted = true;
	}

	/**
	 * Determine if the record cached in this object is deleted or not.
	 * 
	 * @return true if deleted otherwise false.
	 */
	public boolean isDeleted() {
		return isDeleted;
	}

	/**
	 * Get latest data cached in this object.
	 * 
	 * @return the latest data cached in this object. null will be returned if the
	 *         record is deleted.
	 */
	public String[] getData() {
		if (isDeleted) {
			return null;
		}
		return data;
	}

	/**
	 * Converted all transaction context data into a string.
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("TransactionID:");
		buffer.append(transactionID);
		buffer.append("/");
		buffer.append("isDeleted:");
		buffer.append(isDeleted);
		if (data != null) {
			buffer.append("/");
			for (String col : data) {
				buffer.append(col);
				buffer.append(",");
			}
		}

		int len = buffer.length();
		if (buffer.charAt(len - 1) == ',') {
			len -= 1;
		}
		return buffer.substring(0, len);

	}

}
