package com.bretth.osm.conduit.pipeline;

import java.util.Map;

import com.bretth.osm.conduit.ConduitRuntimeException;
import com.bretth.osm.conduit.task.OsmRunnableSource;
import com.bretth.osm.conduit.task.OsmSource;
import com.bretth.osm.conduit.task.Task;


/**
 * A task manager implementation for OsmRunnableSource task implementations.
 * 
 * @author Brett Henderson
 */
public class OsmRunnableSourceManager extends TaskManager {
	private OsmRunnableSource task;
	
	private Thread thread;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param taskId
	 *            A unique identifier for the task. This is used to produce
	 *            meaningful errors when errors occur.
	 * @param task
	 *            The task instance to be managed.
	 * @param pipeArgs
	 *            The arguments defining input and output pipes for the task,
	 *            pipes are a logical concept for identifying how the tasks are
	 *            connected together.
	 */
	public OsmRunnableSourceManager(String taskId, OsmRunnableSource task,
			Map<String, String> pipeArgs) {
		super(taskId, pipeArgs);
		
		this.task = task;
	}
	
	
	/**
	 * Makes the OsmSource task output available in the pipe tasks map. This is
	 * static so that it can be used by classes outside the direct inheritance
	 * tree.
	 * 
	 * @param task
	 *            The task to be connected.
	 * @param taskId
	 *            The unique identifier for the task. This is used to produce
	 *            meaningful errors when errors occur.
	 * @param pipeTasks
	 *            The currently registered pipe tasks. This will be modified to
	 *            add new outputs.
	 * @param pipeArgs
	 *            The arguments defining input and output pipes for the task,
	 *            pipes are a logical concept for identifying how the tasks are
	 *            connected together.
	 */
	public static void connectImpl(OsmSource task, String taskId,
			Map<String, Task> pipeTasks, Map<String, String> pipeArgs) {
		String pipeName;

		// Get the name of the output pipe for this source.
		if (pipeArgs.containsKey(PipelineConstants.OUT_PIPE_ARGUMENT_PREFIX)) {
			pipeName = pipeArgs.get(PipelineConstants.OUT_PIPE_ARGUMENT_PREFIX);
		} else {
			pipeName = PipelineConstants.DEFAULT_PIPE_NAME;
		}

		// Verify that the output pipe is not already taken.
		if (pipeTasks.containsKey(pipeName)) {
			throw new ConduitRuntimeException("Task " + taskId
					+ " cannot write to pipe " + pipeName
					+ " because the pipe is already being written to.");
		}

		// Register the task as writing to the pipe.
		pipeTasks.put(pipeName, task);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void connect(Map<String, Task> pipeTasks) {
		connectImpl(task, getTaskId(), pipeTasks, getPipeArgs());
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		if (thread != null) {
			throw new ConduitRuntimeException("Task " + getTaskId()
					+ " is already running.");
		}

		thread = new Thread(task);

		thread.start();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void waitForCompletion() {
		if (thread != null) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				// Do nothing.
			}

			thread = null;
		}
	}
}
