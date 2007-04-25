package com.bretth.osm.conduit.filter;

import java.util.Map;

import com.bretth.osm.conduit.pipeline.OsmTransformerManager;
import com.bretth.osm.conduit.pipeline.TaskManager;
import com.bretth.osm.conduit.pipeline.TaskManagerFactory;


public class BoundingBoxFilterFactory extends TaskManagerFactory {
	private static final String TASK_TYPE = "bounding-box";
	private static final String ARG_LEFT = "left";
	private static final String ARG_RIGHT = "right";
	private static final String ARG_TOP = "top";
	private static final String ARG_BOTTOM = "bottom";
	private static final String DEFAULT_LEFT = "-180";
	private static final String DEFAULT_RIGHT = "180";
	private static final String DEFAULT_TOP = "90";
	private static final String DEFAULT_BOTTOM = "-90";
	
	
	protected TaskManager createTaskManagerImpl(String taskId, Map<String, String> taskArgs, Map<String, String> pipeArgs) {
		double left;
		double right;
		double top;
		double bottom;
		
		// Get the task arguments.
		left = getDoubleArgument(taskArgs, ARG_LEFT, DEFAULT_LEFT);
		right = getDoubleArgument(taskArgs, ARG_RIGHT, DEFAULT_RIGHT);
		top = getDoubleArgument(taskArgs, ARG_TOP, DEFAULT_TOP);
		bottom = getDoubleArgument(taskArgs, ARG_BOTTOM, DEFAULT_BOTTOM);
		
		return new OsmTransformerManager(
			taskId,
			new BoundingBoxFilter(left, right, top, bottom),
			pipeArgs
		);
	}
	
	
	protected String getTaskType() {
		return TASK_TYPE;
	}
}
