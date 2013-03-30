// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.areafilter.v0_6;

import org.openstreetmap.osmosis.core.filter.common.IdTrackerType;
import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;


/**
 * Extends the basic task manager factory functionality with area filter task
 * specific common methods.
 * 
 * @author Brett Henderson
 */
public abstract class AreaFilterTaskManagerFactory extends TaskManagerFactory {
	private static final IdTrackerType DEFAULT_ID_TRACKER_TYPE = IdTrackerType.Dynamic;


	/**
	 * Utility method for retrieving the login credentials for a database
	 * connection.
	 * 
	 * @param taskConfig
	 *            Contains all information required to instantiate and configure
	 *            the task.
	 * @return The entity identifier tracker type.
	 */
	protected IdTrackerType getIdTrackerType(TaskConfiguration taskConfig) {
		return DEFAULT_ID_TRACKER_TYPE;
	}
}
