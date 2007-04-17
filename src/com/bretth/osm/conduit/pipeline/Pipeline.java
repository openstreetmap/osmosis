package com.bretth.osm.conduit.pipeline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bretth.osm.conduit.ConduitRuntimeException;
import com.bretth.osm.conduit.task.Task;


public class Pipeline {
	
	public List<TaskManager> taskManagers;
	
	
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
				throw new ConduitRuntimeException("Expected argument " + (i + 1) + " to be a name value pair (ie. name=value).");
			}
			
			// Check if the name component of the argument exists.
			if (equalsIndex == 0) {
				throw new ConduitRuntimeException("Argument " + (i + 1) + " doesn't contain a name before the '=' (ie. name=value).");
			}
			
			// Check if the value component of the argument exists.
			if (equalsIndex >= (arg.length() - 1)) {
				throw new ConduitRuntimeException("Argument " + (i + 1) + " doesn't contain a value after the '=' (ie. name=value).");
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
		
		// Create the new task manager and add to the pipeline.
		taskManagers.add(
			TaskManagerFactory.createTaskManager(taskType, taskArgs, pipeArgs)
		);
		
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
	private void buildNodes(String [] programArgs) {
		// Process the command line arguments to build all nodes in the pipeline.
		for (int i = 0; i < programArgs.length; ) {
			String arg;
			
			arg = programArgs[i];
			
			if (arg.indexOf(PipelineConstants.TASK_ARGUMENT_PREFIX) == 0) {
				String taskType;
				
				taskType = arg.substring(PipelineConstants.TASK_ARGUMENT_PREFIX.length());
				
				i = buildNode(taskType, programArgs, ++i);
				
			} else {
				throw new ConduitRuntimeException("Expected argument " + (i + 1) + " to be a task name.");
			}
		}
	}
	
	
	/**
	 * Uses the pipe arguments specified for each task to connect the tasks appropriately.
	 */
	private void connectNodes() {
		Map<String, Task> pipeTasks;
		
		// Create a container to map between the pipe name and the task that has
		// last written to it.
		pipeTasks = new HashMap<String, Task>();
		
		// Request each node to perform connection, each node will update the
		// pipe tasks as it provides and consumes pipes.
		for (TaskManager taskManager : taskManagers) {
			taskManager.connect(pipeTasks);
		}
		
		// Validate that no pipes are left without sinks.
		if (pipeTasks.size() > 0) {
			StringBuilder pipes;
			
			// Build a list of pipes to include in the error.
			pipes = new StringBuilder();
			for (String pipeName : pipeTasks.keySet()) {
				if (pipes.length() > 0) {
					pipes.append(", ");
				}
				pipes.append(pipeName);
			}
			
			throw new ConduitRuntimeException("The following data pipes have not been terminated with appropriate output sinks (" + pipes.toString() + ").");
		}
	}
	
	
	public void prepare(String [] programArgs) {
		// Process the command line arguments to build all nodes in the pipeline.
		buildNodes(programArgs);
		
		// Connect the nodes in the pipeline.
		connectNodes();
	}
	
	
	public void run() {
		// Initiate execution of all nodes.
		for (TaskManager taskManager: taskManagers) {
			taskManager.run();
		}
	}
	
	
	public void waitForCompletion() {
		// Wait for completion of all nodes.
		for (TaskManager taskManager: taskManagers) {
			taskManager.waitForCompletion();
		}
	}
}
