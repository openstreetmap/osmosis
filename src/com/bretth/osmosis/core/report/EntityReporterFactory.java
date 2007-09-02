package com.bretth.osmosis.core.report;

import java.io.File;
import java.util.Map;

import com.bretth.osmosis.core.pipeline.SinkManager;
import com.bretth.osmosis.core.pipeline.TaskManager;
import com.bretth.osmosis.core.pipeline.TaskManagerFactory;


/**
 * The task manager factory for an xml writer.
 * 
 * @author Brett Henderson
 */
public class EntityReporterFactory extends TaskManagerFactory {
	private static final String ARG_FILE_NAME = "file";
	private static final String DEFAULT_FILE_NAME = "report.txt";
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(String taskId, Map<String, String> taskArgs, Map<String, String> pipeArgs) {
		String fileName;
		File file;
		EntityReporter task;
		
		// Get the task arguments.
		fileName = getStringArgument(taskId, taskArgs, ARG_FILE_NAME, DEFAULT_FILE_NAME);
		
		// Create a file object from the file name provided.
		file = new File(fileName);
		
		// Build the task object.
		task = new EntityReporter(file);
		
		return new SinkManager(taskId, task, pipeArgs);
	}
}
