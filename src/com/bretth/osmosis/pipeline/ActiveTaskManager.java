package com.bretth.osmosis.pipeline;

import java.util.Map;
import java.util.logging.Logger;

import com.bretth.osmosis.OsmosisRuntimeException;


/**
 * This task manager implementation supports tasks that perform active
 * processing in a separate thread.
 * 
 * @author Brett Henderson
 */
public abstract class ActiveTaskManager extends TaskManager {
	private static final Logger log = Logger.getLogger(ActiveTaskManager.class.getName());
	
	private Thread thread;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param taskId
	 *            A unique identifier for the task. This is used to produce
	 *            meaningful errors when errors occur.
	 * @param pipeArgs
	 *            The arguments defining input and output pipes for the task,
	 *            pipes are a logical concept for identifying how the tasks are
	 *            connected together.
	 */
	protected ActiveTaskManager(String taskId, Map<String, String> pipeArgs) {
		super(taskId, pipeArgs);
	}
	
	
	/**
	 * Returns the runnable task managed by this manager.
	 * 
	 * @return The task.
	 */
	protected abstract Runnable getTask();
	
		
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void execute() {
		log.fine("Launching task " + getTaskId() + " in a new thread.");
		
		if (thread != null) {
			throw new OsmosisRuntimeException("Task " + getTaskId()
					+ " is already running.");
		}
		
		thread = new Thread(getTask(), "Thread-" + getTaskId());
		
		thread.start();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void waitForCompletion() {
		log.fine("Waiting for task " + getTaskId() + " to complete.");
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
