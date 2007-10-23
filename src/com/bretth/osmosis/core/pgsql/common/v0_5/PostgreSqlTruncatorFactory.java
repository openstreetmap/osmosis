package com.bretth.osmosis.core.pgsql.common.v0_5;

import java.util.Map;

import com.bretth.osmosis.core.database.DatabaseTaskManagerFactory;
import com.bretth.osmosis.core.pipeline.common.RunnableTaskManager;
import com.bretth.osmosis.core.pipeline.common.TaskManager;


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
	protected TaskManager createTaskManagerImpl(String taskId, Map<String, String> taskArgs, Map<String, String> pipeArgs) {
		return new RunnableTaskManager(
			taskId,
			new PostgreSqlTruncator(
				getDatabaseLoginCredentials(taskId, taskArgs),
				getDatabasePreferences(taskId, taskArgs)
			),
			pipeArgs
		);
	}
}
