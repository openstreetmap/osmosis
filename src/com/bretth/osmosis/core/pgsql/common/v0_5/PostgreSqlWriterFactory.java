package com.bretth.osmosis.core.pgsql.common.v0_5;

import java.util.Map;

import com.bretth.osmosis.core.database.DatabaseLoginCredentials;
import com.bretth.osmosis.core.database.DatabasePreferences;
import com.bretth.osmosis.core.database.DatabaseTaskManagerFactory;
import com.bretth.osmosis.core.pipeline.common.TaskManager;
import com.bretth.osmosis.core.pipeline.v0_5.SinkManager;


/**
 * The task manager factory for a database writer.
 * 
 * @author Brett Henderson
 */
public class PostgreSqlWriterFactory extends DatabaseTaskManagerFactory {
	private static final String ARG_LOCK_TABLES = "lockTables";
	private static final String ARG_POPULATE_CURRENT_TABLES = "populateCurrentTables";
	private static final boolean DEFAULT_LOCK_TABLES = true;
	private static final boolean DEFAULT_POPULATE_CURRENT_TABLES = true;
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(String taskId, Map<String, String> taskArgs, Map<String, String> pipeArgs) {
		DatabaseLoginCredentials loginCredentials;
		DatabasePreferences preferences;
		boolean lockTables;
		boolean populateCurrentTables;
		
		// Get the task arguments.
		loginCredentials = getDatabaseLoginCredentials(taskId, taskArgs);
		preferences = getDatabasePreferences(taskId, taskArgs);
		lockTables = getBooleanArgument(taskId, taskArgs, ARG_LOCK_TABLES, DEFAULT_LOCK_TABLES);
		populateCurrentTables = getBooleanArgument(taskId, taskArgs, ARG_POPULATE_CURRENT_TABLES, DEFAULT_POPULATE_CURRENT_TABLES);
		
		return new SinkManager(
			taskId,
			new PostgreSqlWriter(loginCredentials, preferences, lockTables, populateCurrentTables),
			pipeArgs
		);
	}
}
