package com.bretth.osmosis.core.pipeline.v0_4;

import java.util.Map;

import com.bretth.osmosis.core.pipeline.common.PassiveTaskManager;
import com.bretth.osmosis.core.pipeline.common.PipeTasks;
import com.bretth.osmosis.core.task.v0_4.Sink;
import com.bretth.osmosis.core.task.v0_4.Source;


/**
 * A task manager implementation for Sink task implementations.
 * 
 * @author Brett Henderson
 */
public class SinkManager extends PassiveTaskManager {
	private Sink task;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param taskId
	 *            A unique identifier for the task. This is used to produce
	 *            meaningful errors when errors occur.
	 * @param task
	 *            The task instance to be managed.
	 * @param pipeArgs
	 *            The arguments defining input and output pipes for the task,
	 *            pipes are a logical concept for identifying how the tasks are
	 *            connected together.
	 */
	public SinkManager(String taskId, Sink task, Map<String, String> pipeArgs) {
		super(taskId, pipeArgs);
		
		this.task = task;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void connect(PipeTasks pipeTasks) {
		Source source;
		
		// Get the input task. A sink only has one input, this corresponds to
		// pipe index 0.
		source = (Source) getInputTask(pipeTasks, 0, Source.class);
		
		// Cast the input feed to the correct type.
		// Connect the tasks.
		source.setSink(task);
	}
}
