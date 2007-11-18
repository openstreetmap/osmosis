package com.bretth.osmosis.core.mysql.v0_5;

import com.bretth.osmosis.core.cli.TaskConfiguration;
import com.bretth.osmosis.core.database.DatabaseLoginCredentials;
import com.bretth.osmosis.core.database.DatabasePreferences;
import com.bretth.osmosis.core.database.DatabaseTaskManagerFactory;
import com.bretth.osmosis.core.pipeline.common.TaskManager;
import com.bretth.osmosis.core.pipeline.v0_5.RunnableSourceManager;


/**
 * The task manager factory for a database reader.
 * 
 * @author Brett Henderson
 */
public class MySqlCurrentReaderFactory extends DatabaseTaskManagerFactory {
	private static final String ARG_READ_ALL_USERS = "readAllUsers";
	private static final boolean DEFAULT_READ_ALL_USERS = false;
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		DatabaseLoginCredentials loginCredentials;
		DatabasePreferences preferences;
		boolean readAllUsers;
		
		// Get the task arguments.
		loginCredentials = getDatabaseLoginCredentials(taskConfig);
		preferences = getDatabasePreferences(taskConfig);
		readAllUsers = getBooleanArgument(taskConfig, ARG_READ_ALL_USERS, DEFAULT_READ_ALL_USERS);
		
		return new RunnableSourceManager(
			taskConfig.getId(),
			new MySqlCurrentReader(loginCredentials, preferences, readAllUsers),
			taskConfig.getPipeArgs()
		);
	}
}
