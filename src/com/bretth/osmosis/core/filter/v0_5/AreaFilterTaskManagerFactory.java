package com.bretth.osmosis.core.filter.v0_5;

import java.util.Map;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.filter.common.IdTrackerType;
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
	 * @param taskId
	 *            The identifier for the task retrieving the parameter.
	 * @param taskArgs
	 *            The task arguments.
	 * @return The value of the argument.
	 */
	protected IdTrackerType getIdTrackerType(
			String taskId, Map<String, String> taskArgs) {
		if (taskArgs.containsKey(ARG_ID_TRACKER_TYPE)) {
			String idTrackerType;
			
			idTrackerType = taskArgs.get(ARG_ID_TRACKER_TYPE);
			
			try {
				return IdTrackerType.valueOf(idTrackerType);
			} catch (IllegalArgumentException e) {
				throw new OsmosisRuntimeException(
					"Argument " + ARG_ID_TRACKER_TYPE + " for task " + taskId
					+ " must contain a valid id tracker type.", e);
			}
			
		} else {
			return DEFAULT_ID_TRACKER_TYPE;
		}
	}
}
