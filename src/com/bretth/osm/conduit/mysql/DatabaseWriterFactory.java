package com.bretth.osm.conduit.mysql;

import java.util.Map;

import com.bretth.osm.conduit.pipeline.OsmSinkManager;
import com.bretth.osm.conduit.pipeline.TaskManager;
import com.bretth.osm.conduit.pipeline.TaskManagerFactory;


public class DatabaseWriterFactory extends TaskManagerFactory {
	private static final String TASK_TYPE = "write-mysql";
	private static final String ARG_HOST = "host";
	private static final String ARG_DATABASE = "host";
	private static final String ARG_USER = "host";
	private static final String ARG_PASSWORD = "host";
	private static final String DEFAULT_HOST = "localhost";
	private static final String DEFAULT_DATABASE = "osm";
	private static final String DEFAULT_USER = "osm";
	private static final String DEFAULT_PASSWORD = "";
	
	
	protected TaskManager createTaskManagerImpl(Map<String, String> taskArgs, Map<String, String> pipeArgs) {
		String host;
		String database;
		String user;
		String password;
		
		// Get the task arguments.
		if (taskArgs.containsKey(ARG_HOST)) {
			host = taskArgs.get(ARG_HOST);
		} else {
			host = DEFAULT_HOST;
		}
		if (taskArgs.containsKey(ARG_DATABASE)) {
			database = taskArgs.get(ARG_DATABASE);
		} else {
			database = DEFAULT_DATABASE;
		}
		if (taskArgs.containsKey(ARG_USER)) {
			user = taskArgs.get(ARG_USER);
		} else {
			user = DEFAULT_USER;
		}
		if (taskArgs.containsKey(ARG_PASSWORD)) {
			password = taskArgs.get(ARG_PASSWORD);
		} else {
			password = DEFAULT_PASSWORD;
		}
		
		return new OsmSinkManager(
			TASK_TYPE,
			new DatabaseWriter(host, database, user, password),
			pipeArgs
		);
	}
	
	
	protected String getTaskType() {
		return TASK_TYPE;
	}
}
