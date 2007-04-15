package com.bretth.osm.conduit.pipeline;

import java.util.HashMap;
import java.util.Map;

import com.bretth.osm.conduit.ConduitRuntimeException;


public abstract class TaskManagerFactory {
	
	private static Map<String, TaskManagerFactory> factoryMap;
	
	
	static {
		factoryMap = new HashMap<String, TaskManagerFactory>();
	}
	
	
	private static TaskManagerFactory getInstance(String taskType) {
		if (!factoryMap.containsKey(taskType)) {
			throw new ConduitRuntimeException("Task type " + taskType + " doesn't exist.");
		}
		
		return factoryMap.get(taskType);
	}
	
	
	protected TaskManagerFactory() {
		factoryMap.put(getTaskType(), this);
	}
	
	
	protected abstract String getTaskType();
	
	
	protected abstract TaskManager createTaskManagerImpl(Map<String, String> taskArgs, Map<String, String> pipeArgs);
	
	
	public static TaskManager createTaskManager(String taskType, Map<String, String> taskArgs, Map<String, String> pipeArgs) {
		return getInstance(taskType).createTaskManagerImpl(taskArgs, pipeArgs);
	}
}
