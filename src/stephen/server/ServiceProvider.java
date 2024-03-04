/*
 * Basic Java skill show cases
 *
 * Copyright (c) 2024 Stephen Liu. All Rights Reserved. 
 *
 */

package stephen.server;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Logger;

import stephen.common.Messages;
import stephen.common.Utils;
import stephen.db.DBMain;
import stephen.db.lock.LockManager;
import stephen.network.Command;
import stephen.network.CommandHandler;
import stephen.network.CommandType;
import stephen.network.DBResult;

/**
 * This class is responsible to process client requests. It receives the client
 * commands over a socket connect, de-serialize the command objects and then
 * executes the commands in server-side context; If any exception occurs during
 * executing the commands,the exception object will be serialized and return to
 * clients as part of result.
 * <p>
 * Clients can execute multiple commands on a single connection. The working
 * thread of <code>ServiceProvider</code> exits only when clients close the
 * connection or a CLOSE command is received.
 * <p>
 * When the connection is closed, all uncompleted transactions will be rolled
 * back and all lock objects related to this connection will be released.
 * 
 * @author Stephen Liu
 * @see stephen.network.CommandType
 * 
 */
public class ServiceProvider implements Runnable {

	private static Logger logger = Logger.getLogger(ServiceProvider.class.getName());

	private Socket sock;
	private DBMain dbEngine;
	private CommandHandler executor;

	/**
	 * Create an instance of this class and initialize it by passing parameters.
	 * 
	 * @param sock     the socket object where the client request is on.
	 * @param dbEngine database command execute context.
	 */
	public ServiceProvider(Socket sock, DBMain dbEngine) {
		this.sock = sock;
		this.dbEngine = dbEngine;
		executor = new CommandExecutor();

	}

	/**
	 * The method will continuously process client commands until a 'CLOSE' command
	 * is received or client side closes the connection.All the resource related
	 * current connection will be released before exit.
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {

		Object result = null;

		try (OutputStream os = sock.getOutputStream();
			InputStream is = sock.getInputStream();
			ObjectOutputStream oos = new ObjectOutputStream(os);
			ObjectInputStream ois = new ObjectInputStream(is);)
		{
			while (true) {

				logger.finer(Messages.getString("ServiceProvider.start", new Object[] { Thread.currentThread() }));

				// EOFException will be thrown out if client side closed the connection,
				Command command = (Command) ois.readObject();

				if (command.getCommandType() == CommandType.CLOSE) {
					logger.finer(Messages.getString("ServiceProvider.connectionClose"));
					break;
				}

				logger.finer(Messages.getString("ServiceProvider.process",
						new Object[] { Thread.currentThread(), command }));

				result = executor.handle(command);

				logger.finer(
						Messages.getString("ServiceProvider.result", new Object[] { Thread.currentThread(), result }));

				oos.writeObject(result);
				oos.flush();
			}

		} catch (EOFException e) {
			// ignored due to the connection is closed by client.

		} catch (Exception e) {
			logger.severe(Utils.getExceptionString(e));
		} finally {

			// release all transaction lock on this connection when the connection is closed.
			LockManager.getInstance().releaseLocks(Thread.currentThread());
			
			if (sock != null) {
				try {
					sock.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				sock = null;
			}
		}

		logger.info(Messages.getString("ServiceProvider.exit", new Object[] { Thread.currentThread() }));
	}

	/**
	 * This class provides command processing logic in server-side.
	 * 
	 * @author Stephen Liu
	 *
	 */
	private class CommandExecutor implements CommandHandler {

		/**
		 * Execute the command in database server context. Any exception occurred will
		 * be captured and put into <code>DBResult</code> object as the response to
		 * client caller.
		 * 
		 * @param command a database command object.
		 * @return a <code>DBResult</code> object.
		 */

		public Object handle(Command command) {
			DBResult result = new DBResult();

			int recNo;
			String[] data;

			CommandType type = command.getCommandType();
			Object[] parameters = command.getParameters();

			try {
				switch (type) {
				case CREATE:
					data = (String[]) parameters[0];
					result.setResult(dbEngine.create(data));
					break;

				case DELETE:
					recNo = ((Integer) parameters[0]).intValue();
					dbEngine.delete(recNo);
					break;

				case FIND:
					String[] criteria = (String[]) parameters[0];
					result.setResult(dbEngine.find(criteria));
					break;

				case ISLOCKED:
					recNo = ((Integer) parameters[0]).intValue();
					result.setResult(dbEngine.isLocked(recNo));
					break;

				case LOCK:
					recNo = ((Integer) parameters[0]).intValue();
					dbEngine.lock(recNo);
					break;

				case READ:
					recNo = ((Integer) parameters[0]).intValue();
					result.setResult(dbEngine.read(recNo));
					break;

				case UNLOCK:
					recNo = ((Integer) parameters[0]).intValue();
					dbEngine.unlock(recNo);
					break;

				case UPDATE:
					recNo = ((Integer) parameters[0]).intValue();
					data = (String[]) parameters[1];
					dbEngine.update(recNo, data);
					break;

				default:
					throw new UnsupportedOperationException(
							Messages.getString("_Global.operatorNotSupported", new Object[] { type.name() }));

				}
			} catch (Throwable t) {
				result.setException(t);
			}

			return result;

		}
	}

}
