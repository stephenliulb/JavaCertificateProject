/*
 * Basic Java skill show cases
 *
 * Copyright (c) 2024 Stephen Liu. All Rights Reserved. 
 *
 */
package stephen.network;

import java.io.Serializable;

import stephen.common.Utils;

/**
 * This class describes the response returned from remote database server. It contains
 * two kinds of data:
 * <ul>
 * <li>A result object,which is returned after a database command is successfully processed.
 * 
 * <li>An exception object occurred during processing the database command in remote database
 *     server. It will help client users to trace down the problem on server-side.
 * </ul>
 * 
 * @author Stephen Liu
 * 
 */
public class DBResult implements Serializable {
    private static final long serialVersionUID = 1L;
    private Object result;
    private Throwable t;

    /**
     * Get processed result from database.
     * @return the result
     */
    public Object getResult() {
	return result;
    }

    /**
     * Set processed result from database.
     * @param result
     *            the result to set
     */
    public void setResult(Object result) {
	this.result = result;
    }

    /**
     * Get exception object during processing the request in database server side.
     * @return exception object.
     */
    public Throwable getException() {
	return t;
    }

    /**
     * Set exception object occurred during processing the request in database server side.
     * @param t
     *            the exception object to set
     */
    public void setException(Throwable t) {
	this.t = t;
    }

    /**
     * Convert database result data to a string. If there is an exception, the
     * exception stack trace will be converted to a string and returned.
     * 
     * @see java.lang.Object#toString()
     */

    public String toString() {
	StringBuffer buffer = new StringBuffer();
	if (t != null) {
	    buffer.append(Utils.getExceptionString(t));
	} else if (result != null) {
	    buffer.append(result);
	}

	return buffer.toString();
    }

}
