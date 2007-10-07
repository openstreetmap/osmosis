package com.bretth.osmosis.core.pipeline.common;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.cli.TaskInfo;


/**
 * Manages a processing pipeline from parsing of arguments, to creating and
 * instantiating tasks, running of the pipeline, and waiting until pipeline
 * completion.
 * 
 * @author Brett Henderson
 */
public class Pipeline {
	private static final Logger log = Logger.getLogger(Pipeline.class.getName());
	
	private List<TaskManager> taskManagers;
	
	
	/**
	 * Creates a new instance.
	 */
	public Pipeline() {
		taskManagers = new ArrayList<TaskManager>();
	}
	
	
	/**
	 * Creates a new node in the pipeline. The node will be created with the
	 * correct task with all task parameters set. The tasks will not be
	 * connected together.
	 * 
	 * @param taskInfoList
	 *            The list of task information objects.
	 */
	private void buildTasks(List<TaskInfo> taskInfoList) {
		for (TaskInfo taskInfo : taskInfoList) {
			// Create the new task manager and add to the pipeline.
			taskManagers.add(
				TaskManagerFactory.createTaskManager(
					taskInfo.getType(),
					taskInfo.getId(),
					taskInfo.getConfigArgs(),
					taskInfo.getPipeArgs()
				)
			);
			
			if (log.isLoggable(Level.INFO)) {
				log.fine("Created task \"" + taskInfo.getId() + "\"");
			}
		}
	}
	
	
	/**
	 * Uses the pipe arguments specified for each task to connect the tasks appropriately.
	 */
	private void connectTasks() {
		PipeTasks pipeTasks;
		
		// Create a container to map between the pipe name and the task that has
		// last written to it.
		pipeTasks = new PipeTasks();
		
		// Request each node to perform connection, each node will update the
		// pipe tasks as it provides and consumes pipes.
		for (TaskManager taskManager : taskManagers) {
			taskManager.connect(pipeTasks);
			
			if (log.isLoggable(Level.INFO)) {
				log.fine("Connected task \"" + taskManager.getTaskId() + "\"");
			}
		}
		
		// Validate that no pipes are left without sinks.
		if (pipeTasks.size() > 0) {
			StringBuilder pipes;
			
			// Build a list of pipes to include in the error.
			pipes = new StringBuilder();
			for (String pipeName : pipeTasks.getPipeNames()) {
				if (pipes.length() > 0) {
					pipes.append(", ");
				}
				pipes.append(pipeName);
			}
			
			throw new OsmosisRuntimeException("The following data pipes have not been terminated with appropriate output sinks (" + pipes.toString() + ").");
		}
	}
	
	
	/**
	 * Instantiates and configures all tasks within the pipeline.
	 * 
	 * @param taskInfoList
	 *            The list of task information objects.
	 */
	public void prepare(List<TaskInfo> taskInfoList) {
		// Process the command line arguments to build all tasks in the pipeline.
		log.fine("Building tasks.");
		buildTasks(taskInfoList);
		
		// Connect the nodes in the pipeline.
		log.fine("Connecting tasks.");
		connectTasks();
	}
	
	
	/**
	 * Launches the execution of the tasks within the pipeline.
	 */
	public void execute() {
		// Initiate execution of all nodes.
		for (TaskManager taskManager: taskManagers) {
			taskManager.execute();
		}
	}
	
	
	/**
	 * Waits for all tasks within the pipeline to complete.
	 */
	public void waitForCompletion() {
		// Wait for completion of all nodes.
		for (TaskManager taskManager: taskManagers) {
			taskManager.waitForCompletion();
		}
	}
}
