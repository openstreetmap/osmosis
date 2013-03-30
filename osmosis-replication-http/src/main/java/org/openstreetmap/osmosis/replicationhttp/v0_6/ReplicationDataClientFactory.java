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
	private static final String ARG_PATH_PREFIX = "pathPrefix";
	private static final String DEFAULT_HOST = "localhost";
	private static final int DEFAULT_PORT = 0;
	private static final String DEFAULT_PATH_PREFIX = "";


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		String host;
		int port;
		StringBuilder basePath;

		// Get the task arguments.
		host = getStringArgument(taskConfig, ARG_HOST, DEFAULT_HOST);
		port = getIntegerArgument(taskConfig, ARG_PORT, DEFAULT_PORT);
		basePath = new StringBuilder(getStringArgument(taskConfig, ARG_PATH_PREFIX, DEFAULT_PATH_PREFIX));
		
		// Ensure that the base path if it exists has a leading slash but no trailing slash.
		while (basePath.length() > 0 && basePath.charAt(0) == '/') {
			basePath.delete(0, 1);
		}
		while (basePath.length() > 0 && basePath.charAt(basePath.length() - 1) == '/') {
			basePath.delete(basePath.length() - 1, basePath.length());
		}
		if (basePath.length() > 0) {
			basePath.insert(0, '/');
		}
		
		return new RunnableChangeSourceManager(
			taskConfig.getId(),
			new ReplicationDataClient(new InetSocketAddress(host, port), basePath.toString()),
			taskConfig.getPipeArgs()
		);
	}
}
