// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.apidb.v0_6;

import org.openstreetmap.osmosis.core.apidb.common.DatabaseContext;
import org.openstreetmap.osmosis.core.apidb.v0_6.impl.SchemaVersionValidator;
import org.openstreetmap.osmosis.core.database.DatabaseLoginCredentials;
import org.openstreetmap.osmosis.core.database.DatabasePreferences;
import org.openstreetmap.osmosis.core.task.common.RunnableTask;


/**
 * A standalone OSM task with no inputs or outputs that truncates tables in a apidb database. This
 * is used for removing all existing data from tables.
 * 
 * @author Brett Henderson
 */
public class ApidbTruncator implements RunnableTask {

	// These SQL statements will be invoked to truncate each table.
	private static final String[] SQL_STATEMENTS = {"TRUNCATE current_relation_members",
			"TRUNCATE current_relation_tags", "TRUNCATE current_relations", "TRUNCATE current_way_nodes",
			"TRUNCATE current_way_tags", "TRUNCATE current_ways", "TRUNCATE current_node_tags",
			"TRUNCATE current_nodes", "TRUNCATE relation_members", "TRUNCATE relation_tags", "TRUNCATE relations",
			"TRUNCATE way_nodes", "TRUNCATE way_tags", "TRUNCATE ways", "TRUNCATE node_tags", "TRUNCATE nodes",
			"TRUNCATE changeset_tags", "TRUNCATE changesets", "TRUNCATE users" };

	private final DatabaseContext dbCtx;

	private final SchemaVersionValidator schemaVersionValidator;


	/**
	 * Creates a new instance.
	 * 
	 * @param loginCredentials
	 *            Contains all information required to connect to the database.
	 * @param preferences
	 *            Contains preferences configuring database behaviour.
	 */
	public ApidbTruncator(DatabaseLoginCredentials loginCredentials, DatabasePreferences preferences) {
		dbCtx = new DatabaseContext(loginCredentials);

		schemaVersionValidator = new SchemaVersionValidator(loginCredentials, preferences);
	}


	/**
	 * Truncates all data from the database.
	 */
	public void run() {
		try {
			schemaVersionValidator.validateVersion(ApidbVersionConstants.SCHEMA_MIGRATIONS);

			for (String element : SQL_STATEMENTS) {
				dbCtx.executeStatement(element);
			}

		} finally {
			dbCtx.release();
		}
	}
}
