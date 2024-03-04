/*
 * Basic Java skill show cases
 *
 * Copyright (c) 2024 Stephen Liu. All Rights Reserved. 
 *
 */
package stephen.db.exception;

/**
 * Throw out if one transaction operation doesn't in a transaction environment.
 * <p>
 * Transaction environment is based on database lock mechanism. A transaction
 * environment is created after lock operation is executed on a specific record
 * within one unique database connection and destroyed after unlock operation is
 * executed on the same record within same database connection. The normal life
 * cycle of a transaction environment for a specific record within one database
 * connection is composed as the following stages:
 * <ol>
 * <li>Create<br>
 * lock operation is executed to create the transaction environment.
 * <li>Operations<br>
 * multiple database transaction operations are executed within the transaction
 * environment. In this application implementation, the transaction operations
 * refer to DBMain.update() and DBMain.delete().
 * <li>Destroy<br>
 * unlock operation is executed to flush changed data into data store and
 * release the transaction environment.
 * </ol>
 * 
 * Transaction environment is used to isolate concurrent operations on same
 * record from different database connections(different clients);
 * <p>
 * Different transaction environments are exclusive each other and all
 * operations in the same transaction environment are atomic;any exception
 * occurred(such as data file IOException,Database connection exception etc)
 * within transaction will result in data change roll back instead of save into
 * data store.
 * <p>
 * This are some scenarios about transaction:<br>
 * Client1 set up a database connection1 to execute lock operation on the
 * record1, a transaction environment named 'TE1' will be created; update
 * operation1 and update operation2 are executed in TE1.<br>
 * 
 * Client2 set up a database connection2 to execute lock operation on the
 * record1, a transaction environment named 'TE2' will be created; update
 * operation3 and update operation4 are executed in TE2.<br>
 * <br>
 * 
 * Client2 uses same database connection2 to execute lock operation on the
 * record2, a transaction environment named 'TE3' will be created; update
 * operation5 and update operation6 are executed in TE3.<br>
 * <br>
 * 
 * TE1,TE2 and TE3 are exclusive and all operations in each of them are atomic.
 * 
 * @author Stephen Liu
 * 
 */
public class TransactionException extends RuntimeException {
    static final long serialVersionUID = 1L;

    /**
     *Allocates a new Exception object.
     */
    public TransactionException() {
	super();
	return;
    }

    /**
     * Allocates a new Exception object with specific exception message.
     * 
     * @param message
     *            exception's description
     */
    public TransactionException(String message) {
	super(message);
	return;
    }

}
