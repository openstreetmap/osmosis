// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.misc.v0_6;

import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManager;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.pipeline.v0_6.RunnableChangeSourceManager;

/**
 * The task manager factory for an empty change reader.
 * 
 * @author Brett Henderson
 */
public class EmptyChangeReaderFactory extends TaskManagerFactory {

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		return new RunnableChangeSourceManager(taskConfig.getId(), new EmptyChangeReader(), taskConfig.getPipeArgs());
	}
}
