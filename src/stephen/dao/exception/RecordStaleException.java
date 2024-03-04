/*
 * Basic Java skill show cases
 *
 * Copyright (c) 2024 Stephen Liu. All Rights Reserved. 
 *
 */
package stephen.dao.exception;

/**
 * Thrown when try to manipulate an out-of-date record.
 * 
 * @author Stephen Liu
 * 
 */
public class RecordStaleException extends DAOException {
	static final long serialVersionUID = 1L;

	/**
	 * Allocates a new Exception object.
	 */
	public RecordStaleException() {
		super();
		return;
	}

	/**
	 * Allocates a new Exception object with specific exception message.
	 * 
	 * @param message exception's description
	 */
	public RecordStaleException(String message) {
		super(message);
		return;
	}

}
