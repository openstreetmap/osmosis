// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.xml.v0_6;

import java.io.File;

import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManager;
import org.openstreetmap.osmosis.core.pipeline.v0_6.ChangeSinkManager;
import org.openstreetmap.osmosis.xml.common.CompressionMethod;
import org.openstreetmap.osmosis.xml.common.XmlTaskManagerFactory;


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
		XmlChangeWriter task;
		
		// Get the task arguments.
		fileName = getStringArgument(
			taskConfig,
			ARG_FILE_NAME,
			getDefaultStringArgument(taskConfig, DEFAULT_FILE_NAME)
		);
		compressionMethod = getCompressionMethodArgument(taskConfig, fileName);
		
		// Create a file object from the file name provided.
		file = new File(fileName);
		
		// Build the task object.
		task = new XmlChangeWriter(file, compressionMethod);
		
		return new ChangeSinkManager(taskConfig.getId(), task, taskConfig.getPipeArgs());
	}
}
