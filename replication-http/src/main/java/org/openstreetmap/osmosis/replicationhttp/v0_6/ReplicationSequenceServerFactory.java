// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.replicationhttp.v0_6;

import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManager;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.pipeline.v0_6.ChangeSinkChangeSourceManager;


/**
 * The task manager factory for a replication sequence server.
 * 
 * @author Brett Henderson
 */
public class ReplicationSequenceServerFactory extends TaskManagerFactory {
	private static final String ARG_PORT = "port";
	private static final int DEFAULT_PORT = 80;
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		int port;
		
		// Get the task arguments.
		port = getIntegerArgument(
			taskConfig,
			ARG_PORT,
			getDefaultIntegerArgument(taskConfig, DEFAULT_PORT)
		);
		
		return new ChangeSinkChangeSourceManager(
			taskConfig.getId(),
			new ReplicationSequenceServer(port),
			taskConfig.getPipeArgs()
		);
	}
}
