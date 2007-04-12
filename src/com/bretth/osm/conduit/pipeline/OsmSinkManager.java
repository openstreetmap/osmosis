package com.bretth.osm.conduit.pipeline;

import java.util.Map;

import com.bretth.osm.conduit.ConduitRuntimeException;
import com.bretth.osm.conduit.task.OsmSink;
import com.bretth.osm.conduit.task.OsmSource;
import com.bretth.osm.conduit.task.Task;


public abstract class OsmSinkManager extends TaskManager {
	
	public void connectTask(Task task, Map<String, Task> pipeTasks, Map<String, String> pipeArgs) {
		OsmSink sink;
		String pipeName;
		Task pipeWriter;
		OsmSource source;
		
		// Cast the task to the correct type.
		sink = (OsmSink) task;
		
		// Get the name of the input pipe for this sink.
		if (pipeArgs.containsKey(PipelineConstants.IN_PIPE_ARGUMENT_PREFIX)) {
			pipeName = pipeArgs.get(PipelineConstants.IN_PIPE_ARGUMENT_PREFIX);
		} else {
			pipeName = PipelineConstants.DEFAULT_PIPE_NAME;
		}
		
		// Get the task writing to the input pipe.
		if (!pipeTasks.containsKey(pipeName)) {
			throw new ConduitRuntimeException("No pipe named " + pipeName + "is available as input for task " + getTaskName() + ".");
		}
		pipeWriter = pipeTasks.get(pipeName);
		
		// Cast the input feed to the correct type.
		if (!(pipeWriter instanceof OsmSource)) {
			throw new ConduitRuntimeException("Task " + getTaskName() + " does not support data provided by input pipe " + pipeName + ".");
		}
		source = (OsmSource) pipeWriter;
		
		// Connect the tasks.
		source.setOsmSink(sink);
	}
	
	
	public void runTask(Task task) {
		// Nothing to do for a sink because it passively receives data.
	}
	
	
	public void waitOnTask(Task task) {
		// Nothing to do for a sink because it passively receives data.
	}
}
