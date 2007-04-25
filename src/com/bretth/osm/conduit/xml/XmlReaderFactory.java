package com.bretth.osm.conduit.xml;

import java.io.File;
import java.util.Map;

import com.bretth.osm.conduit.pipeline.OsmRunnableSourceManager;
import com.bretth.osm.conduit.pipeline.TaskManager;
import com.bretth.osm.conduit.pipeline.TaskManagerFactory;


public class XmlReaderFactory extends TaskManagerFactory {
	private static final String TASK_TYPE = "read-osm";
	private static final String ARG_FILE_NAME = "file";
	private static final String DEFAULT_FILE_NAME = "dump.osm";
	
	
	@Override
	protected TaskManager createTaskManagerImpl(String taskId, Map<String, String> taskArgs, Map<String, String> pipeArgs) {
		String fileName;
		File file;
		XmlReader task;
		
		// Get the task arguments.
		fileName = getStringArgument(taskArgs, ARG_FILE_NAME, DEFAULT_FILE_NAME);
		
		// Create a file object from the file name provided.
		file = new File(fileName);
		
		// Build the task object.
		task = new XmlReader();
		task.setFile(file);
		
		return new OsmRunnableSourceManager(taskId, task, pipeArgs);
	}
	
	
	@Override
	protected String getTaskType() {
		return TASK_TYPE;
	}
}
