package com.bretth.osm.conduit.pipeline;

import java.util.Map;

import com.bretth.osm.conduit.task.Task;


public abstract class TaskManager {
	private String taskId;
	private Map<String, String> pipeArgs;
	
	
	protected TaskManager(String taskId, Map<String, String> pipeArgs) {
		this.taskId = taskId;
		this.pipeArgs = pipeArgs;
	}
	
	
	protected Map<String, String> getPipeArgs() {
		return pipeArgs;
	}
	
	
	protected String getTaskId() {
		return taskId;
	}
	
	
	public abstract void connect(Map<String, Task> pipeTasks);
	
	
	public abstract void run();
	
	
	public abstract void waitForCompletion();
}
