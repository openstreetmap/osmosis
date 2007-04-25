package com.bretth.osm.conduit.pipeline;

import java.util.Map;

import com.bretth.osm.conduit.ConduitRuntimeException;
import com.bretth.osm.conduit.task.OsmRunnableSource;
import com.bretth.osm.conduit.task.OsmSource;
import com.bretth.osm.conduit.task.Task;


public class OsmRunnableSourceManager extends TaskManager {
	private OsmRunnableSource task;
	private Thread thread;
	
	
	public OsmRunnableSourceManager(String taskId, OsmRunnableSource task, Map<String, String> pipeArgs) {
		super(taskId, pipeArgs);
		
		this.task = task;
	}
	
	
	public static void connectImpl(OsmSource task, String taskType, Map<String, Task> pipeTasks, Map<String, String> pipeArgs) {
		String pipeName;
		
		// Get the name of the output pipe for this source.
		if (pipeArgs.containsKey(PipelineConstants.OUT_PIPE_ARGUMENT_PREFIX)) {
			pipeName = pipeArgs.get(PipelineConstants.OUT_PIPE_ARGUMENT_PREFIX);
		} else {
			pipeName = PipelineConstants.DEFAULT_PIPE_NAME;
		}
		
		// Verify that the output pipe is not already taken.
		if (pipeTasks.containsKey(pipeName)) {
			throw new ConduitRuntimeException("Task " + taskType + " cannot write to pipe " + pipeName + " because the pipe is already being written to.");
		}
		
		// Register the task as writing to the pipe.
		pipeTasks.put(pipeName, task);
	}
	
	
	@Override
	public void connect(Map<String, Task> pipeTasks) {
		connectImpl(task, getTaskId(), pipeTasks, getPipeArgs());
	}
	
	
	@Override
	public void run() {
		if (thread != null) {
			throw new ConduitRuntimeException("Task " + getTaskId() + " is already running.");
		}
		
		thread = new Thread(task);
		
		thread.start();
	}
	
	
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
