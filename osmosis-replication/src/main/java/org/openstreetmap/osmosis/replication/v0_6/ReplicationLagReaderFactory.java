// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.replication.v0_6;

import org.openstreetmap.osmosis.core.pipeline.common.RunnableTaskManager;
import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManager;

/**
 * The task manager factory for a replication lag reader.
 * 
 * @author Peter Koerner
 */
public class ReplicationLagReaderFactory extends WorkingTaskManagerFactory {
	private static final String ARG_HUMAN_READABLE = "humanReadable";
	private static final boolean DEFAULT_HUMAN_READABLE = false;
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		boolean humanReadableFlag;
		humanReadableFlag = getBooleanArgument(
			taskConfig,
			ARG_HUMAN_READABLE, 
			DEFAULT_HUMAN_READABLE
		);
		
		return new RunnableTaskManager(
			taskConfig.getId(),
			new ReplicationLagReader(
				this.getWorkingDirectory(taskConfig), 
				humanReadableFlag
			),
			taskConfig.getPipeArgs()
		);
	}
}
