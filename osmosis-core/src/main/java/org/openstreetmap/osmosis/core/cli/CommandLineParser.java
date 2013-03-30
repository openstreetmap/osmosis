// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.cli;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.pipeline.common.PipelineConstants;
import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;


/**
 * Parses command line arguments into a form that can be consumed by the rest of
 * the application.
 * 
 * @author Brett Henderson
 */
public class CommandLineParser {
	
	private static final String GLOBAL_ARGUMENT_PREFIX = "-";
	private static final String TASK_ARGUMENT_PREFIX = "--";
	private static final String OPTION_QUIET_SHORT = "q";
	private static final String OPTION_QUIET_LONG = "quiet";
	private static final String OPTION_VERBOSE_SHORT = "v";
	private static final String OPTION_VERBOSE_LONG = "verbose";
	private static final String OPTION_PLUGIN_SHORT = "p";
	private static final String OPTION_PLUGIN_LONG = "plugin";
	
	
	/**
	 * The index into the LOG_LEVELS array for the default log level.
	 */
	private static final int DEFAULT_LOG_LEVEL_INDEX = 3;
	
	
	private List<TaskConfiguration> taskConfigList;
	private int quietValue;
	private int verboseValue;
	private List<String> plugins;
	
	
	/**
	 * Creates a new instance.
	 */
	public CommandLineParser() {
		taskConfigList = new ArrayList<TaskConfiguration>();
		
		quietValue = 0;
		verboseValue = 0;
		plugins = new ArrayList<String>();
	}
	
	
	/**
	 * Parses the command line arguments.
	 * 
	 * @param programArgs
	 *            The arguments.
	 */
	public void parse(String [] programArgs) {
		List<GlobalOptionConfiguration> globalOptions;
		
		// Create the global options list.
		globalOptions = new ArrayList<GlobalOptionConfiguration>();
		
		// Process the command line arguments to build all nodes in the pipeline.
		for (int i = 0; i < programArgs.length;) {
			String arg;
			
			arg = programArgs[i];
			
			if (arg.indexOf(TASK_ARGUMENT_PREFIX) == 0) {
				i = parseTask(programArgs, i);
			} else if (arg.indexOf(GLOBAL_ARGUMENT_PREFIX) == 0) {
				i = parseGlobalOption(globalOptions, programArgs, i);
			} else {
				throw new OsmosisRuntimeException("Expected argument " + (i + 1) + " to be an option or task name.");
			}
		}
		
		// Process the global options.
		for (GlobalOptionConfiguration globalOption : globalOptions) {
			if (isArgumentForOption(OPTION_QUIET_SHORT, OPTION_QUIET_LONG, globalOption.name)) {
				quietValue = parseOptionIntegerWithDefault(globalOption, 0) + 1;
			} else if (isArgumentForOption(OPTION_VERBOSE_SHORT, OPTION_VERBOSE_LONG, globalOption.name)) {
				verboseValue = parseOptionIntegerWithDefault(globalOption, 0) + 1;
			} else if (isArgumentForOption(OPTION_PLUGIN_SHORT, OPTION_PLUGIN_LONG, globalOption.name)) {
				plugins.add(parseOptionString(globalOption));
			} else {
				throw new OsmosisRuntimeException("Argument " + (globalOption.offset + 1)
						+ " specifies an unrecognised option \"" + GLOBAL_ARGUMENT_PREFIX + globalOption.name
						+ "\".");
			}
		}
	}
	
	
	/**
	 * Checks if the current command line argument is for the specified option.
	 * 
	 * @param shortOptionName
	 *            The short name of the option to check for.
	 * @param longOptionName
	 *            The long name of the option to check for.
	 * @param argument
	 *            The command line argument without the option prefix.
	 * @return True if the argument is for the specified option.
	 */
	private boolean isArgumentForOption(String shortOptionName, String longOptionName, String argument) {
		return shortOptionName.equals(argument) || longOptionName.equals(argument);
	}
	
	
	/**
	 * Determines if an argument is a parameter to the current option/task or
	 * the start of another option/task.
	 * 
	 * @param argument
	 *            The argument.
	 * @return True if this is an option parameter.
	 */
	private boolean isOptionParameter(String argument) {
		if (argument.length() >= GLOBAL_ARGUMENT_PREFIX.length()) {
			if (argument.substring(0, GLOBAL_ARGUMENT_PREFIX.length()).equals(GLOBAL_ARGUMENT_PREFIX)) {
				return false;
			}
		}
		
		if (argument.length() >= TASK_ARGUMENT_PREFIX.length()) {
			if (argument.substring(0, TASK_ARGUMENT_PREFIX.length()).equals(TASK_ARGUMENT_PREFIX)) {
				return false;
			}
		}
		
		return true;
	}
	
	
	/**
	 * Parses a command line option into an integer. If none is specified, zero
	 * will be returned.
	 * 
	 * @param globalOption
	 *            The global option to be parsed.
	 * @return The integer value.
	 */
	private int parseOptionIntegerWithDefault(GlobalOptionConfiguration globalOption, int defaultValue) {
		int result;
		
		// If no parameters are available, we use the default value.
		if (globalOption.parameters.size() <= 0) {
			return defaultValue;
		}
		
		// An integer option may only have one parameter.
		if (globalOption.parameters.size() > 1) {
			throw new OsmosisRuntimeException(
					"Expected argument " + (globalOption.offset + 1) + " to have no more than one parameter.");
		}
		
		// Parse the option.
		try {
			result = Integer.parseInt(globalOption.parameters.get(0));
			
		} catch (NumberFormatException e) {
			throw new OsmosisRuntimeException(
					"Expected argument " + (globalOption.offset + 2) + " to contain an integer value.");
		}
		
		return result;
	}
	
	
	/**
	 * Parses a command line option into an string.
	 * 
	 * @param globalOption
	 *            The global option to be parsed.
	 */
	private String parseOptionString(GlobalOptionConfiguration globalOption) {
		// A string option must have one parameter.
		if (globalOption.parameters.size() != 1) {
			throw new OsmosisRuntimeException(
					"Expected argument " + (globalOption.offset + 1) + " to have one parameter.");
		}
		
		return globalOption.parameters.get(0);
	}
	
	
	/**
	 * Parses the details of a single option.
	 * 
	 * @param globalOptions
	 *            The list of global options being parsed, the new option will
	 *            be stored into this object.
	 * @param programArgs
	 *            The command line arguments passed to this application.
	 * @param offset
	 *            The current offset through the command line arguments.
	 * @return The new offset through the command line arguments.
	 */
	private int parseGlobalOption(List<GlobalOptionConfiguration> globalOptions, String [] programArgs, int offset) {
		int i;
		String argument;
		GlobalOptionConfiguration globalOption;
		
		i = offset;
		argument = programArgs[i++].substring(1);
		
		globalOption = new GlobalOptionConfiguration();
		globalOption.name = argument;
		globalOption.offset = offset;
		
		// Loop until the next option or task is reached and add the arguments as parameters to the option.
		while ((i < programArgs.length) && isOptionParameter(programArgs[i])) {
			globalOption.parameters.add(programArgs[i++]);
		}
		
		// Add the fully populated global option object to the list.
		globalOptions.add(globalOption);
		
		return i;
	}
	
	
	/**
	 * Parses the details of a single task and creates a task information object.
	 * 
	 * @param programArgs
	 *            The command line arguments passed to this application.
	 * @param offset
	 *            The current offset through the command line arguments.
	 * @return The new offset through the command line arguments.
	 */
	private int parseTask(String [] programArgs, int offset) {
		int i;
		String taskType;
		Map<String, String> taskArgs;
		Map<String, String> pipeArgs;
		String taskId;
		int defaultArgIndex;
		String defaultArg;
		
		i = offset;
		
		// Extract the task type from the current argument.
		taskType = programArgs[i++].substring(TASK_ARGUMENT_PREFIX.length());
		
		// Build up a list of task and pipe arguments.
		taskArgs = new HashMap<String, String>();
		pipeArgs = new HashMap<String, String>();
		defaultArg = null;
		defaultArgIndex = -1;
		while (i < programArgs.length) {
			String arg;
			int equalsIndex;
			String argName;
			String argValue;
			
			arg = programArgs[i];
			
			if (arg.indexOf(TASK_ARGUMENT_PREFIX) == 0) {
				break;
			}
			
			equalsIndex = arg.indexOf("=");
			
			// If an equals sign exists this is a named argument, otherwise it is a default argument.
			if (equalsIndex >= 0) {
				// Check if the name component of the argument exists.
				if (equalsIndex == 0) {
					throw new OsmosisRuntimeException(
							"Argument " + (i + 1) + " doesn't contain a name before the '=' (ie. name=value).");
				}
				
				// Check if the value component of the argument exists.
				if (equalsIndex >= (arg.length() - 1)) {
					throw new OsmosisRuntimeException(
							"Argument " + (i + 1) + " doesn't contain a value after the '=' (ie. name=value).");
				}
				
				// Split the argument into name and value.
				argName = arg.substring(0, equalsIndex);
				argValue = arg.substring(equalsIndex + 1);
				
				// Add pipeline arguments to pipeArgs, all other arguments to taskArgs.
				// A pipeline arg is inPipe, inPipe.x, outPipe or outPipe.x.
				if (
						PipelineConstants.IN_PIPE_ARGUMENT_PREFIX.equals(argName)
						|| argName.indexOf(PipelineConstants.IN_PIPE_ARGUMENT_PREFIX + ".") == 0
						|| PipelineConstants.OUT_PIPE_ARGUMENT_PREFIX.equals(argName)
						|| argName.indexOf(PipelineConstants.OUT_PIPE_ARGUMENT_PREFIX + ".") == 0) {
					pipeArgs.put(argName, argValue);
				} else {
					taskArgs.put(argName, argValue);
				}
				
			} else {
				if (defaultArgIndex >= 0) {
					throw new OsmosisRuntimeException(
							"Only one default (un-named) argument can exist per task.  Arguments "
							+ (i + 1) + " and " + (defaultArgIndex + 1) + " have no name.");
				}
				
				defaultArg = arg;
				defaultArgIndex = i;
			}
			
			i++;
		}
		
		// Build a unique task id.
		taskId = (taskConfigList.size() + 1) + "-" + taskType;
		
		// Create a new task information object and add it to the list.
		taskConfigList.add(
			new TaskConfiguration(taskId, taskType, pipeArgs, taskArgs, defaultArg)
		);
		
		return i;
	}
	
	
	/**
	 * The list of task information objects.
	 * 
	 * @return The taskInfoList.
	 */
	public List<TaskConfiguration> getTaskInfoList() {
		return taskConfigList;
	}
	
	
	/**
	 * Gets the level of logging required. This is a number that can be used to
	 * access a log level from the LogLevels class.
	 * 
	 * @return The index of the log level to be used.
	 */
	public int getLogLevelIndex() {
		return DEFAULT_LOG_LEVEL_INDEX + verboseValue - quietValue;
	}
	
	
	/**
	 * Returns the plugins to be loaded.
	 * 
	 * @return The list of plugin class names.
	 */
	public List<String> getPlugins() {
		return plugins;
	}
	
	
	/**
	 * A data storage class holding information relating to a global option
	 * during parsing.
	 */
	private class GlobalOptionConfiguration {
		/**
		 * The name of the option.
		 */
		public String name;
		/**
		 * The parameters for the option.
		 */
		public List<String> parameters;
		/**
		 * The command line argument offset of this global option.
		 */
		public int offset;
		
		
		/**
		 * Creates a new instance.
		 */
		public GlobalOptionConfiguration() {
			parameters = new ArrayList<String>();
		}
	}
}
