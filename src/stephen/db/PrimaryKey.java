/*
 * Basic Java skill show cases
 *
 * Copyright (c) 2024 Stephen Liu. All Rights Reserved. 
 *
 */

package stephen.db;

import java.util.ArrayList;
import java.util.List;

import stephen.db.file.Record;

/**
 * This class describes primary key concept of database system.The primary key's combination 
 * depends on specific database schema. Different database schema will have different
 * primary key.<br>
 * For example:<br>
 * The primary key of <code>DBSchemaV1</code> is combination of 'name' and 'location';<br>
 * The primary key of <code>DBSchemaV2</code> is combination of 'name','room' and 'location';<br>
 * <p>
 *   
 * Empty value of any part in primary key in this application context is considered to be valid
 * for the back-compatibility. This is because column 'room' is added into database schema(V2)and it
 * is part of primary key; Originally,the default value for 'room' in data file (such as,db-1x1.db) 
 * is empty.
 * 
 * @author Stephen Liu
 * 
 */
public class PrimaryKey {

    private List<String> primarykeys = new ArrayList<String>();
    private DBSchema schema;

    /**
     * Create an primary key object from string array.
     * 
     * @param schema
     *            the database schema that this primary key is bound to.
     * 
     * @param recordData
     *            string array of record data values; the sequence must 
     *            match corresponding database schema.
     */
    public PrimaryKey(DBSchema schema, String... recordData) {
	this.schema = schema;	
	int[] seqNo = schema.getPrimaryKeySequenceNo();	
	for (int seq : seqNo) {
	    primarykeys.add(recordData[seq]);
	}
    }

    /**
     * Create an primary key object from an record object.
     * 
     * @param schema
     *            the database schema that this primary key is bound to.
     * @param record
     *            record object.
     */
    public PrimaryKey(DBSchema schema, Record record) {
	this.schema = schema;
	int[] seqNo = schema.getPrimaryKeySequenceNo();
	for (int i : seqNo) {
	    primarykeys.add(record.getString(i));
	}
    }

    /**
     * Construct a search criteria based on primary key values.
     * 
     * @return search criteria which is string array.
     */
    public String[] constructSearchCriteria() {
	String[] columns = new String[schema.getColumnNumber()];

	int index = 0;
	int[] seqNo = schema.getPrimaryKeySequenceNo();
	for (int i : seqNo) {
	    columns[i] = primarykeys.get(index++);
	}

	return constructSearchCriteriaByPrimaryKeyFields(schema, columns);
    }

    /**
     * Construct a primary key searching criteria.In the criteria, only primary
     * key fields are filled with values and other non-primary key fields will
     * be left to be null. It is possible that primary key field can be null.
     * 
     * @param schema
     *            the database schema that this primary key is bound to.
     * @param columns
     *            data record.
     * @return a same length of new string array in which the values only in primary
     *         key fields are assigned.
     */
    public static String[] constructSearchCriteriaByPrimaryKeyFields(
	    DBSchema schema, String[] columns) {
	String[] primaryKeySearchCriteria = new String[columns.length];

	int[] seqNo = schema.getPrimaryKeySequenceNo();
	for (int i : seqNo) {
	    primaryKeySearchCriteria[i] = columns[i];

	}

	return primaryKeySearchCriteria;
    }

    /**
     * Extract valid primary key values from the data record. If there is no
     * valid primary key in the record data, just return null.
     * 
     * @param schema
     *            the database schema that this primary key is bound to.
     * @param columns
     *            record data.
     * @return primary key value array if primary key exists in the record;
     *         otherwise, null will be returned.
     */
    public static PrimaryKey getPrimaryKey(DBSchema schema, String[] columns) {
	// check if valid primary key exists in record data.
	boolean isPrimaryKeyNotEmpty = true;

	int[] seqNo = schema.getPrimaryKeySequenceNo();
	String[] primaryKeyValues = new String[seqNo.length];
	for (int i : seqNo) {
	    if (columns[i] == null) {
		isPrimaryKeyNotEmpty = false;
		break;
	    }
	    primaryKeyValues[i] = columns[i];
	}

	if (!isPrimaryKeyNotEmpty) {
	    // no valid primary key found
	    return null;
	}

	return new PrimaryKey(schema, primaryKeyValues);
    }

    /*
     * All primary key fields' values must be equal.
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
	if (!(obj instanceof PrimaryKey)) {
	    return false;
	}

	PrimaryKey other = (PrimaryKey) obj;

	for (int i = 0; i < primarykeys.size(); i++) {
	    if (!primarykeys.get(i).equals(other.primarykeys.get(i))) {
		return false;
	    }
	}

	return true;
    }

    /*
     * Calculate the hash code based on each field value in primary key.
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
	final int PRIME = 31;
	int result = 1;
	for (int i = 0; i < primarykeys.size(); i++) {
	    result = PRIME * result + primarykeys.get(i).hashCode();
	}
	return result;
    }

}
