/*
 * Basic Java skill show cases
 *
 * Copyright (c) 2024 Stephen Liu. All Rights Reserved. 
 *
 */

package stephen.common;

import java.util.Locale;

/**
 * This class manages all constant values used in this application.
 * @author Stephen Liu
 * 
 */
public interface Constant {
    /**
     * Default locale value in the application.
     */
    public Locale DEFAULT_LOCALE = Locale.US;
    
    /**
     * String encoding charset in the database data file.
     */
    public String CHARSET = "US-ASCII";
    
    /**
     * The date representation format
     */
    public final String DATE_PATTERN = "yyyy/MM/dd";
    
    /**
     * The maximum number of cached row lock objects in memory.
     */
    public int MAX_CACHED_LOCK = 1000;
    
    /**
     * Lock expired period which is used to prevent deadlock. it is milliseconds.
     */
    public long LOCK_EXPIRED_PERIOD = 60000;
    
    
    /**
     * the maximum time to wait in milliseconds for a locked resource.it is milliseconds.
     */
    public final int RECHECK_LOCK_INTERVAL = 10000; // seconds
    
    
    /**
     * It is used for reading multiple records in one IO operation to improve IO
     * efficiency.
     */
    public final int RECORD_FETCHSIZE = 1000;
    
    /**
     * Constant String "OR"
     */
    public String OR = "OR";
    
    /**
     * Constant string "AND"
     */
    public String AND = "AND";
    
    /**
     * Default database server address.
     */
    public String DEFAULT_SERVER = "Localhost";
    
    /**
     * Default database server port.
     */
    public int DEFAULT_PORT = 8899;    
    
   
    /**
     * This enum type describes the work modes of the application.
     * @author Stephen Liu
     *
     */
    public enum Mode {
	/**
	 * standalone mode.
	 */
	alone(0), 
	/**
	 * network mode.
	 */
	network(1), 
	
	/**
	 * Database server mode.
	 */
	server(2);
	
	/**
	 * index value of each work mode.
	 */
	public final int index;

	private Mode(int index) {
	    this.index = index;
	}
    }

    /**
     * Default database data file name.
     */
    public String DEFAULT_DATAFILE_NAME = "db-1x1.db";
    
    
    /**
     * Default log file if file handler is configurated in java logging.properties.
     */
    public String DEFAULT_LOG_FILE="output.log";
    /**
     * Default application configuration file name.
     */
    public String PROPERTY_FILENAME = "application.properties";

    /**
     * All configuration parameters' names .
     * @author Stephen Liu
     *
     */
    public interface ConfigParameters {
	/**
	 * work mode's parameter name.
	 */
	public String MODE = "mode";
	/**
	 * data file name's parameter name.
	 */
	public String DATAFILE = "datafile";
	/**
	 * Server address's parameter name.
	 */
	public String SERVER = "server";
	/**
	 * Server port's parameter name.
	 */
	public String PORT = "port";
	/**
	 * Comments in configuration file.
	 */
	public String COMMENTS = 
	        MODE
		+ ": working mode of the application; the range of value is [alone,server,network].\n"
		+ "#" + DATAFILE
		+ ": database data file path; it is for alone mode and server mode.\n"
		+ "#" + SERVER
		+ ": remote database server hostname or IP address; it is for network mode.\n"
		+ "#" + PORT
		+ ": remote database server port number; it is for network mode and server mode.";

    }
}
