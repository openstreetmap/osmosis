// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.pipeline.common;

import java.util.Map;

import org.openstreetmap.osmosis.core.task.common.RunnableTask;


/**
 * A task manager implementation for RunnableTask task implementations.
 * 
 * @author Brett Henderson
 */
public class RunnableTaskManager extends ActiveTaskManager {
	private RunnableTask task;
	
	
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
	public RunnableTaskManager(String taskId, RunnableTask task,
			Map<String, String> pipeArgs) {
		super(taskId, pipeArgs);
		
		this.task = task;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void connect(PipeTasks pipeTasks) {
		// This type of task has no inputs or outputs.
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Runnable getTask() {
		return task;
	}
}
