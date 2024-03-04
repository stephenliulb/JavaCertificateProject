/*
 * Basic Java skill show cases
 *
 * Copyright (c) 2024 Stephen Liu. All Rights Reserved. 
 *
 */

package stephen.ui.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import stephen.common.Messages;
import stephen.dao.DAOInterface;
import stephen.dao.DataTransferObject;
import stephen.dao.ResultSet;
import stephen.dao.exception.DAOException;
import stephen.dao.spec.Spec;

/**
 * The class describes the data model used by client GUI. Base on the specific
 * filter, All selected data records will be retrieved from database and they
 * are cached in local memory for GUI representation. GUI can use APIs in this
 * data model to manipulate data records in database and synchronize with
 * database.
 * <p>
 * The class is data model part in MVC design pattern applied in GUI.
 * 
 * @author Stephen Liu
 * 
 */
public class UIDataModel {
	private static Logger logger = Logger.getLogger(UIDataModel.class.getName());

	private DAOInterface<DataTransferObject> dao;
	private Spec filter;

	private List<DataTransferObject> dataCache = new ArrayList<DataTransferObject>();

	/**
	 * Set DAO object which will be used to access data in database.
	 * 
	 * @param dao DAO object.
	 */
	public void setDAO(DAOInterface<DataTransferObject> dao) {
		this.dao = dao;
	}

	/**
	 * Set the filter to select specific data from database.
	 * 
	 * @param filter retrieve criteria.
	 */
	public void setFilter(Spec filter) {
		this.filter = filter;
	}

	/**
	 * Synchronize local cached data with the database.
	 * 
	 * @throws DAOException throws when an exception occurred during accessing the
	 *                      database.
	 */
	public void refresh() throws DAOException {
		if (dao == null) {
			dataCache.clear();
			logger.finer(Messages.getString("UIDataModel.DBDisconnected")); //$NON-NLS-1$
			return;
		}

		ResultSet<DataTransferObject> rs = dao.select(filter);

		dataCache.clear();
		for (DataTransferObject item : rs) {
			dataCache.add(item);
		}

	}

	/**
	 * Update the record data in the database.
	 * 
	 * @param recordData an record data
	 * @throws DAOException throws when an exception occurred during accessing the
	 *                      database.
	 */
	public void updateData(DataTransferObject recordData) throws DAOException {
		if (dao == null) {
			logger.finer(Messages.getString("UIDataModel.DBDisconnected"));
			return;
		}

		dao.update(Arrays.asList(recordData));

		// sync up with database
		refresh();
	}

	/**
	 * Delete a record from the database.
	 * 
	 * @param recordData an record data
	 * @throws DAOException throws when an exception occurred during accessing the
	 *                      database.
	 */
	public void deleteData(DataTransferObject recordData) throws DAOException {
		if (dao == null) {
			logger.finer(Messages.getString("UIDataModel.DBDisconnected")); //$NON-NLS-1$
			return;
		}

		dao.delete(Arrays.asList(recordData));

		// sync up with database
		refresh();
	}

	/**
	 * Insert a record into the database.
	 * 
	 * @param recordData an record data
	 * @throws DAOException throws when an exception occurred during accessing the
	 *                      database.
	 */
	public void insertData(DataTransferObject recordData) throws DAOException {
		if (dao == null) {
			logger.finer(Messages.getString("UIDataModel.DBDisconnected")); //$NON-NLS-1$
			return;
		}

		dao.insert(Arrays.asList(recordData));

		// sync up with database
		refresh();
	}

	/**
	 * Get all data retrieved from database complied with the setting filter. All
	 * returned data records are not allowed to change outside the class.
	 * 
	 * @return all cached data records.
	 */
	public List<DataTransferObject> getData() {
		return Collections.unmodifiableList(dataCache);
	}

}
