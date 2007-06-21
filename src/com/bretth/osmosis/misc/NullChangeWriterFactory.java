package com.bretth.osmosis.misc;

import java.util.Map;

import com.bretth.osmosis.pipeline.ChangeSinkManager;
import com.bretth.osmosis.pipeline.TaskManager;
import com.bretth.osmosis.pipeline.TaskManagerFactory;


/**
 * The task manager factory for a null change writer.
 * 
 * @author Brett Henderson
 */
public class NullChangeWriterFactory extends TaskManagerFactory {
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(String taskId, Map<String, String> taskArgs, Map<String, String> pipeArgs) {
		return new ChangeSinkManager(taskId, new NullChangeWriter(), pipeArgs);
	}
}
