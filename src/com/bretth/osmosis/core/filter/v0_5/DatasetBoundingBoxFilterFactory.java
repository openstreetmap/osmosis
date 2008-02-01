// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.filter.v0_5;

import com.bretth.osmosis.core.cli.TaskConfiguration;
import com.bretth.osmosis.core.pipeline.common.TaskManager;
import com.bretth.osmosis.core.pipeline.common.TaskManagerFactory;
import com.bretth.osmosis.core.pipeline.v0_5.DatasetSinkSourceManager;


/**
 * The task manager factory for a dataset based bounding box filter.
 * 
 * @author Brett Henderson
 */
public class DatasetBoundingBoxFilterFactory extends TaskManagerFactory {
	private static final String ARG_LEFT = "left";
	private static final String ARG_RIGHT = "right";
	private static final String ARG_TOP = "top";
	private static final String ARG_BOTTOM = "bottom";
	private static final double DEFAULT_LEFT = -180;
	private static final double DEFAULT_RIGHT = 180;
	private static final double DEFAULT_TOP = 90;
	private static final double DEFAULT_BOTTOM = -90;
	private static final String ARG_COMPLETE_WAYS = "completeWays";
	private static final boolean DEFAULT_COMPLETE_WAYS = false;

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		double left;
		double right;
		double top;
		double bottom;
		boolean completeWays;
		
		// Get the task arguments.
		left = getDoubleArgument(taskConfig, ARG_LEFT, DEFAULT_LEFT);
		right = getDoubleArgument(taskConfig, ARG_RIGHT, DEFAULT_RIGHT);
		top = getDoubleArgument(taskConfig, ARG_TOP, DEFAULT_TOP);
		bottom = getDoubleArgument(taskConfig, ARG_BOTTOM, DEFAULT_BOTTOM);
		completeWays = getBooleanArgument(taskConfig, ARG_COMPLETE_WAYS, DEFAULT_COMPLETE_WAYS);
		
		return new DatasetSinkSourceManager(
			taskConfig.getId(),
			new DatasetBoundingBoxFilter(left, right, top, bottom, completeWays),
			taskConfig.getPipeArgs()
		);
	}
}
