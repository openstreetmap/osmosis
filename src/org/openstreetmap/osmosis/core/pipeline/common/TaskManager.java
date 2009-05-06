// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.pipeline.common;

import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.task.common.Task;


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
	private Map<Integer, String> inputPipeNames;
	private Map<Integer, String> outputPipeNames;
	
	
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
		
		inputPipeNames = buildPipes(pipeArgs, true);
		outputPipeNames = buildPipes(pipeArgs, false);
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
				throw new OsmosisRuntimeException(
					"Task " + taskId
					+ " contains a pipe definition without '.' between prefix and suffix."
				);
			}
			
			// The remaining suffix must be a number defining the index.
			indexString = pipeArgNameSuffix.substring(1);
			
			// Ensure the index exists.
			if (indexString.length() <= 0) {
				throw new OsmosisRuntimeException(
					"Task " + taskId
					+ " contains a pipe definition without an index after the '.'."
				);
			}
			
			// Parse the suffix string into a number.
			try {
				pipeIndex = Integer.parseInt(indexString);
				
			} catch (NumberFormatException e) {
				throw new OsmosisRuntimeException("Task " + taskId + " has a pipe with an incorrect index suffix.");
			}
		}
		
		return pipeIndex;
	}
	
	
	/**
	 * Builds a list of pipe names keyed by their specified index.
	 * 
	 * @param pipeArgs
	 *            The task pipe arguments.
	 * @param buildInputPipes
	 *            If true will build the list of input pipes, else output pipes.
	 * @return The list of pipe argument values.
	 */
	private Map<Integer, String> buildPipes(Map<String, String> pipeArgs, boolean buildInputPipes) {
		String pipeArgumentPrefix;
		String pipeType;
		Map<Integer, String> pipes;
		
		pipes = new HashMap<Integer, String>();
		
		// Setup processing variables based on whether we're building input or
		// output pipes.
		if (buildInputPipes) {
			pipeArgumentPrefix = PipelineConstants.IN_PIPE_ARGUMENT_PREFIX;
			pipeType = "input";
		} else {
			pipeArgumentPrefix = PipelineConstants.OUT_PIPE_ARGUMENT_PREFIX;
			pipeType = "output";
		}
		
		// Iterate through all the pipes searching for the required pipe definitions.
		for (String pipeArgName : pipeArgs.keySet()) {
			// It is an input pipe definition if starts with the input pipe prefix.
			if (pipeArgName.indexOf(pipeArgumentPrefix) == 0) {
				Integer pipeIndex;
				
				pipeIndex = new Integer(
					getPipeIndex(pipeArgName.substring(pipeArgumentPrefix.length()))
				);
				
				// Ensure that there aren't two pipes with the same index.
				if (pipes.containsKey(pipeIndex)) {
					throw new OsmosisRuntimeException(
							"Task " + taskId + " has a duplicate " + pipeType + " pipe with index " + pipeIndex + ".");
				}
				
				// The pipe is valid, so add it to the pipe map keyed by its index.
				pipes.put(pipeIndex, pipeArgs.get(pipeArgName));
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
	protected Task getInputTask(PipeTasks pipeTasks, int pipeIndex, Class<? extends Task> requiredTaskType) {
		Task inputTask;
		Integer pipeIndexO = new Integer(pipeIndex);
		
		// We use the specified pipe name if it exists, otherwise we get the
		// next available default pipe.
		if (inputPipeNames.containsKey(pipeIndexO)) {
			inputTask = pipeTasks.retrieveTask(taskId, inputPipeNames.get(pipeIndexO), requiredTaskType);
		} else {
			inputTask = pipeTasks.retrieveTask(taskId, requiredTaskType);
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
	protected void setOutputTask(PipeTasks pipeTasks, Task outputTask, int pipeIndex) {
		Integer pipeIndexO = new Integer(pipeIndex);
		
		// We use the specified pipe name if it exists, otherwise we register
		// using the next available default pipe name.
		if (outputPipeNames.containsKey(pipeIndexO)) {
			pipeTasks.putTask(taskId, outputPipeNames.get(pipeIndexO), outputTask);
		} else {
			pipeTasks.putTask(taskId, outputTask);
		}
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
	public abstract void connect(PipeTasks pipeTasks);
	
	
	/**
	 * Begins execution of the task. For many sink tasks, this will not do
	 * anything. Source tasks are likely to begin execution within a new thread.
	 */
	public abstract void execute();
	
	
	/**
	 * Waits until all tasks have completed execution before returning. This is
	 * intended for source tasks that run within a separate thread, sink tasks
	 * will not do anything here.
	 * 
	 * @return True if the thread completed successfully.
	 */
	public abstract boolean waitForCompletion();
}
