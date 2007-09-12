package com.bretth.osmosis.core.mysql.v0_4;

import java.io.File;
import java.util.Map;

import com.bretth.osmosis.core.mysql.common.AuthenticationPropertiesLoader;
import com.bretth.osmosis.core.mysql.common.DatabaseLoginCredentials;
import com.bretth.osmosis.core.mysql.common.MysqlConstants;
import com.bretth.osmosis.core.pipeline.common.TaskManagerFactory;


/**
 * Extends the basic task manager factory functionality with MySQL task specific common methods.
 * 
 * @author Brett Henderson
 */
public abstract class MysqlTaskManagerFactory extends TaskManagerFactory {
	
	/**
	 * Utility method for retrieving the login credentials for a database connection.
	 * 
	 * @param taskId
	 *            The identifier for the task retrieving the parameter.
	 * @param taskArgs
	 *            The task arguments.
	 * @return The value of the argument.
	 */
	protected DatabaseLoginCredentials getDatabaseLoginCredentials(
			String taskId, Map<String, String> taskArgs) {
		DatabaseLoginCredentials loginCredentials;
		
		// Create a new credential object with default values.
		loginCredentials = new DatabaseLoginCredentials(
			MysqlConstants.TASK_DEFAULT_HOST,
			MysqlConstants.TASK_DEFAULT_DATABASE,
			MysqlConstants.TASK_DEFAULT_USER,
			MysqlConstants.TASK_DEFAULT_PASSWORD
		);
		
		// If an authentication properties file has been supplied, load override
		// values from there.
		if (doesArgumentExist(taskArgs, MysqlConstants.TASK_ARG_AUTH_FILE)) {
			AuthenticationPropertiesLoader authLoader;
			
			authLoader = new AuthenticationPropertiesLoader(
				new File(
					getStringArgument(taskId, taskArgs, MysqlConstants.TASK_ARG_AUTH_FILE)
				)
			);
			
			authLoader.updateLoginCredentials(loginCredentials);
		}
		
		// Update the credentials with any explicit arguments provided on the
		// command line.
		loginCredentials.setHost(
			getStringArgument(
				taskId,
				taskArgs,
				MysqlConstants.TASK_ARG_HOST,
				loginCredentials.getHost()
			)
		);
		loginCredentials.setDatabase(
			getStringArgument(
				taskId,
				taskArgs,
				MysqlConstants.TASK_ARG_DATABASE,
				loginCredentials.getDatabase()
			)
		);
		loginCredentials.setUser(
			getStringArgument(
				taskId,
				taskArgs,
				MysqlConstants.TASK_ARG_USER,
				loginCredentials.getUser()
			)
		);
		loginCredentials.setPassword(
			getStringArgument(
				taskId,
				taskArgs,
				MysqlConstants.TASK_ARG_PASSWORD,
				loginCredentials.getPassword()
			)
		);
		
		return loginCredentials;
	}
}
