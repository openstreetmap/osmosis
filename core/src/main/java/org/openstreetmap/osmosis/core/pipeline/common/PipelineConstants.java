// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.pipeline.common;


/**
 * Defines constants used by pipeline management.
 * 
 * @author Brett Henderson
 */
public final class PipelineConstants {
	
	/**
	 * This class cannot be instantiated.
	 */
	private PipelineConstants() {
	}
	

	/**
	 * Defines the prefix used for command line input pipe arguments.
	 */
	public static final String IN_PIPE_ARGUMENT_PREFIX = "inPipe";

	/**
	 * Defines the prefix used for command line output pipe arguments.
	 */
	public static final String OUT_PIPE_ARGUMENT_PREFIX = "outPipe";

	/**
	 * Defines the prefix for default pipe names used when no pipes are
	 * specified.
	 */
	public static final String DEFAULT_PIPE_PREFIX = "default";
}
