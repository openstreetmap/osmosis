package com.bretth.osmosis.pipeline;

import java.util.HashMap;
import java.util.Map;

import com.bretth.osmosis.OsmosisRuntimeException;

/**
 * All task implementations require a corresponding task manager factory. This
 * task manager factory is responsible for instantiating a task based upon
 * command line arguments, and instantiating a task manager to manage the task
 * within a pipeline. The factories are singleton instances registered globally
 * and re-used for every task to be created.
 * 
 * @author Brett Henderson
 */
public abstract class TaskManagerFactory {

	/**
	 * The global register of task manager factories, keyed by a unique
	 * identifier.
	 */
	private static Map<String, TaskManagerFactory> factoryMap;

	static {
		factoryMap = new HashMap<String, TaskManagerFactory>();
	}
	
	
	/**
	 * Registers a new factory.
	 * 
	 * @param taskType
	 *            The name the factory is identified by.
	 * @param factory
	 *            The factory to be registered.
	 */
	public static void register(String taskType, TaskManagerFactory factory) {
		if (factoryMap.containsKey(taskType)) {
			throw new OsmosisRuntimeException("Task type \"" + taskType + "\" already exists.");
		}
		
		factoryMap.put(taskType, factory);
	}
	

	/**
	 * Get a task manager factory from the register.
	 * 
	 * @param taskType
	 *            The type of task requiring a factory.
	 * @return The factory instance.
	 */
	private static TaskManagerFactory getInstance(String taskType) {
		if (!factoryMap.containsKey(taskType)) {
			throw new OsmosisRuntimeException("Task type " + taskType
					+ " doesn't exist.");
		}

		return factoryMap.get(taskType);
	}

	/**
	 * Create a new task manager containing a task instance.
	 * 
	 * @param taskType
	 *            The identifier for the task type.
	 * @param taskId
	 *            The unique identifier for this task instance.
	 * @param taskArgs
	 *            The arguments used for initialising the task instance.
	 * @param pipeArgs
	 *            The arguments used for connecting the task inputs and outputs.
	 * @return The newly created task manager.
	 */
	public static TaskManager createTaskManager(String taskType, String taskId,
			Map<String, String> taskArgs, Map<String, String> pipeArgs) {
		return getInstance(taskType).createTaskManagerImpl(taskId, taskArgs,
				pipeArgs);
	}
	

	/**
	 * Create a new task manager containing a task instance.
	 * 
	 * @param taskId
	 *            The unique identifier for this task instance.
	 * @param taskArgs
	 *            The arguments used for initialising the task instance.
	 * @param pipeArgs
	 *            The arguments used for connecting the task inputs and outputs.
	 * @return The newly created task manager.
	 */
	protected abstract TaskManager createTaskManagerImpl(String taskId,
			Map<String, String> taskArgs, Map<String, String> pipeArgs);
	
	
	/**
	 * Utility method for retrieving a String argument value from a Map of task
	 * arguments.
	 * 
	 * @param taskArgs
	 *            The task arguments.
	 * @param argName
	 *            The name of the argument.
	 * @param defaultValue
	 *            The default value of the argument if not value is available.
	 * @return The value of the argument.
	 */
	protected String getStringArgument(Map<String, String> taskArgs,
			String argName, String defaultValue) {
		if (taskArgs.containsKey(argName)) {
			return taskArgs.get(argName);
		} else {
			return defaultValue;
		}
	}
	
	
	/**
	 * Utility method for retrieving a double argument value from a Map of task
	 * arguments.
	 * 
	 * @param taskArgs
	 *            The task arguments.
	 * @param argName
	 *            The name of the argument.
	 * @param defaultValue
	 *            The default value of the argument if not value is available.
	 * @return The value of the argument.
	 */
	protected double getDoubleArgument(Map<String, String> taskArgs,
			String argName, String defaultValue) {
		String rawValue;

		rawValue = getStringArgument(taskArgs, argName, defaultValue);

		return Double.parseDouble(rawValue);
	}
}
