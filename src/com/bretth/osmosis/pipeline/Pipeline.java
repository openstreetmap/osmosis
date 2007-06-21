package com.bretth.osmosis.pipeline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.bretth.osmosis.OsmosisRuntimeException;


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
	 * @param taskType
	 *            The name of the task to be created.
	 * @param programArgs
	 *            The command line arguments passed to this application.
	 * @param offset
	 *            The current offset through the command line arguments.
	 * @return The new offset through the command line arguments.
	 */
	private int buildNode(String taskType, String [] programArgs, int offset) {
		int i;
		Map<String, String> taskArgs;
		Map<String, String> pipeArgs;
		String taskId;
		
		i = offset;
		
		// Build up a list of task and pipe arguments.
		taskArgs = new HashMap<String, String>();
		pipeArgs = new HashMap<String, String>();
		while (i < programArgs.length) {
			String arg;
			int equalsIndex;
			String argName;
			String argValue;
			
			arg = programArgs[i];
			
			if (arg.indexOf(PipelineConstants.TASK_ARGUMENT_PREFIX) == 0) {
				break;
			}
			
			equalsIndex = arg.indexOf("=");
			
			// Check if the equals exists.
			if (equalsIndex < 0) {
				throw new OsmosisRuntimeException("Expected argument " + (i + 1) + " to be a name value pair (ie. name=value).");
			}
			
			// Check if the name component of the argument exists.
			if (equalsIndex == 0) {
				throw new OsmosisRuntimeException("Argument " + (i + 1) + " doesn't contain a name before the '=' (ie. name=value).");
			}
			
			// Check if the value component of the argument exists.
			if (equalsIndex >= (arg.length() - 1)) {
				throw new OsmosisRuntimeException("Argument " + (i + 1) + " doesn't contain a value after the '=' (ie. name=value).");
			}
			
			// Split the argument into name and value.
			argName = arg.substring(0, equalsIndex);
			argValue = arg.substring(equalsIndex + 1);
			
			// Add pipeline arguments to pipeArgs, all other arguments to taskArgs.
			// A pipeline arg is inPipe, inPipe.x, outPipe or outPipe.x.
			if (PipelineConstants.IN_PIPE_ARGUMENT_PREFIX.equals(argName) || argName.indexOf(PipelineConstants.IN_PIPE_ARGUMENT_PREFIX + ".") == 0 || PipelineConstants.OUT_PIPE_ARGUMENT_PREFIX.equals(argName) || argName.indexOf(PipelineConstants.OUT_PIPE_ARGUMENT_PREFIX + ".") == 0) {
				pipeArgs.put(argName, argValue);
			} else {
				taskArgs.put(argName, argValue);
			}
			
			i++;
		}
		
		// Build a unique task id.
		taskId = (taskManagers.size() + 1) + "-" + taskType;
		
		// Create the new task manager and add to the pipeline.
		taskManagers.add(
			TaskManagerFactory.createTaskManager(taskType, taskId, taskArgs, pipeArgs)
		);
		
		if (log.isLoggable(Level.INFO)) {
			log.info("Created task \"" + taskId + "\"");
		}
		
		return i;
	}
	
	
	/**
	 * Creates all nodes in the pipeline. The nodes will be created with the
	 * correct task with all task parameters set. The tasks will not be
	 * connected together.
	 * 
	 * @param programArgs
	 *            The command line arguments passed to this application.
	 */
	private void buildTasks(String [] programArgs) {
		// Process the command line arguments to build all nodes in the pipeline.
		for (int i = 0; i < programArgs.length; ) {
			String arg;
			
			arg = programArgs[i];
			
			if (arg.indexOf(PipelineConstants.TASK_ARGUMENT_PREFIX) == 0) {
				String taskType;
				
				taskType = arg.substring(PipelineConstants.TASK_ARGUMENT_PREFIX.length());
				
				i = buildNode(taskType, programArgs, ++i);
				
			} else {
				throw new OsmosisRuntimeException("Expected argument " + (i + 1) + " to be a task name.");
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
				log.info("Connected task \"" + taskManager.getTaskId() + "\"");
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
	 * @param programArgs
	 *            The command line arguments for configuring the pipeline.
	 */
	public void prepare(String [] programArgs) {
		// Process the command line arguments to build all tasks in the pipeline.
		log.fine("Building tasks.");
		buildTasks(programArgs);
		
		// Connect the nodes in the pipeline.
		log.fine("Connecting tasks.");
		connectTasks();
	}
	
	
	/**
	 * Launches the execution of the tasks within the pipeline.
	 *
	 */
	public void run() {
		// Initiate execution of all nodes.
		for (TaskManager taskManager: taskManagers) {
			taskManager.run();
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
