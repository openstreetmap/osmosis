// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.cli;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.pipeline.common.PipelineConstants;


/**
 * Parses command line arguments into a form that can be consumed by the rest of
 * the application.
 * 
 * @author Brett Henderson
 */
public class CommandLineParser {
	
	private final String GLOBAL_ARGUMENT_PREFIX = "-";
	private final String TASK_ARGUMENT_PREFIX = "--";
	private final String OPTION_QUIET = "q";
	private final String OPTION_VERBOSE = "v";
	
	
	/**
	 * Defines the log levels supported from the command line.
	 */
	private static final Level [] logLevels = {
		Level.OFF,
		Level.SEVERE,
		Level.WARNING,
		Level.INFO,
		Level.FINE,
		Level.FINER,
		Level.FINEST
	};
	
	
	/**
	 * The index into the logLevels array for the default log level.
	 */
	private static final int defaultLogLevelIndex = 3;
	
	
	private List<TaskConfiguration> taskConfigList;
	private int quietValue;
	private int verboseValue;
	
	
	/**
	 * Creates a new instance.
	 */
	public CommandLineParser() {
		taskConfigList = new ArrayList<TaskConfiguration>();
		
		quietValue = 0;
		verboseValue = 0;
	}
	
	
	/**
	 * Parses the command line arguments.
	 * 
	 * @param programArgs
	 *            The arguments.
	 */
	public void parse(String [] programArgs) {
		// Process the command line arguments to build all nodes in the pipeline.
		for (int i = 0; i < programArgs.length; ) {
			String arg;
			
			arg = programArgs[i];
			
			if (arg.indexOf(TASK_ARGUMENT_PREFIX) == 0) {
				i = parseTask(programArgs, i);
			} else if (arg.indexOf(GLOBAL_ARGUMENT_PREFIX) == 0) {
				i = parseGlobalOption(programArgs, i);
			} else {
				throw new OsmosisRuntimeException("Expected argument " + (i + 1) + " to be an option or task name.");
			}
		}
	}
	
	
	/**
	 * Checks if the current command line argument is for the specified option.
	 * 
	 * @param optionName
	 *            The name of the option to check for.
	 * @param rawArgument
	 *            The command line argument without the option prefix.
	 * @return True if the argument is for the specified option.
	 */
	private boolean isArgumentForOption(String optionName, String rawArgument) {
		return rawArgument.substring(0, optionName.length()).equals(optionName);
	}
	
	
	/**
	 * Parses a command line argument into an integer. If none is specified,
	 * zero will be returned.
	 * 
	 * @param optionName
	 *            The name of the command line option.
	 * @param rawArgument
	 *            The command line argument, this must include optionName as a
	 *            prefix which will be stripped off.
	 * @return The integer value.
	 */
	private int parseOptionInteger(String optionName, String rawArgument, int offset) {
		String optionValue;
		
		optionValue = rawArgument.substring(optionName.length());
		if (optionValue.length() <= 0) {
			return 0;
		}
		
		try {
			return Integer.parseInt(optionValue);
			
		} catch (NumberFormatException e) {
			throw new OsmosisRuntimeException("Expected argument " + (offset + 1) + " to contain an integer value.");
		}
	}
	
	
	/**
	 * Parses the details of a single option.
	 * 
	 * @param programArgs
	 *            The command line arguments passed to this application.
	 * @param offset
	 *            The current offset through the command line arguments.
	 * @return The new offset through the command line arguments.
	 */
	private int parseGlobalOption(String [] programArgs, int offset) {
		int i;
		String argument;
		
		i = offset;
		argument = programArgs[i++].substring(1);
		
		if (isArgumentForOption(OPTION_QUIET, argument)) {
			quietValue = parseOptionInteger(OPTION_QUIET, argument, i - 1) + 1;
		}
		if (isArgumentForOption(OPTION_VERBOSE, argument)) {
			verboseValue = parseOptionInteger(OPTION_VERBOSE, argument, i - 1) + 1;
		}
		
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
					throw new OsmosisRuntimeException("Argument " + (i + 1) + " doesn't contain a name before the '=' (ie. name=value).");
				}
				
				// Check if the value component of the argument exists.
				if (equalsIndex >= (arg.length() - 1)) {
					throw new OsmosisRuntimeException("Argument " + (i + 1) + " doesn't contain a value after the '=' (ie. name=value).");
				}
				
				// Split the argument into name and value.
				argName = arg.substring(0, equalsIndex);
				argValue = arg.substring(equalsIndex + 1);
				
				// Add pipeline arguments to pipeArgs, all other arguments to taskArgs.
				// A pipeline arg is inPipe, inPipe.x, outPipe or outPipe.x.
				if (PipelineConstants.IN_PIPE_ARGUMENT_PREFIX.equals(argName) || argName.indexOf(PipelineConstants.IN_PIPE_ARGUMENT_PREFIX + ".") == 0 || PipelineConstants.OUT_PIPE_ARGUMENT_PREFIX.equals(argName) || argName.indexOf(PipelineConstants.OUT_PIPE_ARGUMENT_PREFIX + ".") == 0) {
					pipeArgs.put(argName, argValue);
				} else {
					taskArgs.put(argName, argValue);
				}
				
			} else {
				if (defaultArgIndex >= 0) {
					throw new OsmosisRuntimeException("Only one default (un-named) argument can exist per task.  Arguments " + (i + 1) + " and " + (defaultArgIndex + 1) + " have no name.");
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
	 * The level of logging required.
	 * 
	 * @return The log level to be used.
	 */
	public Level getLogLevel() {
		int logLevelIndex;
		
		logLevelIndex = defaultLogLevelIndex + verboseValue - quietValue;
		
		if (logLevelIndex < 0) {
			logLevelIndex = 0;
		}
		if (logLevelIndex >= logLevels.length) {
			logLevelIndex = logLevels.length - 1;
		}
		
		return logLevels[logLevelIndex];
	}
}
