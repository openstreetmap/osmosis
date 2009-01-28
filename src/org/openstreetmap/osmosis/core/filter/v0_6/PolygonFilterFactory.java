// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.filter.v0_6;

import java.io.File;
import org.openstreetmap.osmosis.core.filter.common.IdTrackerType;
import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManager;
import org.openstreetmap.osmosis.core.pipeline.v0_6.SinkSourceManager;


/**
 * The task manager factory for a polygon filter.
 * 
 * @author Brett Henderson
 */
public class PolygonFilterFactory extends AreaFilterTaskManagerFactory {
	private static final String ARG_FILE = "file";
	private static final String DEFAULT_FILE = "polygon.txt";
	private static final String ARG_CLIP_INCOMPLETE_ENTITIES = "clipIncompleteEntities";
	private static final String ARG_COMPLETE_WAYS = "completeWays";
	private static final String ARG_COMPLETE_RELATIONS = "completeRelations";
	private static final boolean DEFAULT_CLIP_INCOMPLETE_ENTITIES = false;
	private static final boolean DEFAULT_COMPLETE_WAYS = false;
	private static final boolean DEFAULT_COMPLETE_RELATIONS = false;

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		IdTrackerType idTrackerType;
		String fileName;
		File file;
		boolean clipIncompleteEntities;
		boolean completeWays;
		boolean completeRelations;
		
		// Get the task arguments.
		idTrackerType = getIdTrackerType(taskConfig);
		fileName = getStringArgument(taskConfig, ARG_FILE, DEFAULT_FILE);
		clipIncompleteEntities = getBooleanArgument(taskConfig, ARG_CLIP_INCOMPLETE_ENTITIES, DEFAULT_CLIP_INCOMPLETE_ENTITIES);
		completeWays = getBooleanArgument(taskConfig, ARG_COMPLETE_WAYS, DEFAULT_COMPLETE_WAYS);
		completeRelations = getBooleanArgument(taskConfig, ARG_COMPLETE_RELATIONS, DEFAULT_COMPLETE_RELATIONS);
		
		// Create a file object from the file name provided.
		file = new File(fileName);
		
		return new SinkSourceManager(
			taskConfig.getId(),
			new PolygonFilter(idTrackerType, file, clipIncompleteEntities, completeWays, completeRelations),
			taskConfig.getPipeArgs()
		);
	}
}
