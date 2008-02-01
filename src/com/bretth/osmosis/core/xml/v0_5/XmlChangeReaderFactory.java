// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.xml.v0_5;

import java.io.File;

import com.bretth.osmosis.core.cli.TaskConfiguration;
import com.bretth.osmosis.core.pipeline.common.TaskManager;
import com.bretth.osmosis.core.pipeline.v0_5.RunnableChangeSourceManager;
import com.bretth.osmosis.core.xml.common.CompressionMethod;
import com.bretth.osmosis.core.xml.common.XmlTaskManagerFactory;


/**
 * The task manager factory for an xml change reader.
 * 
 * @author Brett Henderson
 */
public class XmlChangeReaderFactory extends XmlTaskManagerFactory {
	private static final String ARG_FILE_NAME = "file";
	private static final String DEFAULT_FILE_NAME = "change.osc";
	private static final String ARG_ENABLE_DATE_PARSING = "enableDateParsing";
	private static final boolean DEFAULT_ENABLE_DATE_PARSING = true;

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		String fileName;
		File file;
		boolean enableDateParsing;
		CompressionMethod compressionMethod;
		XmlChangeReader task;
		
		// Get the task arguments.
		fileName = getStringArgument(
			taskConfig,
			ARG_FILE_NAME,
			getDefaultStringArgument(taskConfig, DEFAULT_FILE_NAME)
		);
		enableDateParsing = getBooleanArgument(taskConfig, ARG_ENABLE_DATE_PARSING, DEFAULT_ENABLE_DATE_PARSING);
		compressionMethod = getCompressionMethodArgument(taskConfig, fileName);
		
		// Create a file object from the file name provided.
		file = new File(fileName);
		
		// Build the task object.
		task = new XmlChangeReader(file, enableDateParsing, compressionMethod);
		
		return new RunnableChangeSourceManager(taskConfig.getId(), task, taskConfig.getPipeArgs());
	}
}
