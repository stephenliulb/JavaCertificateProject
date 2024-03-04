/*
 * Basic Java skill show cases
 *
 * Copyright (c) 2024 Stephen Liu. All Rights Reserved. 
 *
 */

package stephen.dao.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import stephen.common.Messages;
import stephen.dao.DAOInterface;
import stephen.dao.DataTransferObject;
import stephen.dao.ResultSet;
import stephen.dao.exception.DAOException;
import stephen.dao.exception.RecordStaleException;
import stephen.dao.spec.Spec;
import stephen.db.DBSchemaV2;
import stephen.db.DuplicateKeyException;
import stephen.db.PrimaryKey;
import stephen.db.exception.RecordNotFoundException;
import stephen.network.Command;
import stephen.network.CommandHandler;
import stephen.network.CommandType;

/**
 * This class implements the interface <code>DAOInterface</code> by using data
 * file as the media of data store. It provides high level encapsulation to data
 * operations and help decouple applications business logic from data accessing
 * logic. <br>
 * 
 * The DAO implementation is configurated by a <code>CommandHandler</code>
 * object, which is responsible to interact with underlying data store. There
 * are different types of <code>CommandHandler</code>, such as
 * <ul>
 * <li><code>DBLocalProxy</code><br>
 * which is for non-Networked Mode and directly call database server
 * code,bypassing any network code.
 * <li><code>DBRemoteProxy</code><br>
 * which is for network Mode and transmit the database command to remote
 * database server and receive the result from remote database server.
 * </ul>
 * 
 * <p>
 * When update/delete a out of date record, an DAOException will be thrown out
 * to notify callers to sync up data with data store.
 * 
 * @see stephen.network.CommandHandler
 * @see stephen.network.DBLocalProxy
 * @see stephen.network.DBRemoteProxy
 * 
 * @author Stephen Liu
 * 
 */
public class DAOImpl implements DAOInterface<DataTransferObject> {
	private static Logger logger = Logger.getLogger(DAOImpl.class.getName());

	private CommandAdapter commandAdapter;

	/**
	 * Create a local dao object for a specific data file.
	 * 
	 * @param datafile -- database data file.
	 */
	DAOImpl(CommandHandler dbproxy) {
		this.commandAdapter = new CommandAdapter(dbproxy);
	}

	/**
	 * Delete a list of records from data file. All records matched by primary key
	 * fields' values of each records in the list will be removed from data file.
	 * The match rule will comply with same rule in the method
	 * <code>stephen.db.DBMain.find(String[] criteria)<code>.
	 * <p>
	 * If any record in the list is out of date, the operation will be terminated
	 * and an DAOException will be thrown out.
	 * 
	 * @see stephen.dao.DAOInterface#delete(java.util.List)
	 */
	public void delete(List<DataTransferObject> deleteRecords) throws DAOException {
		for (DataTransferObject transferObj : deleteRecords) {
			String[] recordValues = transferObj.getData();

			int[] deleteRecordNumbers = null;
			try {
				String[] primaryKeySearchCriteria = PrimaryKey
						.constructSearchCriteriaByPrimaryKeyFields(DBSchemaV2.getInstance(), recordValues);

				deleteRecordNumbers = commandAdapter.find(primaryKeySearchCriteria);

				// try to lock all selected records and detect if these records
				// are out of date.
				for (int recNo : deleteRecordNumbers) {
					commandAdapter.lock(recNo);

					// make sure the old record value has been not changed by
					// other CSRS.If old data has been changed before this
					// update operation, this update operation has to be canceled and a
					// notification will be send back to the CSRS.
					String[] currentRecordValuesInDatabase = commandAdapter.read(recNo);

					if (!compareRecords(recordValues, currentRecordValuesInDatabase)) {
						String errMsg = Messages.getString("DAOImpl.staleRecord", new Object[] { transferObj.getName(),
								transferObj.getRoom(), transferObj.getLocation() });
						RecordStaleException rse = new RecordStaleException(errMsg);
						throw rse;
					}
				}

				// now it is time to safely remove all selected records
				for (int recNo : deleteRecordNumbers) {
					commandAdapter.delete(recNo);
				}

			} catch (RecordNotFoundException e) {
				String errMsg = Messages.getString("DAOImpl.primaryKeyNonexist",
						new Object[] { transferObj.getName(), transferObj.getRoom(), transferObj.getLocation() });
				DAOException re = new DAOException(errMsg);
				re.initCause(e);
				throw re;
			} catch (Exception e) {
				DAOException d = new DAOException(e.getMessage());
				d.initCause(e);
				throw d;
			} finally {
				// release the lock on selected records.
				if (deleteRecordNumbers != null) {
					try {
						for (int recNo : deleteRecordNumbers) {
							commandAdapter.unlock(recNo);
						}
					} catch (Exception e) {
						// ignore the exception.
						e.printStackTrace();
					}
				}
			}
		}

		return;
	}

