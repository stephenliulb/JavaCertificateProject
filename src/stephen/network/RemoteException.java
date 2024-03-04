/*
 * Basic Java skill show cases
 *
 * Copyright (c) 2024 Stephen Liu. All Rights Reserved. 
 *
 */
package stephen.network;

/**
 * Throws when any network exception occurred or server-side exception occurred.
 * @author Stephen Liu
 *
 */
public class RemoteException extends Exception {
    private static final long serialVersionUID = 1L;

    /**
     *Allocates a new Exception object.
     */
    public RemoteException() {
	super();
	return;
    }

    /**
     * Allocates a new Exception object with specific exception message.
     * 
     * @param message
     *            exception's description
     */
    public RemoteException(String message) {
	super(message);
	return;
    }

}
