/*
 * Basic Java skill show cases
 *
 * Copyright (c) 2024 Stephen Liu. All Rights Reserved. 
 *
 */

package stephen.dao.exception;

/**
 * Thrown when some unexpected errors occur during accessing database via DAO
 * interface.
 * 
 * @author Stephen Liu
 */
public class DAOException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Allocates a new Exception object.
	 */
	public DAOException() {
		super();
		return;
	}

	/**
	 * Allocates a new Exception object with specific exception message.
	 * 
	 * @param message exception's description
	 */
	public DAOException(String message) {
		super(message);
		return;
	}

}
