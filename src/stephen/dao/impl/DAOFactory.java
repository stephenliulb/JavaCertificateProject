/*
 * Basic Java skill show cases
 *
 * Copyright (c) 2024 Stephen Liu. All Rights Reserved. 
 *
 */

package stephen.dao.impl;

import stephen.dao.DAOInterface;
import stephen.dao.DataTransferObject;
import stephen.dao.exception.DAOException;
import stephen.network.DBLocalProxy;
import stephen.network.CommandHandler;
import stephen.network.DBRemoteProxy;

/**
 * This class provides facility to simplify the creation of DAO object. Through
 * the unified DAO creation API, DAO object will automatically created and
 * configurated for different conditions.
 * <p>
 * 
 * @see stephen.dao.impl.DAOImpl
 * 
 * @author Stephen Liu
 * 
 */
public class DAOFactory {

	/**
	 * Create one DAO object based on the specified parameters.It provides a unified
	 * form to create DAO object for non-network mode or network mode.
	 * <p>
	 * For non-network mode,it completely bypass the network and directly access
	 * database file;so<br>
	 * parameter <code>isUseLocalDatabase</code> should be true and<br>
	 * parameter <code>datafile</code> should refer to database data file.
	 * <p>
	 * For network mode, which will use serialized objects to communicate with
	 * remote database server. so<br>
	 * parameter <code>isUseLocalDatabase</code> should be false, <br>
	 * parameter <code>server</code> should refer to remote database server and <br>
	 * parameter <code>port</code> should refer to database server port.
	 * 
	 * @param isUseLocalDatabase true for non-network mode; false for network mode.
	 * @param datafile           the path of local data file which is used only when
	 *                           <code>isUseLocalDatabase</code>=true.
	 * @param server             remote database server host name or IP address only
	 *                           when <code>isUseLocalDatabase</code> = false.
	 * @param port               remote database server listening port only when
	 *                           <code>isUseLocalDatabase</code> = false.
	 * @return DAO object.
	 * @throws DAOException throws when any exception happens during DAO object
	 *                      creation.
	 */
	public static synchronized DAOInterface<DataTransferObject> getDAO(boolean isUseLocalDatabase, String datafile,
			String server, int port) throws DAOException {

		CommandHandler proxy = null;
		DAOInterface<DataTransferObject> daoObj = null;

		try {
			if (isUseLocalDatabase) {
				proxy = new DBLocalProxy(datafile);
			} else {
				proxy = new DBRemoteProxy(server, port);
			}

			daoObj = new DAOImpl(proxy);
			return daoObj;

		} catch (Exception e) {
			DAOException re = new DAOException(e.getMessage());
			throw re;
		}
	}
}
