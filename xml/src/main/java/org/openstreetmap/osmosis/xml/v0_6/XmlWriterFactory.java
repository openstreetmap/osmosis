// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.xml.v0_6;

import java.io.File;

import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManager;
import org.openstreetmap.osmosis.core.pipeline.v0_6.SinkManager;
import org.openstreetmap.osmosis.xml.common.CompressionMethod;
import org.openstreetmap.osmosis.xml.common.XmlTaskManagerFactory;


/**
 * The task manager factory for an xml writer.
 * 
 * @author Brett Henderson
 */
public class XmlWriterFactory extends XmlTaskManagerFactory {
	private static final String ARG_FILE_NAME = "file";
	private static final String DEFAULT_FILE_NAME = "dump.osm";

	private static final String ARG_LEGACY_BOUND = "useLegacyBound";
	private static final boolean DEFAULT_LEGACY_BOUND = false;
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		String fileName;
		File file;
		XmlWriter task;
		CompressionMethod compressionMethod;
		
		// Get the task arguments.
		fileName = getStringArgument(
			taskConfig,
			ARG_FILE_NAME,
			getDefaultStringArgument(taskConfig, DEFAULT_FILE_NAME)
		);
		compressionMethod = getCompressionMethodArgument(taskConfig, fileName);
		
		// Create a file object from the file name provided.
		file = new File(fileName);
		
		boolean legacyBound = getBooleanArgument(taskConfig, ARG_LEGACY_BOUND, DEFAULT_LEGACY_BOUND);
		
		// Build the task object.
		task = new XmlWriter(file, compressionMethod, legacyBound);
		
		return new SinkManager(taskConfig.getId(), task, taskConfig.getPipeArgs());
	}
}
