// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.mysql.v0_5;

import com.bretth.osmosis.core.database.DatabaseLoginCredentials;
import com.bretth.osmosis.core.database.DatabasePreferences;
import com.bretth.osmosis.core.mysql.common.DatabaseContext;
import com.bretth.osmosis.core.mysql.common.SchemaVersionValidator;
import com.bretth.osmosis.core.task.common.RunnableTask;


/**
 * A standalone OSM task with no inputs or outputs that truncates tables in a
 * mysql database. This is used for removing all existing data from tables.
 * 
 * @author Brett Henderson
 */
public class MysqlTruncator implements RunnableTask {
	
	// These SQL statements will be invoked to truncate each table.
	private static final String[] SQL_STATEMENTS = {
		"TRUNCATE current_relation_members",
		"TRUNCATE current_relation_tags",
		"TRUNCATE current_relations",
		"TRUNCATE current_way_nodes",
		"TRUNCATE current_way_tags",
		"TRUNCATE current_ways",
		"TRUNCATE current_nodes",
		"TRUNCATE relation_members",
		"TRUNCATE relation_tags",
		"TRUNCATE relations",
		"TRUNCATE way_nodes",
		"TRUNCATE way_tags",
		"TRUNCATE ways",
		"TRUNCATE nodes"
	};
	
	
	private DatabaseContext dbCtx;
	private DatabasePreferences preferences;
	private SchemaVersionValidator schemaVersionValidator;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param loginCredentials
	 *            Contains all information required to connect to the database.
	 * @param preferences
	 *            Contains preferences configuring database behaviour.
	 */
	public MysqlTruncator(DatabaseLoginCredentials loginCredentials, DatabasePreferences preferences) {
		this.preferences = preferences;
		
		dbCtx = new DatabaseContext(loginCredentials);
		
		schemaVersionValidator = new SchemaVersionValidator(loginCredentials);
	}
	
	
	/**
	 * Truncates all data from the database.
	 */
	public void run() {
		try {
			if (preferences.getValidateSchemaVersion()) {
				schemaVersionValidator.validateVersion(MySqlVersionConstants.SCHEMA_VERSION);
			}
			
			for (int i = 0; i < SQL_STATEMENTS.length; i++) {
				dbCtx.executeStatement(SQL_STATEMENTS[i]);
			}
			
		} finally {
			dbCtx.release();
		}
	}
}
