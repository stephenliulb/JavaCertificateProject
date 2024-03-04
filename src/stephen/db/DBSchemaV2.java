/*
 * Basic Java skill show cases
 *
 * Copyright (c) 2024 Stephen Liu. All Rights Reserved. 
 *
 */

package stephen.db;

/**
 * This class describes a new database schema as opposed to
 * <code>DBSchemaV1</code>. A new column 'room' is added into the database
 * schema and the primary key is the combination of 'name','room' and
 * 'location'. This database schema is actually used in the application context.
 * <p>
 * During persistence into data file, the 'name' value in this schema will map
 * to first segment[0-55 byte]of the field 'name' in data file schema and the
 * 'room' value will map to last segment[56-63 byte]of same field 'name' in data
 * file schema; It won't change the data file schema for the purpose of
 * back-compatibility.
 * <p>
 * When load data from the data file, the data file schema will be used to
 * correctly read data file header and data file schema; but this database
 * schema will be used to guide to parse the record data to get extra column
 * 'room' information.
 * 
 * @author Stephen Liu
 * 
 */
public class DBSchemaV2 extends DBSchema {
	private static DBSchema dbSchema;

	private DBSchemaV2() {
		columns = new Column[] {
				// column 'NAME'(56 bytes) and column 'ROOM' (8 bytes) corresponding to the
				// field 'name'(64bytes) in file schema.
				new Column(true, NAME, 56, "Hotel Name"), new Column(true, ROOM, 8, "Room number"),
				new Column(true, LOCATION, 64, "City"), new Column(false, SIZE, 4, "Maximum occupancy of this room"),
				new Column(false, SMOKING, 1, "Is the room smoking or non-smoking"),
				new Column(false, RATE, 8, "Price per night"), new Column(false, DATE, 10, "Data available"),
				new Column(false, OWNER, 8, "Customer holding this record") };
	}

	/**
	 * Get a DBSchemaV2 object.
	 * 
	 * @return a DBSchemaV2 object.
	 */
	public static synchronized DBSchema getInstance() {
		if (dbSchema == null) {
			dbSchema = new DBSchemaV2();
		}

		return dbSchema;
	}
}
