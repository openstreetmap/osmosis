// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.repdb.v0_6;

import java.util.Date;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.apidb.common.DatabaseContext;
import org.openstreetmap.osmosis.core.database.DatabaseLoginCredentials;
import org.openstreetmap.osmosis.core.database.DatabasePreferences;
import org.openstreetmap.osmosis.core.pgsql.common.SchemaVersionValidator;
import org.openstreetmap.osmosis.core.repdb.v0_6.impl.QueueManager;
import org.openstreetmap.osmosis.core.repdb.v0_6.impl.ReplicationDbVersionConstants;
import org.openstreetmap.osmosis.core.repdb.v0_6.impl.SystemTimestampManager;
import org.openstreetmap.osmosis.core.task.common.RunnableTask;


/**
 * Modifies the current position of a queue.
 */
public class ReplicationDbQueueSeeker implements RunnableTask {
	
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
	 *            The name of the queue.
	 * @param queueTimestamp
	 *            The timestamp to seek the queue to.
	 */
	public ReplicationDbQueueSeeker(DatabaseLoginCredentials loginCredentials, DatabasePreferences preferences,
			String queueName, Date queueTimestamp) {
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
		SystemTimestampManager systemTimestampManager = new SystemTimestampManager(dbCtx);
		
		try {
			Date systemTimestamp;
			
			new SchemaVersionValidator(loginCredentials, preferences)
					.validateVersion(ReplicationDbVersionConstants.SCHEMA_VERSION);
			
			// The timestamp that we launch the process with must be less than or equal to the system
			// timestamp. A queue cannot read past the time allowed by the system time.
			systemTimestamp = systemTimestampManager.getTimestamp();
			if (queueTimestamp.compareTo(systemTimestamp) < 0) {
				throw new OsmosisRuntimeException("The requested queue timestamp of " + queueTimestamp
						+ " exceeds the current system timestamp of " + systemTimestamp + ".");
			}
			
			queueMgr.seekQueue(queueName, queueTimestamp);
			
			dbCtx.commit();
			
		} finally {
			systemTimestampManager.release();
			queueMgr.release();
			dbCtx.release();
		}
	}
}
