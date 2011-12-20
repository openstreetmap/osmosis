// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.progress.v0_6;

import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManager;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.pipeline.v0_6.ChangeSinkChangeSourceManager;


/**
 * The task manager factory for a change progress logger.
 * 
 * @author Brett Henderson
 */
public class ChangeProgressLoggerFactory extends TaskManagerFactory {
	private static final String ARG_LOG_INTERVAL = "interval";
	private static final int DEFAULT_LOG_INTERVAL = 5;

	private static final String ARG_LABEL = "label";
	private static final String DEFAULT_LABEL = "";
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		ChangeProgressLogger task;
		int interval;
		String label;
		
		// Get the task arguments.
		interval = getIntegerArgument(taskConfig, ARG_LOG_INTERVAL, DEFAULT_LOG_INTERVAL);
		label = getStringArgument(taskConfig, ARG_LABEL, DEFAULT_LABEL);

		// Build the task object.
		task = new ChangeProgressLogger(interval * 1000, label);
		
		return new ChangeSinkChangeSourceManager(taskConfig.getId(), task, taskConfig.getPipeArgs());
	}
}
