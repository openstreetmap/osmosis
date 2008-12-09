// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.mysql.v0_6;

import java.util.Date;

import com.bretth.osmosis.core.database.DatabaseLoginCredentials;
import com.bretth.osmosis.core.database.DatabasePreferences;
import com.bretth.osmosis.core.database.DatabaseTaskManagerFactory;
import com.bretth.osmosis.core.pipeline.common.TaskConfiguration;
import com.bretth.osmosis.core.pipeline.common.TaskManager;
import com.bretth.osmosis.core.pipeline.v0_6.RunnableChangeSourceManager;


/**
 * The task manager factory for a database change reader.
 * 
 * @author Brett Henderson
 */
public class MysqlChangeReaderFactory extends DatabaseTaskManagerFactory {
	private static final String ARG_READ_ALL_USERS = "readAllUsers";
	private static final String ARG_INTERVAL_BEGIN = "intervalBegin";
	private static final String ARG_INTERVAL_END = "intervalEnd";
	private static final String ARG_READ_FULL_HISTORY = "readFullHistory";
	private static final boolean DEFAULT_READ_ALL_USERS = false;
	private static final boolean DEFAULT_READ_FULL_HISTORY = false;
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		DatabaseLoginCredentials loginCredentials;
		DatabasePreferences preferences;
		boolean readAllUsers;
		Date intervalBegin;
		Date intervalEnd;
		boolean fullHistory;
		
		// Get the task arguments.
		loginCredentials = getDatabaseLoginCredentials(taskConfig);
		preferences = getDatabasePreferences(taskConfig);
		readAllUsers = getBooleanArgument(taskConfig, ARG_READ_ALL_USERS, DEFAULT_READ_ALL_USERS);
		intervalBegin = getDateArgument(taskConfig, ARG_INTERVAL_BEGIN, new Date(0));
		intervalEnd = getDateArgument(taskConfig, ARG_INTERVAL_END, new Date());
		fullHistory = getBooleanArgument(taskConfig, ARG_READ_FULL_HISTORY, DEFAULT_READ_FULL_HISTORY);
		
		return new RunnableChangeSourceManager(
			taskConfig.getId(),
			new MysqlChangeReader(loginCredentials, preferences, readAllUsers, intervalBegin, intervalEnd, fullHistory),
			taskConfig.getPipeArgs()
		);
	}
}
