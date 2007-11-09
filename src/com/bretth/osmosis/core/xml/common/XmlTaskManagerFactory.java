package com.bretth.osmosis.core.xml.common;

import java.util.Map;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.pipeline.common.TaskManagerFactory;


/**
 * Extends the basic task manager factory functionality with xml task specific common methods.
 * 
 * @author Brett Henderson
 */
public abstract class XmlTaskManagerFactory extends TaskManagerFactory {
	private static final String ARG_COMPRESSION_METHOD = "compressionMethod";
	private static final String ARG_ENCODING_HACK = "encodingHack";
	private static final CompressionMethod DEFAULT_COMPRESSION_METHOD = CompressionMethod.None;
	private static final boolean DEFAULT_ENCODING_HACK = false;
	private static final String FILE_SUFFIX_GZIP = ".gz";
	private static final String FILE_SUFFIX_BZIP2 = ".bz2";
	
	
	/**
	 * Utility method for retrieving a CompressionMethod argument value from a
	 * Map of task arguments.
	 * 
	 * @param taskId
	 *            The identifier for the task retrieving the parameter.
	 * @param taskArgs
	 *            The task arguments.
	 * @param fileName
	 *            The file name used to determine the default compression
	 *            method.
	 * @return The value of the argument.
	 */
	protected CompressionMethod getCompressionMethodArgument(
			String taskId, Map<String, String> taskArgs, String fileName) {
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
			if (fileName.endsWith(FILE_SUFFIX_GZIP)) {
				result = CompressionMethod.GZip;
			} else if (fileName.endsWith(FILE_SUFFIX_BZIP2)) {
				result = CompressionMethod.BZip2;
			} else {
				result = DEFAULT_COMPRESSION_METHOD;
			}
		}
		
		return result;
	}
	
	
	/**
	 * Utility method for retrieving the argument specifying whether to enable
	 * the production file encoding hack to work around a bug in the current
	 * production configuration.
	 * 
	 * @param taskId
	 *            The identifier for the task retrieving the parameter.
	 * @param taskArgs
	 *            The task arguments.
	 * @param fileName
	 *            The file name used to determine the default compression
	 *            method.
	 * @return The value of the argument.
	 */
	protected boolean getProdEncodingHackArgument(
			String taskId, Map<String, String> taskArgs) {
		return getBooleanArgument(taskId, taskArgs, ARG_ENCODING_HACK, DEFAULT_ENCODING_HACK);
	}
}
