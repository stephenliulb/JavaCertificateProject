/*
 * Sun Certified Deeloper for the Java 2 Platform
 *
 * Copyright (c) 2024 Stephen Liu. All Rights Reserved. 
 *
 */

package stephen.db;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import stephen.common.Constant;
import stephen.common.Messages;
import stephen.common.Utils;
import stephen.db.exception.RecordNotFoundException;
import stephen.db.exception.TransactionException;
import stephen.db.file.Field;
import stephen.db.file.FieldNotExistException;
import stephen.db.file.FileSchema;
import stephen.db.file.PhysicalFile;
import stephen.db.file.Record;
import stephen.db.file.RecordBlock;
import stephen.db.lock.LockManager;
import stephen.db.lock.StatefulLock;
import stephen.db.lock.TransactionContext;

/**
 * This class implements DBMain interface. It uses data file as persistence
 * layer.
 * <p>
 * This class supports general data manipulation operations in data store:
 * read()/update()/delete()/find()/create()/lock()/unlock().<br>
 * Row level lock mechanism is provided to support record lock/unlock
 * operation.Each record has unique lock object in whole system. Any attempt to
 * lock a resource that is already locked will cause the current thread to wait
 * until notifications that the desired resource becomes available are received.
 * <p>
 * When a user tries to change a record in database, the record must be locked
 * firstly; then the user can manipulate the record;at last the lock on the
 * record has to be released. To make changed data roll back easily in case any
 * exception, the user just plays on a copy of the record data in memory cache,
 * only when users unlock the record, the final changed data will flush into
 * data file.<br>
 * For example, update a record:<br>
 * <code>
 *   Data db = new Data(...);<br>
 *   ...<br>
 *   db.lock(recNo);<br>
 *   ...<br>
 *   db.update(recNo,newRecordData);<br>
 *   ...<br>
 *   
 *   db.unlock(recNo);<br>   
 * </code>
 * 
 * <p>
 * When the record is locked, a transaction environment is created around this
 * record.it isolates concurrent operations on same record from different
 * database connections;all operations in the transaction environment is atomic,
 * that means if any exception occurred(such as client database connection
 * suddenly reset, or data file IO exception),all changed data will be roll back
 * to keep data be consistent in data file.
 * <p>
 * There are three scenarios resulting in data changes roll back:<br>
 * <ul>
 * <li>Database connection is reset by peer
 * <li>Deadlock is detected
 * <li>Any exception during processing the database operations.
 * <p>
 * 
 * Check <code>TransactionException</code> and <code>StatefullLock</code> for
 * details about transaction environment.
 * 
 * There are three levels of data model concept in the context of the
 * system:<br>
 * <TABLE BORDER=1>
 * <TR>
 * <TD>Application
 * <TD>(Java data object)
 * <TR>
 * <TD>Database schema
 * <TD>(Business view of data)
 * <TR>
 * <TD>Data file schema
 * <TD>(Physical view of data)
 * </TABLE>
 * 
 * <ul>
 * <li>data file schema (data file storage schema on the disk)<br>
 * which describes the data storage format on the disk. It is only used when
 * save/load records data into/from data file.
 * <li>database schema<br>
 * which is the business view of data model and it is understanding to the data
 * model from the view of end-users; database schema has to be mapped to data
 * file schema to persist data into data file.
 * <li>application<br>
 * which uses java data objects mapped from database schema,such as DAO data
 * transfer object; Application business model will base on java data object
 * model.
 * </ul>
 * 
 * In the implementation, firstly data file will be parsed by
 * <code>PhysicalFile</code> object into three parts: file header,<I>data file
 * schema</I> and record data area, and then the <I>data file schema</I> will be
 * adusted to match the specific <I>database schema</I> definition(Note:changed
 * <I>data file schema</I> never be saved back into data file for
 * back-compatibility);after that,all record data will be loaded and parsed
 * based on the changed data file schema in <code>PhysicalFile</code> object; at
 * last,application will map the record data into java data object. On the other
 * hand, when save record data back into data file, application will firstly map
 * java data object into data format of <I>database schema</I>, and then used
 * changed <I>data file schema</I> to save record data into corresponding record
 * area in data file.
 * <p>
 * Currently, there are two kinds of database schema supported:
 * <ul>
 * <li><code>DBSchemaV1</code> which exactly maps to <I>data file schema</I> and
 * is only for the purpose of back-compatibility; its primary key is the
 * combination of 'name' and 'location'.
 * <li><code>DBSchemaV2</code> in which a new field 'room'(room number) is added
 * and its primary key is the combination of 'name','room' and 'location'.
 * </ul>
 * These two kind of <I>database schema</I> share same <I>data file schema</I>.
 * Only <code>DBSchemaV2</code> is used in this implementation.
 * <p>
 * To improve data search performance, an internal records index are set up and
 * maintained in the memory cache. the internal records index is part of back
 * end database server and shared by all clients.
 * <p>
 * This implementation doesn't consider the situation of huge amount of data
 * records. When the memory occupied by internal records index becomes an issue
 * in the future, the implementation for internal records index should be
 * revisited and refactored to adapt the new business condition.
 * 
 * @see stephen.db.DBSchemaV1
 * @see stephen.db.DBSchemaV2
 * @see stephen.db.exception.TransactionException
 * @see stephen.db.lock.StatefulLock
 * 
 * @author Stephen Liu
 * 
 */
