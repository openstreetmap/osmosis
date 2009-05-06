// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.change.v0_5;

import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManager;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.pipeline.v0_5.MultiSinkRunnableChangeSourceManager;


/**
 * The task manager factory for a change deriver.
 * 
 * @author Brett Henderson
 */
public class ChangeDeriverFactory extends TaskManagerFactory {
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		return new MultiSinkRunnableChangeSourceManager(
			taskConfig.getId(),
			new ChangeDeriver(10),
			taskConfig.getPipeArgs()
		);
	}
}
