package com.bretth.osmosis.pipeline;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
	private static final String DATE_FORMAT = "yyyy-MM-dd_HH:mm:ss";
	
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
	 * @param taskId
	 *            The identifier for the task retrieving the parameter.
	 * @param taskArgs
	 *            The task arguments.
	 * @param argName
	 *            The name of the argument.
	 * @param defaultValue
	 *            The default value of the argument if not value is available.
	 * @return The value of the argument.
	 */
	protected String getStringArgument(String taskId, Map<String, String> taskArgs,
			String argName, String defaultValue) {
		if (taskArgs.containsKey(argName)) {
			return taskArgs.get(argName);
		} else {
			return defaultValue;
		}
	}
	
	
	/**
	 * Utility method for retrieving an integer argument value from a Map of
	 * task arguments.
	 * 
	 * @param taskId
	 *            The identifier for the task retrieving the parameter.
	 * @param taskArgs
	 *            The task arguments.
	 * @param argName
	 *            The name of the argument.
	 * @param defaultValue
	 *            The default value of the argument if not value is available.
	 * @return The value of the argument.
	 */
	protected int getIntegerArgument(String taskId, Map<String, String> taskArgs,
			String argName, int defaultValue) {
		int result;
		
		if (taskArgs.containsKey(argName)) {
			try {
				result = Integer.parseInt(taskArgs.get(argName));
			} catch (NumberFormatException e) {
				throw new OsmosisRuntimeException(
					"Argument " + argName + " for task " + taskId
					+ " must be an integer number.", e);
			}
		} else {
			result = defaultValue;
		}
		
		return result;
	}
	
	
	/**
	 * Utility method for retrieving a double argument value from a Map of task
	 * arguments.
	 * 
	 * @param taskId
	 *            The identifier for the task retrieving the parameter.
	 * @param taskArgs
	 *            The task arguments.
	 * @param argName
	 *            The name of the argument.
	 * @param defaultValue
	 *            The default value of the argument if not value is available.
	 * @return The value of the argument.
	 */
	protected double getDoubleArgument(String taskId, Map<String, String> taskArgs,
			String argName, double defaultValue) {
		double result;
		
		if (taskArgs.containsKey(argName)) {
			try {
				result = Double.parseDouble(taskArgs.get(argName));
			} catch (NumberFormatException e) {
				throw new OsmosisRuntimeException(
					"Argument " + argName + " for task " + taskId
					+ " must be a decimal number.", e);
			}
		} else {
			result = defaultValue;
		}
		
		return result;
	}
	
	
	/**
	 * Utility method for retrieving a date argument value from a Map of task
	 * arguments.
	 * 
	 * @param taskId
	 *            The identifier for the task retrieving the parameter.
	 * @param taskArgs
	 *            The task arguments.
	 * @param argName
	 *            The name of the argument.
	 * @param defaultValue
	 *            The default value of the argument if not value is available.
	 * @return The value of the argument.
	 */
	protected Date getDateArgument(String taskId, Map<String, String> taskArgs,
			String argName, Date defaultValue) {
		Date result;
		
		if (taskArgs.containsKey(argName)) {
			try {
				SimpleDateFormat dateFormat;
				
				dateFormat = new SimpleDateFormat(DATE_FORMAT);
				
				result = dateFormat.parse(taskArgs.get(argName));
				
			} catch (ParseException e) {
				throw new OsmosisRuntimeException(
					"Argument " + argName + " for task " + taskId
					+ " must be a date in format " + DATE_FORMAT + ".", e);
			}
		} else {
			result = defaultValue;
		}
		
		return result;
	}
	
	
	/**
	 * Utility method for retrieving a boolean argument value from a Map of task
	 * arguments.
	 * 
	 * @param taskId
	 *            The identifier for the task retrieving the parameter.
	 * @param taskArgs
	 *            The task arguments.
	 * @param argName
	 *            The name of the argument.
	 * @param defaultValue
	 *            The default value of the argument if not value is available.
	 * @return The value of the argument.
	 */
	protected boolean getBooleanArgument(String taskId, Map<String, String> taskArgs,
			String argName, boolean defaultValue) {
		boolean result;
		
		if (taskArgs.containsKey(argName)) {
			String rawValue;
			
			rawValue = taskArgs.get(argName).toLowerCase();
			
			if ("true".equals(rawValue) || "yes".equals(rawValue)) {
				result = true;
				
			} else if ("false".equals(rawValue) || "no".equals(rawValue)) {
				result = false;
				
			} else {
				throw new OsmosisRuntimeException(
					"Argument " + argName + " for task " + taskId
					+ " must be one of yes, no, true or false.");
			}
			
		} else {
			result = defaultValue;
		}
		
		return result;
	}
}
