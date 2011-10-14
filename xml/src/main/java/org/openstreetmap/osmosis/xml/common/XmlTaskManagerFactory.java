// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.xml.common;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;


/**
 * Extends the basic task manager factory functionality with xml task specific common methods.
 * 
 * @author Brett Henderson
 */
public abstract class XmlTaskManagerFactory extends TaskManagerFactory {
	private static final String ARG_COMPRESSION_METHOD = "compressionMethod";
	private static final String ARG_ENCODING_HACK = "encodingHack";
	private static final boolean DEFAULT_ENCODING_HACK = false;
	
	
	/**
	 * Utility method for retrieving a CompressionMethod argument value from a
	 * Map of task arguments.
	 * 
	 * @param taskConfig
	 *            Contains all information required to instantiate and configure
	 *            the task.
	 * @param fileName
	 *            The file name used to determine the default compression
	 *            method.
	 * @return The value of the argument.
	 */
	protected CompressionMethod getCompressionMethodArgument(
			TaskConfiguration taskConfig, String fileName) {
		CompressionMethod result;
		
		String rawValue = getStringArgument(taskConfig, ARG_COMPRESSION_METHOD, "auto").toLowerCase();
		
		if ("none".equals(rawValue)) {
			result = CompressionMethod.None;
		} else if ("gzip".equals(rawValue)) {
			result = CompressionMethod.GZip;
		} else if ("bzip2".equals(rawValue)) {
			result = CompressionMethod.BZip2;
		} else if ("auto".equals(rawValue)) {
			result = new CompressionMethodDeriver().deriveCompressionMethod(fileName);
		} else {
			throw new OsmosisRuntimeException(
				"Argument " + ARG_COMPRESSION_METHOD + " for task " + taskConfig.getId()
				+ " must be one of none, gzip, bzip2 or auto.");
		}		
		return result;
	}
	
	
	/**
	 * Utility method for retrieving the argument specifying whether to enable
	 * the production file encoding hack to work around a bug in the current
	 * production configuration.
	 * 
	 * @param taskConfig
	 *            Contains all information required to instantiate and configure
	 *            the task.
	 * @return The value of the argument.
	 */
	protected boolean getProdEncodingHackArgument(
			TaskConfiguration taskConfig) {
		return getBooleanArgument(taskConfig, ARG_ENCODING_HACK, DEFAULT_ENCODING_HACK);
	}
}
