package com.bretth.osmosis.core.pipeline.v0_4;

import java.util.Map;

import com.bretth.osmosis.core.pipeline.common.ActiveTaskManager;
import com.bretth.osmosis.core.pipeline.common.PipeTasks;
import com.bretth.osmosis.core.task.v0_4.RunnableSource;


/**
 * A task manager implementation for RunnableSource task implementations.
 * 
 * @author Brett Henderson
 */
public class RunnableSourceManager extends ActiveTaskManager {
	private RunnableSource task;
	
	
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
	public RunnableSourceManager(String taskId, RunnableSource task,
			Map<String, String> pipeArgs) {
		super(taskId, pipeArgs);
		
		this.task = task;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void connect(PipeTasks pipeTasks) {
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
