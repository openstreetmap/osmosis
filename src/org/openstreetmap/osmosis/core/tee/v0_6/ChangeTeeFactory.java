// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.tee.v0_6;

import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManager;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.pipeline.v0_6.ChangeSinkMultiChangeSourceManager;


/**
 * The task manager factory for a change tee.
 * 
 * @author Brett Henderson
 */
public class ChangeTeeFactory extends TaskManagerFactory {
	private static final String ARG_OUTPUT_COUNT = "outputCount";
	private static final int DEFAULT_OUTPUT_COUNT = 2;
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		int outputCount;
		
		// Get the task arguments.
		outputCount = getIntegerArgument(
			taskConfig,
			ARG_OUTPUT_COUNT,
			getDefaultIntegerArgument(taskConfig, DEFAULT_OUTPUT_COUNT)
		);
		
		return new ChangeSinkMultiChangeSourceManager(
			taskConfig.getId(),
			new ChangeTee(outputCount),
			taskConfig.getPipeArgs()
		);
	}
}
