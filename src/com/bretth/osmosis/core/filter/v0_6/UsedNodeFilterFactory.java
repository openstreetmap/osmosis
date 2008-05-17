package com.bretth.osmosis.core.filter.v0_6;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.cli.TaskConfiguration;
import com.bretth.osmosis.core.filter.common.IdTrackerType;
import com.bretth.osmosis.core.pipeline.common.TaskManagerFactory;
import com.bretth.osmosis.core.pipeline.common.TaskManager;
import com.bretth.osmosis.core.pipeline.v0_6.SinkSourceManager;


/**
 * Extends the basic task manager factory functionality with used-node filter task
 * specific common methods.
 * 
 * @author Brett Henderson
 * @author Christoph Sommer
 */
public class UsedNodeFilterFactory extends TaskManagerFactory {
	private static final String ARG_ID_TRACKER_TYPE = "idTrackerType";
	private static final IdTrackerType DEFAULT_ID_TRACKER_TYPE = IdTrackerType.IdList;
	
	
	/**
	 * Utility method that returns the IdTrackerType to use for a given taskConfig.
	 * TODO: Copy-pasted from AreaFilterTaskManagerFactory. Is there a base class for Filters?
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {

		IdTrackerType idTrackerType = getIdTrackerType(taskConfig);

		return new SinkSourceManager(
			taskConfig.getId(),
			new UsedNodeFilter(idTrackerType),
			taskConfig.getPipeArgs()
		);
	}

}
