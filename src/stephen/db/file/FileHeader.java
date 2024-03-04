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

/**
 * This class describes the file header defined in data file
 * 
 * @author Stephen Liu
 * 
 */
public class FileHeader {
	private byte[] magicCookie = new byte[4];
	private byte[] recordLength = new byte[4];
	private byte[] numberOfFieldsInRecord = new byte[2];

	/**
	 * Read data file header.
	 * 
	 * @param input -- data file stream
	 * @throws IOException --if the file does not exist, is a directory rather than
	 *                     a regular file, or for some other reason cannot be opened
	 *                     for reading
	 */
	protected void readFrom(DataInput input) throws IOException {
		input.readFully(magicCookie);
		input.readFully(recordLength);
		input.readFully(numberOfFieldsInRecord);
	}

	/**
	 * Get data file magic Cookie
	 * 
	 * @return magic Cookie in data file.
	 */
	public byte[] getMagicCookie() {
		return magicCookie;
	}

	/**
	 * Get data file record length.
	 * 
	 * @return record length in data file.
	 */
	public int getRecordLength() {
		return ByteManipulator.bytesToInt(recordLength);
	}

	/**
	 * Get number of Fields in each record.
	 * 
	 * @return number of Fields in each record
	 */
	public int getNumberOfFieldsInRecord() {
		return ByteManipulator.bytesToInt(numberOfFieldsInRecord);
	}

	void setNumberOfFieldsInRecord(int fieldCount) {
		numberOfFieldsInRecord = ByteManipulator.intTo2bytes(fieldCount);
	}

}
