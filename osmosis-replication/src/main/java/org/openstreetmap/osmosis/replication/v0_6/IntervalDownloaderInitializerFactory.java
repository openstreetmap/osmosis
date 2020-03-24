// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.replication.v0_6;

import java.util.Date;
import java.util.TimeZone;

import org.openstreetmap.osmosis.core.pipeline.common.RunnableTaskManager;
import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManager;

/**
 * The task manager factory for a change download initializer.
 * 
 * @author Brett Henderson
 */
public class IntervalDownloaderInitializerFactory extends WorkingTaskManagerFactory {
	private static final String ARG_INITIAL_DATE = "initialDate";
    
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		Date initialDate;
		// Get the task arguments.
		initialDate = getDateArgument(taskConfig, ARG_INITIAL_DATE, TimeZone.getTimeZone("UTC"));
		
		return new RunnableTaskManager(
			taskConfig.getId(),
			new IntervalDownloaderInitializer(
				this.getWorkingDirectory(taskConfig),
				initialDate
			),
			taskConfig.getPipeArgs()
		);
	}
}
