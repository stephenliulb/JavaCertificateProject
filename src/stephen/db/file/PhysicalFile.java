/*
 * Basic Java skill show cases
 *
 * Copyright (c) 2024 Stephen Liu. All Rights Reserved. 
 *
 */

package stephen.db.file;

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;

import stephen.common.Messages;

/**
 * PhysicalFile object is bound to a database data file in file system; It
 * encapsulates low level file IO operation to provide easily using API to
 * manipulate the data file.
 * 
 * @author Stephen Liu
 * 
 */
public class PhysicalFile {
	private RandomAccessFile datafile;

	private final FileHeader header;
	private final FileSchema schema;

	// Record data area from here
	private final long dataSectionStartPointer;
	private final int recordStorageLength;

	/**
	 * Creates a PhysicalFile object mapping to an actual file.
	 * 
	 * @param filename the system-dependent file name
	 */
	public PhysicalFile(String filename) throws IOException {
		datafile = new RandomAccessFile(filename, "rw");

		try {
			// read file header
			header = new FileHeader();

			header.readFrom(datafile);

			// read file schema
			schema = new FileSchema(header.getNumberOfFieldsInRecord());
			schema.readFrom(datafile);
		} catch (EOFException e) {
			String errMsg = Messages.getString("PhysicalFile.corrupted", new Object[] { filename }); //$NON-NLS-1$
			throw new IOException(errMsg);
		}

		// cheap check to guarantee recordlength = SUM(every field length);
		if (header.getRecordLength() != schema.getAllFieldsLength()) {
			String errMsg = Messages.getString("PhysicalFile.inconsistentRecordLength", //$NON-NLS-1$
					new Object[] { header.getRecordLength(), schema.getAllFieldsLength() });
			throw new IOException(errMsg);
		}

		// cheap check to guarantee number of fields in header = the number of
		// fields in schema;
		if (header.getNumberOfFieldsInRecord() != schema.getFieldsNumber()) {
			String errMsg = Messages.getString("PhysicalFile.inconsistentFieldsNumber", //$NON-NLS-1$
					new Object[] { header.getNumberOfFieldsInRecord(), schema.getFieldsNumber() });

			throw new IOException(errMsg);
		}

		// start point of data records
		dataSectionStartPointer = datafile.getFilePointer();
		// Calculate the record length occupied in the disk
		recordStorageLength = header.getRecordLength() + 1; // '1' is the flag
		// of delete
	}

	/**
	 * Get one record data by a specified record number
	 * 
	 * @param recNo record number
	 * @return record data for the record number; null if the record is deleted or
	 *         doesn't exist.
	 * @throws IOException if file format is incorrect.
	 */
	public synchronized Record getRecord(int recNo) throws IOException {
		Record record = new Record(schema);

		moveTo(recNo);
		try {
			record.readFrom(datafile);
			if (record.isDeleted()) {
				record = null;
			}
		} catch (EOFException e) {
			record = null;
		}

		return record;
	}

	/**
	 * Get multiple records data. The records data will start from the record
	 * <code>fromRecNo</code> and the number of the records is specified by the
	 * parameter <code>numberOfRecord</code>.
	 * <p>
	 * 
	 * It helps improve the performance to retrieve data from data file in disk.
	 * 
	 * @param fromRecNo      the first record.
	 * @param numberOfRecord number of records read from data file in disk.
	 * @return a multiple records object.
	 * @throws IOException if data file format is wrong.
	 */
	public synchronized RecordBlock getRecordBlock(int fromRecNo, int numberOfRecord) throws IOException {
		byte[] content = new byte[recordStorageLength * numberOfRecord];
		moveTo(fromRecNo);

		// fill in the byte array by multiple IO operations.
		int offset = 0, len = content.length;
		while (true) {
			int count = datafile.read(content, offset, len);
			if (count == -1) {
				break;
			}

			offset += count;
			len -= count;

			// byteArray is full
			if (len == 0) {
				break;
			}
		}

		// nothing is read from datafile due to end of file
		if (offset == 0) {
			return null;
		}

		// Cheap check of the data length; valid data length must be integer
		// times of recordLen.
		int validLenght = offset;
		if (validLenght != ((validLenght / recordStorageLength) * recordStorageLength)) {
			throw new IOException(Messages.getString("PhysicalFile.wrongRecordLength")); //$NON-NLS-1$
		}

		RecordBlock rb = new RecordBlock(content, 0, validLenght, schema);

		return rb;
	}

