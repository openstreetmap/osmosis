package com.bretth.osmosis.core.xml;

import java.util.Map;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.pipeline.TaskManagerFactory;


/**
 * Extends the basic task manager factory functionality with xml task specific common methods.
 * 
 * @author Brett Henderson
 */
public abstract class XmlTaskManagerFactory extends TaskManagerFactory {
	private static final String ARG_COMPRESSION_METHOD = "compressionMethod";
	private static final CompressionMethod DEFAULT_COMPRESSION_METHOD = CompressionMethod.None;
	
	
	/**
	 * Utility method for retrieving a CompressionMethod argument value from a
	 * Map of task arguments.
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
	protected CompressionMethod getCompressionMethodArgument(
			String taskId, Map<String, String> taskArgs) {
		CompressionMethod result;
		
		if (taskArgs.containsKey(ARG_COMPRESSION_METHOD)) {
			String rawValue;
			
			rawValue = taskArgs.get(ARG_COMPRESSION_METHOD).toLowerCase();
			
			if ("none".equals(rawValue)) {
				result = CompressionMethod.None;
			} else if ("gzip".equals(rawValue)) {
				result = CompressionMethod.GZip;
			} else if ("bzip2".equals(rawValue)) {
				result = CompressionMethod.BZip2;
			} else {
				throw new OsmosisRuntimeException(
					"Argument " + ARG_COMPRESSION_METHOD + " for task " + taskId
					+ " must be one of none, gzip, or bzip2.");
			}
			
		} else {
			result = DEFAULT_COMPRESSION_METHOD;
		}
		
		return result;
	}
}
