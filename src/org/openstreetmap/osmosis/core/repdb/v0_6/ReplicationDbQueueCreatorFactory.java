// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.repdb.v0_6;

import org.openstreetmap.osmosis.core.database.DatabaseLoginCredentials;
import org.openstreetmap.osmosis.core.database.DatabasePreferences;
import org.openstreetmap.osmosis.core.database.DatabaseTaskManagerFactory;
import org.openstreetmap.osmosis.core.pipeline.common.RunnableTaskManager;
import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManager;


/**
 * The task manager factory for a queue creator.
 */
public class ReplicationDbQueueCreatorFactory extends DatabaseTaskManagerFactory {

	private static final String ARG_QUEUE_NAME = "queueName";
	private static final String DEFAULT_QUEUE_NAME = "queue1";
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		DatabaseLoginCredentials loginCredentials;
		DatabasePreferences preferences;
		String queueName;
		
		// Get the task arguments.
		loginCredentials = getDatabaseLoginCredentials(taskConfig);
		preferences = getDatabasePreferences(taskConfig);
		queueName = getStringArgument(taskConfig, ARG_QUEUE_NAME, DEFAULT_QUEUE_NAME);
		
		return new RunnableTaskManager(
			taskConfig.getId(),
			new ReplicationDbQueueCreator(
				loginCredentials,
				preferences,
				queueName
			),
			taskConfig.getPipeArgs()
		);
	}
}
