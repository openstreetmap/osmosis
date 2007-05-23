package com.bretth.osm.conduit.pipeline;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.bretth.osm.conduit.ConduitRuntimeException;
import com.bretth.osm.conduit.task.Task;


/**
 * All task instances within a pipeline are managed by a task manager. This
 * manager is not responsible for the creation of the task, but it is
 * responsible for connecting it to its input and output tasks and responsible
 * for managing the invocation of the task.
 * 
 * @author Brett Henderson
 */
public abstract class TaskManager {
	private String taskId;
	private List<String> inputPipes;
	private List<String> outputPipes;
	
	
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
	protected TaskManager(String taskId, Map<String, String> pipeArgs) {
		this.taskId = taskId;
		
		inputPipes = buildPipes(pipeArgs, true);
		outputPipes = buildPipes(pipeArgs, false);
	}
	
	
	/**
	 * Returns the index of a pipe based upon its suffix.
	 * 
	 * @param pipeArgNameSuffix
	 *            The suffix of the pipe argument name.
	 * @return The index of the pipe being referred to by the pipe argument.
	 */
	private int getPipeIndex(String pipeArgNameSuffix) {
		int pipeIndex;
		
		// If there is no suffix, then it is the default pipe 0.
		// Otherwise we must check the suffix.
		if (pipeArgNameSuffix.length() <= 0) {
			pipeIndex = 0;
		} else {
			String indexString;
			
			// Validate that the suffix begins with a '.' character.
			if (pipeArgNameSuffix.indexOf('.') != 0) {
				throw new ConduitRuntimeException(
					"Task " + taskId +
					" contains a pipe definition without '.' between prefix and suffix."
				);
			}
			
			// The remaining suffix must be a number defining the index.
			indexString = pipeArgNameSuffix.substring(1);
			
			// Ensure the index exists.
			if (indexString.length() <= 0) {
				throw new ConduitRuntimeException(
					"Task " + taskId +
					" contains a pipe definition without an index after the '.'."
				);
			}
			
			// Parse the suffix string into a number.
			try {
				pipeIndex = Integer.parseInt(indexString);
				
			} catch (NumberFormatException e) {
				throw new ConduitRuntimeException("Task " + taskId + " has a pipe with an incorrect index suffix.");
			}
		}
		
		return pipeIndex;
	}
	
	
	/**
	 * Builds a list of pipes ordered by their specified index.
	 * 
	 * @param pipeArgs
	 *            The task pipe arguments.
	 * @param buildInputPipes
	 *            If true will build the list of input pipes, else output pipes.
	 * @return The list of pipe argument values.
	 */
	private List<String> buildPipes(Map<String, String> pipeArgs, boolean buildInputPipes) {
		String pipeArgumentPrefix;
		String pipeType;
		List<String> pipes;
		
		pipes = new ArrayList<String>();
		
		// Setup processing variables based on whether we're building input or
		// output pipes.
		if (buildInputPipes) {
			pipeArgumentPrefix = PipelineConstants.IN_PIPE_ARGUMENT_PREFIX;
			pipeType = "input";
		} else {
			pipeArgumentPrefix = PipelineConstants.OUT_PIPE_ARGUMENT_PREFIX;
			pipeType = "output";
		}
		
		// Iterate through all the pipes searching for input pipe definitions.
		for (String pipeArgName : pipeArgs.keySet()) {
			// It is an input pipe definition if starts with the input pipe prefix.
			if (pipeArgName.indexOf(pipeArgumentPrefix) == 0) {
				int pipeIndex;
				
				pipeIndex = getPipeIndex(
					pipeArgName.substring(pipeArgumentPrefix.length())
				);
				
				// Ensure that there aren't two pipes with the same index.
				if (pipes.size() > pipeIndex && pipes.get(pipeIndex) != null) {
					throw new ConduitRuntimeException("Task " + taskId + " has a duplicate " + pipeType + " pipe with index " + pipeIndex + ".");
				}
				
				// Increase the list size to cater for the new pipe index if required.
				for (int i = pipes.size(); i <= pipeIndex; i++) {
					pipes.add(null);
				}
				
				// The pipe is valid, so add it to the list of pipes at the correct index.
				pipes.set(pipeIndex, pipeArgs.get(pipeArgName));
			}
			
			// Verify that we have no null elements left in the list.
			for (int i = 0; i < pipes.size(); i++) {
				if (pipes.get(i) == null) {
					throw new ConduitRuntimeException("Task " + taskId + " is missing " + pipeType + " pipe with index " + i + ".");
				}
			}
		}
		
		return pipes;
	}
	
	
	/**
	 * Finds the specified pipe task, unregisters it from the available tasks
	 * and returns it.
	 * 
	 * @param pipeTasks
	 *            The currently registered pipe tasks.
	 * @param pipeIndex
	 *            The input pipe index for the current task.
	 * @param requiredTaskType
	 *            The required type of the input task.
	 * @return The task to be used as input at the specified index.
	 */
	protected Task getInputTask(Map<String, Task> pipeTasks, int pipeIndex, Class<? extends Task> requiredTaskType) {
		String pipeName;
		Task inputTask;
		
		// If the name for the specified pipe index is not available, we need to
		// generate a default pipe name.
		if (inputPipes.size() > (pipeIndex + 1)) {
			pipeName = inputPipes.get(pipeIndex);
		} else {
			pipeName = PipelineConstants.DEFAULT_PIPE_PREFIX + "." + pipeIndex;
		}
		
		// Get the task writing to the input pipe.
		if (!pipeTasks.containsKey(pipeName)) {
			throw new ConduitRuntimeException("No pipe named " + pipeName + " is available as input for task " + taskId + ".");
		}
		inputTask = pipeTasks.remove(pipeName);
		
		// Ensure that the input task is of the correct type.
		if (!requiredTaskType.isInstance(inputTask)) {
			throw new ConduitRuntimeException("Task " + taskId + " does not support data provided by input pipe " + pipeName + ".");
		}
		
		return inputTask;
	}
	
	
	/**
	 * Registers the specified task as an output.
	 * 
	 * @param pipeTasks
	 *            The currently registered pipe tasks.
	 * @param outputTask
	 *            The task to be registered.
	 * @param pipeIndex
	 *            The index of the pipe on the current task.
	 */
	protected void setOutputTask(Map<String, Task> pipeTasks, Task outputTask, int pipeIndex) {
		String pipeName;
		
		// If the name for the specified pipe index is not available, we need to
		// generate a default pipe name.
		if (outputPipes.size() > (pipeIndex + 1)) {
			pipeName = outputPipes.get(pipeIndex);
		} else {
			pipeName = PipelineConstants.DEFAULT_PIPE_PREFIX + "." + pipeIndex;
		}
		
		// Verify that the output pipe is not already taken.
		if (pipeTasks.containsKey(pipeName)) {
			throw new ConduitRuntimeException("Task " + taskId
					+ " cannot write to pipe " + pipeName
					+ " because the pipe is already being written to.");
		}
		
		// Register the task as writing to the pipe.
		pipeTasks.put(pipeName, outputTask);
	}
	
	
	/**
	 * @return The taskId.
	 */
	protected String getTaskId() {
		return taskId;
	}
	
	
	/**
	 * Connects the task to any input tasks based upon the pipes created by
	 * source tasks, and makes any output pipes available to be used by
	 * subsequent sink tasks.
	 * 
	 * @param pipeTasks
	 *            The currently registered pipe tasks. This will be modified to
	 *            remove any consumed inputs, and modified to add new outputs.
	 */
	public abstract void connect(Map<String, Task> pipeTasks);
	
	
	/**
	 * Begins execution of the task. For many sink tasks, this will not do
	 * anything. Source tasks are likely to begin execution within a new thread.
	 */
	public abstract void run();
	
	
	/**
	 * Waits until all tasks have completed execution before returning. This is
	 * intended for source tasks that run within a separate thread, sink tasks
	 * will not do anything here.
	 */
	public abstract void waitForCompletion();
}