	/**
	 * Update the record.
	 * 
	 * @param recNo  record number which will specify which record data will be
	 *               updated.
	 * @param record new record data.
	 * @throws IOException if an I/O error occurs.
	 */
	public synchronized void updateRecord(int recNo, Record record) throws IOException {
		moveTo(recNo);
		record.writeTo(datafile);
	}

	/**
	 * Add a new record.
	 * 
	 * @param record new record data.
	 * @return record number referred to the new record data.
	 * @throws IOException if an I/O error occurs.
	 */
	public synchronized int add(Record record) throws IOException {
		int recNo = getAvailableRecordNumber(0);

		moveTo(recNo);
		record.writeTo(datafile);
		return recNo;
	}

	/**
	 * Delete a record.
	 * 
	 * @param recNo the record number which will be delelted.
	 * @throws IOException if an I/O error occurs.
	 */
	public synchronized void delete(int recNo) throws IOException {
		Record record = new Record(this.schema);

		moveTo(recNo);
		record.readFrom(datafile);

		record.markDeleted();

		moveTo(recNo);
		record.writeTo(datafile);

	}

	/**
	 * Get a empty record object, which will be used to load one record data and
	 * save it into data file in disk.
	 * 
	 * @return a empty record object.
	 */
	public Record getEmptyRecord() {
		return new Record(schema);
	}

	/**
	 * Get data file header information.
	 * 
	 * @return DBFileHeader object which contains current data file's header
	 *         information.
	 */
	public FileHeader getFileHeader() {
		return this.header;
	}

	/**
	 * Get data file schema information.
	 * 
	 * @return DBFileSchema object which contains current data file's schema
	 *         information.
	 */
	public FileSchema getFileSchema() {
		return this.schema;
	}

	/**
	 * Split the storage area of one field in file schema into several smaller
	 * segments which correspond to a list of new fields. The total length of new
	 * fields should be exactly equal to the length of original field.
	 * <p>
	 * For example,<br>
	 * there is one field 'name' in file schema, which has 64 bytes of storage
	 * area[0-63 byte]. This field can be split into two parts:[0-55] and [56-63].
	 * <br>
	 * the first part[0-55] corresponds to a new field 'name'; the second
	 * part[56-63] corresponds to another new field 'room'.<br>
	 * The total length(56 bytes + 8 bytes) of new field 'name' and 'room' MUST be
	 * exactly 64 bytes of original field 'name'.
	 * <p>
	 * Note: This change of the file schema is never saved into data file.The
	 * purpose of this method try to change the view to parse each record.
	 * 
	 * @param fieldNo   field sequence No in file schema.
	 * @param newFields new fields which will replace the original field in file
	 *                  schema and occupy its storage area.
	 * @return true if operation is successful; false if the total length of new
	 *         fields is not equal to original field length.
	 */
	public boolean splitField(int fieldNo, Field[] newFields) {
		Field originalField = this.schema.getField(fieldNo);

		// check the length of new records
		int totalNewFieldslen = 0;
		for (Field fd : newFields) {
			totalNewFieldslen += fd.getFieldLength();
		}

		if (totalNewFieldslen != originalField.getFieldLength()) {
			return false;
		}

		// change file schema
		this.schema.removeField(fieldNo);
		for (int i = (newFields.length - 1); i >= 0; i--) {
			this.schema.insertField(fieldNo, newFields[i]);
		}

		// change file header
		this.header.setNumberOfFieldsInRecord(this.header.getNumberOfFieldsInRecord() + newFields.length);

		return true;
	}

	/**
	 * Look for an unused record number, which can be deleted or new record number.
	 * 
	 * @param from start position to look for an unused record number.
	 * @return an unused record number.
	 * @throws IOException if an IO error occurs.
	 */
	private int getAvailableRecordNumber(int from) throws IOException {
		// look for deleted record for re-usage

		// begin at the first record.
		moveTo(from);
		Record record = new Record(schema);

		int recNo = from;

		try {
			while (true) {
				record.readFrom(datafile);
				if (record.isDeleted()) {
					break;
				}
				recNo++;
			}
		} catch (EOFException e) {
			// end of file
		}

		return recNo;
	}

	/**
	 * Locate file pointer to the start of a specific record; It will be ready for
	 * following writing or reading.
	 * 
	 * @param recNo record number specifying a record.
	 * @throws IOException if an IO errors occurs.
	 */
	private void moveTo(int recNo) throws IOException {
		long position = dataSectionStartPointer + recNo * recordStorageLength;

		datafile.seek(position);
	}

}