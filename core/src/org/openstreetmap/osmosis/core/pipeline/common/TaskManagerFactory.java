// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.pipeline.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;


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
	private static final Locale DATE_LOCALE = Locale.US;
	
	/**
	 * This stores the task options that have been accessed during task
	 * configuration. Any unused options are typically misspelt options that
	 * should raise an error. Minor overkill but this is stored as a thread
	 * local to ensure multiple threads can access a factory safely.
	 */
	private ThreadLocal<Set<String>> accessedTaskOptions;
	
	
	/**
	 * Creates a new instance.
	 */
	protected TaskManagerFactory() {
		accessedTaskOptions = new ThreadLocal<Set<String>>();
	}
	
	
	/**
	 * Create a new task manager containing a task instance.
	 * 
	 * @param taskConfig
	 *            Contains all information required to instantiate and configure
	 *            the task.
	 * @return The newly created task manager.
	 */
	public TaskManager createTaskManager(TaskConfiguration taskConfig) {
		TaskManager taskManager;
		
		// Create a new accessed task options store.
		accessedTaskOptions.set(new HashSet<String>());
		
		taskManager = createTaskManagerImpl(taskConfig);
		
		for (String argName : taskConfig.getConfigArgs().keySet()) {
			if (!accessedTaskOptions.get().contains(argName)) {
				throw new OsmosisRuntimeException(
					"Argument " + argName + " for task " + taskConfig.getId() + " was not recognised.");
			}
		}
		
		// Clear the accessed task options.
		accessedTaskOptions.set(null);
		
		return taskManager;
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
		
		accessedTaskOptions.get().add(argName);
		
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
		
		accessedTaskOptions.get().add(argName);
		
		configArgs = taskConfig.getConfigArgs();
		
		if (configArgs.containsKey(argName)) {
			return configArgs.get(argName);
		} else {
			return defaultValue;
		}
	}
	
	
	/**
	 * Utility method for retrieving the default argument for the task as an integer.
	 * 
	 * @param taskConfig
	 *            Contains all information required to instantiate and configure
	 *            the task.
	 * @param defaultValue
	 *            The default value of the argument if not value is available.
	 * @return The value of the argument.
	 */
	protected int getDefaultIntegerArgument(TaskConfiguration taskConfig, int defaultValue) {
		String defaultArg;
		
		defaultArg = taskConfig.getDefaultArg();
		
		if (defaultArg != null) {
			try {
				return Integer.parseInt(defaultArg);
			} catch (NumberFormatException e) {
				throw new OsmosisRuntimeException(
					"Default argument for task " + taskConfig.getId()
					+ " must be an integer number.", e);
			}
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
	 * @return The value of the argument.
	 */
	protected int getIntegerArgument(TaskConfiguration taskConfig, String argName) {
		Map<String, String> configArgs;
		
		accessedTaskOptions.get().add(argName);
		
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
			throw new OsmosisRuntimeException(
				"Argument " + argName + " for task " + taskConfig.getId() + " does not exist."
			);
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
		
		accessedTaskOptions.get().add(argName);
		
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
		
		accessedTaskOptions.get().add(argName);
		
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
		
		accessedTaskOptions.get().add(argName);
		
		configArgs = taskConfig.getConfigArgs();
		
		if (configArgs.containsKey(argName)) {
			try {
				SimpleDateFormat dateFormat;
				
				dateFormat = new SimpleDateFormat(DATE_FORMAT, DATE_LOCALE);
				
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
	 * Utility method for retrieving a date argument value from a Map of task
	 * arguments.
	 * 
	 * @param taskConfig
	 *            Contains all information required to instantiate and configure
	 *            the task.
	 * @param argName
	 *            The name of the argument.
	 * @param timeZone
	 *            The time zone to parse the date in.
	 * @return The value of the argument.
	 */
	protected Date getDateArgument(TaskConfiguration taskConfig, 
			String argName, TimeZone timeZone) {
		Map<String, String> configArgs;
		
		accessedTaskOptions.get().add(argName);
		
		configArgs = taskConfig.getConfigArgs();
		
		if (configArgs.containsKey(argName)) {
			try {
				SimpleDateFormat dateFormat;
				
				dateFormat = new SimpleDateFormat(DATE_FORMAT, DATE_LOCALE);
				dateFormat.setTimeZone(timeZone);
				
				return dateFormat.parse(configArgs.get(argName));
				
			} catch (ParseException e) {
				throw new OsmosisRuntimeException(
					"Argument " + argName + " for task " + taskConfig.getId()
					+ " must be a date in format " + DATE_FORMAT + ".", e);
			}
		} else {
			throw new OsmosisRuntimeException("Argument " + argName
					+ " for task " + taskConfig.getId() + " does not exist.");
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
		
		accessedTaskOptions.get().add(argName);
		
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
