// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsimple.v0_6;

import org.openstreetmap.osmosis.core.database.DatabaseLoginCredentials;
import org.openstreetmap.osmosis.core.database.DatabasePreferences;
import org.openstreetmap.osmosis.core.database.DatabaseTaskManagerFactory;
import org.openstreetmap.osmosis.pgsimple.common.NodeLocationStoreType;
import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManager;
import org.openstreetmap.osmosis.core.pipeline.v0_6.SinkManager;


/**
 * The task manager factory for a database writer using the PostgreSQL COPY method.
 * 
 * @author Brett Henderson
 */
public class PostgreSqlCopyWriterFactory extends DatabaseTaskManagerFactory {
	private static final String ARG_NODE_LOCATION_STORE_TYPE = "nodeLocationStoreType";
	private static final String DEFAULT_NODE_LOCATION_STORE_TYPE = "CompactTempFile";
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		DatabaseLoginCredentials loginCredentials;
		DatabasePreferences preferences;
		NodeLocationStoreType storeType;
		
		// Get the task arguments.
		loginCredentials = getDatabaseLoginCredentials(taskConfig);
		preferences = getDatabasePreferences(taskConfig);
		storeType = Enum.valueOf(
				NodeLocationStoreType.class,
				getStringArgument(taskConfig, ARG_NODE_LOCATION_STORE_TYPE, DEFAULT_NODE_LOCATION_STORE_TYPE));
		
		return new SinkManager(
			taskConfig.getId(),
			new PostgreSqlCopyWriter(loginCredentials, preferences,	storeType),
			taskConfig.getPipeArgs()
		);
	}
}
