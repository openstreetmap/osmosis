package com.bretth.osmosis.core.mysql.v0_5;

import com.bretth.osmosis.core.cli.TaskConfiguration;
import com.bretth.osmosis.core.database.DatabaseTaskManagerFactory;
import com.bretth.osmosis.core.pipeline.common.TaskManager;
import com.bretth.osmosis.core.pipeline.v0_5.ChangeSinkManager;


/**
 * The task manager factory for a database change writer.
 * 
 * @author Brett Henderson
 */
public class MysqlChangeWriterFactory extends DatabaseTaskManagerFactory {
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		return new ChangeSinkManager(
			taskConfig.getId(),
			new MysqlChangeWriter(
				getDatabaseLoginCredentials(taskConfig),
				getDatabasePreferences(taskConfig)
			),
			taskConfig.getPipeArgs()
		);
	}
}