	/**
	 * Insert a list of records into data file.
	 * 
	 * @see stephen.dao.DAOInterface#insert(java.util.List)
	 */
	public void insert(List<DataTransferObject> newRecords) throws DAOException {
		for (DataTransferObject transferObj : newRecords) {
			String[] recordValues = transferObj.getData();

			try {
				commandAdapter.create(recordValues);
			} catch (DuplicateKeyException e) {
				String errMsg = Messages.getString("DAOImpl.primaryKeyDuplicated",
						new Object[] { transferObj.getName(), transferObj.getRoom(), transferObj.getLocation() });
				DAOException re = new DAOException(errMsg);
				re.initCause(e);
				throw re;
			} catch (Exception e) {
				DAOException d = new DAOException(e.getMessage());
				d.initCause(e);
				throw d;
			}
		}

		return;
	}

	/**
	 * Search records from data file based on specific searching condition.
	 * 
	 * @see stephen.dao.DAOInterface#select(stephen.dao.spec.Spec)
	 */
	public ResultSet<DataTransferObject> select(Spec spec) throws DAOException {

		List<DataTransferObject> result = new ArrayList<DataTransferObject>();

		if (spec == null) {
			return new ResultSetImpl(result);
		}

		List<List<String>> criterias = spec.getCriteria();
		Set<Integer> retrievedRecordsNumbers = new TreeSet<Integer>();

		for (List<String> criteria : criterias) {
			String[] normCriteria = getNormCriteria(criteria);

			int[] selectedRecordNumbers = null;
			try {
				selectedRecordNumbers = commandAdapter.find(normCriteria);
			} catch (RecordNotFoundException e) {
				// ignore
			} catch (Exception e) {
				DAOException d = new DAOException(e.getMessage());
				d.initCause(e);
				throw d;
			}

			if (selectedRecordNumbers == null) {
				continue;
			}

			// add into recordNumbers to accumulate all selected unique record
			// numbers;
			for (int rn : selectedRecordNumbers) {
				retrievedRecordsNumbers.add(rn);
			}
		}

		try {
			Iterator<Integer> rnItr = retrievedRecordsNumbers.iterator();
			while (rnItr.hasNext()) {
				int rn = rnItr.next();

				String[] recordData = commandAdapter.read(rn);

				result.add(new DataTransferObject(recordData));
			}
		} catch (Exception e) {
			String errMsg = Messages.getString("DAOImpl.failedRetrieve", new Object[] { e.getMessage() });
			DAOException re = new DAOException(errMsg);
			re.initCause(e);
			throw re;
		}

		return new ResultSetImpl(result);
	}

	/**
	 * Update a list of records in data file. Each record in the list must match
	 * only record in data file; If one record match multiple records in database
	 * file, the update for this record will be skipped.
	 * <p>
	 * If any record in the list is out of date, the operation will be terminated
	 * and an DAOException will be thrown out.
	 * 
	 * @see stephen.dao.DAOInterface#update(java.util.List)
	 */
	public void update(List<DataTransferObject> updateRecords) throws DAOException {

		for (DataTransferObject transferObj : updateRecords) {
			String[] oldRecordValues = transferObj.getData();
			String[] newRecordValues = transferObj.getLatestData();

			int recNo = -1;
			try {
				String[] primaryKeySearchCriteria = PrimaryKey
						.constructSearchCriteriaByPrimaryKeyFields(DBSchemaV2.getInstance(), oldRecordValues);

				int[] updateRecordNos = commandAdapter.find(primaryKeySearchCriteria);

				if (updateRecordNos.length > 1) {
					String errMsg = Messages.getString("DAOImpl.multipleRecordsMatched",
							new Object[] { transferObj.getName(), transferObj.getRoom(), transferObj.getLocation() });
					logger.warning(errMsg);
					continue;
				}

				recNo = updateRecordNos[0];

				commandAdapter.lock(recNo);

				// make sure the old record value has been not changed by other
				// CSRs.If old data has been changed before this update
				// operation, this update operation has to be terminated and a
				// notification will be send back to the CSRs.
				String[] currentRecordValuesInDatabase = commandAdapter.read(recNo);

				if (!compareRecords(oldRecordValues, currentRecordValuesInDatabase)) {
					String errMsg = Messages.getString("DAOImpl.staleRecord",
							new Object[] { transferObj.getName(), transferObj.getRoom(), transferObj.getLocation() });
					RecordStaleException rse = new RecordStaleException(errMsg);
					throw rse;
				}

				commandAdapter.update(recNo, newRecordValues);

			} catch (RecordNotFoundException e) {
				String errMsg = Messages.getString("DAOImpl.primaryKeyNonexist",
						new Object[] { transferObj.getName(), transferObj.getRoom(), transferObj.getLocation() });
				DAOException re = new DAOException(errMsg);
				re.initCause(e);
				throw re;
			} catch (Exception e) {
				DAOException d = new DAOException(e.getMessage());
				d.initCause(e);
				throw d;
			} finally {
				if (recNo != -1) {
					try {
						commandAdapter.unlock(recNo);
					} catch (Exception e) {
						// ignore the exception.
						e.printStackTrace();
					}
				}
			}
		}

		return;
	}

