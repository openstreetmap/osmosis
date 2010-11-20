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
 * The task manager factory for a database writer.
 * 
 * @author Brett Henderson
 */
public class PostgreSqlWriterFactory extends DatabaseTaskManagerFactory {
	private static final String ARG_ENABLE_BBOX_BUILDER = "enableBboxBuilder";
	private static final String ARG_ENABLE_LINESTRING_BUILDER = "enableLinestringBuilder";
	private static final String ARG_NODE_LOCATION_STORE_TYPE = "nodeLocationStoreType";
	private static final boolean DEFAULT_ENABLE_BBOX_BUILDER = false;
	private static final boolean DEFAULT_ENABLE_LINESTRING_BUILDER = false;
	private static final String DEFAULT_NODE_LOCATION_STORE_TYPE = "CompactTempFile";
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		DatabaseLoginCredentials loginCredentials;
		DatabasePreferences preferences;
		boolean enableBboxBuilder;
		boolean enableLinestringBuilder;
		NodeLocationStoreType storeType;
		
		// Get the task arguments.
		loginCredentials = getDatabaseLoginCredentials(taskConfig);
		preferences = getDatabasePreferences(taskConfig);
		enableBboxBuilder = getBooleanArgument(taskConfig, ARG_ENABLE_BBOX_BUILDER, DEFAULT_ENABLE_BBOX_BUILDER);
		enableLinestringBuilder = getBooleanArgument(
				taskConfig, ARG_ENABLE_LINESTRING_BUILDER, DEFAULT_ENABLE_LINESTRING_BUILDER);
		storeType = Enum.valueOf(
				NodeLocationStoreType.class,
				getStringArgument(taskConfig, ARG_NODE_LOCATION_STORE_TYPE, DEFAULT_NODE_LOCATION_STORE_TYPE));
		
		return new SinkManager(
			taskConfig.getId(),
			new PostgreSqlWriter(loginCredentials, preferences, enableBboxBuilder, enableLinestringBuilder, storeType),
			taskConfig.getPipeArgs()
		);
	}
}
