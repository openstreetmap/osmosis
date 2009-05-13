// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.repdb.v0_6;

import java.util.Date;

import org.openstreetmap.osmosis.core.database.DatabaseLoginCredentials;
import org.openstreetmap.osmosis.core.database.DatabasePreferences;
import org.openstreetmap.osmosis.core.database.DatabaseTaskManagerFactory;
import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManager;
import org.openstreetmap.osmosis.core.pipeline.v0_6.RunnableChangeSourceManager;


/**
 * The task manager factory for a replication database reader.
 * 
 * @author Brett Henderson
 */
public class ReplicationDbReaderFactory extends DatabaseTaskManagerFactory {

	private static final String ARG_QUEUE_NAME = "queueName";
	private static final String DEFAULT_QUEUE_NAME = "queue1";
	private static final String ARG_QUEUE_TIMESTAMP = "queueTimestamp";
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		DatabaseLoginCredentials loginCredentials;
		DatabasePreferences preferences;
		String queueName;
		Date queueTimestamp;
		
		// Get the task arguments.
		loginCredentials = getDatabaseLoginCredentials(taskConfig);
		preferences = getDatabasePreferences(taskConfig);
		queueName = getStringArgument(taskConfig, ARG_QUEUE_NAME, DEFAULT_QUEUE_NAME);
		queueTimestamp = getDateArgument(taskConfig, ARG_QUEUE_TIMESTAMP, new Date());
		
		return new RunnableChangeSourceManager(
			taskConfig.getId(),
			new ReplicationDbReader(
				loginCredentials,
				preferences,
				queueName,
				queueTimestamp
			),
			taskConfig.getPipeArgs()
		);
	}
}
