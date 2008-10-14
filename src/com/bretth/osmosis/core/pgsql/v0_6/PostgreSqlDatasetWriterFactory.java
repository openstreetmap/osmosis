// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.pgsql.v0_6;

import com.bretth.osmosis.core.database.DatabaseLoginCredentials;
import com.bretth.osmosis.core.database.DatabasePreferences;
import com.bretth.osmosis.core.database.DatabaseTaskManagerFactory;
import com.bretth.osmosis.core.pipeline.common.TaskConfiguration;
import com.bretth.osmosis.core.pipeline.common.TaskManager;
import com.bretth.osmosis.core.pipeline.v0_6.SinkManager;


/**
 * The task manager factory for a database writer.
 * 
 * @author Brett Henderson
 */
public class PostgreSqlDatasetWriterFactory extends DatabaseTaskManagerFactory {
	private static final String ARG_IN_MEMORY_BBOX = "inMemoryBbox";
	private static final String ARG_IN_MEMORY_LINESTRING = "inMemoryLinestring";
	private static final boolean DEFAULT_IN_MEMORY_BBOX = false;
	private static final boolean DEFAULT_IN_MEMORY_LINESTRING = false;
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		DatabaseLoginCredentials loginCredentials;
		DatabasePreferences preferences;
		boolean inMemoryBbox;
		boolean inMemoryLinestring;
		
		// Get the task arguments.
		loginCredentials = getDatabaseLoginCredentials(taskConfig);
		preferences = getDatabasePreferences(taskConfig);
		inMemoryBbox = getBooleanArgument(taskConfig, ARG_IN_MEMORY_BBOX, DEFAULT_IN_MEMORY_BBOX);
		inMemoryLinestring = getBooleanArgument(taskConfig, ARG_IN_MEMORY_LINESTRING, DEFAULT_IN_MEMORY_LINESTRING);
		
		return new SinkManager(
			taskConfig.getId(),
			new PostgreSqlWriter(loginCredentials, preferences, inMemoryBbox, inMemoryLinestring),
			taskConfig.getPipeArgs()
		);
	}
}
