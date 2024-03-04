/*
 * Basic Java skill show cases
 *
 * Copyright (c) 2024 Stephen Liu. All Rights Reserved. 
 *
 */
package stephen.db;

/**
 * This class describes abstract database schema. The database schema is
 * different from data file schema.
 * 
 * <p>
 * The database schema is business view of data,which is high level opinion to
 * the data model from end users; but data file schema is physical view of
 * data,which describes the storage format how the data is stored in data file
 * on the disk.
 * <p>
 * The database schema is not necessary to be exactly same as data file
 * schema,but they are closely related. All columns in database schema have to
 * map to corresponding data file schema to be persisted. On the other hand,
 * when load data from data file, data file schema will be used to correctly
 * parse the file storage format; but different database schema will have
 * different way to parse the same record data and get different views to the
 * record data.
 * <p>
 * For example:<br>
 * There is a one field in data file schema: name[64] (hotel name)<br>
 * There are two database schemas:<br>
 * <ul>
 * <li>Database schema1: there is one column: name[64] which is corresponding to
 * DataFileSchema.name[64], having same name and same length.
 * 
 * <li>Database schema2: there are two column:name[56] (hotel name] and room[8]
 * (room number), both of them are corresponding to single field
 * DataFileSchema.name[64] where name[56] is occupying[0-55 byte] and room[8] is
 * occupying[56-63byte].
 * 
 * </ul>
 * 
 * @author Stephen Liu
 * 
 */
public abstract class DBSchema {
	/**
	 * constant field name 'name' string
	 */
	public static final String NAME = "name";
	/**
	 * constant field name 'room' string; this is added field.
	 */
	public static final String ROOM = "room";
	/**
	 * constant field name 'location' string
	 */
	public static final String LOCATION = "location";
	/**
	 * constant field name 'size' string
	 */
	public static final String SIZE = "size";
	/**
	 * constant field name 'smoking' string
	 */
	public static final String SMOKING = "smoking";
	/**
	 * constant field name 'rate' string
	 */
	public static final String RATE = "rate";
	/**
	 * constant field name 'date' string
	 */
	public static final String DATE = "date";
	/**
	 * constant field name 'owner' string
	 */
	public static final String OWNER = "owner";

	/**
	 * All columns' attributes.
	 */
	protected Column[] columns;

	/**
	 * Get primary key index of the schema. The index refers to the column element
	 * in column array.
	 * 
	 * @return primary key.
	 */
	public int[] getPrimaryKeySequenceNo() {
		int count = 0;
		for (Column col : columns) {
			if (col.isPrimaryKey) {
				count++;
			}
		}

		int index = 0;
		int[] keys = new int[count];
		for (int i = 0; i < columns.length; i++) {
			if (columns[i].isPrimaryKey) {
				keys[index++] = i;
			}
		}

		return keys;
	}

	/**
	 * Map the column name to sequence number in the schema
	 * 
	 * @param columnName column name
	 * @return the sequence number appearing in the schema; -1 means not existing.
	 */
	public int getColumnIndex(String columnName) {
		int seqNo = -1;

		for (int i = 0; i < columns.length; i++) {
			if (columns[i].databaseColumnName.equals(columnName)) {
				seqNo = i;
				break;
			}
		}

		return seqNo;
	}

	/**
	 * Get the number of columns in this db schema.
	 * 
	 * @return number of columns
	 */
	public int getColumnNumber() {
		return columns.length;
	}

	/**
	 * Get all column names in the DB schemas.
	 * 
	 * @return column names array.
	 */
	public String[] getColumnNames() {
		String[] colNames = new String[columns.length];
		for (int i = 0; i < columns.length; i++) {
			colNames[i] = columns[i].databaseColumnName;
		}
		return colNames;
	}

	/**
	 * Get column length.
	 * 
	 * @param name column name.
	 * @return column length.
	 */
	public int getColumnLength(String name) {
		int colLength = 0;
		for (Column col : columns) {
			if (col.databaseColumnName.equals(name)) {
				colLength = col.columnLength;
				break;
			}
		}
		return colLength;
	}

	/**
	 * This class encapsulates all attributes of one column in database schema.
	 * Database schema will be represented by a list of Column objects.
	 * 
	 * @author Stephen Liu
	 * 
	 */
	public static class Column {
		/**
		 * Indicate if the column is part of primary key.
		 */
		public final boolean isPrimaryKey;
		/**
		 * Column name.
		 */
		public final String databaseColumnName;
		/**
		 * Column length.
		 */
		public final int columnLength;
		/**
		 * Column description.
		 */
		public final String columnDescriptiveName;

		/**
		 * Create a column object by all column attributes.
		 * 
		 * @param isPrimaryKey          indicate if the column is part of primary key.
		 * @param databaseColumnName    column name in database schema.
		 * @param columnLength          column length.
		 * @param columnDescriptiveName column description.
		 */
		public Column(boolean isPrimaryKey, String databaseColumnName, int columnLength, String columnDescriptiveName) {
			this.isPrimaryKey = isPrimaryKey;
			this.databaseColumnName = databaseColumnName;
			this.columnLength = columnLength;
			this.columnDescriptiveName = columnDescriptiveName;
		}
	}
}
