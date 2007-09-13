package com.bretth.osmosis.core.mysql.v0_5;

import java.util.Date;
import java.util.Map;

import com.bretth.osmosis.core.mysql.common.DatabaseLoginCredentials;
import com.bretth.osmosis.core.mysql.common.MysqlTaskManagerFactory;
import com.bretth.osmosis.core.pipeline.common.TaskManager;
import com.bretth.osmosis.core.pipeline.v0_5.RunnableSourceManager;


/**
 * The task manager factory for a database reader.
 * 
 * @author Brett Henderson
 */
public class MysqlReaderFactory extends MysqlTaskManagerFactory {
	private static final String ARG_READ_ALL_USERS = "readAllUsers";
	private static final String ARG_SNAPSHOT_INSTANT = "snapshotInstant";
	private static final boolean DEFAULT_READ_ALL_USERS = false;
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(String taskId, Map<String, String> taskArgs, Map<String, String> pipeArgs) {
		DatabaseLoginCredentials loginCredentials;
		boolean readAllUsers;
		Date snapshotInstant;
		
		// Get the task arguments.
		loginCredentials = getDatabaseLoginCredentials(taskId, taskArgs);
		readAllUsers = getBooleanArgument(taskId, taskArgs, ARG_READ_ALL_USERS, DEFAULT_READ_ALL_USERS);
		snapshotInstant = getDateArgument(taskId, taskArgs, ARG_SNAPSHOT_INSTANT, new Date());
		
		return new RunnableSourceManager(
			taskId,
			new MysqlReader(loginCredentials, snapshotInstant, readAllUsers),
			pipeArgs
		);
	}
}