	private String[] getNormCriteria(List<String> criteria) {
		String[] normCriteria = new String[DBSchemaV2.getInstance().getColumnNumber()];

		for (String pair : criteria) {

			int idx = pair.indexOf(Spec.EQUALOP);

			if (idx < 0) {
				String errMsg = Messages.getString("_Global.operatorNotSupported", new Object[] { pair });
				logger.warning(errMsg);
				continue;
			}

			String name = pair.substring(0, idx);
			String value = pair.substring(idx + 1, pair.length());

			int seqNo = DBSchemaV2.getInstance().getColumnIndex(name);
			if (seqNo == -1) {
				String errMsg = Messages.getString("DAOImpl.columnNonexist", new Object[] { name, pair });
				logger.warning(errMsg);
				continue;
			}

			normCriteria[DBSchemaV2.getInstance().getColumnIndex(name)] = (value.length() == 0 ? null : value);
		}

		return normCriteria;
	}

	private boolean compareRecords(String[] record1, String[] record2) {
		if (record1.length != record2.length) {
			return false;
		}

		for (int i = 0; i < record1.length; i++) {

			if (record1[i].equals(record2[i])) {
				continue;
			}

			// null value will be treated as empty string.
			if ((record1[i] == null || record1[i].trim().length() == 0)
					&& (record2[i] == null || record2[i].trim().length() == 0)) {
				continue;
			}

			return false;
		}

		return true;
	}

	/**
	 * This class provides the adapter interface to convert database operation
	 * parameters into a command object, and then pass the command object to a
	 * pre-configurated command handler. the Object type result from the command
	 * handler will be converted back into the specific type result corresponding to
	 * each command type.
	 * 
	 * @author Stephen Liu
	 * 
	 */
	private class CommandAdapter {
		private CommandHandler handler;

		public CommandAdapter(CommandHandler handler) {
			this.handler = handler;
		}

		/**
		 * Create a new record in remote database server.
		 */
		public int create(String[] data) throws Exception {

			Command command = new Command(CommandType.CREATE, (Object) data);

			Object r = this.handler.handle(command);

			return (Integer) r;
		}

		/**
		 * Delete a record from remote database server.
		 */
		public void delete(int recNo) throws Exception {

			Command command = new Command(CommandType.DELETE, recNo);

			this.handler.handle(command);
			return;
		}

		/**
		 * Search records from remote database server.
		 */
		public int[] find(String[] criteria) throws Exception {

			Command command = new Command(CommandType.FIND, (Object) criteria);

			Object r = this.handler.handle(command);

			return (int[]) r;
		}

		/**
		 * Lock a record in remote database server.
		 * 
		 */
		public void lock(int recNo) throws Exception {

			Command command = new Command(CommandType.LOCK, recNo);

			this.handler.handle(command);
			return;
		}

		/**
		 * Read a specific data record from remote database server.
		 * 
		 */
		public String[] read(int recNo) throws Exception {

			Command command = new Command(CommandType.READ, recNo);

			Object r = this.handler.handle(command);

			return (String[]) r;
		}

		/**
		 * Unlock a specific data record in remote database server.
		 * 
		 */
		public void unlock(int recNo) throws Exception {

			Command command = new Command(CommandType.UNLOCK, recNo);

			this.handler.handle(command);
			return;
		}

		/**
		 * Update a specific data record in remote database server.
		 * 
		 */
		public void update(int recNo, String[] data) throws Exception {

			Command command = new Command(CommandType.UPDATE, recNo, (Object) data);

			this.handler.handle(command);
			return;
		}
	}

}
