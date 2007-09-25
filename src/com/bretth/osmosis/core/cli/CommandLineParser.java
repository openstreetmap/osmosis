package com.bretth.osmosis.core.cli;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.pipeline.common.PipelineConstants;


/**
 * Parses command line arguments into a form that can be consumed by the rest of
 * the application.
 * 
 * @author Brett Henderson
 */
public class CommandLineParser {
	
	private List<TaskInfo> taskInfoList;
	
	
	/**
	 * Creates a new instance.
	 */
	public CommandLineParser() {
		taskInfoList = new ArrayList<TaskInfo>();
	}
	
	
	/**
	 * Parses the command line arguments.
	 * 
	 * @param programArgs
	 *            The arguments.
	 */
	public void parse(String [] programArgs) {
		// Process the command line arguments to build all nodes in the pipeline.
		for (int i = 0; i < programArgs.length; ) {
			String arg;
			
			arg = programArgs[i];
			
			if (arg.indexOf(PipelineConstants.TASK_ARGUMENT_PREFIX) == 0) {
				i = parseTask(programArgs, i);
				
			} else {
				throw new OsmosisRuntimeException("Expected argument " + (i + 1) + " to be a task name.");
			}
		}
	}
	
	

	
	
	/**
	 * Parses the details of a single task and creates a task information object.
	 * 
	 * @param programArgs
	 *            The command line arguments passed to this application.
	 * @param offset
	 *            The current offset through the command line arguments.
	 * @return The new offset through the command line arguments.
	 */
	private int parseTask(String [] programArgs, int offset) {
		int i;
		String taskType;
		Map<String, String> taskArgs;
		Map<String, String> pipeArgs;
		String taskId;
		
		i = offset;
		
		// Extract the task type from the current argument.
		taskType = programArgs[i].substring(PipelineConstants.TASK_ARGUMENT_PREFIX.length());
		
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
		taskId = (taskInfoList.size() + 1) + "-" + taskType;
		
		// Create a new task information object and add it to the list.
		taskInfoList.add(
			new TaskInfo(taskId, taskType, pipeArgs, taskArgs)
		);
		
		return i;
	}
	
	
	/**
	 * The list of task information objects.
	 * 
	 * @return The taskInfoList.
	 */
	public List<TaskInfo> getTaskInfoList() {
		return taskInfoList;
	}
}
