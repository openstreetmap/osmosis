package com.bretth.osmosis.core.mysql.v0_5;

import java.util.Map;

import com.bretth.osmosis.core.mysql.common.MysqlTaskManagerFactory;
import com.bretth.osmosis.core.pipeline.common.TaskManager;
import com.bretth.osmosis.core.pipeline.v0_5.ChangeSinkManager;


/**
 * The task manager factory for a database change writer.
 * 
 * @author Brett Henderson
 */
public class MysqlChangeWriterFactory extends MysqlTaskManagerFactory {
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(String taskId, Map<String, String> taskArgs, Map<String, String> pipeArgs) {
		return new ChangeSinkManager(
			taskId,
			new MysqlChangeWriter(
				getDatabaseLoginCredentials(taskId, taskArgs),
				getDatabasePreferences(taskId, taskArgs)
			),
			pipeArgs
		);
	}
}
