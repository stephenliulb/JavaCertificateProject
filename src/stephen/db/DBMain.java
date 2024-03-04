/*
 * Basic Java skill show cases
 *
 * Copyright (c) 2024 Stephen Liu. All Rights Reserved. 
 *
 */

package stephen.db;

import stephen.db.exception.RecordNotFoundException;

/**
 * DB API specification.
 * 
 * @author Stephen Liu
 * 
 */
public interface DBMain {

    /**
     * Reads a record from the file. Returns an array where each element is a
     * record value.
     * 
     * @param recNo
     *            record number
     * @return string array
     * @throws RecordNotFoundException
     *             if the record doesn't exist.
     */
    public String[] read(int recNo) throws RecordNotFoundException;

    /**
     * Modifies the fields of a record. The new value for field n appears in
     * data[n].
     * 
     * @param recNo
     *            record number.
     * @param data
     *            new record data.
     * @throws RecordNotFoundException
     *             if the record doesn't exist.
     */
    public void update(int recNo, String[] data) throws RecordNotFoundException;

    /**
     * Deletes a record, making the record number and associated disk storage
     * available for reuse.
     * 
     * @param recNo
     *            record number.
     * @throws RecordNotFoundException
     *             if the record doesn't exist.
     */
    public void delete(int recNo) throws RecordNotFoundException;

    /**
     * Returns an array of record numbers that match the specified criteria.
     * Field n in the database file is described by criteria[n]. A null value in
     * criteria[n] matches any field value. A non-null value in criteria[n]
     * matches any field value that begins with criteria[n]. (For example,
     * "Fred" matches "Fred" or "Freddy".)
     * 
     * @param criteria
     *            search condition.
     * @return int array of record number which match the search condition.
     * @throws RecordNotFoundException
     *             if no records match the criteria.
     */

    public int[] find(String[] criteria) throws RecordNotFoundException;

    /**
     * Creates a new record in the database (possibly reusing a deleted entry).
     * Inserts the given data, and returns the record number of the new record.
     * 
     * @param data
     *            new record data.
     * @return new record number.
     * @throws DuplicateKeyException
     *             if the primary key in data does exist in data file.
     */

    public int create(String[] data) throws DuplicateKeyException;

    /**
     * Locks a record so that it can only be updated or deleted by this client.
     * If the specified record is already locked, the current thread gives up
     * the CPU and consumes no CPU cycles until the record is unlocked.
     * 
     * @param recNo
     *            record number.
     * @throws RecordNotFoundException
     *             if the record doesn't exist.
     */
    public void lock(int recNo) throws RecordNotFoundException;

    /**
     * Releases the lock on a record.
     * 
     * @param recNo
     *            record number.
     * @throws RecordNotFoundException
     *             if the record doesn't exist.
     */
    public void unlock(int recNo) throws RecordNotFoundException;

    /**
     * Determines if a record is currently locked. Returns true if the record is
     * locked, false otherwise.
     * 
     * @param recNo
     *            record number.
     * @return true if the record is locked. otherwise, false.
     * @throws RecordNotFoundException
     *             if the record doesn't exist.
     */
    public boolean isLocked(int recNo) throws RecordNotFoundException;
}
