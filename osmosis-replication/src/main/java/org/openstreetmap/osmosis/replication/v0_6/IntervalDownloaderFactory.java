// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.replication.v0_6;

import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManager;
import org.openstreetmap.osmosis.core.pipeline.v0_6.RunnableChangeSourceManager;

/**
 * The task manager factory for a change downloader.
 * 
 * @author Brett Henderson
 */
public class IntervalDownloaderFactory extends WorkingTaskManagerFactory {
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		return new RunnableChangeSourceManager(
			taskConfig.getId(),
			new IntervalDownloader(
				taskConfig.getId(),
				this.getWorkingDirectory(taskConfig)
			),
			taskConfig.getPipeArgs()
		);
	}
}
