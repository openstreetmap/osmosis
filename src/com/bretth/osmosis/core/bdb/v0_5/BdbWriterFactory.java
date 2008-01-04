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
	private static final String ARG_HOME_NAME = "home";
	private static final String DEFAULT_HOME_NAME = "dataset";
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		String homeName;
		File home;
		BdbWriter task;
		
		// Get the task arguments.
		homeName = getStringArgument(
			taskConfig,
			ARG_HOME_NAME,
			getDefaultStringArgument(taskConfig, DEFAULT_HOME_NAME)
		);
		
		// Create a file object from the directory name provided.
		home = new File(homeName);
		
		// Build the task object.
		task = new BdbWriter(home);
		
		return new SinkManager(
			taskConfig.getId(),
			task,
			taskConfig.getPipeArgs()
		);
	}
}
