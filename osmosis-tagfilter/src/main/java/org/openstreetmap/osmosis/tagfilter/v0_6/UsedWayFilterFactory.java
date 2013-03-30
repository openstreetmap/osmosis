// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.tagfilter.v0_6;

import org.openstreetmap.osmosis.core.filter.common.IdTrackerType;
import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManager;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.pipeline.v0_6.SinkSourceManager;

/**
 * Extends the basic task manager factory functionality with used-way filter task
 * specific common methods.
 * 
 * @author Brett Henderson
 * @author Christoph Sommer
 * @author Bartosz Fabianowski
 */
public class UsedWayFilterFactory extends TaskManagerFactory {
	private static final IdTrackerType DEFAULT_ID_TRACKER_TYPE = IdTrackerType.Dynamic;
	
	
	/**
	 * Utility method that returns the IdTrackerType to use for a given taskConfig.
	 * 
	 * @param taskConfig
	 *            Contains all information required to instantiate and configure
	 *            the task.
	 * @return The entity identifier tracker type.
	 */
	protected IdTrackerType getIdTrackerType(
			TaskConfiguration taskConfig) {
		return DEFAULT_ID_TRACKER_TYPE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {

		IdTrackerType idTrackerType = getIdTrackerType(taskConfig);

		return new SinkSourceManager(
			taskConfig.getId(),
			new UsedWayFilter(idTrackerType),
			taskConfig.getPipeArgs()
		);
	}

}
