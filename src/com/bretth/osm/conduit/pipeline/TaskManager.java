package com.bretth.osm.conduit.pipeline;

import java.util.HashMap;
import java.util.Map;

import com.bretth.osm.conduit.ConduitRuntimeException;
import com.bretth.osm.conduit.task.Task;


public abstract class TaskManager {
	
	private static Map<String, TaskManager> taskBuilderMap;
	
	
	static {
		taskBuilderMap = new HashMap<String, TaskManager>();
	}
	
	
	public static TaskManager getInstance(String taskName) {
		if (!taskBuilderMap.containsKey(taskName)) {
			throw new ConduitRuntimeException("Pipeline task " + taskName + " doesn't exist.");
		}
		
		return taskBuilderMap.get(taskName);
	}
	
	
	protected TaskManager() {
		taskBuilderMap.put(getTaskName(), this);
	}
	
	
	protected abstract String getTaskName();
	
	
	public abstract Task createTask(Map<String, String> args);
	
	
	public abstract void connectTask(Task task, Map<String, Task> pipeTasks, Map<String, String> pipeArgs);
	
	
	public abstract void runTask(Task task);
	
	
	public abstract void waitOnTask(Task task);
}
