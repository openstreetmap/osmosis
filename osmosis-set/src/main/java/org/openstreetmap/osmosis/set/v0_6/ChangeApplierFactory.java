// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.set.v0_6;

import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManager;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.pipeline.v0_6.MultiSinkMultiChangeSinkRunnableSourceManager;


/**
 * The task manager factory for a change applier.
 * 
 * @author Brett Henderson
 */
public class ChangeApplierFactory extends TaskManagerFactory {

	private static final String ARG_BUFFER_CAPACITY = "bufferCapacity";
	private static final int DEFAULT_BUFFER_CAPACITY = 20;
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		
		int bufferCapacity = getIntegerArgument(
				taskConfig,
				ARG_BUFFER_CAPACITY,
				getDefaultIntegerArgument(taskConfig, DEFAULT_BUFFER_CAPACITY)
			);
		
		return new MultiSinkMultiChangeSinkRunnableSourceManager(
			taskConfig.getId(),
			new ChangeApplier(bufferCapacity),
			taskConfig.getPipeArgs()
		);
	}
}
