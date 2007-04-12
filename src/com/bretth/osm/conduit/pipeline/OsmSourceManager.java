package com.bretth.osm.conduit.pipeline;

import java.util.Map;

import com.bretth.osm.conduit.ConduitRuntimeException;
import com.bretth.osm.conduit.task.OsmSource;
import com.bretth.osm.conduit.task.Task;


public abstract class OsmSourceManager extends TaskManager {
	private Thread thread;
	
	
	public static void connectTaskImpl(Task task, String taskName, Map<String, Task> pipeTasks, Map<String, String> pipeArgs) {
		OsmSource source;
		String pipeName;
		
		// Cast the task to the correct type.
		source = (OsmSource) task;
		
		// Get the name of the output pipe for this source.
		if (pipeArgs.containsKey(PipelineConstants.OUT_PIPE_ARGUMENT_PREFIX)) {
			pipeName = pipeArgs.get(PipelineConstants.OUT_PIPE_ARGUMENT_PREFIX);
		} else {
			pipeName = PipelineConstants.DEFAULT_PIPE_NAME;
		}
		
		// Verify that the output pipe is not already taken.
		if (pipeTasks.containsKey(pipeName)) {
			throw new ConduitRuntimeException("Task " + taskName + " cannot write to pipe " + pipeName + " because the pipe is already being written to.");
		}
		
		// Register the task as writing to the pipe.
		pipeTasks.put(pipeName, source);
	}
	
	
	public void connectTask(Task task, Map<String, Task> pipeTasks,
			Map<String, String> pipeArgs) {
		connectTaskImpl(task, getTaskName(), pipeTasks, pipeArgs);
	}
	
	
	public void runTask(Task task) {
		OsmSource source;
		
		// Cast the task to the correct type.
		source = (OsmSource) task;
		
		if (thread != null) {
			throw new ConduitRuntimeException("Task " + getTaskName() + " is already running.");
		}
		
		thread = new Thread(source);
		
		thread.start();
	}
	
	
	public void waitOnTask(Task task) {
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