public class DBMainImpl implements DBMain {
	private static Logger logger = Logger.getLogger(DBMainImpl.class.getName());

	/**
	 * Internal record index built in memory.
	 */
	private PrimaryKeyIndice primaryKeyIndex;

	/**
	 * It is used to load/manipulate/save the records in the data file on the disk.
	 * This object encapsulates low level file IO operation and simplify the
	 * operation on data file.
	 */
	private PhysicalFile pfile;

	/**
	 * Database schema used to parse record data.
	 */
	private DBSchema dbSchema;

	/**
	 * Create a Data Object which will use the data file as persistence media.
	 * 
	 * 
	 * @param datafile datafile filename.
	 * @param dbSchema database schema used to parse record data.
	 * @throws IOException If an IO error occurs.
	 */
	public DBMainImpl(String datafile, DBSchema dbSchema) throws IOException {
		this.dbSchema = dbSchema;
		this.pfile = initPhysicalFile(datafile, dbSchema);

		primaryKeyIndex = new PrimaryKeyIndice();
		primaryKeyIndex.init();
	}

	/**
	 * Create an PhysicalFile object bound to a specified database data file. The
	 * database schema will guide physicalFile how to parse each record data and it
	 * will not change data file storage schema which is used to load data file
	 * storage structure information.
	 * <p>
	 * When database schema is <code>DBSchemaV2</code>, the field 'name' in the file
	 * schema in PhysicalFile object is split to two fields 'name' and 'room' which
	 * keeps same view to record data with the database schema. When reading record,
	 * PhysicalFile Object will use the new view to parse the record data.
	 * <p>
	 * Note: PhysicalFile object never saves the changed file schema into datafile.
	 * 
	 * @param datafile a file name of database data file.
	 * @param schema   database schema which will guide PhysicalFile object how to
	 *                 parse the record data.
	 * @return
	 * @throws IOException
	 */
	private PhysicalFile initPhysicalFile(String datafile, DBSchema schema) throws IOException {
		PhysicalFile pf = new PhysicalFile(datafile);

		if (schema instanceof DBSchemaV2) {
			FileSchema fs = pf.getFileSchema();
			if (fs.isFieldExisted(DBSchema.ROOM)) {
				return pf;
			}
			// split field 'name' into two new field 'name' and field 'room'
			int fieldNo = fs.getFieldNo(DBSchema.NAME);
			boolean isSuc = pf.splitField(fieldNo,
					new Field[] { new Field(DBSchema.NAME, schema.getColumnLength(DBSchema.NAME)),
							new Field(DBSchema.ROOM, schema.getColumnLength(DBSchema.ROOM)) });
			if (!isSuc) {
				throw new RuntimeException(Messages.getString("Data.failedSplitField"));
			}
		}

		return pf;
	}

