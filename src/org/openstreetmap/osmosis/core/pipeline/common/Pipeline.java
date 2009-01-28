// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.pipeline.common;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;


/**
 * Manages a processing pipeline from parsing of arguments, to creating and
 * instantiating tasks, running of the pipeline, and waiting until pipeline
 * completion.
 * 
 * @author Brett Henderson
 */
public class Pipeline {
	private static final Logger log = Logger.getLogger(Pipeline.class.getName());
	
	private TaskManagerFactoryRegister factoryRegister;
	private List<TaskManager> taskManagers;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param factoryRegister
	 *            The register containing all known task manager factories.
	 */
	public Pipeline(TaskManagerFactoryRegister factoryRegister) {
		this.factoryRegister = factoryRegister;
		
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
	private void buildTasks(List<TaskConfiguration> taskInfoList) {
		for (TaskConfiguration taskConfig : taskInfoList) {
			// Create the new task manager and add to the pipeline.
			taskManagers.add(
				factoryRegister.getInstance(taskConfig.getType()).createTaskManager(taskConfig)
			);
			
			if (log.isLoggable(Level.FINE)) {
				log.fine("Created task \"" + taskConfig.getId() + "\"");
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
			
			if (log.isLoggable(Level.FINE)) {
				log.fine("Connected task \"" + taskManager.getTaskId() + "\"");
			}
		}
		
		// Validate that no pipes are left without sinks.
		if (pipeTasks.size() > 0) {
			StringBuilder namedPipes;
			
			// Build a list of pipes to include in the error.
			namedPipes = new StringBuilder();
			for (String pipeName : pipeTasks.getPipeNames()) {
				if (namedPipes.length() > 0) {
					namedPipes.append(", ");
				}
				namedPipes.append(pipeName);
			}
			
			throw new OsmosisRuntimeException(
				"The following named pipes (" + namedPipes + ") and " +
				pipeTasks.defaultTaskSize() +
				" default pipes have not been terminated with appropriate output sinks."
			);
		}
	}
	
	
	/**
	 * Instantiates and configures all tasks within the pipeline.
	 * 
	 * @param taskInfoList
	 *            The list of task information objects.
	 */
	public void prepare(List<TaskConfiguration> taskInfoList) {
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
		boolean successful;
		
		// Wait for completion of all nodes.
		successful = true;
		for (TaskManager taskManager: taskManagers) {
			if (!taskManager.waitForCompletion()) {
				successful = false;
			}
		}
		
		if (!successful) {
			throw new OsmosisRuntimeException("One or more tasks failed.");
		}
	}
}
