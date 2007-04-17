package com.bretth.osm.conduit.pipeline;

import java.util.Map;

import com.bretth.osm.conduit.ConduitRuntimeException;
import com.bretth.osm.conduit.task.OsmSink;
import com.bretth.osm.conduit.task.OsmSource;
import com.bretth.osm.conduit.task.Task;


public class OsmSinkManager extends TaskManager {
	private OsmSink task;
	
	
	public OsmSinkManager(String taskId, OsmSink task, Map<String, String> pipeArgs) {
		super(taskId, pipeArgs);
		
		this.task = task;
	}
	
	
	public void connect(Map<String, Task> pipeTasks) {
		Map<String, String> pipeArgs;
		String pipeName;
		Task pipeWriter;
		OsmSource source;
		
		pipeArgs = getPipeArgs();
		
		// Get the name of the input pipe for this sink.
		if (pipeArgs.containsKey(PipelineConstants.IN_PIPE_ARGUMENT_PREFIX)) {
			pipeName = pipeArgs.get(PipelineConstants.IN_PIPE_ARGUMENT_PREFIX);
		} else {
			pipeName = PipelineConstants.DEFAULT_PIPE_NAME;
		}
		
		// Get the task writing to the input pipe.
		if (!pipeTasks.containsKey(pipeName)) {
			throw new ConduitRuntimeException("No pipe named " + pipeName + " is available as input for task " + getTaskId() + ".");
		}
		pipeWriter = pipeTasks.remove(pipeName);
		
		// Cast the input feed to the correct type.
		if (!(pipeWriter instanceof OsmSource)) {
			throw new ConduitRuntimeException("Task " + getTaskId() + " does not support data provided by input pipe " + pipeName + ".");
		}
		source = (OsmSource) pipeWriter;
		
		// Connect the tasks.
		source.setOsmSink(task);
	}
	
	
	public void run() {
		// Nothing to do for a sink because it passively receives data.
	}
	
	
	public void waitForCompletion() {
		// Nothing to do for a sink because it passively receives data.
	}
}
