package com.bretth.osm.osmosis.pipeline;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.bretth.osm.osmosis.OsmosisRuntimeException;
import com.bretth.osm.osmosis.task.Task;


/**
 * Maintains the tasks that have been registered as producing data during the
 * connection process.
 * 
 * @author Brett Henderson
 */
public class PipeTasks {
	private static final Logger log = Logger.getLogger(PipeTasks.class.getName());
	
	private Map<String, Task> pipeTasks;
	private int defaultNameCreationIndex;
	private int defaultNameConsumptionIndex;
	
	
	/**
	 * Creates a new instance.
	 */
	public PipeTasks() {
		pipeTasks = new HashMap<String, Task>();
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
		if (pipeTasks.containsKey(pipeName)) {
			throw new OsmosisRuntimeException("Task " + taskId
					+ " cannot write to pipe " + pipeName
					+ " because the pipe is already being written to.");
		}
		
		pipeTasks.put(pipeName, task);
		
		if (log.isLoggable(Level.FINE)) {
			log.fine("Task \"" + taskId + "\" produced pipe \"" + pipeName + "\"");
		}
	}
	
	
	/**
	 * Adds the specified task using a generated pipe name.
	 * 
	 * @param taskId
	 *            The unique identifier of the task perfroming this request.
	 * @param task
	 *            The task to be added.
	 * @return The name that the task was registered under.
	 */
	public String putTask(String taskId, Task task) {
		String pipeName;
		
		// Generate a unique pipe name.
		do {
			pipeName = PipelineConstants.DEFAULT_PIPE_PREFIX + "." + defaultNameCreationIndex;
			defaultNameCreationIndex++;
		} while (pipeTasks.containsKey(pipeName));
		
		putTask(taskId, pipeName, task);
		
		return pipeName;
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
		
		if (!pipeTasks.containsKey(pipeName)) {
			throw new OsmosisRuntimeException("No pipe named " + pipeName + " is available as input for task " + taskId + ".");
		}
		
		task = pipeTasks.remove(pipeName);
		
		// Ensure the task is of the correct type.
		if (!requiredTaskType.isInstance(task)) {
			throw new OsmosisRuntimeException("Task " + taskId + " does not support data provided by input pipe " + pipeName + ".");
		}
		
		if (log.isLoggable(Level.FINE)) {
			log.fine("Task \"" + taskId + "\" consumed pipe \"" + pipeName + "\"");
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
		String pipeName;
		
		// Find the next available default pipe.
		do {
			if (defaultNameConsumptionIndex >= defaultNameCreationIndex) {
				throw new OsmosisRuntimeException("No default pipes are available as input for task " + taskId + ".");
			}
			
			pipeName = PipelineConstants.DEFAULT_PIPE_PREFIX + "." + defaultNameConsumptionIndex;
			
			defaultNameConsumptionIndex++;
		} while (!pipeTasks.containsKey(pipeName));
		
		return retrieveTask(taskId, pipeName, requiredTaskType);
	}
	
	
	/**
	 * Returns the number of pipes stored in this container.
	 * 
	 * @return The number of pipes.
	 */
	public int size() {
		return pipeTasks.size();
	}
	
	
	/**
	 * Returns the names of all of the currently registered pipes.
	 * 
	 * @return The set of pipe names.
	 */
	public Set<String> getPipeNames() {
		return pipeTasks.keySet();
	}
}
