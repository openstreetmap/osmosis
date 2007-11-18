package com.bretth.osmosis.core.pipeline.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.cli.TaskConfiguration;


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
	 * @param taskConfig
	 *            Contains all information required to instantiate and configure
	 *            the task.
	 * @return The newly created task manager.
	 */
	public static TaskManager createTaskManager(TaskConfiguration taskConfig) {
		return getInstance(taskConfig.getType()).createTaskManagerImpl(taskConfig);
	}
	
	
	/**
	 * Create a new task manager containing a task instance.
	 * 
	 * @param taskConfig
	 *            Contains all information required to instantiate and configure
	 *            the task.
	 * @return The newly created task manager.
	 */
	protected abstract TaskManager createTaskManagerImpl(TaskConfiguration taskConfig);
	
	
	/**
	 * Checks if the specified argument has been supplied.
	 * 
	 * @param taskConfig
	 *            Contains all information required to instantiate and configure
	 *            the task.
	 * @param argName
	 *            The name of the argument.
	 * @return True if the argument has been supplied.
	 */
	protected boolean doesArgumentExist(TaskConfiguration taskConfig, String argName) {
		return taskConfig.getConfigArgs().containsKey(argName);
	}
	
	
	/**
	 * Utility method for retrieving the default argument for the task as a String.
	 * 
	 * @param taskConfig
	 *            Contains all information required to instantiate and configure
	 *            the task.
	 * @param defaultValue
	 *            The default value of the argument if not value is available.
	 * @return The value of the argument.
	 */
	protected String getDefaultStringArgument(TaskConfiguration taskConfig, String defaultValue) {
		if (taskConfig.getDefaultArg() != null) {
			return taskConfig.getDefaultArg();
		} else {
			return defaultValue;
		}
	}
	
	
	/**
	 * Utility method for retrieving a String argument value from a Map of task
	 * arguments.
	 * 
	 * @param taskConfig
	 *            Contains all information required to instantiate and configure
	 *            the task.
	 * @param argName
	 *            The name of the argument.
	 * @return The value of the argument.
	 */
	protected String getStringArgument(TaskConfiguration taskConfig, String argName) {
		Map<String, String> configArgs;
		
		configArgs = taskConfig.getConfigArgs();
		
		if (configArgs.containsKey(argName)) {
			return configArgs.get(argName);
		} else {
			throw new OsmosisRuntimeException(
				"Argument " + argName
					+ " for task " + taskConfig.getId() + " does not exist.");
		}
	}
	
	
	/**
	 * Utility method for retrieving a String argument value from a Map of task
	 * arguments.
	 * 
	 * @param taskConfig
	 *            Contains all information required to instantiate and configure
	 *            the task.
	 * @param argName
	 *            The name of the argument.
	 * @param defaultValue
	 *            The default value of the argument if not value is available.
	 * @return The value of the argument.
	 */
	protected String getStringArgument(TaskConfiguration taskConfig, String argName, String defaultValue) {
		Map<String, String> configArgs;
		
		configArgs = taskConfig.getConfigArgs();
		
		if (configArgs.containsKey(argName)) {
			return configArgs.get(argName);
		} else {
			return defaultValue;
		}
	}
	
	
	/**
	 * Utility method for retrieving an integer argument value from a Map of
	 * task arguments.
	 * 
	 * @param taskConfig
	 *            Contains all information required to instantiate and configure
	 *            the task.
	 * @param argName
	 *            The name of the argument.
	 * @param defaultValue
	 *            The default value of the argument if not value is available.
	 * @return The value of the argument.
	 */
	protected int getIntegerArgument(TaskConfiguration taskConfig, String argName, int defaultValue) {
		Map<String, String> configArgs;
		
		configArgs = taskConfig.getConfigArgs();
		
		if (configArgs.containsKey(argName)) {
			try {
				return Integer.parseInt(configArgs.get(argName));
			} catch (NumberFormatException e) {
				throw new OsmosisRuntimeException(
					"Argument " + argName + " for task " + taskConfig.getId()
					+ " must be an integer number.", e);
			}
		} else {
			return defaultValue;
		}
	}
	
	
	/**
	 * Utility method for retrieving a double argument value from a Map of task
	 * arguments.
	 * 
	 * @param taskConfig
	 *            Contains all information required to instantiate and configure
	 *            the task.
	 * @param argName
	 *            The name of the argument.
	 * @param defaultValue
	 *            The default value of the argument if not value is available.
	 * @return The value of the argument.
	 */
	protected double getDoubleArgument(TaskConfiguration taskConfig, 
			String argName, double defaultValue) {
		Map<String, String> configArgs;
		
		configArgs = taskConfig.getConfigArgs();
		
		if (configArgs.containsKey(argName)) {
			try {
				return Double.parseDouble(configArgs.get(argName));
			} catch (NumberFormatException e) {
				throw new OsmosisRuntimeException(
					"Argument " + argName + " for task " + taskConfig.getId()
					+ " must be a decimal number.", e);
			}
		} else {
			return defaultValue;
		}
	}
	
	
	/**
	 * Utility method for retrieving a date argument value from a Map of task
	 * arguments.
	 * 
	 * @param taskConfig
	 *            Contains all information required to instantiate and configure
	 *            the task.
	 * @param argName
	 *            The name of the argument.
	 * @param defaultValue
	 *            The default value of the argument if not value is available.
	 * @return The value of the argument.
	 */
	protected Date getDateArgument(TaskConfiguration taskConfig, 
			String argName, Date defaultValue) {
		Map<String, String> configArgs;
		
		configArgs = taskConfig.getConfigArgs();
		
		if (configArgs.containsKey(argName)) {
			try {
				SimpleDateFormat dateFormat;
				
				dateFormat = new SimpleDateFormat(DATE_FORMAT);
				
				return dateFormat.parse(configArgs.get(argName));
				
			} catch (ParseException e) {
				throw new OsmosisRuntimeException(
					"Argument " + argName + " for task " + taskConfig.getId()
					+ " must be a date in format " + DATE_FORMAT + ".", e);
			}
		} else {
			return defaultValue;
		}
	}
	
	
	/**
	 * Utility method for retrieving a boolean argument value from a Map of task
	 * arguments.
	 * 
	 * @param taskConfig
	 *            Contains all information required to instantiate and configure
	 *            the task.
	 * @param argName
	 *            The name of the argument.
	 * @param defaultValue
	 *            The default value of the argument if not value is available.
	 * @return The value of the argument.
	 */
	protected boolean getBooleanArgument(TaskConfiguration taskConfig, 
			String argName, boolean defaultValue) {
		Map<String, String> configArgs;
		
		configArgs = taskConfig.getConfigArgs();
		
		if (configArgs.containsKey(argName)) {
			String rawValue;
			
			rawValue = configArgs.get(argName).toLowerCase();
			
			if ("true".equals(rawValue) || "yes".equals(rawValue)) {
				return true;
				
			} else if ("false".equals(rawValue) || "no".equals(rawValue)) {
				return false;
				
			} else {
				throw new OsmosisRuntimeException(
					"Argument " + argName + " for task " + taskConfig.getId()
					+ " must be one of yes, no, true or false.");
			}
			
		} else {
			return defaultValue;
		}
	}
}
