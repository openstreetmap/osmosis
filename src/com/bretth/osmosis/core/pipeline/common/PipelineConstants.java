package com.bretth.osmosis.core.pipeline.common;


/**
 * Defines constants used by pipeline management.
 * 
 * @author Brett Henderson
 */
public interface PipelineConstants {

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
