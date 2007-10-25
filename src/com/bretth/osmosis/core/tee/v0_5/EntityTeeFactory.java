package com.bretth.osmosis.core.tee.v0_5;

import java.util.Map;

import com.bretth.osmosis.core.pipeline.common.TaskManager;
import com.bretth.osmosis.core.pipeline.common.TaskManagerFactory;
import com.bretth.osmosis.core.pipeline.v0_5.SinkMultiSourceManager;


/**
 * The task manager factory for an entity tee.
 * 
 * @author Brett Henderson
 */
public class EntityTeeFactory extends TaskManagerFactory {
	private static final String ARG_OUTPUT_COUNT = "outputCount";
	private static final int DEFAULT_OUTPUT_COUNT = 2;
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(String taskId, Map<String, String> taskArgs, Map<String, String> pipeArgs) {
		int outputCount;
		
		// Get the task arguments.
		outputCount = getIntegerArgument(taskId, taskArgs, ARG_OUTPUT_COUNT, DEFAULT_OUTPUT_COUNT);
		
		return new SinkMultiSourceManager(
			taskId,
			new EntityTee(outputCount),
			pipeArgs
		);
	}
}
