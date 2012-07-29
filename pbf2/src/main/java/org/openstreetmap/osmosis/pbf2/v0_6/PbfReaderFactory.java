// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pbf2.v0_6;

import java.io.File;

import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManager;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.pipeline.v0_6.RunnableSourceManager;


/**
 * The task manager factory for a PBF reader.
 * 
 * @author Brett Henderson
 */
public class PbfReaderFactory extends TaskManagerFactory {
	private static final String ARG_FILE_NAME = "file";
	private static final String DEFAULT_FILE_NAME = "dump.osm.pbf";
	private static final String ARG_WORKERS = "workers";
	private static final int DEFAULT_WORKERS = 1;


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		String fileName;
		File file;
		PbfReader task;
		int workers;

		// Get the task arguments.
		fileName = getStringArgument(taskConfig, ARG_FILE_NAME,
				getDefaultStringArgument(taskConfig, DEFAULT_FILE_NAME));
		workers = getIntegerArgument(taskConfig, ARG_WORKERS, DEFAULT_WORKERS);

		// Create a file object from the file name provided.
		file = new File(fileName);

		// Build the task object.
		task = new PbfReader(file, workers);

		return new RunnableSourceManager(taskConfig.getId(), task, taskConfig.getPipeArgs());
	}

}
