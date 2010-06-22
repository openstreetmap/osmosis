// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.dataset.v0_6;

import java.io.File;

import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManager;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.pipeline.v0_6.SinkManager;


/**
 * The task manager factory for writing an entity stream to a dataset.
 * 
 * @author Brett Henderson
 */
public class WriteDatasetFactory extends TaskManagerFactory {
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
		WriteDataset task;
		
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
		task = new WriteDataset(directory, enableWayTileIndex);
		
		return new SinkManager(
			taskConfig.getId(),
			task,
			taskConfig.getPipeArgs()
		);
	}
}
