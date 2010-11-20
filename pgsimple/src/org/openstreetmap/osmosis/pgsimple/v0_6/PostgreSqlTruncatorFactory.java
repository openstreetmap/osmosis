// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsimple.v0_6;

import org.openstreetmap.osmosis.core.database.DatabaseTaskManagerFactory;
import org.openstreetmap.osmosis.core.pipeline.common.RunnableTaskManager;
import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManager;


/**
 * The task manager factory for a database table truncator.
 * 
 * @author Brett Henderson
 */
public class PostgreSqlTruncatorFactory extends DatabaseTaskManagerFactory {
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		return new RunnableTaskManager(
			taskConfig.getId(),
			new PostgreSqlTruncator(
				getDatabaseLoginCredentials(taskConfig),
				getDatabasePreferences(taskConfig)
			),
			taskConfig.getPipeArgs()
		);
	}
}