	/**
	 * Create a new record. After the new record is inserted into data file,for
	 * safety, the method will release the lock on the new record to avoid an
	 * extremely situation where the lock object is still valid for a re-used
	 * record.
	 * <p>
	 * If the record exists in data file, a <code>DuplicateKeyException</code> is
	 * throws out. If any other IO exception happens, a RuntimeException will be
	 * throws out.
	 * <p>
	 * Notes: the method is synchronized to avoid concurrency issue where multiple
	 * clients create different records on same record number.
	 * 
	 * 
	 * @see stephen.db.DBMain#create(java.lang.String[])
	 */
	public synchronized int create(String[] data) throws DuplicateKeyException {
		// Determine if the record exists or not in data file.
		String[] primaryKeySearchCriteria = PrimaryKey.constructSearchCriteriaByPrimaryKeyFields(dbSchema, data);

		int[] duplicatedKeys = null;
		try {
			duplicatedKeys = find(primaryKeySearchCriteria, true, null);
		} catch (RecordNotFoundException e) {
			duplicatedKeys = null;
		}

		if (duplicatedKeys != null && duplicatedKeys.length > 0) {
			String errMsg = Messages.getString("Data.duplicatedKey", new Object[] { Arrays.asList(data) }); //$NON-NLS-1$
			DuplicateKeyException e = new DuplicateKeyException(errMsg);
			throw e;
		}

		// Insert the new data
		Record record = pfile.getEmptyRecord();
		record.setData(data);

		int recNo;
		try {
			recNo = pfile.add(record);

			// update new index
			primaryKeyIndex.add(recNo, record);

		} catch (IOException e) {
			String errMsg = e.getMessage();
			RuntimeException re = new RuntimeException(errMsg);
			re.initCause(e);
			throw re;
		}

		// reset lock object if exists in case the record was reused but the
		// lock on it wasn't released.
		if (LockManager.getInstance().isLockExist(recNo)) {
			StatefulLock lockObj = LockManager.getInstance().getLock(recNo);
			lockObj.release();
		}

		logger.finer(Messages.getString("Data.createdRecord", new Object[] { recNo })); //$NON-NLS-1$

		return recNo;
	}

	/**
	 * Try to lock a specific record.Any attempt to lock a resource that is already
	 * locked will cause the current thread to wait until notifications that the
	 * desired resource becomes available are received.
	 * <p>
	 * If successful, a transaction environment for the specific record on an unique
	 * database connection will be created. The transaction environment is based on
	 * the lock mechanism; it is composed by the current thread object(lock's owner
	 * who is corresponding to an unique database connection) and a transaction
	 * context(which is used to cache all data change on this record instead of
	 * apply on database file) .
	 * 
	 * <p>
	 * Check details about transaction environment in
	 * <code>TransactionException</code>.
	 * 
	 * @see stephen.db.DBMain#lock(int)
	 * @see stephen.db.exception.TransactionException
	 */
	public void lock(int recNo) throws RecordNotFoundException {

		// verify if the record exists or not.
		retrieveRecord(recNo);

		StatefulLock lock = LockManager.getInstance().getLock(recNo);

		while (true) {
			synchronized (lock) {
				if (lock.isAvailable()) {
					lock.occupy(Thread.currentThread(), new TransactionContext(recNo));

					logger.finer(Messages.getString("Data.lockedRecord", new Object[] { recNo, lock }));

					break;
				} else {
					try {
						logger.finest(Messages.getString("Data.waitForLocker",
								new Object[] { Thread.currentThread(), lock, recNo }));

						lock.wait(Constant.RECHECK_LOCK_INTERVAL);
					} catch (InterruptedException e) {
						// ignore
						e.printStackTrace();
					}
				}
			}
		}

	}

