package com.bretth.osmosis.core.filter.v0_5;

import com.bretth.osmosis.core.cli.TaskConfiguration;
import com.bretth.osmosis.core.filter.common.IdTrackerType;
import com.bretth.osmosis.core.pipeline.common.TaskManager;
import com.bretth.osmosis.core.pipeline.v0_5.SinkSourceManager;


/**
 * The task manager factory for a bounding box filter.
 * 
 * @author Brett Henderson
 */
public class BoundingBoxFilterFactory extends AreaFilterTaskManagerFactory {
	private static final String ARG_LEFT = "left";
	private static final String ARG_RIGHT = "right";
	private static final String ARG_TOP = "top";
	private static final String ARG_BOTTOM = "bottom";
	private static final double DEFAULT_LEFT = -180;
	private static final double DEFAULT_RIGHT = 180;
	private static final double DEFAULT_TOP = 90;
	private static final double DEFAULT_BOTTOM = -90;
	private static final String ARG_COMPLETE_WAYS = "completeWays";
	private static final String ARG_COMPLETE_RELATIONS = "completeRelations";
	private static final boolean DEFAULT_COMPLETE_WAYS = false;
	private static final boolean DEFAULT_COMPLETE_RELATIONS = false;

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		IdTrackerType idTrackerType;
		double left;
		double right;
		double top;
		double bottom;
		boolean completeWays;
		boolean completeRelations;
		
		// Get the task arguments.
		idTrackerType = getIdTrackerType(taskConfig);
		left = getDoubleArgument(taskConfig, ARG_LEFT, DEFAULT_LEFT);
		right = getDoubleArgument(taskConfig, ARG_RIGHT, DEFAULT_RIGHT);
		top = getDoubleArgument(taskConfig, ARG_TOP, DEFAULT_TOP);
		bottom = getDoubleArgument(taskConfig, ARG_BOTTOM, DEFAULT_BOTTOM);
		completeWays = getBooleanArgument(taskConfig, ARG_COMPLETE_WAYS, DEFAULT_COMPLETE_WAYS);
		completeRelations = getBooleanArgument(taskConfig, ARG_COMPLETE_RELATIONS, DEFAULT_COMPLETE_RELATIONS);
		
		return new SinkSourceManager(
			taskConfig.getId(),
			new BoundingBoxFilter(idTrackerType, left, right, top, bottom, completeWays, completeRelations),
			taskConfig.getPipeArgs()
		);
	}
}
