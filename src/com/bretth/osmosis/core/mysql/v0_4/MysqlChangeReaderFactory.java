package com.bretth.osmosis.core.mysql.v0_4;

import java.util.Date;
import java.util.Map;

import com.bretth.osmosis.core.database.DatabaseLoginCredentials;
import com.bretth.osmosis.core.database.DatabasePreferences;
import com.bretth.osmosis.core.database.DatabaseTaskManagerFactory;
import com.bretth.osmosis.core.pipeline.common.TaskManager;
import com.bretth.osmosis.core.pipeline.v0_4.RunnableChangeSourceManager;


/**
 * The task manager factory for a database change reader.
 * 
 * @author Brett Henderson
 */
public class MysqlChangeReaderFactory extends DatabaseTaskManagerFactory {
	private static final String ARG_READ_ALL_USERS = "readAllUsers";
	private static final String ARG_INTERVAL_BEGIN = "intervalBegin";
	private static final String ARG_INTERVAL_END = "intervalEnd";
	private static final boolean DEFAULT_READ_ALL_USERS = false;
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(String taskId, Map<String, String> taskArgs, Map<String, String> pipeArgs) {
		DatabaseLoginCredentials loginCredentials;
		DatabasePreferences preferences;
		boolean readAllUsers;
		Date intervalBegin;
		Date intervalEnd;
		
		// Get the task arguments.
		loginCredentials = getDatabaseLoginCredentials(taskId, taskArgs);
		preferences = getDatabasePreferences(taskId, taskArgs);
		readAllUsers = getBooleanArgument(taskId, taskArgs, ARG_READ_ALL_USERS, DEFAULT_READ_ALL_USERS);
		intervalBegin = getDateArgument(taskId, taskArgs, ARG_INTERVAL_BEGIN, new Date(0));
		intervalEnd = getDateArgument(taskId, taskArgs, ARG_INTERVAL_END, new Date());
		
		return new RunnableChangeSourceManager(
			taskId,
			new MysqlChangeReader(loginCredentials, preferences, readAllUsers, intervalBegin, intervalEnd),
			pipeArgs
		);
	}
}
