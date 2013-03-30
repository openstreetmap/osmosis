// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.set.v0_6;

import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManager;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.pipeline.v0_6.MultiChangeSinkRunnableChangeSourceManager;


/**
 * The task manager factory for a change appender.
 * 
 * @author Brett Henderson
 */
public class ChangeAppenderFactory extends TaskManagerFactory {
	private static final String ARG_SOURCE_COUNT = "sourceCount";
	private static final int DEFAULT_SOURCE_COUNT = 2;
	
	private static final String ARG_BUFFER_CAPACITY = "bufferCapacity";
	private static final int DEFAULT_BUFFER_CAPACITY = 20;
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		int sourceCount;
		
		sourceCount = getIntegerArgument(taskConfig, ARG_SOURCE_COUNT, DEFAULT_SOURCE_COUNT);
		
		int bufferCapacity = getIntegerArgument(
				taskConfig,
				ARG_BUFFER_CAPACITY,
				getDefaultIntegerArgument(taskConfig, DEFAULT_BUFFER_CAPACITY)
			);

		
		return new MultiChangeSinkRunnableChangeSourceManager(
			taskConfig.getId(),
			new ChangeAppender(sourceCount, bufferCapacity),
			taskConfig.getPipeArgs()
		);
	}
}
