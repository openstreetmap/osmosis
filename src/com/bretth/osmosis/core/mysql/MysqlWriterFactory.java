package com.bretth.osmosis.core.mysql;

import java.util.Map;

import com.bretth.osmosis.core.pipeline.SinkManager;
import com.bretth.osmosis.core.pipeline.TaskManager;
import com.bretth.osmosis.core.pipeline.TaskManagerFactory;


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
	private static final String ARG_POPULATE_CURRENT_TABLES = "populateCurrentTables";
	private static final String DEFAULT_HOST = "localhost";
	private static final String DEFAULT_DATABASE = "osm";
	private static final String DEFAULT_USER = "osm";
	private static final String DEFAULT_PASSWORD = "";
	private static final boolean DEFAULT_LOCK_TABLES = true;
	private static final boolean DEFAULT_POPULATE_CURRENT_TABLES = true;
	
	
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
		boolean populateCurrentTables;
		
		// Get the task arguments.
		host = getStringArgument(taskId, taskArgs, ARG_HOST, DEFAULT_HOST);
		database = getStringArgument(taskId, taskArgs, ARG_DATABASE, DEFAULT_DATABASE);
		user = getStringArgument(taskId, taskArgs, ARG_USER, DEFAULT_USER);
		password = getStringArgument(taskId, taskArgs, ARG_PASSWORD, DEFAULT_PASSWORD);
		lockTables = getBooleanArgument(taskId, taskArgs, ARG_LOCK_TABLES, DEFAULT_LOCK_TABLES);
		populateCurrentTables = getBooleanArgument(taskId, taskArgs, ARG_POPULATE_CURRENT_TABLES, DEFAULT_POPULATE_CURRENT_TABLES);
		
		return new SinkManager(
			taskId,
			new MysqlWriter(host, database, user, password, lockTables, populateCurrentTables),
			pipeArgs
		);
	}
}
