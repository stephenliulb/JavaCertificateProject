/*
 * Basic Java skill show cases
 *
 * Copyright (c) 2024 Stephen Liu. All Rights Reserved. 
 *
 */

package stephen.db.file;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;

import stephen.common.ByteManipulator;
import stephen.common.Constant;

/**
 * This class describes the record defined in data file; It provides APIs to
 * facilitate reading/writing each field value based on the schema.
 * 
 * @author Stephen Liu
 * 
 */
public class Record {
	/**
	 * 0 -- valid record; 1 -- deleted record
	 */
	private final byte[] isDeleted = new byte[1];
	private final byte[] content;

	private FileSchema schema;

	/**
	 * Create a record object bound to an schema object, which will be used to parse
	 * the record content.
	 * 
	 * @param schema data file schema object.
	 */
	protected Record(FileSchema schema) {
		this.schema = schema;
		content = new byte[this.schema.getAllFieldsLength()];
	}

	/**
	 * Read an record from data file at current file position
	 * 
	 * @param input data file stream
	 * @throws IOException  if file format is incorrect.
	 * @throws EOFException - if this stream reaches the end before reading all the
	 *                      bytes.
	 */
	protected void readFrom(DataInput input) throws IOException {
		input.readFully(isDeleted);
		input.readFully(content);
	}

	/**
	 * Read an record from byte array
	 * 
	 * @param source byte array where the record data will be read.
	 * @param offset the start position of reading the record data.
	 */
	protected void readFrom(byte[] source, int offset) {
		int index = offset;
		System.arraycopy(source, index, isDeleted, 0, isDeleted.length);
		index += isDeleted.length;
		System.arraycopy(source, index, content, 0, content.length);
	}

	/**
	 * Write the record to data file at current file position.
	 * 
	 * @param output data file stream
	 * @throws IOException if the file does not exist, or the file is a directory
	 *                     rather than a regular file, or for some other reason
	 *                     cannot be opened for reading
	 */
	public void writeTo(DataOutput output) throws IOException {
		output.write(isDeleted);
		output.write(content);
	}

	/**
	 * Mark the record be deleted
	 */
	public void markDeleted() {
		isDeleted[0] = 0x01;
	}

	/**
	 * Determine if the record is valid or invalid.
	 * 
	 * @return true if the record is valid; false if record is invalid.
	 */
	public boolean isDeleted() {
		return (isDeleted[0] == 0x00 ? false : true);

	}

	/**
	 * Change the record according to the data string array; NULL value in data[n]
	 * will clear up corresponding field value. Each element in data array will
	 * correspond to the field in the record with the same order on the basis of 0.
	 * 
	 * @param data data string array
	 */
	public void setData(String[] data) {
		int fieldsNumber = schema.getFieldsNumber();
		int count = (data.length < fieldsNumber ? data.length : fieldsNumber);

		try {
			for (int i = 0; i < count; i++) {
				int offset = schema.getAllFieldsLengthBefore(i);
				int fieldlen = schema.getFieldLength(i);

				// clear field
				Arrays.fill(content, offset, offset + fieldlen, (byte) 0x00);

				// copy
				byte[] src = ByteManipulator.stringToBytes(data[i], Constant.CHARSET);
				if (src == null)
					continue;

				int copyLen = (src.length < fieldlen ? src.length : fieldlen);
				System.arraycopy(src, 0, content, offset, copyLen);

			}
		} catch (FieldNotExistException e) {
			// It will never happen.
			e.printStackTrace();

		}
	}

	/**
	 * Get string value of the field with the fieldNo.
	 * 
	 * @param fieldNo field No.
	 * @return the field string value.
	 * @throws FieldNotExistException if fieldNo is greater than or equal to the max
	 *                                number of fields in the schema, or if fieldNo
	 *                                is less than 0;
	 */
	public String getString(int fieldNo) throws FieldNotExistException {
		int fieldLen = this.schema.getFieldLength(fieldNo);
		int offset = this.schema.getAllFieldsLengthBefore(fieldNo);

		String str = ByteManipulator.bytesToString(content, offset, fieldLen, Constant.CHARSET);
		return str;
	}

	/**
	 * Get all columns values in the record.
	 * 
	 * @return String array; The value for field n appears in the index [n] of
	 *         String array.
	 * @throws FieldNotExistException if the record data doesn't match the data file
	 *                                schema.
	 */
	public String[] getColumns() throws FieldNotExistException {
		int colCount = schema.getFieldsNumber();
		String[] cols = new String[colCount];

		for (int i = 0; i < cols.length; i++) {
			cols[i] = this.getString(i);
		}

		return cols;
	}

	/**
	 * Get the storage size of the record occupied on the disk
	 * 
	 * @return the storage size of the record
	 */
	public int length() {
		return (isDeleted.length + content.length);
	}

}
