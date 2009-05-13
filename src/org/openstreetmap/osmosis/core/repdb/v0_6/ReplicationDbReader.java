// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.repdb.v0_6;

import java.util.Date;

import org.openstreetmap.osmosis.core.apidb.common.DatabaseContext;
import org.openstreetmap.osmosis.core.database.DatabaseLoginCredentials;
import org.openstreetmap.osmosis.core.database.DatabasePreferences;
import org.openstreetmap.osmosis.core.pgsql.common.SchemaVersionValidator;
import org.openstreetmap.osmosis.core.repdb.v0_6.impl.ReplicationDbReaderImpl;
import org.openstreetmap.osmosis.core.repdb.v0_6.impl.ReplicationDbVersionConstants;
import org.openstreetmap.osmosis.core.task.v0_6.ChangeSink;
import org.openstreetmap.osmosis.core.task.v0_6.RunnableChangeSource;


/**
 * Reads a stream of changes from a replication database.
 */
public class ReplicationDbReader implements RunnableChangeSource {
	
	private ChangeSink changeSink;
	private DatabaseLoginCredentials loginCredentials;
	private DatabasePreferences preferences;
	private String queueName;
	private Date queueTimestamp;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param loginCredentials
	 *            Contains all information required to connect to the database.
	 * @param preferences
	 *            Contains preferences configuring database behaviour.
	 * @param queueName
	 *            The name of the queue to read from.
	 * @param queueTimestamp
	 *            The timestamp marking the end of the time period of change data to be retrieved.
	 */
	public ReplicationDbReader(DatabaseLoginCredentials loginCredentials, DatabasePreferences preferences,
			String queueName, Date queueTimestamp) {
		this.loginCredentials = loginCredentials;
		this.preferences = preferences;
		this.queueName = queueName;
		this.queueTimestamp = queueTimestamp;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		DatabaseContext dbCtx = new DatabaseContext(loginCredentials);
		ReplicationDbReaderImpl reader = new ReplicationDbReaderImpl(dbCtx, queueName);
		
		try {
			new SchemaVersionValidator(loginCredentials, preferences)
					.validateVersion(ReplicationDbVersionConstants.SCHEMA_VERSION);
			
			reader.setChangeSink(changeSink);
			reader.process(queueTimestamp);
			
			changeSink.complete();
			
		} finally {
			reader.release();
			dbCtx.release();
			changeSink.release();
		}
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setChangeSink(ChangeSink changeSink) {
		this.changeSink = changeSink;
	}
}
