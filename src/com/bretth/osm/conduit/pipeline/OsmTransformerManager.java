package com.bretth.osm.conduit.pipeline;

import java.util.Map;

import com.bretth.osm.conduit.task.OsmTransformer;
import com.bretth.osm.conduit.task.Task;


public class OsmTransformerManager extends OsmSinkManager {
	private OsmTransformer task;
	
	
	public OsmTransformerManager(String taskType, OsmTransformer task, Map<String, String> pipeArgs) {
		super(taskType, task, pipeArgs);
		
		this.task = task;
	}
	
	
	@Override
	public void connect(Map<String, Task> pipeTasks) {
		// Connect the pipeline inputs.
		super.connect(pipeTasks);
		
		// Connect the pipeline outputs.
		OsmRunnableSourceManager.connectImpl(task, getTaskId(), pipeTasks, getPipeArgs());
	}
}
