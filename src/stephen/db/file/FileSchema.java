/*
 * Basic Java skill show cases
 *
 * Copyright (c) 2024 Stephen Liu. All Rights Reserved. 
 *
 */

package stephen.db.file;

import java.io.DataInput;
import java.io.IOException;

import stephen.common.Messages;

/**
 * This class describes the file schema defined in data file
 * 
 * @author Stephen Liu
 * 
 */
public class FileSchema {

	private Field[] fields;

	/**
	 * Create a FileScheam Object with max number of fields.
	 * 
	 * @param numberOfFields
	 */
	public FileSchema(int numberOfFields) {
		fields = new Field[numberOfFields];

		for (int i = 0; i < fields.length; i++) {
			fields[i] = new Field();
		}
	}

	/**
	 * Read data file schema
	 * 
	 * @param input data file stream
	 * @throws IOException --if the file does not exist, is a directory rather than
	 *                     a regular file, or for some other reason cannot be opened
	 *                     for reading
	 */
	protected void readFrom(DataInput input) throws IOException {
		for (Field field : fields) {
			field.readFrom(input);
		}
	}

	/**
	 * Test if a specific field name exist in this schema or not.
	 * 
	 * @param name field name.
	 * @return true if the name exists; otherwise,false.
	 */
	public boolean isFieldExisted(String name) {
		boolean isFinded = false;

		for (Field fd : fields) {
			if (fd.getFieldName().equals(name)) {
				isFinded = true;
				break;
			}
		}

		return isFinded;
	}

	/**
	 * Remove one field from the schema.
	 * 
	 * @param fieldNo the index of file schema.
	 */
	void removeField(int fieldNo) {
		if (fieldNo < 0 || fieldNo >= fields.length) {
			return;
		}

		Field[] newFields = new Field[fields.length - 1];

		for (int i = 0; i < newFields.length; i++) {
			if (i < fieldNo) {
				newFields[i] = fields[i];
			} else {
				newFields[i] = fields[i + 1];
			}
		}

		this.fields = newFields;
	}

	/**
	 * Insert a new field at specific position of file schema.
	 * 
	 * @param fieldNo the index in file schema.
	 * @param fd      the new field.
	 */
	void insertField(int fieldNo, Field fd) {
		if (fieldNo < 0 || fieldNo >= fields.length) {
			return;
		}

		Field[] newFields = new Field[fields.length + 1];

		for (int i = 0; i < newFields.length; i++) {
			if (i < fieldNo) {
				newFields[i] = fields[i];
			} else if (i == fieldNo) {
				newFields[i] = fd;
			} else {
				newFields[i] = fields[i - 1];
			}
		}

		this.fields = newFields;
	}

	/**
	 * Get the count of all fields in schema.
	 * 
	 * @return count of all fields.
	 */
	public int getFieldsNumber() {
		return fields.length;
	}

	/**
	 * Get field object at specific position in the schema.
	 * 
	 * @param fieldNo the index of a specific field in the schema.
	 * @return field object corresponding to the field number.
	 */
	public Field getField(int fieldNo) {
		return fields[fieldNo];
	}

	/**
	 * Get the field sequence number in the schema for a specific field name.
	 * 
	 * @param fieldName field name
	 * @return the field sequence number in schema object
	 * @throws FieldNotExistException when fieldName doesn't exist
	 */
	public int getFieldNo(String fieldName) throws FieldNotExistException {
		int index = -1;
		for (int i = 0; i < fields.length; i++) {
			if (fields[i].getFieldName().equals(fieldName)) {
				index = i;
				break;
			}
		}

		if (index == -1) {
			String errMsg = Messages.getString("FileSchema.nonExistField", new Object[] { fieldName }); //$NON-NLS-1$
			throw new FieldNotExistException(errMsg);
		}

		return index;
	}

	/**
	 * Get field length for the field with the fieldNo.
	 * 
	 * @param fieldNo field sequence number in the schema
	 * @return the field length
	 * @throws FieldNotExistException if fieldNo is greater than or equal to the max
	 *                                number of fields in the schema, or if fieldNo
	 *                                is less than 0;
	 */
	public int getFieldLength(int fieldNo) throws FieldNotExistException {
		if (fieldNo >= fields.length || fieldNo < 0) {
			String errMsg = Messages.getString("FileSchema.nonExistFieldNo", new Object[] { fieldNo }); //$NON-NLS-1$
			throw new FieldNotExistException(errMsg);
		}
		return fields[fieldNo].getFieldLength();
	}

	/**
	 * Get the sum of all field lengths before the field with the fieldNo.
	 * 
	 * @param fieldNo field number
	 * @return the overall length for all fields before the fieldNo.
	 * @throws FieldNotExistException if fieldNo is greater than or equal to the max
	 *                                number of fields in the schema, or if fieldNo
	 *                                is less than 0;
	 */
	public int getAllFieldsLengthBefore(int fieldNo) throws FieldNotExistException {
		if (fieldNo >= fields.length || fieldNo < 0) {
			String errMsg = Messages.getString("FileSchema.nonExistFieldNo", new Object[] { fieldNo }); //$NON-NLS-1$
			throw new FieldNotExistException(errMsg);
		}

		int len = 0;
		for (int i = 0; i < fieldNo; i++) {
			len += fields[i].getFieldLength();
		}

		return len;
	}

	/**
	 * Get the sum of all fields length
	 * 
	 * @return overall length of all fields
	 */
	public int getAllFieldsLength() {
		int len = 0;
		for (int i = 0; i < fields.length; i++) {
			len += fields[i].getFieldLength();
		}

		return len;
	}

}