	/**
	 * Determine if there is a lock on a specific record.
	 * 
	 * @see stephen.db.DBMain#isLocked(int)
	 */
	public boolean isLocked(int recNo) {

		// If no lock exists for the recNo, just return false.
		// Firstly calling LockManager.isLockExist() will avoid creating
		// unnecessary lock object for
		// the record number as opposed to the method LocakManager.getLock()
		if (!LockManager.getInstance().isLockExist(recNo)) {
			return false;
		}

		// if lock object exists for the record number, then get this lock
		// object.
		StatefulLock lock = LockManager.getInstance().getLock(recNo);

		if (lock.isAvailable()) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Delete a specific data record from data file. The lock on the record must be
	 * attained before the operation continues,otherwise a transaction exception
	 * will be thrown out.
	 * <p>
	 * For the sake of easy database roll back in case any exception occurred, the
	 * operation only applies on <code>TransactionContext</code> and the record
	 * delete on database file will be deferred to unlock() operation.
	 * <p>
	 * If a specified record doesn't exist or is marked as deleted in the database
	 * file, <code>RecordNotFoundException</code> will be thrown out;<br>
	 * If the operation is not in a transaction environment, an
	 * <code>TransactionException</code> will be thrown out. Check details about
	 * transaction environment in <code>TransactionException</code>.
	 * 
	 * <p>
	 * If IO exception happens, a RuntimeException will be throws out.
	 * 
	 * @see stephen.db.DBMain#delete(int)
	 * @see stephen.db.exception.TransactionException
	 */
	public void delete(int recNo) {

		StatefulLock lock = checkTransaction(recNo);

		lock.getTransactionContext().delete();

		return;
	}

	/**
	 * Update a specific record to new values. The lock on the record must be
	 * attained before the operation continues,otherwise a transaction exception
	 * will be thrown out.
	 * <p>
	 * The update operation works on non-primary key fields. For the sake of easy
	 * database roll back in case any exception occurred, the operation only applies
	 * on <code>TransactionContext</code> and the record update on database file
	 * will be deferred to unlock() operation.
	 * <p>
	 * When trying to update primary key in a record, an RuntimeException will be
	 * thrown out.<br>
	 * If the operation is not in a transaction environment, an
	 * <code>TransactionException</code> will be thrown out.
	 * <p>
	 * If primary key value should be modified, firstly delete the record with
	 * original primary key value and then insert a new record with new primary key
	 * value.<br>
	 * 
	 * <p>
	 * Check details about transaction environment in
	 * <code>TransactionException</code>.
	 * 
	 * @see stephen.db.DBMain#update(int, java.lang.String[])
	 * @see stephen.db.exception.TransactionException
	 */
	public void update(int recNo, String[] data) {

		StatefulLock lock = checkTransaction(recNo);

		lock.getTransactionContext().update(data);

		return;
	}

	/**
	 * The method will flush all changed data in transaction context into data file
	 * and then release the lock for a specific record if its lock object
	 * exists;also the transaction environment related to the lock object will be
	 * destroyed.
	 * <p>
	 * If any exceptions occurs, all changed data in transaction context will be
	 * simply removed instead of save into database. It is called <I>roll back</I>
	 * to keep data be consistent in database.
	 * <p>
	 * Check details about transaction environment in
	 * <code>TransactionException</code>.
	 * 
	 * @throws RecordNotFoundException
	 * @see stephen.db.DBMain#unlock(int)
	 * @see stephen.db.exception.TransactionException
	 */
	public void unlock(int recNo) throws RecordNotFoundException {

		StatefulLock lock = checkTransaction(recNo);

		// The following parts will commit all the changes into database
		TransactionContext tc = lock.getTransactionContext();

		try {
			if (tc != null) {
				if (tc.isDeleted()) {
					// delete record on database data file
					Record record = retrieveRecord(recNo);

					pfile.delete(recNo);

					// update indices object
					primaryKeyIndex.remove(record);

					logger.finer(Messages.getString("Data.deletedRecord", new Object[] { recNo }));

				} else {
					// update record on database data file
					String[] data = tc.getData();
					if (data != null) {
						Record record = retrieveRecord(recNo);

						// Compare the primary keys between the updating
						// record and the corresponding database record;
						// Update operation MUST not update primary key.
						PrimaryKey updateDataPK = new PrimaryKey(dbSchema, data);
						PrimaryKey dbPK = new PrimaryKey(dbSchema, record);
						if (!updateDataPK.equals(dbPK)) {
							String errMsg = Messages.getString("Data.primaryKeyUpdating",
									new Object[] { Arrays.asList(data), recNo });
							throw new RuntimeException(errMsg);
						}

						record.setData(data);
						pfile.updateRecord(recNo, record);

						logger.finer(Messages.getString("Data.updatedRecord", new Object[] { recNo }));
					}

				}
			}

		} catch (IOException e) {
			String errMsg = e.getMessage();
			RuntimeException re = new RuntimeException(errMsg);
			re.initCause(e);
			throw re;

		} finally {
			// release lock and notify all waiting threads on this lock
			lock.release();

			logger.finer(Messages.getString("Data.releaseLocker", new Object[] { lock.getOwner(), recNo }));
		}
	}

	/**
	 * @see stephen.db.DBMain#read(int)
	 */
	public String[] read(int recNo) throws RecordNotFoundException {
		String[] colums = null;
		Record record = retrieveRecord(recNo);
		colums = record.getColumns();
		return colums;
	}

	/**
	 * Search data according to criteria. If criteria contains primary key values,
	 * the result will directly retrieved from internal records index to improve
	 * search performance.
	 * 
	 * @see stephen.db.DBMain#find(java.lang.String[])
	 */
	public int[] find(String[] criteria) throws RecordNotFoundException {

		int[] searchingResult = null;

		PrimaryKey pk = PrimaryKey.getPrimaryKey(dbSchema, criteria);
		if (pk != null) {
			searchingResult = Utils.getIntArray(primaryKeyIndex.getIndex(pk));
		} else {
			searchingResult = find(criteria, false, null);
		}

		return searchingResult;
	}

	/**
	 * Search the data records based on the criteria. If exactMatch is true,it will
	 * return the records which exactly match non-null values in criteria,
	 * otherwise,it will return the records which values begin with corresponding
	 * criteria[n].
	 * <p>
	 * If IO exception happens, a RuntimeException will be throws out.
	 * 
	 * @param criteria   search condition.
	 * @param exactMatch exactly match or not.
	 * @param listener   record listener which will provide further immediately
	 *                   processing for each matched record
	 * @return matched records data.
	 * @throws RecordNotFoundException if no records are found.
	 */
	private int[] find(String[] criteria, boolean exactMatch, RecordListener listener) throws RecordNotFoundException {
		List<Integer> foundRecNos = new ArrayList<Integer>();

		// trace record number
		int index = 0;
		try {
			while (true) {
				RecordBlock records = pfile.getRecordBlock(index, Constant.RECORD_FETCHSIZE);
				if (records == null) {
					// end of the file
					break;
				}

				for (Record record : records) {
					// filter out invalid record
					if (record.isDeleted()) {
						index++;
						continue;
					}

					// filter each record by the criteria
					boolean isSatisfied = true;
					for (int n = 0; n < criteria.length; n++) {
						if (criteria[n] == null) { // A null value matches
							// any field value
							continue;
						}
						try {
							String fieldValue = record.getString(n).trim();
							String criteriaItem = criteria[n].trim();
							if (exactMatch) {
								if (!fieldValue.equals(criteriaItem)) {
									isSatisfied = false;
									break; // next record
								}
							} else {
								// A non-null value in criteria[n] exactly
								// match any field value that begins with
								// criteria[n].
								if (!fieldValue.startsWith(criteriaItem)) {
									isSatisfied = false;
									break;// next record
								}
							}
						} catch (FieldNotExistException e) {
							isSatisfied = false;
							break;
						}
					}

					if (isSatisfied) {
						foundRecNos.add(index);

						// do processing to the matched record.
						if (listener != null) {
							listener.process(index, record);
						}
					}

					// trace the record number
					index++;
				}
			}
		} catch (IOException e) {
			String errMsg = e.getMessage();
			RuntimeException re = new RuntimeException(errMsg);
			re.initCause(e);
			throw re;
		}

		if (foundRecNos.size() == 0) {
			String errMsg = Messages.getString("Data.noRecordFound", new Object[] { Arrays.asList(criteria) });
			throw new RecordNotFoundException(errMsg);
		}

		return Utils.getIntArray(foundRecNos);
	}

	/**
	 * Retrieve a record by the record number; If a specified record doesn't exist
	 * or is marked as deleted in the database file,
	 * <code>RecordNotFoundException</code> will be thrown out.
	 * 
	 * @param recNo record number.
	 * @return the record if exists.
	 * @throws RecordNotFoundException if the record doesn't exist.
	 */
	private Record retrieveRecord(int recNo) throws RecordNotFoundException {
		Record record = null;

		try {
			record = pfile.getRecord(recNo);
			if (record == null) {
				String errMsg = Messages.getString("Data.unexistingRecord", new Object[] { recNo });
				throw new RecordNotFoundException(errMsg);
			}

		} catch (IOException e) {
			String errMsg = e.getMessage();
			RuntimeException re = new RuntimeException(errMsg);
			re.initCause(e);
			throw re;
		}
		return record;
	}

	/**
	 * This class implements the interface <code>Indexable</code> by parameterized
	 * class <code>PrimaryKey</code>.<br>
	 * It maintains an internal memory cache to store all the mappings for each
	 * record's primary key to its record number.
	 * 
	 * @author Stephen Liu
	 * 
	 */
	private class PrimaryKeyIndice implements Indexable<PrimaryKey> {
		private HashMap<PrimaryKey, Integer> indice = new HashMap<PrimaryKey, Integer>();

		/**
		 * Set up index for all records in data file.
		 */
		void init() {
			// Set up the primary key indices for all records in database
			String[] criteria = { null, null };
			try {
				find(criteria, false, new RecordListener() {
					public void process(int recNo, Record record) {
						add(recNo, record);
					}
				});
			} catch (RecordNotFoundException e) {
				indice.clear();
				String errMsg = Messages.getString("Data.emptyDB"); //$NON-NLS-1$
				logger.warning(errMsg);
			}
		}

		/**
		 * Add a new index entry.
		 * 
		 * @param recordNo record number.
		 * @param record   record data which includes primary key value.
		 */
		void add(Integer recordNo, Record record) {
			PrimaryKey pk = new PrimaryKey(dbSchema, record);
			if (!indice.containsKey(pk)) {
				indice.put(pk, recordNo);
			}
		}

		/**
		 * Remove a index entry for a specific record.
		 * 
		 * @param record record data which includes primary key value.
		 */
		void remove(Record record) {
			PrimaryKey pk = new PrimaryKey(dbSchema, record);
			if (!indice.containsKey(pk)) {
				indice.remove(pk);
			}
		}

		/**
		 * Get a list of record numbers for a specified primary key(or prefix primary
		 * key).
		 * <p>
		 * If the primary key(or prefix primary key) can't be found in internal records
		 * index, the method will search whole data file and adds matched records into
		 * the internal records index if found; otherwise,An
		 * <code> RecordNotFoundException</code> will be thrown out.
		 * 
		 * @see stephen.db.Indexable#getIndex(java.lang.Object)
		 */
		public List<Integer> getIndex(PrimaryKey key) throws RecordNotFoundException {
			Integer recNo = indice.get(key);

			if (recNo != null) {
				return Arrays.asList(recNo);
			}

			final List<Integer> matchedRecordNumbers = new ArrayList<Integer>();
			// look for database and add the matched records into the records
			// index if found.
			String[] criteria = key.constructSearchCriteria();
			find(criteria, false, new RecordListener() {
				public void process(int recNo, Record record) {
					add(recNo, record);
					matchedRecordNumbers.add(recNo);
				}
			});

			return matchedRecordNumbers;
		}
	}

	/**
	 * 
	 * @param recNo
	 * @return
	 */
	private StatefulLock checkTransaction(int recNo) {
		if (!LockManager.getInstance().isLockExist(recNo)) {
			throw new TransactionException(Messages.getString("Data.recordNotLocked", new Object[] { recNo }));
		}

		StatefulLock lock = LockManager.getInstance().getLock(recNo);
		if (lock.isAvailable()) {
			throw new TransactionException(Messages.getString("Data.recordNotLocked", new Object[] { recNo }));

		} else if (!lock.getOwner().equals(Thread.currentThread())) {
			throw new TransactionException(Messages.getString("Data.conflictLock",
					new Object[] { Thread.currentThread(), lock.toString(), recNo }));
		}

		return lock;
	}

	/**
	 * Interface to process each matched record during traversing all data records
	 * in database.
	 * 
	 * @author Stephen Liu
	 * 
	 */
	private interface RecordListener {
		/**
		 * Process one record.
		 * 
		 * @param recNo  record number.
		 * @param record record date
		 */
		public void process(int recNo, Record record);
	}

}
