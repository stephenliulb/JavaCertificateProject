/*
 * Basic Java skill show cases
 *
 * Copyright (c) 2024 Stephen Liu. All Rights Reserved. 
 *
 */

package stephen.db.file;

import java.io.DataInput;
import java.io.IOException;

import stephen.common.ByteManipulator;
import stephen.common.Constant;

/**
 * This class describes the field defined in data file schema.
 * 
 * @author Stephen Liu
 * 
 */
public class Field {
	private byte[] fieldNameLength = new byte[2];
	private byte[] fieldname;
	private byte[] fieldLength = new byte[2];

	/**
	 * Create a field object.
	 */
	public Field() {
		// default constructor.
	}

	/**
	 * Create a field object initialized by the incoming parameter.
	 * 
	 * @param fieldName   field name.
	 * @param fieldLength field length.
	 */
	public Field(String fieldName, int fieldLength) {
		this.fieldNameLength = ByteManipulator.intTo2bytes(fieldName.length());
		this.fieldname = ByteManipulator.stringToBytes(fieldName, Constant.CHARSET);
		this.fieldLength = ByteManipulator.intTo2bytes(fieldLength);
	}

	/**
	 * Read one field
	 * 
	 * @param input data file stream
	 * @throws IOException if the file does not exist, is a directory rather than a
	 *                     regular file, or for some other reason cannot be opened
	 *                     for reading
	 */
	protected void readFrom(DataInput input) throws IOException {
		input.readFully(fieldNameLength);

		int len = ByteManipulator.bytesToInt(fieldNameLength);
		fieldname = new byte[len];
		input.readFully(fieldname);

		input.readFully(fieldLength);
	}

	/**
	 * Get field name.
	 * 
	 * @return field name
	 */
	public String getFieldName() {
		return ByteManipulator.bytesToString(fieldname, Constant.CHARSET);
	}

	/**
	 * Get field length
	 * 
	 * @return field length
	 */
	public int getFieldLength() {
		int len = ByteManipulator.bytesToInt(fieldLength);
		return len;
	}

}