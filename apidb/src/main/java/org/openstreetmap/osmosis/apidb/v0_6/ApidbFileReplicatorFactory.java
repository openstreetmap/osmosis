// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.apidb.v0_6;

import org.openstreetmap.osmosis.core.database.DatabaseLoginCredentials;
import org.openstreetmap.osmosis.core.database.DatabasePreferences;
import org.openstreetmap.osmosis.core.database.DatabaseTaskManagerFactory;
import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManager;
import org.openstreetmap.osmosis.core.pipeline.v0_6.RunnableChangeSourceManager;


/**
 * The task factory for a file-based database replicator.
 */
public class ApidbFileReplicatorFactory extends DatabaseTaskManagerFactory {
	private static final String ARG_ITERATIONS = "iterations";
	private static final String ARG_INTERVAL = "interval";
	private static final int DEFAULT_ITERATIONS = 1;
	private static final int DEFAULT_INTERVAL = 5000;
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		DatabaseLoginCredentials loginCredentials;
		DatabasePreferences preferences;
		int iterations;
		int interval;
		
		// Get the task arguments.
		loginCredentials = getDatabaseLoginCredentials(taskConfig);
		preferences = getDatabasePreferences(taskConfig);
		iterations = getIntegerArgument(taskConfig, ARG_ITERATIONS, DEFAULT_ITERATIONS);
		interval = getIntegerArgument(taskConfig, ARG_INTERVAL, DEFAULT_INTERVAL);
		
		return new RunnableChangeSourceManager(
			taskConfig.getId(),
			new ApidbFileReplicator(loginCredentials, preferences, iterations, interval),
			taskConfig.getPipeArgs()
		);
	}
}
