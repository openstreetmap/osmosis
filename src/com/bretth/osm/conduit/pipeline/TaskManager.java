package com.bretth.osm.conduit.pipeline;

import java.util.Map;

import com.bretth.osm.conduit.task.Task;


public abstract class TaskManager {
	private String taskName;
	private Map<String, String> pipeArgs;
	
	
	protected TaskManager(String taskName, Map<String, String> pipeArgs) {
		this.taskName = taskName;
		this.pipeArgs = pipeArgs;
	}
	
	
	protected Map<String, String> getPipeArgs() {
		return pipeArgs;
	}
	
	
	protected String getTaskName() {
		return taskName;
	}
	
	
	public abstract void connect(Map<String, Task> pipeTasks);
	
	
	public abstract void run();
	
	
	public abstract void waitForCompletion();
}
