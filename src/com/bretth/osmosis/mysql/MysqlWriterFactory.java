package com.bretth.osmosis.mysql;

import java.util.Map;

import com.bretth.osmosis.pipeline.SinkManager;
import com.bretth.osmosis.pipeline.TaskManager;
import com.bretth.osmosis.pipeline.TaskManagerFactory;


/**
 * The task manager factory for a database writer.
 * 
 * @author Brett Henderson
 */
public class MysqlWriterFactory extends TaskManagerFactory {
	private static final String ARG_HOST = "host";
	private static final String ARG_DATABASE = "database";
	private static final String ARG_USER = "user";
	private static final String ARG_PASSWORD = "password";
	private static final String ARG_LOCK_TABLES = "lockTables";
	private static final String DEFAULT_HOST = "localhost";
	private static final String DEFAULT_DATABASE = "osm";
	private static final String DEFAULT_USER = "osm";
	private static final String DEFAULT_PASSWORD = "";
	private static final String DEFAULT_LOCK_TABLES = "true";
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(String taskId, Map<String, String> taskArgs, Map<String, String> pipeArgs) {
		String host;
		String database;
		String user;
		String password;
		boolean lockTables;
		
		// Get the task arguments.
		host = getStringArgument(taskArgs, ARG_HOST, DEFAULT_HOST);
		database = getStringArgument(taskArgs, ARG_DATABASE, DEFAULT_DATABASE);
		user = getStringArgument(taskArgs, ARG_USER, DEFAULT_USER);
		password = getStringArgument(taskArgs, ARG_PASSWORD, DEFAULT_PASSWORD);
		lockTables = getBooleanArgument(taskArgs, ARG_LOCK_TABLES, DEFAULT_LOCK_TABLES);
		
		return new SinkManager(
			taskId,
			new MysqlWriter(host, database, user, password, lockTables),
			pipeArgs
		);
	}
}
