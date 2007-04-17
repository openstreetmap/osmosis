package com.bretth.osm.conduit.xml;

import java.io.File;
import java.util.Map;

import com.bretth.osm.conduit.pipeline.OsmSourceManager;
import com.bretth.osm.conduit.pipeline.TaskManager;
import com.bretth.osm.conduit.pipeline.TaskManagerFactory;


public class XmlReaderFactory extends TaskManagerFactory {
	private static final String TASK_TYPE = "read-osm";
	private static final String ARG_FILE_NAME = "file";
	private static final String DEFAULT_FILE_NAME = "dump.osm";
	
	
	protected TaskManager createTaskManagerImpl(Map<String, String> taskArgs, Map<String, String> pipeArgs) {
		String fileName;
		File file;
		XmlReader task;
		
		// Get the task arguments.
		if (taskArgs.containsKey(ARG_FILE_NAME)) {
			fileName = taskArgs.get(ARG_FILE_NAME);
		} else {
			fileName = DEFAULT_FILE_NAME;
		}
		
		// Create a file object from the file name provided.
		file = new File(fileName);
		
		// Build the task object.
		task = new XmlReader();
		task.setFile(file);
		
		return new OsmSourceManager(TASK_TYPE, task, pipeArgs);
	}
	
	
	protected String getTaskType() {
		return TASK_TYPE;
	}
}
