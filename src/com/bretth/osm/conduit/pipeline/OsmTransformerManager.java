package com.bretth.osm.conduit.pipeline;

import java.util.Map;

import com.bretth.osm.conduit.task.Task;


public abstract class OsmTransformerManager extends OsmSinkManager {

	public void connectTask(Task task, Map<String, Task> pipeTasks, Map<String, String> pipeArgs) {
		OsmSourceManager.connectTaskImpl(task, getTaskName(), pipeTasks, pipeArgs);
	}
}
