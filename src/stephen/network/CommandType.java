/*
 * Basic Java skill show cases
 *
 * Copyright (c) 2024 Stephen Liu. All Rights Reserved. 
 *
 */
package stephen.network;

/**
 * A list of Database operation commands and database connection management
 * commands. Most of database operation commands correspond to methods in database
 * interface <code>DBMain</code>.
 * <p>
 * @author Stephen Liu
 * 
 */
public enum CommandType {
    /**
     * Create a new record in data store.
     */
    CREATE,
    /**
     * Delete a specified record from data store.
     */
    DELETE,
    /**
     * Find records from data store based on a criteria.
     */
    FIND,
    /**
     * Determine if a specified record is locked or not.
     */
    ISLOCKED,
    /**
     * Lock a specified record in data store.
     */
    LOCK,
    /**
     * Read a specified record data.
     */
    READ,
    /**
     * Unlock a specified record in data store.
     */
    UNLOCK,
    /**
     * Update a specified record.
     */
    UPDATE,
    /**
     * It is an administrative command to notify database server that there is
     * no more commands to execute on current connection. After server side receives this
     * command,it can safely close current connection. It will be useful when there
     * are a batch of database commands to be executed on same connection.
     */
    CLOSE
}
