package com.bretth.osmosis.pipeline;


/**
 * Defines constants used by pipeline management.
 * 
 * @author Brett Henderson
 */
public interface PipelineConstants {

	/**
	 * Defines the prefix used for command line task arguments.
	 */
	final String TASK_ARGUMENT_PREFIX = "--";

	/**
	 * Defines the prefix used for command line input pipe arguments.
	 */
	final String IN_PIPE_ARGUMENT_PREFIX = "inPipe";

	/**
	 * Defines the prefix used for command line output pipe arguments.
	 */
	final String OUT_PIPE_ARGUMENT_PREFIX = "outPipe";

	/**
	 * Defines the prefix for default pipe names used when no pipes are
	 * specified.
	 */
	final String DEFAULT_PIPE_PREFIX = "default";
}
