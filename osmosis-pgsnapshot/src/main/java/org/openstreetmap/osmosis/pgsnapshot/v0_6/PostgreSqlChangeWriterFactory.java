// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsnapshot.v0_6;

import org.openstreetmap.osmosis.core.database.DatabaseTaskManagerFactory;
import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManager;
import org.openstreetmap.osmosis.core.pipeline.v0_6.ChangeSinkManager;


/**
 * The task manager factory for a database change writer.
 * 
 * @author Brett Henderson
 */
public class PostgreSqlChangeWriterFactory extends DatabaseTaskManagerFactory {
	
	private static final String ARG_KEEP_INVALID_WAYS = "keepInvalidWays";
	private static final boolean DEFAULT_KEEP_INVALID_WAYS = true;
	private static final String ARG_LOGGING = "logging";
	private static final boolean DEFAULT_LOGGING = false;
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		boolean keepInvalidWays = getBooleanArgument(taskConfig, ARG_KEEP_INVALID_WAYS, DEFAULT_KEEP_INVALID_WAYS);
		boolean logging = getBooleanArgument(taskConfig, ARG_LOGGING, DEFAULT_LOGGING);
		
		return new ChangeSinkManager(
			taskConfig.getId(),
			new PostgreSqlChangeWriter(
				getDatabaseLoginCredentials(taskConfig),
				getDatabasePreferences(taskConfig),
				keepInvalidWays,
				logging
			),
			taskConfig.getPipeArgs()
		);
	}
}
