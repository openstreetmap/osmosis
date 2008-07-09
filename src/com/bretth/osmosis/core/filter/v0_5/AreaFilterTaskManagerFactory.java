// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.filter.v0_5;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.filter.common.IdTrackerType;
import com.bretth.osmosis.core.pipeline.common.TaskConfiguration;
import com.bretth.osmosis.core.pipeline.common.TaskManagerFactory;


/**
 * Extends the basic task manager factory functionality with area filter task
 * specific common methods.
 * 
 * @author Brett Henderson
 */
public abstract class AreaFilterTaskManagerFactory extends TaskManagerFactory {
	private static final String ARG_ID_TRACKER_TYPE = "idTrackerType";
	private static final IdTrackerType DEFAULT_ID_TRACKER_TYPE = IdTrackerType.IdList;
	
	
	/**
	 * Utility method for retrieving the login credentials for a database connection.
	 * 
	 * @param taskConfig
	 *            Contains all information required to instantiate and configure
	 *            the task.
	 * @return The entity identifier tracker type.
	 */
	protected IdTrackerType getIdTrackerType(
			TaskConfiguration taskConfig) {
		if (doesArgumentExist(taskConfig, ARG_ID_TRACKER_TYPE)) {
			String idTrackerType;
			
			idTrackerType = getStringArgument(taskConfig, ARG_ID_TRACKER_TYPE);
			
			try {
				return IdTrackerType.valueOf(idTrackerType);
			} catch (IllegalArgumentException e) {
				throw new OsmosisRuntimeException(
					"Argument " + ARG_ID_TRACKER_TYPE + " for task " + taskConfig.getId()
					+ " must contain a valid id tracker type.", e);
			}
			
		} else {
			return DEFAULT_ID_TRACKER_TYPE;
		}
	}
}
