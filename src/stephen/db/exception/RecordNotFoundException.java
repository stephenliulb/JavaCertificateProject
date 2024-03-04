/*
 * Basic Java skill show cases
 *
 * Copyright (c) 2024 Stephen Liu. All Rights Reserved. 
 *
 */

package stephen.db.exception;

/**
 * Thrown when a specified record doesn't exit or is marked as deleted in the
 * database file.
 * 
 * @author Stephen Liu
 * 
 */
public class RecordNotFoundException extends Exception {
    static final long serialVersionUID = 1L; 

    /**
     *Allocates a new Exception object.
     */
    public RecordNotFoundException() {
	super();
	return;
    }

    /**
     * Allocates a new Exception object with specific exception message.
     * 
     * @param message
     *            exception's description
     */
    public RecordNotFoundException(String message) {
	super(message);
	return;
    }
}
