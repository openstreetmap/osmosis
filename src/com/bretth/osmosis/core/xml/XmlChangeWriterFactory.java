package com.bretth.osmosis.core.xml;

import java.io.File;
import java.util.Map;

import com.bretth.osmosis.core.pipeline.ChangeSinkManager;
import com.bretth.osmosis.core.pipeline.TaskManager;
import com.bretth.osmosis.core.pipeline.TaskManagerFactory;


/**
 * The task manager factory for an xml change writer.
 * 
 * @author Brett Henderson
 */
public class XmlChangeWriterFactory extends TaskManagerFactory {
	private static final String ARG_FILE_NAME = "file";
	private static final String DEFAULT_FILE_NAME = "change.osc";

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(String taskId, Map<String, String> taskArgs, Map<String, String> pipeArgs) {
		String fileName;
		File file;
		XmlChangeWriter task;
		
		// Get the task arguments.
		fileName = getStringArgument(taskId, taskArgs, ARG_FILE_NAME, DEFAULT_FILE_NAME);
		
		// Create a file object from the file name provided.
		file = new File(fileName);
		
		// Build the task object.
		task = new XmlChangeWriter(file);
		
		return new ChangeSinkManager(taskId, task, pipeArgs);
	}
}
