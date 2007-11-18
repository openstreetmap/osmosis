package com.bretth.osmosis.core.xml.v0_5;

import java.io.File;

import com.bretth.osmosis.core.cli.TaskConfiguration;
import com.bretth.osmosis.core.pipeline.common.TaskManager;
import com.bretth.osmosis.core.pipeline.v0_5.SinkManager;
import com.bretth.osmosis.core.xml.common.CompressionMethod;
import com.bretth.osmosis.core.xml.common.XmlTaskManagerFactory;


/**
 * The task manager factory for an xml writer.
 * 
 * @author Brett Henderson
 */
public class XmlWriterFactory extends XmlTaskManagerFactory {
	private static final String ARG_FILE_NAME = "file";
	private static final String DEFAULT_FILE_NAME = "dump.osm";
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		String fileName;
		File file;
		XmlWriter task;
		CompressionMethod compressionMethod;
		boolean enableProdEncodingHack;
		
		// Get the task arguments.
		fileName = getStringArgument(taskConfig, ARG_FILE_NAME, DEFAULT_FILE_NAME);
		compressionMethod = getCompressionMethodArgument(taskConfig, fileName);
		enableProdEncodingHack = getProdEncodingHackArgument(taskConfig);
		
		// Create a file object from the file name provided.
		file = new File(fileName);
		
		// Build the task object.
		task = new XmlWriter(file, compressionMethod, enableProdEncodingHack);
		
		return new SinkManager(taskConfig.getId(), task, taskConfig.getPipeArgs());
	}
}
