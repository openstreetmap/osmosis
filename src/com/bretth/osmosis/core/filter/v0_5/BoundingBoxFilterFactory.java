package com.bretth.osmosis.core.filter.v0_5;

import java.util.Map;

import com.bretth.osmosis.core.pipeline.common.TaskManager;
import com.bretth.osmosis.core.pipeline.common.TaskManagerFactory;
import com.bretth.osmosis.core.pipeline.v0_5.SinkSourceManager;


/**
 * The task manager factory for a bounding box filter.
 * 
 * @author Brett Henderson
 */
public class BoundingBoxFilterFactory extends TaskManagerFactory {
	private static final String ARG_LEFT = "left";
	private static final String ARG_RIGHT = "right";
	private static final String ARG_TOP = "top";
	private static final String ARG_BOTTOM = "bottom";
	private static final double DEFAULT_LEFT = -180;
	private static final double DEFAULT_RIGHT = 180;
	private static final double DEFAULT_TOP = 90;
	private static final double DEFAULT_BOTTOM = -90;

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(String taskId, Map<String, String> taskArgs, Map<String, String> pipeArgs) {
		double left;
		double right;
		double top;
		double bottom;
		
		// Get the task arguments.
		left = getDoubleArgument(taskId, taskArgs, ARG_LEFT, DEFAULT_LEFT);
		right = getDoubleArgument(taskId, taskArgs, ARG_RIGHT, DEFAULT_RIGHT);
		top = getDoubleArgument(taskId, taskArgs, ARG_TOP, DEFAULT_TOP);
		bottom = getDoubleArgument(taskId, taskArgs, ARG_BOTTOM, DEFAULT_BOTTOM);
		
		return new SinkSourceManager(
			taskId,
			new BoundingBoxFilter(left, right, top, bottom),
			pipeArgs
		);
	}
}
