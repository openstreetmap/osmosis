// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.replicationhttp.v0_6;

import java.io.File;

import org.openstreetmap.osmosis.core.pipeline.common.RunnableTaskManager;
import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManager;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;


/**
 * The task manager factory for a replication sequence server.
 * 
 * @author Brett Henderson
 */
public class ReplicationDataServerFactory extends TaskManagerFactory {
	private static final String ARG_NOTIFICATION_PORT = "notificationPort";
	private static final String ARG_DATA_DIRECTORY = "dataDirectory";
	private static final String ARG_PORT = "port";
	private static final int DEFAULT_NOTIFICATION_PORT = 0;
	private static final String DEFAULT_DATA_DIRECTORY = "./";
	private static final int DEFAULT_PORT = 0;


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		int port;
		String dataDirectoryString;
		File dataDirectory;
		int notificationPort;

		// Get the task arguments.
		port = getIntegerArgument(taskConfig, ARG_PORT, DEFAULT_PORT);
		dataDirectoryString = getStringArgument(taskConfig, ARG_DATA_DIRECTORY,
				getDefaultStringArgument(taskConfig, DEFAULT_DATA_DIRECTORY));
		notificationPort = getIntegerArgument(taskConfig, ARG_NOTIFICATION_PORT, DEFAULT_NOTIFICATION_PORT);

		// Convert argument strings to strongly typed objects.
		dataDirectory = new File(dataDirectoryString);

		return new RunnableTaskManager(
			taskConfig.getId(),
			new ReplicationDataServer(notificationPort, dataDirectory, port),
			taskConfig.getPipeArgs()
		);
	}
}
