// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
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
	private static final Logger log = Logger.getLogger(ActiveTaskManager.class.getName());
	
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
		log.fine("Launching task " + getTaskId() + " in a new thread.");
		
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
		log.fine("Waiting for task " + getTaskId() + " to complete.");
		if (thread != null) {
			boolean successful;
			
			try {
				thread.join();
			} catch (InterruptedException e) {
				// Do nothing.
			}
			
			successful = thread.isSuccessful();
			
			if (!successful) {
				log.log(Level.SEVERE, "Thread for task " + getTaskId() + " failed", thread.getException());
			}
			
			thread = null;
			
			return successful;
		}
		
		return true;
	}
}
