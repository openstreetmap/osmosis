package com.bretth.osm.conduit.pipeline;

import java.util.Map;

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

	private Map<String, String> pipeArgs;

	
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
		this.pipeArgs = pipeArgs;
	}
	
	
	/**
	 * @return The pipeArgs.
	 */
	protected Map<String, String> getPipeArgs() {
		return pipeArgs;
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
