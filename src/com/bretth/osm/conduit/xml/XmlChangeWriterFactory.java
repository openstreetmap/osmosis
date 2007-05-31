package com.bretth.osm.conduit.xml;

import java.io.File;
import java.util.Map;

import com.bretth.osm.conduit.pipeline.ChangeSinkManager;
import com.bretth.osm.conduit.pipeline.TaskManager;
import com.bretth.osm.conduit.pipeline.TaskManagerFactory;


/**
 * The task manager factory for an xml change writer.
 * 
 * @author Brett Henderson
 */
public class XmlChangeWriterFactory extends TaskManagerFactory {
	private static final String ARG_FILE_NAME = "file";
	private static final String DEFAULT_FILE_NAME = "change.osc";

	
	/**
	 * Creates a new instance and adds the class to the global register.
	 * 
	 * @param taskType
	 *            The name to register the type against.
	 */
	public XmlChangeWriterFactory(String taskType) {
		super(taskType);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(String taskId, Map<String, String> taskArgs, Map<String, String> pipeArgs) {
		String fileName;
		File file;
		XmlChangeWriter task;
		
		// Get the task arguments.
		fileName = getStringArgument(taskArgs, ARG_FILE_NAME, DEFAULT_FILE_NAME);
		
		// Create a file object from the file name provided.
		file = new File(fileName);
		
		// Build the task object.
		task = new XmlChangeWriter(file);
		
		return new ChangeSinkManager(taskId, task, pipeArgs);
	}
}
