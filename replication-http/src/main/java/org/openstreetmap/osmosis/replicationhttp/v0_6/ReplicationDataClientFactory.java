// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.replicationhttp.v0_6;

import java.net.InetSocketAddress;

import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManager;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.pipeline.v0_6.RunnableChangeSourceManager;


/**
 * The task manager factory for a HTTP replication data client.
 * 
 * @author Brett Henderson
 */
public class ReplicationDataClientFactory extends TaskManagerFactory {
	private static final String ARG_HOST = "host";
	private static final String ARG_PORT = "port";
	private static final String DEFAULT_HOST = "localhost";
	private static final int DEFAULT_PORT = 8080;


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		String host;
		int port;

		// Get the task arguments.
		host = getStringArgument(taskConfig, ARG_HOST,
				getDefaultStringArgument(taskConfig, DEFAULT_HOST));
		port = getIntegerArgument(taskConfig, ARG_PORT, getDefaultIntegerArgument(taskConfig, DEFAULT_PORT));
		
		return new RunnableChangeSourceManager(
			taskConfig.getId(),
			new ReplicationDataClient(new InetSocketAddress(host, port)),
			taskConfig.getPipeArgs()
		);
	}
}
