// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.pgsql.v0_6;

import java.util.logging.Logger;

import com.bretth.osmosis.core.database.DatabaseLoginCredentials;
import com.bretth.osmosis.core.database.DatabasePreferences;
import com.bretth.osmosis.core.pgsql.common.DatabaseContext;
import com.bretth.osmosis.core.pgsql.common.SchemaVersionValidator;
import com.bretth.osmosis.core.task.common.RunnableTask;


/**
 * A standalone OSM task with no inputs or outputs that truncates tables in a
 * mysql database. This is used for removing all existing data from tables.
 * 
 * @author Brett Henderson
 */
public class PostgreSqlDatasetTruncator implements RunnableTask {
	
	private static final Logger log = Logger.getLogger(PostgreSqlDatasetTruncator.class.getName());
	
	
	// These SQL statements will be invoked to truncate each table.
	private static final String[] SQL_STATEMENTS = {
		"TRUNCATE users, nodes, node_tags, ways, way_tags, way_nodes, relations, relation_tags, relation_members"
	};
	
	
	private DatabaseContext dbCtx;
	private SchemaVersionValidator schemaVersionValidator;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param loginCredentials
	 *            Contains all information required to connect to the database.
	 * @param preferences
	 *            Contains preferences configuring database behaviour.
	 */
	public PostgreSqlDatasetTruncator(DatabaseLoginCredentials loginCredentials, DatabasePreferences preferences) {
		dbCtx = new DatabaseContext(loginCredentials);
		
		schemaVersionValidator = new SchemaVersionValidator(loginCredentials, preferences);
	}
	
	
	/**
	 * Truncates all data from the database.
	 */
	public void run() {
		try {
			schemaVersionValidator.validateVersion(PostgreSqlVersionConstants.SCHEMA_VERSION);
			
			log.fine("Truncating tables.");
			for (int i = 0; i < SQL_STATEMENTS.length; i++) {
				dbCtx.executeStatement(SQL_STATEMENTS[i]);
			}
			
			log.fine("Committing changes.");
			dbCtx.commit();
			
			log.fine("Vacuuming database.");
			dbCtx.setAutoCommit(true);
			dbCtx.executeStatement("VACUUM ANALYZE");
			log.fine("Complete.");
			
		} finally {
			dbCtx.release();
		}
	}
}
