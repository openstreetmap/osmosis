// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsimple.v0_6;

import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.database.DatabaseLoginCredentials;
import org.openstreetmap.osmosis.core.database.DatabasePreferences;
import org.openstreetmap.osmosis.pgsimple.common.DatabaseContext;
import org.openstreetmap.osmosis.pgsimple.common.SchemaVersionValidator;
import org.openstreetmap.osmosis.core.task.common.RunnableTask;


/**
 * A standalone OSM task with no inputs or outputs that truncates tables in a
 * PostgreSQL database. This is used for removing all existing data from tables.
 * 
 * @author Brett Henderson
 */
public class PostgreSqlTruncator implements RunnableTask {
	
	private static final Logger LOG = Logger.getLogger(PostgreSqlTruncator.class.getName());
	
	
	// These tables will be truncated.
	private static final String[] SQL_TABLE_NAMES = {
		"actions",
		"users",
		"nodes", "node_tags",
		"ways", "way_tags", "way_nodes",
		"relations", "relation_tags", "relation_members"
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
	public PostgreSqlTruncator(DatabaseLoginCredentials loginCredentials, DatabasePreferences preferences) {
		dbCtx = new DatabaseContext(loginCredentials);
		
		schemaVersionValidator = new SchemaVersionValidator(dbCtx, preferences);
	}
	
	
	/**
	 * Truncates all data from the database.
	 */
	public void run() {
		try {
			schemaVersionValidator.validateVersion(PostgreSqlVersionConstants.SCHEMA_VERSION);
			
			LOG.fine("Truncating tables.");
			for (int i = 0; i < SQL_TABLE_NAMES.length; i++) {
				if (dbCtx.doesTableExist(SQL_TABLE_NAMES[i])) {
					LOG.finer("Truncating table " + SQL_TABLE_NAMES[i] + ".");
					dbCtx.executeStatement("TRUNCATE " + SQL_TABLE_NAMES[i]);
				} else {
					LOG.finer("Skipping table " + SQL_TABLE_NAMES[i] + " which doesn't exist in the current schema.");
				}
			}
			
			LOG.fine("Committing changes.");
			dbCtx.commit();
			
			LOG.fine("Vacuuming database.");
			dbCtx.setAutoCommit(true);
			dbCtx.executeStatement("VACUUM ANALYZE");
			LOG.fine("Complete.");
			
		} finally {
			dbCtx.release();
		}
	}
}
