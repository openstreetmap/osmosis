package com.bretth.osmosis.mysql;

import java.util.Map;

import com.bretth.osmosis.pipeline.RunnableTaskManager;
import com.bretth.osmosis.pipeline.TaskManager;
import com.bretth.osmosis.pipeline.TaskManagerFactory;


/**
 * The task manager factory for a database table truncator.
 * 
 * @author Brett Henderson
 */
public class MysqlTruncatorFactory extends TaskManagerFactory {
	private static final String ARG_HOST = "host";
	private static final String ARG_DATABASE = "database";
	private static final String ARG_USER = "user";
	private static final String ARG_PASSWORD = "password";
	private static final String DEFAULT_HOST = "localhost";
	private static final String DEFAULT_DATABASE = "osm";
	private static final String DEFAULT_USER = "osm";
	private static final String DEFAULT_PASSWORD = "";
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(String taskId, Map<String, String> taskArgs, Map<String, String> pipeArgs) {
		String host;
		String database;
		String user;
		String password;
		
		// Get the task arguments.
		host = getStringArgument(taskArgs, ARG_HOST, DEFAULT_HOST);
		database = getStringArgument(taskArgs, ARG_DATABASE, DEFAULT_DATABASE);
		user = getStringArgument(taskArgs, ARG_USER, DEFAULT_USER);
		password = getStringArgument(taskArgs, ARG_PASSWORD, DEFAULT_PASSWORD);
		
		return new RunnableTaskManager(
			taskId,
			new MysqlTruncator(host, database, user, password),
			pipeArgs
		);
	}
}
