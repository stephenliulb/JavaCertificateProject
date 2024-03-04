/*
 * Basic Java skill show cases
 *
 * Copyright (c) 2024 Stephen Liu. All Rights Reserved. 
 *
 */

package stephen.db.file;

/**
 * Thrown when one field name doesn't exist in data file schema.
 * 
 * @author Stephen Liu
 * 
 */
public class FieldNotExistException extends RuntimeException {
    static final long serialVersionUID = 1L;

    /**
     *Allocates a new Exception object.
     */
    public FieldNotExistException() {
	super();
	return;
    }

    /**
     * Allocates a new Exception object with specific exception message.
     * 
     * @param message
     *            exception's description
     */
    public FieldNotExistException(String message) {
	super(message);
	return;
    }

}
