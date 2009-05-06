// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.pipeline.common;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.task.common.Task;


/**
 * Maintains the tasks that have been registered as producing data during the
 * connection process.
 * 
 * @author Brett Henderson
 */
public class PipeTasks {
	private static final Logger LOG = Logger.getLogger(PipeTasks.class.getName());
	
	private Map<String, Task> namedTasks;
	private Deque<Task> defaultTasks;
	
	
	/**
	 * Creates a new instance.
	 */
	public PipeTasks() {
		namedTasks = new HashMap<String, Task>();
		defaultTasks = new ArrayDeque<Task>();
	}
	
	
	/**
	 * Adds the specified task using the specified name.
	 * 
	 * @param taskId
	 *            The unique identifier of the task perfroming this request.
	 * @param pipeName
	 *            The name to register the task under.
	 * @param task
	 *            The task to be added.
	 */
	public void putTask(String taskId, String pipeName, Task task) {
		// Verify that the output pipe is not already taken.
		if (namedTasks.containsKey(pipeName)) {
			throw new OsmosisRuntimeException("Task " + taskId
					+ " cannot write to pipe " + pipeName
					+ " because the pipe is already being written to.");
		}
		
		namedTasks.put(pipeName, task);
		
		if (LOG.isLoggable(Level.FINE)) {
			LOG.fine("Task \"" + taskId + "\" produced pipe \"" + pipeName + "\"");
		}
	}
	
	
	/**
	 * Adds the specified task to the default pipe list.
	 * 
	 * @param taskId
	 *            The unique identifier of the task performing this request.
	 * @param task
	 *            The task to be added.
	 */
	public void putTask(String taskId, Task task) {
		
		// Push the new task onto the top of the default pipe stack.
		defaultTasks.push(task);
		
		if (LOG.isLoggable(Level.FINE)) {
			LOG.fine("Task \"" + taskId + "\" produced unnamed pipe stored at level "
					+ defaultTasks.size() + " in the default pipe stack.");
		}
	}
	
	
	/**
	 * Checks if the specified task matches the required task type.
	 * 
	 * @param requiredTaskType
	 *            The type of task required.
	 * @param task
	 *            The task to be checked.
	 * @return True if the task type is a match.
	 */
	private boolean verifyPipeType(Class<? extends Task> requiredTaskType, Task task) {
		// Ensure the task is of the correct type.
		return requiredTaskType.isInstance(task);
	}
	
	
	/**
	 * Removes and returns the task registered under the specified name.
	 * 
	 * @param taskId
	 *            The unique identifier of the task perfroming this request.
	 * @param pipeName
	 *            The name of the registered task.
	 * @param requiredTaskType
	 *            The required type of the input task.
	 * @return The matching task.
	 */
	public Task retrieveTask(String taskId, String pipeName, Class<? extends Task> requiredTaskType) {
		Task task;
		
		if (!namedTasks.containsKey(pipeName)) {
			throw new OsmosisRuntimeException(
					"No pipe named " + pipeName + " is available as input for task " + taskId + ".");
		}
		
		task = namedTasks.remove(pipeName);
		
		// Ensure the task is of the correct type.
		if (!verifyPipeType(requiredTaskType, task)) {
			throw new OsmosisRuntimeException(
					"Task " + taskId + " does not support data provided by input pipe " + pipeName + ".");
		}
		
		if (LOG.isLoggable(Level.FINE)) {
			LOG.fine("Task \"" + taskId + "\" consumed pipe \"" + pipeName + "\"");
		}
		
		return task;
	}
	
	
	/**
	 * Removes and returns the next available task registered under a default name.
	 * 
	 * @param taskId
	 *            The unique identifier of the task perfroming this request.
	 * @param requiredTaskType
	 *            The required type of the input task.
	 * @return The matching task.
	 */
	public Task retrieveTask(String taskId, Class<? extends Task> requiredTaskType) {
		Task task;
		int defaultTaskCount;
		
		defaultTaskCount = defaultTasks.size();
		
		if (defaultTaskCount == 0) {
			throw new OsmosisRuntimeException("No default pipes are available as input for task " + taskId + ".");
		}
		
		task = defaultTasks.pop();
		
		// Ensure the task is of the correct type.
		if (!verifyPipeType(requiredTaskType, task)) {
			throw new OsmosisRuntimeException(
					"Task " + taskId + " does not support data provided by default pipe stored at level "
					+ (defaultTasks.size() + 1) + " in the default pipe stack.");
		}
		
		if (LOG.isLoggable(Level.FINE)) {
			LOG.fine("Task \"" + taskId + "\" consumed unnamed pipe stored at level "
					+ defaultTaskCount + " in the default pipe stack.");
		}
		
		return task;
	}
	
	
	/**
	 * Returns the number of pipes stored in this container.
	 * 
	 * @return The number of pipes.
	 */
	public int size() {
		return namedTasks.size() + defaultTasks.size();
	}
	
	
	/**
	 * Returns how many default pipes are stored in this container. This is a
	 * subset of the count returned by size().
	 * 
	 * @return The number of default pipes.
	 */
	public int defaultTaskSize() {
		return defaultTasks.size();
	}
	
	
	/**
	 * Returns the names of all of the currently registered pipes.
	 * 
	 * @return The set of pipe names.
	 */
	public Set<String> getPipeNames() {
		return namedTasks.keySet();
	}
}
