/*
 * Basic Java skill show cases
 *
 * Copyright (c) 2024 Stephen Liu. All Rights Reserved. 
 *
 */

package stephen.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.WindowConstants;

import stephen.Config;
import stephen.common.Constant;
import stephen.common.Constant.Mode;
import stephen.common.Messages;
import stephen.common.Utils;
import stephen.db.DBMain;
import stephen.db.DBMainImpl;
import stephen.db.DBSchemaV2;
import stephen.ui.view.ConfigDialogs;

/**
 * This class describes the font end of database management system. Its major
 * responsibility is to receive client requests and delegate them to the class
 * <code>ServiceProvider</code> ,which will process these requests and return
 * the results to clients.
 * <p>
 * Clients can execute multiple commands on single connection to database
 * server; The connection will be closed when clients firstly close the
 * connection or a CLOSE command is received.
 * <p>
 * The server listening port and database data file are configurable. If these
 * parameters is unavailable or invalid, a configuration dialog will be popped
 * up and ask users to provide these parameters.
 * 
 * @author Stephen Liu
 * @see stephen.network.CommandType
 * 
 */
public class DBServer {

	private static Logger logger = Logger.getLogger(DBServer.class.getName());

	private int port;
	private String dbfile;
	private Config config;
	
	TaskExecutor executor;

	/**
	 * Create an instance of database server and initialize it by listening port and
	 * data file path in <code>Config</code> object. These parameters will be
	 * quickly checked; if any of them is unavailable or invalid, a configuration
	 * dialog will be popped up and ask users to provide these parameters.
	 * 
	 * @param config a configuration object which is expected to provide the server
	 *               listening port and database data file location.
	 */
	public DBServer(Config config) {
		this.config = config;

		String modeStr = config.getProperty(Constant.ConfigParameters.MODE);
		String portStr = config.getProperty(Constant.ConfigParameters.PORT);
		String dataFile = config.getProperty(Constant.ConfigParameters.DATAFILE);

		if (modeStr == null || (!modeStr.equalsIgnoreCase(Mode.server.name()) || dataFile == null
				|| !Config.validatePort(portStr))) {
			ConfigDialogs diag = new ConfigDialogs(null, Mode.server, config);
			diag.pack();
			diag.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			diag.setVisible(true);
			boolean isApproved = diag.isApproved();
			if (!isApproved) {
				cleanupWrongConfiguration();
				logger.severe(Messages.getString("DBServer.wrongConfiguration", new Object[] { portStr, dataFile }));
				System.exit(1);
			}
		}

		this.port = Integer.valueOf(config.getProperty(Constant.ConfigParameters.PORT));
		this.dbfile = config.getProperty(Constant.ConfigParameters.DATAFILE);
		
		executor = TaskExecutor.getInstance((t,e) -> {handleTaskException(t,e);});
	}
	
	private final void handleTaskException(Thread t, Throwable e) {
		
	}

	/**
	 * The main loop body of database system. It starts up the database server and
	 * infinitely waits for client requests. Once a client request is received, the
	 * client request will be fully delegated to a dynamically created working
	 * thread on the class <code>ServiceProvider</code>.
	 * <p>
	 * If any exception occurs due to incorrect parameters,all these parameter
	 * values will be cleaned up in the configuration file before database server
	 * exits.
	 */
	public void doService() {
		DBMain dbEngine = null;
		// load data file
		try {
			dbEngine = new DBMainImpl(dbfile, DBSchemaV2.getInstance());
		} catch (IOException e) {
			cleanupWrongConfiguration();
			logger.severe(Utils.getExceptionString(e));
			logger.severe(Messages.getString("DBServer.badDataFile", new Object[] { dbfile, e.getMessage() }));
			System.exit(1);
		}

		// listen on server port
		ServerSocket ss = null;
		try {
			ss = new ServerSocket(port);
			logger.log(Level.INFO, Messages.getString("DBServer.started", new Object[] { port }));
		} catch (Throwable e) {
			// detect any IOException, runtime exception related to port number.
			cleanupWrongConfiguration();
			logger.severe(Utils.getExceptionString(e));
			logger.severe(Messages.getString("DBServer.badPort", new Object[] { port, e.getMessage() }));
			System.exit(1);
		}

		// accept requests
		Socket sock = null;
		while (true) {

			try {
				sock = ss.accept();
				logger.finer(
						Messages.getString("DBServer.acceptedRequest", new Object[] { sock.getRemoteSocketAddress() }));
			} catch (IOException e) {
				logger.severe(Utils.getExceptionString(e));
				logger.severe(Messages.getString("DBServer.networkError", new Object[] { port, e.getMessage() }));
				continue;
			}

			try {
				ServiceProvider sg = new ServiceProvider(sock, dbEngine);
				executor.exec(sg);
			}catch(Throwable t) {
				logger.severe("Failed to process the request: " + t.getMessage());
				
			}
		}

	}

	private void cleanupWrongConfiguration() {
		config.clear();
		try {
			config.save();
		} catch (IOException e) {
			logger.severe(Messages.getString("DBServer.failedCleanUpConfiguration",
					new Object[] { config.getConfigFileName(), e.getMessage() }));
		}
	}

}
