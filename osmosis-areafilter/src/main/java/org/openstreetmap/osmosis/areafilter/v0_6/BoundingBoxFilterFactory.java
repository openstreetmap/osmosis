// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.areafilter.v0_6;

import org.openstreetmap.osmosis.core.filter.common.IdTrackerType;
import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManager;
import org.openstreetmap.osmosis.core.pipeline.v0_6.SinkSourceManager;


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
	private static final String ARG_X1 = "x1";
	private static final String ARG_Y1 = "y1";
	private static final String ARG_X2 = "x2";
	private static final String ARG_Y2 = "y2";
	private static final String ARG_ZOOM = "zoom";
	private static final String ARG_CLIP_INCOMPLETE_ENTITIES = "clipIncompleteEntities";
	private static final String ARG_COMPLETE_WAYS = "completeWays";
	private static final String ARG_COMPLETE_RELATIONS = "completeRelations";
	private static final String ARG_CASCADING_RELATIONS = "cascadingRelations";
	private static final double DEFAULT_LEFT = -180;
	private static final double DEFAULT_RIGHT = 180;
	private static final double DEFAULT_TOP = 90;
	private static final double DEFAULT_BOTTOM = -90;
	private static final int DEFAULT_ZOOM = 12;
	private static final boolean DEFAULT_CLIP_INCOMPLETE_ENTITIES = false;
	private static final boolean DEFAULT_COMPLETE_WAYS = false;
	private static final boolean DEFAULT_COMPLETE_RELATIONS = false;
	private static final boolean DEFAULT_CASCADING_RELATIONS = false;

	
	private double xToLon(int zoom, int x) {
	    double unit = 360 / Math.pow(2, zoom);
	    return -180 + x * unit;
	}
	
	private double projectF(double lat) {
		// Project latitude to mercator
	    return Math.log(Math.tan(lat) + 1 / Math.cos(lat));
	}
	
	private double projectMercToLat(double y) {
	    return Math.toDegrees(Math.atan(Math.sinh(y)));
	}
	
	private double yToLat(int zoom, int y) {

		// Convert zoom/y to mercator
		
		// Get maximum range of mercator coordinates
		double limitY = projectF(Math.atan(Math.sinh(Math.PI)));
		double limitY2 = projectF((Math.atan(Math.sinh(-Math.PI))));
		double rangeY = limitY - limitY2;

 
		double unit = 1 / Math.pow(2, zoom);
	    double relY = limitY - rangeY * y * unit;
	    
	    // Mercator to latitude
	    return projectMercToLat(relY);	    
	}
	
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
		boolean clipIncompleteEntities;
		boolean completeWays;
		boolean completeRelations;
		boolean cascadingRelations;
		int zoom;
		
		// Get the task arguments.
		idTrackerType = getIdTrackerType(taskConfig);
		left = getDoubleArgument(taskConfig, ARG_LEFT, DEFAULT_LEFT);
		right = getDoubleArgument(taskConfig, ARG_RIGHT, DEFAULT_RIGHT);
		top = getDoubleArgument(taskConfig, ARG_TOP, DEFAULT_TOP);
		bottom = getDoubleArgument(taskConfig, ARG_BOTTOM, DEFAULT_BOTTOM);
		clipIncompleteEntities = getBooleanArgument(
				taskConfig, ARG_CLIP_INCOMPLETE_ENTITIES, DEFAULT_CLIP_INCOMPLETE_ENTITIES);
		completeWays = getBooleanArgument(taskConfig, ARG_COMPLETE_WAYS, DEFAULT_COMPLETE_WAYS);
		completeRelations = getBooleanArgument(taskConfig, ARG_COMPLETE_RELATIONS, DEFAULT_COMPLETE_RELATIONS);
		cascadingRelations = getBooleanArgument(taskConfig, ARG_CASCADING_RELATIONS, DEFAULT_CASCADING_RELATIONS);
		
		zoom = getIntegerArgument(taskConfig, ARG_ZOOM, DEFAULT_ZOOM);
		if (doesArgumentExist(taskConfig, ARG_X1)) {
			int x1 = getIntegerArgument(taskConfig, ARG_X1);
			left = xToLon(zoom, x1);
			right = xToLon(zoom, getIntegerArgument(taskConfig, ARG_X2, x1) + 1);
		}
		if (doesArgumentExist(taskConfig, ARG_Y1)) {
			int y1 = getIntegerArgument(taskConfig, ARG_Y1);
			top = yToLat(zoom, y1);
			bottom = yToLat(zoom, getIntegerArgument(taskConfig, ARG_Y2, y1) + 1);
		}
		
		return new SinkSourceManager(
			taskConfig.getId(),
			new BoundingBoxFilter(idTrackerType, left, right, top, bottom, clipIncompleteEntities, completeWays,
					completeRelations, cascadingRelations),
			taskConfig.getPipeArgs()
		);
	}
}
