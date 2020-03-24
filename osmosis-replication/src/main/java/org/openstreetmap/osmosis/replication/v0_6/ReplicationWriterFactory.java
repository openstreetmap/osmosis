// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.replication.v0_6;

import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManager;
import org.openstreetmap.osmosis.core.pipeline.v0_6.ChangeSinkManager;


/**
 * The task manager factory for a replication file downloader.
 */
public class ReplicationWriterFactory extends WorkingTaskManagerFactory {

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		return new ChangeSinkManager(
				taskConfig.getId(),
				new ReplicationWriter(this.getWorkingDirectory(taskConfig)),
				taskConfig.getPipeArgs());
	}
}
