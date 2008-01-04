// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.bdb.v0_5;

import java.io.File;

import com.bretth.osmosis.core.cli.TaskConfiguration;
import com.bretth.osmosis.core.pipeline.common.TaskManager;
import com.bretth.osmosis.core.pipeline.common.TaskManagerFactory;
import com.bretth.osmosis.core.pipeline.v0_5.SinkManager;


/**
 * The task manager factory for reading the entire contents of a dataset.
 * 
 * @author Brett Henderson
 */
public class BdbWriterFactory extends TaskManagerFactory {
	private static final String ARG_DIRECTORY_NAME = "directory";
	private static final String DEFAULT_DIRECTORY_NAME = "dataset";
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		String directoryName;
		File directory;
		BdbWriter task;
		
		// Get the task arguments.
		directoryName = getStringArgument(
			taskConfig,
			ARG_DIRECTORY_NAME,
			getDefaultStringArgument(taskConfig, DEFAULT_DIRECTORY_NAME)
		);
		
		// Create a file object from the directory name provided.
		directory = new File(directoryName);
		
		// Build the task object.
		task = new BdbWriter(directory);
		
		return new SinkManager(
			taskConfig.getId(),
			task,
			taskConfig.getPipeArgs()
		);
	}
}
