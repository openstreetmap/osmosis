// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.repdb.v0_6;

import java.util.Date;

import org.openstreetmap.osmosis.core.database.DatabaseLoginCredentials;
import org.openstreetmap.osmosis.core.database.DatabasePreferences;
import org.openstreetmap.osmosis.core.database.DatabaseTaskManagerFactory;
import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManager;
import org.openstreetmap.osmosis.core.pipeline.v0_6.ChangeSinkManager;


/**
 * The task manager factory for a replication database writer.
 * 
 * @author Brett Henderson
 */
public class ReplicationDbWriterFactory extends DatabaseTaskManagerFactory {

	private static final String ARG_SYSTEM_TIMESTAMP = "systemTimestamp";
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		DatabaseLoginCredentials loginCredentials;
		DatabasePreferences preferences;
		Date systemTimestamp;
		
		// Get the task arguments.
		loginCredentials = getDatabaseLoginCredentials(taskConfig);
		preferences = getDatabasePreferences(taskConfig);
		systemTimestamp = getDateArgument(taskConfig, ARG_SYSTEM_TIMESTAMP, new Date());
		
		return new ChangeSinkManager(
			taskConfig.getId(),
			new ReplicationDbWriter(
				loginCredentials,
				preferences,
				systemTimestamp
			),
			taskConfig.getPipeArgs()
		);
	}
}
