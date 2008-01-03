// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.mysql.v0_5;

import com.bretth.osmosis.core.cli.TaskConfiguration;
import com.bretth.osmosis.core.database.DatabaseTaskManagerFactory;
import com.bretth.osmosis.core.pipeline.common.RunnableTaskManager;
import com.bretth.osmosis.core.pipeline.common.TaskManager;


/**
 * The task manager factory for a database table truncator.
 * 
 * @author Brett Henderson
 */
public class MysqlTruncatorFactory extends DatabaseTaskManagerFactory {
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		return new RunnableTaskManager(
			taskConfig.getId(),
			new MysqlTruncator(
				getDatabaseLoginCredentials(taskConfig),
				getDatabasePreferences(taskConfig)
			),
			taskConfig.getPipeArgs()
		);
	}
}
