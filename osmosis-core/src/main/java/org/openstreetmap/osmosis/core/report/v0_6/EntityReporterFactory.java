// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.report.v0_6;

import java.io.File;

import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManager;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.pipeline.v0_6.SinkManager;


/**
 * The task manager factory for an entity reporter.
 * 
 * @author Brett Henderson
 */
public class EntityReporterFactory extends TaskManagerFactory {
	private static final String ARG_FILE_NAME = "file";
	private static final String DEFAULT_FILE_NAME = "entity-report.txt";
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		String fileName;
		File file;
		EntityReporter task;
		
		// Get the task arguments.
		fileName = getStringArgument(
			taskConfig,
			ARG_FILE_NAME,
			getDefaultStringArgument(taskConfig, DEFAULT_FILE_NAME)
		);
		
		// Create a file object from the file name provided.
		file = new File(fileName);
		
		// Build the task object.
		task = new EntityReporter(file);
		
		return new SinkManager(taskConfig.getId(), task, taskConfig.getPipeArgs());
	}
}
