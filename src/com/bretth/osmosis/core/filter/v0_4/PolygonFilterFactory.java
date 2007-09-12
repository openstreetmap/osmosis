package com.bretth.osmosis.core.filter.v0_4;

import java.io.File;
import java.util.Map;

import com.bretth.osmosis.core.pipeline.common.TaskManager;
import com.bretth.osmosis.core.pipeline.common.TaskManagerFactory;
import com.bretth.osmosis.core.pipeline.v0_4.SinkSourceManager;


/**
 * The task manager factory for a polygon filter.
 * 
 * @author Brett Henderson
 */
public class PolygonFilterFactory extends TaskManagerFactory {
	private static final String ARG_FILE = "file";
	private static final String DEFAULT_FILE = "polygon.txt";

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(String taskId, Map<String, String> taskArgs, Map<String, String> pipeArgs) {
		String fileName;
		File file;
		
		// Get the task arguments.
		fileName = getStringArgument(taskId, taskArgs, ARG_FILE, DEFAULT_FILE);
		
		// Create a file object from the file name provided.
		file = new File(fileName);
		
		return new SinkSourceManager(
			taskId,
			new PolygonFilter(file),
			pipeArgs
		);
	}
}
