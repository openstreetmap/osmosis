// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.apidb.v0_6;

import java.util.Arrays;
import java.util.List;

import org.openstreetmap.osmosis.apidb.common.DatabaseContext;
import org.openstreetmap.osmosis.apidb.v0_6.impl.SchemaVersionValidator;
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

	// These tables will be truncated by the sql query.
	private static final List<String> TRUNCATE_TABLES = Arrays.asList(new String[] {
		"current_relation_members",
			"current_relation_tags", "current_relations", "current_way_nodes",
			"current_way_tags", "current_ways", "current_node_tags",
			"current_nodes", "relation_members", "relation_tags", "relations",
			"way_nodes", "way_tags", "ways", "node_tags", "nodes",
			"changeset_tags", "changesets", "users"});

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

			dbCtx.truncateTables(TRUNCATE_TABLES);
			
			dbCtx.commit();

		} finally {
			dbCtx.release();
		}
	}
}
