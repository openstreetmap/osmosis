// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.repdb.v0_6;

import org.openstreetmap.osmosis.core.apidb.common.DatabaseContext;
import org.openstreetmap.osmosis.core.database.DatabaseLoginCredentials;
import org.openstreetmap.osmosis.core.database.DatabasePreferences;
import org.openstreetmap.osmosis.core.pgsql.common.SchemaVersionValidator;
import org.openstreetmap.osmosis.core.repdb.v0_6.impl.QueueManager;
import org.openstreetmap.osmosis.core.repdb.v0_6.impl.ReplicationDbVersionConstants;
import org.openstreetmap.osmosis.core.task.common.RunnableTask;


/**
 * Creates a new queue in a replication database.
 */
public class ReplicationDbQueueCreator implements RunnableTask {
	
	private DatabaseLoginCredentials loginCredentials;
	private DatabasePreferences preferences;
	private String queueName;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param loginCredentials
	 *            Contains all information required to connect to the database.
	 * @param preferences
	 *            Contains preferences configuring database behaviour.
	 * @param queueName
	 *            The name of the queue to read from.
	 */
	public ReplicationDbQueueCreator(DatabaseLoginCredentials loginCredentials, DatabasePreferences preferences,
			String queueName) {
		this.loginCredentials = loginCredentials;
		this.preferences = preferences;
		this.queueName = queueName;
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		DatabaseContext dbCtx = new DatabaseContext(loginCredentials);
		QueueManager queueMgr = new QueueManager(dbCtx); 
		
		try {
			new SchemaVersionValidator(loginCredentials, preferences)
					.validateVersion(ReplicationDbVersionConstants.SCHEMA_VERSION);
			
			queueMgr.createQueue(queueName);
			
			dbCtx.commit();
			
		} finally {
			queueMgr.release();
			dbCtx.release();
		}
	}
}
