// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.bdb.v0_5;

import java.io.File;

import com.bretth.osmosis.core.cli.TaskConfiguration;
import com.bretth.osmosis.core.pipeline.common.TaskManager;
import com.bretth.osmosis.core.pipeline.common.TaskManagerFactory;
import com.bretth.osmosis.core.pipeline.v0_5.RunnableDatasetSourceManager;


/**
 * The task manager factory for a Berkeley database reader.
 * 
 * @author Brett Henderson
 */
public class BdbReaderFactory extends TaskManagerFactory {
	private static final String ARG_HOME_NAME = "home";
	private static final String DEFAULT_HOME_NAME = "dataset";
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		String homeName;
		File home;
		BdbReader task;
		
		// Get the task arguments.
		homeName = getStringArgument(
			taskConfig,
			ARG_HOME_NAME,
			getDefaultStringArgument(taskConfig, DEFAULT_HOME_NAME)
		);
		
		// Create a file object from the directory name provided.
		home = new File(homeName);
		
		// Build the task object.
		task = new BdbReader(home);
		
		return new RunnableDatasetSourceManager(
			taskConfig.getId(),
			task,
			taskConfig.getPipeArgs()
		);
	}
}
