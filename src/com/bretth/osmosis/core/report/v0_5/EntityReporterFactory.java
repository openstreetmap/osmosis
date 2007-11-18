package com.bretth.osmosis.core.report.v0_5;

import java.io.File;

import com.bretth.osmosis.core.cli.TaskConfiguration;
import com.bretth.osmosis.core.pipeline.common.TaskManager;
import com.bretth.osmosis.core.pipeline.common.TaskManagerFactory;
import com.bretth.osmosis.core.pipeline.v0_5.SinkManager;


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
		fileName = getStringArgument(taskConfig, ARG_FILE_NAME, DEFAULT_FILE_NAME);
		
		// Create a file object from the file name provided.
		file = new File(fileName);
		
		// Build the task object.
		task = new EntityReporter(file);
		
		return new SinkManager(taskConfig.getId(), task, taskConfig.getPipeArgs());
	}
}
