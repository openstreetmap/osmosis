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
	
	
	public static TaskManager createTaskManager(String taskType, String taskId, Map<String, String> taskArgs, Map<String, String> pipeArgs) {
		return getInstance(taskType).createTaskManagerImpl(taskId, taskArgs, pipeArgs);
	}
	
	
	protected TaskManagerFactory() {
		factoryMap.put(getTaskType(), this);
	}
	
	
	protected abstract String getTaskType();
	
	
	protected abstract TaskManager createTaskManagerImpl(String taskId, Map<String, String> taskArgs, Map<String, String> pipeArgs);
	
	
	protected String getStringArgument(Map<String, String> taskArgs, String argName, String defaultValue) {
		if (taskArgs.containsKey(argName)) {
			return taskArgs.get(argName);
		} else {
			return defaultValue;
		}
	}
	
	
	protected double getDoubleArgument(Map<String, String> taskArgs, String argName, String defaultValue) {
		String rawValue;
		
		rawValue = getStringArgument(taskArgs, argName, defaultValue);
		
		return Double.parseDouble(rawValue);
	}
}
