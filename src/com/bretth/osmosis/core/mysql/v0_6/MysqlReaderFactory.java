// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.mysql.v0_6;

import java.util.Date;

import com.bretth.osmosis.core.cli.TaskConfiguration;
import com.bretth.osmosis.core.database.DatabaseLoginCredentials;
import com.bretth.osmosis.core.database.DatabasePreferences;
import com.bretth.osmosis.core.database.DatabaseTaskManagerFactory;
import com.bretth.osmosis.core.pipeline.common.TaskManager;
import com.bretth.osmosis.core.pipeline.v0_6.RunnableSourceManager;


/**
 * The task manager factory for a database reader.
 * 
 * @author Brett Henderson
 */
public class MysqlReaderFactory extends DatabaseTaskManagerFactory {
	private static final String ARG_READ_ALL_USERS = "readAllUsers";
	private static final String ARG_SNAPSHOT_INSTANT = "snapshotInstant";
	private static final boolean DEFAULT_READ_ALL_USERS = false;
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		DatabaseLoginCredentials loginCredentials;
		DatabasePreferences preferences;
		boolean readAllUsers;
		Date snapshotInstant;
		
		// Get the task arguments.
		loginCredentials = getDatabaseLoginCredentials(taskConfig);
		preferences = getDatabasePreferences(taskConfig);
		readAllUsers = getBooleanArgument(taskConfig, ARG_READ_ALL_USERS, DEFAULT_READ_ALL_USERS);
		snapshotInstant = getDateArgument(taskConfig, ARG_SNAPSHOT_INSTANT, new Date());
		
		return new RunnableSourceManager(
			taskConfig.getId(),
			new MysqlReader(loginCredentials, preferences, snapshotInstant, readAllUsers),
			taskConfig.getPipeArgs()
		);
	}
}
