// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.customdb.v0_5;

import java.io.File;

import com.bretth.osmosis.core.cli.TaskConfiguration;
import com.bretth.osmosis.core.pipeline.common.TaskManager;
import com.bretth.osmosis.core.pipeline.common.TaskManagerFactory;
import com.bretth.osmosis.core.pipeline.v0_5.RunnableDatasetSourceManager;


/**
 * The task manager factory for a custom database reader.
 * 
 * @author Brett Henderson
 */
public class ReadDatasetFactory extends TaskManagerFactory {
	private static final String ARG_DIRECTORY_NAME = "directory";
	private static final String ARG_ENABLE_WAY_TILE_INDEX = "enableWayTileIndex";
	private static final String DEFAULT_DIRECTORY_NAME = "dataset";
	private static final boolean DEFAULT_ENABLE_WAY_TILE_INDEX = false;
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		String directoryName;
		File directory;
		boolean enableWayTileIndex;
		ReadDataset task;
		
		// Get the task arguments.
		directoryName = getStringArgument(
			taskConfig,
			ARG_DIRECTORY_NAME,
			getDefaultStringArgument(taskConfig, DEFAULT_DIRECTORY_NAME)
		);
		enableWayTileIndex = getBooleanArgument(
			taskConfig,
			ARG_ENABLE_WAY_TILE_INDEX,
			DEFAULT_ENABLE_WAY_TILE_INDEX
		);
		
		// Create a file object from the directory name provided.
		directory = new File(directoryName);
		
		// Build the task object.
		task = new ReadDataset(directory, enableWayTileIndex);
		
		return new RunnableDatasetSourceManager(
			taskConfig.getId(),
			task,
			taskConfig.getPipeArgs()
		);
	}
}
