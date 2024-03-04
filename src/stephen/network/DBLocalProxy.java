/*
 * Basic Java skill show cases
 *
 * Copyright (c) 2024 Stephen Liu. All Rights Reserved. 
 *
 */
package stephen.network;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import stephen.common.Messages;
import stephen.db.DBMain;
import stephen.db.DBSchemaV2;
import stephen.db.DBMainImpl;
import stephen.db.DuplicateKeyException;
import stephen.db.exception.RecordNotFoundException;

/**
 * The class provides logic to execute database command in <I>alone</I> working
 * mode,in which client GUI works with local database server within the same
 * JVM<br>
 * Whole network function will be bypassed and database server code is directly
 * called .
 * 
 * @author Stephen Liu
 * 
 */
public class DBLocalProxy implements CommandHandler {
	private static Logger logger = Logger.getLogger(DBLocalProxy.class.getName());

	private DBMain dbEngine;

	/**
	 * Create an DBLocalProxy instance.
	 * 
	 * @param datafile database data file used to initialize database server code.
	 * @throws IOException thrown if any IO error occurred during database server
	 *                     code initialization.
	 */
	public DBLocalProxy(String datafile) throws IOException {
		dbEngine = new DBMainImpl(datafile, DBSchemaV2.getInstance());

		logger.info(Messages.getString("DBLocalProxy.created", new Object[] { getFullFilename(datafile) }));
	}

	/**
	 * Process the command in local database server. The database server code is
	 * directly called. The result type varies as the command type changes.
	 * 
	 * @see stephen.network.CommandHandler#handle(stephen.network.Command)
	 */
	public Object handle(Command command) throws RecordNotFoundException, DuplicateKeyException, RemoteException {

		int recNo;
		String[] data;

		CommandType type = command.getCommandType();
		Object[] parameters = command.getParameters();

		Object result = null;

		switch (command.getCommandType()) {
		case CREATE:
			data = (String[]) parameters[0];
			result = dbEngine.create(data);
			break;

		case DELETE:
			recNo = ((Integer) parameters[0]).intValue();
			dbEngine.delete(recNo);
			break;

		case FIND:
			String[] criteria = (String[]) parameters[0];
			result = dbEngine.find(criteria);
			break;

		case ISLOCKED:
			recNo = ((Integer) parameters[0]).intValue();
			result = dbEngine.isLocked(recNo);
			break;

		case LOCK:
			recNo = ((Integer) parameters[0]).intValue();
			dbEngine.lock(recNo);
			break;

		case READ:
			recNo = ((Integer) parameters[0]).intValue();
			result = dbEngine.read(recNo);
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

		case CLOSE:
			break;

		default:
			throw new UnsupportedOperationException(
					Messages.getString("_Global.operatorNotSupported", new Object[] { type.name() }));
		}

		return result;
	}

	private String getFullFilename(String datafile) {
		String fullPath = null;
		try {
			fullPath = new File(datafile).getCanonicalPath();
		} catch (IOException e) {
			fullPath = datafile;
		}

		return fullPath;
	}

}
