// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.replication.v0_6;

import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManager;
import org.openstreetmap.osmosis.core.pipeline.v0_6.RunnableChangeSourceManager;

/**
 * The task manager factory for a replication file downloader.
 */
public class ReplicationDownloaderFactory extends WorkingTaskManagerFactory {
	private static final String ARG_SINGLE = "single";
	private static final boolean DEFAULT_SINGLE = false;
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		boolean single = getBooleanArgument(taskConfig, ARG_SINGLE, DEFAULT_SINGLE);	

		return new RunnableChangeSourceManager(
			taskConfig.getId(),
			new ReplicationDownloader(
				this.getWorkingDirectory(taskConfig),
				single
			),
			taskConfig.getPipeArgs()
		);
	}
}
