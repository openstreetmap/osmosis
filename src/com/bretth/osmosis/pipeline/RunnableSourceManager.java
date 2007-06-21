package com.bretth.osmosis.pipeline;

import java.util.Map;

import com.bretth.osmosis.OsmosisRuntimeException;
import com.bretth.osmosis.task.RunnableSource;


/**
 * A task manager implementation for RunnableSource task implementations.
 * 
 * @author Brett Henderson
 */
public class RunnableSourceManager extends TaskManager {
	private RunnableSource task;
	
	private Thread thread;
	
	
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
	public void run() {
		if (thread != null) {
			throw new OsmosisRuntimeException("Task " + getTaskId()
					+ " is already running.");
		}

		thread = new Thread(task, "Thread-" + getTaskId());

		thread.start();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void waitForCompletion() {
		if (thread != null) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				// Do nothing.
			}

			thread = null;
		}
	}
}
