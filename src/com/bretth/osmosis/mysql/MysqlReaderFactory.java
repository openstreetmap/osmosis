package com.bretth.osmosis.mysql;

import java.util.Map;

import com.bretth.osmosis.pipeline.RunnableSourceManager;
import com.bretth.osmosis.pipeline.TaskManager;
import com.bretth.osmosis.pipeline.TaskManagerFactory;


/**
 * The task manager factory for a database reader.
 * 
 * @author Brett Henderson
 */
public class MysqlReaderFactory extends TaskManagerFactory {
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
		host = getStringArgument(taskId, taskArgs, ARG_HOST, DEFAULT_HOST);
		database = getStringArgument(taskId, taskArgs, ARG_DATABASE, DEFAULT_DATABASE);
		user = getStringArgument(taskId, taskArgs, ARG_USER, DEFAULT_USER);
		password = getStringArgument(taskId, taskArgs, ARG_PASSWORD, DEFAULT_PASSWORD);
		
		return new RunnableSourceManager(
			taskId,
			new MysqlReader(host, database, user, password),
			pipeArgs
		);
	}
}
