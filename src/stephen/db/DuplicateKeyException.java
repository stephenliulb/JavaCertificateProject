/*
 * Basic Java skill show cases
 *
 * Copyright (c) 2024 Stephen Liu. All Rights Reserved. 
 *
 */

package stephen.db;

/**
 * Thrown when creating a new record which key exists in the data store.
 * 
 * @author Stephen Liu
 * 
 */
public class DuplicateKeyException extends Exception {
	static final long serialVersionUID = 1L;

	/**
	 * Allocates a new Exception object.
	 */
	public DuplicateKeyException() {
		super();
		return;
	}

	/**
	 * Allocates a new Exception object with specific exception message.
	 * 
	 * @param message exception's description
	 */
	public DuplicateKeyException(String message) {
		super(message);
		return;
	}

}
