// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.replication.v0_6;

import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManager;
import org.openstreetmap.osmosis.core.pipeline.v0_6.ChangeSinkChangeSourceManager;

/**
 * The task manager factory for a replication to change writer.
 */
public class ReplicationToChangeWriterFactory extends WorkingTaskManagerFactory {

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		return new ChangeSinkChangeSourceManager(
				taskConfig.getId(),
				new ReplicationToChangeWriter(this.getWorkingDirectory(taskConfig)),
				taskConfig.getPipeArgs());
	}
}
