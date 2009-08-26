// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.apidb.v0_6;

import java.util.Date;

import org.openstreetmap.osmosis.core.database.DatabaseLoginCredentials;
import org.openstreetmap.osmosis.core.database.DatabasePreferences;
import org.openstreetmap.osmosis.core.database.DatabaseTaskManagerFactory;
import org.openstreetmap.osmosis.core.pipeline.common.RunnableTaskManager;
import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManager;


/**
 * The task manager factory for a database change reader.
 * 
 * @author Brett Henderson
 */
public class ApidbTestReaderFactory extends DatabaseTaskManagerFactory {
	private static final String ARG_INTERVAL_BEGIN = "intervalBegin";
	private static final String ARG_INTERVAL_END = "intervalEnd";
	private static final String ARG_USE_SPRING = "useSpring";
	private static final boolean DEFAULT_USE_SPRING = false;
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		DatabaseLoginCredentials loginCredentials;
		DatabasePreferences preferences;
		Date intervalBegin;
		Date intervalEnd;
		boolean useSpring;
		
		// Get the task arguments.
		loginCredentials = getDatabaseLoginCredentials(taskConfig);
		preferences = getDatabasePreferences(taskConfig);
		intervalBegin = getDateArgument(taskConfig, ARG_INTERVAL_BEGIN, new Date(0));
		intervalEnd = getDateArgument(taskConfig, ARG_INTERVAL_END, new Date());
		useSpring = getBooleanArgument(taskConfig, ARG_USE_SPRING, DEFAULT_USE_SPRING);
		
		return new RunnableTaskManager(
			taskConfig.getId(),
			new ApidbTestReader(loginCredentials, preferences, intervalBegin, intervalEnd, useSpring),
			taskConfig.getPipeArgs()
		);
	}
}
