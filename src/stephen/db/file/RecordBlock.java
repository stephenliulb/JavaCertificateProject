/*
 * Basic Java skill show cases
 *
 * Copyright (c) 2024 Stephen Liu. All Rights Reserved. 
 *
 */

package stephen.db.file;

import java.util.Iterator;

import stephen.common.Messages;

/**
 * This class parses each record from a byte sub array where multiple
 * consecutive records are stored. The purpose of this class is to improve the
 * IO efficiency when reading records from the data file on the disk.
 * 
 * The records returned maybe be deleted or still be valid. Users of this class
 * should decide how to handle deleted records or valid records.
 * 
 * @author Stephen Liu
 * 
 */
public class RecordBlock implements Iterable<Record> {

	private byte[] source = null;
	private int offset;
	private int length;
	private FileSchema schema;

	/**
	 * Create a RecordBlock object based on byte sub array.
	 * 
	 * @param source byte array.
	 * @param offset start position of sub array.
	 * @param length the length of the sub array.
	 * @param schema record schema which will guide how to read data from byte
	 *               array.
	 */
	protected RecordBlock(byte[] source, int offset, int length, FileSchema schema) {
		this.source = source;
		this.offset = offset;
		this.length = length;
		this.schema = schema;
	}

	/**
	 * Get Iterator Object to help traverse all records stored in this object.
	 * 
	 * @return record Iterator object.
	 */
	public Iterator<Record> iterator() {
		return new MyIterator();
	}

	/**
	 * It is an inner class implementing Iterator interface.
	 * 
	 * @author Stephen Liu
	 */
	private class MyIterator implements Iterator<Record> {
		private int index = offset;

		/**
		 * @see Iterator#hasNext().
		 */
		public boolean hasNext() {
			int lastRecordStartPointer = (offset + length) - (schema.getAllFieldsLength() + 1);

			if (index <= lastRecordStartPointer) {
				return true;
			} else {
				return false;
			}
		}

		/**
		 * @see Iterator#next();
		 */
		public Record next() {
			Record record = new Record(schema);

			record.readFrom(source, index);
			index += record.length();

			return record;
		}

		/**
		 * @see Iterator#remove(); This method is not supported in this class.
		 */
		public void remove() {
			throw new UnsupportedOperationException(Messages.getString("_Global.removeNotSupported")); //$NON-NLS-1$
		}

	}

}
