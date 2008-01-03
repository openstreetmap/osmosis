// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.mysql.v0_5;

import com.bretth.osmosis.core.cli.TaskConfiguration;
import com.bretth.osmosis.core.database.DatabaseLoginCredentials;
import com.bretth.osmosis.core.database.DatabasePreferences;
import com.bretth.osmosis.core.database.DatabaseTaskManagerFactory;
import com.bretth.osmosis.core.pipeline.common.TaskManager;
import com.bretth.osmosis.core.pipeline.v0_5.ChangeSinkManager;


/**
 * The task manager factory for a database change writer.
 * 
 * @author Brett Henderson
 */
public class MysqlChangeWriterFactory extends DatabaseTaskManagerFactory {
	private static final String ARG_POPULATE_CURRENT_TABLES = "populateCurrentTables";
	private static final boolean DEFAULT_POPULATE_CURRENT_TABLES = true;
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		DatabaseLoginCredentials loginCredentials;
		DatabasePreferences preferences;
		boolean populateCurrentTables;
		
		// Get the task arguments.
		loginCredentials = getDatabaseLoginCredentials(taskConfig);
		preferences = getDatabasePreferences(taskConfig);
		populateCurrentTables = getBooleanArgument(taskConfig, ARG_POPULATE_CURRENT_TABLES, DEFAULT_POPULATE_CURRENT_TABLES);
		
		return new ChangeSinkManager(
			taskConfig.getId(),
			new MysqlChangeWriter(
				loginCredentials,
				preferences,
				populateCurrentTables
			),
			taskConfig.getPipeArgs()
		);
	}
}
