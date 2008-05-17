// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.pipeline.v0_6;

import java.util.Map;

import com.bretth.osmosis.core.pipeline.common.ActiveTaskManager;
import com.bretth.osmosis.core.pipeline.common.PipeTasks;
import com.bretth.osmosis.core.task.v0_6.RunnableDatasetSource;


/**
 * A task manager implementation for RunnableDatasetSource task implementations.
 * 
 * @author Brett Henderson
 */
public class RunnableDatasetSourceManager extends ActiveTaskManager {
	private RunnableDatasetSource task;
	
	
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
	public RunnableDatasetSourceManager(String taskId, RunnableDatasetSource task,
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
