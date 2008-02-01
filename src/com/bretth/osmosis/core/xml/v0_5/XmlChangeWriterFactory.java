// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.xml.v0_5;

import java.io.File;

import com.bretth.osmosis.core.cli.TaskConfiguration;
import com.bretth.osmosis.core.pipeline.common.TaskManager;
import com.bretth.osmosis.core.pipeline.v0_5.ChangeSinkManager;
import com.bretth.osmosis.core.xml.common.CompressionMethod;
import com.bretth.osmosis.core.xml.common.XmlTaskManagerFactory;


/**
 * The task manager factory for an xml change writer.
 * 
 * @author Brett Henderson
 */
public class XmlChangeWriterFactory extends XmlTaskManagerFactory {
	private static final String ARG_FILE_NAME = "file";
	private static final String DEFAULT_FILE_NAME = "change.osc";

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		String fileName;
		File file;
		CompressionMethod compressionMethod;
		boolean enableProdEncodingHack;
		XmlChangeWriter task;
		
		// Get the task arguments.
		fileName = getStringArgument(
			taskConfig,
			ARG_FILE_NAME,
			getDefaultStringArgument(taskConfig, DEFAULT_FILE_NAME)
		);
		compressionMethod = getCompressionMethodArgument(taskConfig, fileName);
		enableProdEncodingHack = getProdEncodingHackArgument(taskConfig);
		
		// Create a file object from the file name provided.
		file = new File(fileName);
		
		// Build the task object.
		task = new XmlChangeWriter(file, compressionMethod, enableProdEncodingHack);
		
		return new ChangeSinkManager(taskConfig.getId(), task, taskConfig.getPipeArgs());
	}
}
