package com.bretth.osmosis.core.pipeline.v0_4;

import java.util.Map;

import com.bretth.osmosis.core.pipeline.common.ActiveTaskManager;
import com.bretth.osmosis.core.pipeline.common.PipeTasks;
import com.bretth.osmosis.core.task.v0_4.SinkRunnableSource;
import com.bretth.osmosis.core.task.v0_4.Source;


/**
 * A task manager implementation for SinkRunnableSource task implementations.
 * 
 * @author Brett Henderson
 */
public class SinkRunnableSourceManager extends ActiveTaskManager {
	private SinkRunnableSource task;
	
	
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
	public SinkRunnableSourceManager(String taskId, SinkRunnableSource task,
			Map<String, String> pipeArgs) {
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
		
		// Connect the tasks.
		source.setSink(task);
		
		// Register the task as an output. A source only has one output, this
		// corresponds to pipe index 0.
		setOutputTask(pipeTasks, task, 0);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Runnable getTask() {
		return task;
	}
}
