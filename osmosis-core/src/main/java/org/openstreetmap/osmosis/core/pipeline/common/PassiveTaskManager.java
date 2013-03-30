// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.pipeline.common;

import java.util.Map;
import java.util.logging.Logger;


/**
 * This task manager implementation supports tasks that perform passive
 * processing on data received from other tasks.
 * 
 * @author Brett Henderson
 */
public abstract class PassiveTaskManager extends TaskManager {
	private static final Logger LOG = Logger.getLogger(PassiveTaskManager.class.getName());
	
	
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
	protected PassiveTaskManager(String taskId, Map<String, String> pipeArgs) {
		super(taskId, pipeArgs);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void execute() {
		// Nothing to do for a sink because it passively receives data.
		LOG.fine("Task " + getTaskId() + " is passive, no execution required.");
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean waitForCompletion() {
		// Nothing to do for a sink because it passively receives data.
		LOG.fine("Task " + getTaskId() + " is passive, no completion wait required.");
		
		return true;
	}
}
