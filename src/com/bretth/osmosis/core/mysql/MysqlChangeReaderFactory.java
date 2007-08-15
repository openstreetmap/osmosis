package com.bretth.osmosis.core.mysql;

import java.util.Date;
import java.util.Map;

import com.bretth.osmosis.core.pipeline.RunnableChangeSourceManager;
import com.bretth.osmosis.core.pipeline.TaskManager;
import com.bretth.osmosis.core.pipeline.TaskManagerFactory;


/**
 * The task manager factory for a database change reader.
 * 
 * @author Brett Henderson
 */
public class MysqlChangeReaderFactory extends TaskManagerFactory {
	private static final String ARG_HOST = "host";
	private static final String ARG_DATABASE = "database";
	private static final String ARG_USER = "user";
	private static final String ARG_PASSWORD = "password";
	private static final String ARG_INTERVAL_BEGIN = "intervalBegin";
	private static final String ARG_INTERVAL_END = "intervalEnd";
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
		Date intervalBegin;
		Date intervalEnd;
		
		// Get the task arguments.
		host = getStringArgument(taskId, taskArgs, ARG_HOST, DEFAULT_HOST);
		database = getStringArgument(taskId, taskArgs, ARG_DATABASE, DEFAULT_DATABASE);
		user = getStringArgument(taskId, taskArgs, ARG_USER, DEFAULT_USER);
		password = getStringArgument(taskId, taskArgs, ARG_PASSWORD, DEFAULT_PASSWORD);
		intervalBegin = getDateArgument(taskId, taskArgs, ARG_INTERVAL_BEGIN, new Date(0));
		intervalEnd = getDateArgument(taskId, taskArgs, ARG_INTERVAL_END, new Date());
		
		return new RunnableChangeSourceManager(
			taskId,
			new MysqlChangeReader(host, database, user, password, intervalBegin, intervalEnd),
			pipeArgs
		);
	}
}
