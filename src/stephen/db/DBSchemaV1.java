/*
 * Basic Java skill show cases
 *
 * Copyright (c) 2024 Stephen Liu. All Rights Reserved. 
 *
 */
package stephen.db;

/**
 * The class defines the database schema which is exactly mapping to data file
 * schema in data file. In this database schema, the primary key is the
 * combination of 'name' and 'location'. This database schema is not used in
 * this application context and its existence is for purpose of
 * back-compatibility.
 * 
 * @author Stephen Liu
 * 
 */
public class DBSchemaV1 extends DBSchema {
	private static DBSchemaV1 dbSchema;

	private DBSchemaV1() {
		columns = new Column[] { 
				new Column(true, NAME, 56, "Hotel Name"), 
				new Column(true, LOCATION, 64, "City"),
				new Column(false, SIZE, 4, "Maximum occupancy of this room"),
				new Column(false, SMOKING, 1, "Is the room smoking or non-smoking"),
				new Column(false, RATE, 8, "Price per night"), 
				new Column(false, DATE, 10, "Data available"),
				new Column(false, OWNER, 8, "Customer holding this record") };
	}

	/**
	 * Get a DBSchemav1 object.
	 * 
	 * @return a DBSchemav1 object
	 */
	public static synchronized DBSchema getInstance() {
		if (dbSchema == null) {
			dbSchema = new DBSchemaV1();
		}

		return dbSchema;
	}
}
