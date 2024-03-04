/*
 * Basic Java skill show cases
 *
 * Copyright (c) 2024 Stephen Liu. All Rights Reserved. 
 *
 */
package stephen.network;

import java.io.IOException;
import java.lang.ref.Cleaner;
import java.util.logging.Logger;

import stephen.common.Messages;
import stephen.db.DuplicateKeyException;
import stephen.db.exception.RecordNotFoundException;

/**
 * This class provides the logic to execute database command in <I>network</I>
 * working mode, in which client application to interact with remote database
 * server. the network connection protocol is based on serialized objects over a
 * simple socket connection.
 * <p>
 * The database connection won't be release immediately after executed the
 * command; It will be kept for re-use purpose. Whenever network exception
 * occurs, the connection to database server will be close; before any new
 * command is executed, the connection will be re-checked and be re-opened if
 * disconnected.
 * 
 * @author Stephen Liu
 * 
 */
public class DBRemoteProxy implements CommandHandler {
	private static Logger logger = Logger.getLogger(DBRemoteProxy.class.getName());
	private RPC rpc;

	/**
	 * Create an instance, and initiate a connection to remote server; Even though
	 * failed to connect to remote server, it won't affect to instantiate the class
	 * because the connection will be re-checked whenever a new command is executed
	 * on it.
	 * 
	 * @param server database server address.
	 * @param port   database server port.
	 */
	public DBRemoteProxy(String server, int port) {
		rpc = new RPC(server, port);
		Cleaner.create().register(rpc,

				new Runnable() {
					final RPC cleaner = rpc;

					@Override
					public void run() {
						try {
							cleaner.close();
						} catch (Exception e) {
							String errMsg = "Encounter the error during closing rpc connection:" + e.getMessage();
							throw new RuntimeException(errMsg, e);
						}
					}
				});

		// open the connection to the server;
		// If failed, just leave it there. Each invoked method will firstly check the
		// connection.
		try {
			openConnection();
		} catch (Exception e) {
			throw new RuntimeException("Failed to setup connections", e);
		}
	}

	/**
	 * Disconnect to remote database server.
	 * 
	 * @throws IOException thrown when network error occurs.
	 */
	public void close() throws IOException {
		rpc.close();
	}

	/**
	 * Execute commands on remote database server. The command will firstly be
	 * serialized and transmitted to remote database server; a result will be
	 * returned when server finishes to process the command; If any exception occurs
	 * during executing the command on remote server side,the exception object will
	 * be encapsulated and returned as part of the result,then the exception object
	 * from server side will be parsed and transformed to one of the client
	 * exception: <code>RecordNotFoundException</code>,
	 * <code>DuplicateKeyException</code> or <code>RemoteException</code>.
	 * <p>
	 * The result type varies as the command type changes.
	 * 
	 * @see stephen.network.CommandHandler#handle(stephen.network.Command)
	 */
	public Object handle(Command command) throws RecordNotFoundException, DuplicateKeyException, RemoteException {

		openConnection();

		DBResult r = null;
		try {
			r = this.execute(rpc, command);
		} catch (RemoteException e) {
			try {
				rpc.close();
				rpc = null;
			} catch (IOException ioe) {
				logger.severe("Failed to close RPC connection: " + ioe.getLocalizedMessage());
			}

			throw e;
		}

		if (r.getException() != null) {
			Throwable t = r.getException();
			if (t instanceof RecordNotFoundException) {
				RecordNotFoundException e = new RecordNotFoundException(t.getMessage());
				e.initCause(t);
				throw e;
			} else if (t instanceof DuplicateKeyException) {
				DuplicateKeyException e = new DuplicateKeyException(t.getMessage());
				e.initCause(t);
				throw e;
			} else {
				RemoteException e = new RemoteException(t.getMessage());
				e.initCause(t);
				throw e;
			}
		}

		return r.getResult();
	}

	/**
	 * If connection to database hasn't been set up, try to connect to database
	 * server.
	 * 
	 * @throws RemoteException throws if any networks exception occurs.
	 */
	private void openConnection() throws RemoteException {
		if (!rpc.isConnected()) {
			try {
				rpc.open();
			} catch (IOException e) {
				String errMsg = Messages.getString("DBRemoteProxy.failedConnection",
						new Object[] { rpc.getServer(), rpc.getPort(), e.getMessage() });
				RemoteException re = new RemoteException(errMsg);
				re.initCause(e);
				throw re;
			}
		}
	}

	/**
	 * Transmit a database command to remote database server and receive the
	 * response. Any network exception will be encapsulated into a RemoteException
	 * object and be thrown out.
	 * 
	 * @param server  database server address.
	 * @param port    database server port.
	 * @param command database remote operation command.
	 * @return DBResult object.
	 * @throws RemoteException thrown if any network exception or server-side
	 *                         exception occurred.
	 */
	private DBResult execute(RPC connection, Command command) throws RemoteException {
		try {

			DBResult result = (DBResult) rpc.execute(command);

			return result;

		} catch (Exception e) {
			String errMsg = Messages.getString("DBRemoteProxy.failedRetrieve",
					new Object[] { e.getMessage(), rpc.getServer(), rpc.getPort() });
			RemoteException re = new RemoteException(errMsg);
			re.initCause(e);
			throw re;

		} finally {
			// don't release the connection for the purpose of re-use.
		}
	}

}
