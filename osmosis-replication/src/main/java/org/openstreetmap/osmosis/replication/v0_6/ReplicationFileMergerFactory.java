// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.replication.v0_6;

import java.io.File;

import org.openstreetmap.osmosis.core.pipeline.common.RunnableTaskManager;
import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManager;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;


/**
 * The task manager factory for a replication file merger.
 */
public class ReplicationFileMergerFactory extends TaskManagerFactory {
	private static final String ARG_WORKING_DIRECTORY = "workingDirectory";
	private static final String DEFAULT_WORKING_DIRECTORY = "./";
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		String workingDirectoryString;
		File workingDirectory;
		
		// Get the task arguments.
		workingDirectoryString = getStringArgument(
			taskConfig,
			ARG_WORKING_DIRECTORY,
			getDefaultStringArgument(taskConfig, DEFAULT_WORKING_DIRECTORY)
		);
		
		// Convert argument strings to strongly typed objects.
		workingDirectory = new File(workingDirectoryString);
		
		return new RunnableTaskManager(
			taskConfig.getId(),
			new ReplicationFileMerger(
				workingDirectory
			),
			taskConfig.getPipeArgs()
		);
	}
}
