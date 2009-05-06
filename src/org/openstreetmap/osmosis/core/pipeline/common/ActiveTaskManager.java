// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.pipeline.common;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;


/**
 * This task manager implementation supports tasks that perform active
 * processing in a separate thread.
 * 
 * @author Brett Henderson
 */
public abstract class ActiveTaskManager extends TaskManager {
	private static final Logger LOG = Logger.getLogger(ActiveTaskManager.class.getName());
	
	private TaskRunner thread;
	
	
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
		LOG.fine("Launching task " + getTaskId() + " in a new thread.");
		
		if (thread != null) {
			throw new OsmosisRuntimeException("Task " + getTaskId()
					+ " is already running.");
		}
		
		thread = new TaskRunner(getTask(), "Thread-" + getTaskId());
		
		thread.start();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean waitForCompletion() {
		LOG.fine("Waiting for task " + getTaskId() + " to complete.");
		if (thread != null) {
			boolean successful;
			
			try {
				thread.join();
			} catch (InterruptedException e) {
				// We are already in an error condition so log and continue.
				LOG.log(Level.WARNING, "The wait for task completion was interrupted.", e);
			}
			
			successful = thread.isSuccessful();
			
			if (!successful) {
				LOG.log(Level.SEVERE, "Thread for task " + getTaskId() + " failed", thread.getException());
			}
			
			thread = null;
			
			return successful;
		}
		
		return true;
	}
}
