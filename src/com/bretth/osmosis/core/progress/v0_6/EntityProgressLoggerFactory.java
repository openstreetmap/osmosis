// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.progress.v0_6;

import com.bretth.osmosis.core.cli.TaskConfiguration;
import com.bretth.osmosis.core.pipeline.common.TaskManager;
import com.bretth.osmosis.core.pipeline.common.TaskManagerFactory;
import com.bretth.osmosis.core.pipeline.v0_6.SinkSourceManager;


/**
 * The task manager factory for an entity progress logger.
 * 
 * @author Brett Henderson
 */
public class EntityProgressLoggerFactory extends TaskManagerFactory {
	private static final String ARG_LOG_INTERVAL = "interval";
	private static final int DEFAULT_LOG_INTERVAL = 5;
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		EntityProgressLogger task;
		int interval;
		
		// Get the task arguments.
		interval = getIntegerArgument(taskConfig, ARG_LOG_INTERVAL, DEFAULT_LOG_INTERVAL);
		
		// Build the task object.
		task = new EntityProgressLogger(interval * 1000);
		
		return new SinkSourceManager(taskConfig.getId(), task, taskConfig.getPipeArgs());
	}
}
