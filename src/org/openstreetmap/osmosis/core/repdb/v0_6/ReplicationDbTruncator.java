// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.repdb.v0_6;

import java.util.Arrays;

import org.openstreetmap.osmosis.core.apidb.common.DatabaseContext;
import org.openstreetmap.osmosis.core.database.DatabaseLoginCredentials;
import org.openstreetmap.osmosis.core.database.DatabasePreferences;
import org.openstreetmap.osmosis.core.pgsql.common.SchemaVersionValidator;
import org.openstreetmap.osmosis.core.repdb.v0_6.impl.ReplicationDbVersionConstants;
import org.openstreetmap.osmosis.core.task.common.RunnableTask;


/**
 * Completely empties a replication database.
 */
public class ReplicationDbTruncator implements RunnableTask {
	
	private DatabaseLoginCredentials loginCredentials;
	private DatabasePreferences preferences;


	/**
	 * Creates a new instance.
	 * 
	 * @param loginCredentials
	 *            Contains all information required to connect to the database.
	 * @param preferences
	 *            Contains preferences configuring database behaviour.
	 */
	public ReplicationDbTruncator(DatabaseLoginCredentials loginCredentials, DatabasePreferences preferences) {
		this.loginCredentials = loginCredentials;
		this.preferences = preferences;
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		DatabaseContext dbCtx = new DatabaseContext(loginCredentials);
		
		try {
			new SchemaVersionValidator(loginCredentials, preferences)
					.validateVersion(ReplicationDbVersionConstants.SCHEMA_VERSION);
			
			dbCtx.truncateTables(Arrays.asList(new String[]{"queue", "item", "system"}));
			dbCtx.executeStatement("INSERT INTO system (id, tstamp) VALUES (1, timestamp 'January 1, 1970')");
			
			dbCtx.commit();
			
		} finally {
			dbCtx.release();
		}
	}
}
