// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.filter.v0_6;

import java.io.File;
import com.bretth.osmosis.core.filter.common.IdTrackerType;
import com.bretth.osmosis.core.pipeline.common.TaskConfiguration;
import com.bretth.osmosis.core.pipeline.common.TaskManager;
import com.bretth.osmosis.core.pipeline.v0_6.SinkSourceManager;


/**
 * The task manager factory for a polygon filter.
 * 
 * @author Brett Henderson
 */
public class PolygonFilterFactory extends AreaFilterTaskManagerFactory {
	private static final String ARG_FILE = "file";
	private static final String DEFAULT_FILE = "polygon.txt";
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
		String fileName;
		File file;
		boolean completeWays;
		boolean completeRelations;
		
		// Get the task arguments.
		idTrackerType = getIdTrackerType(taskConfig);
		fileName = getStringArgument(taskConfig, ARG_FILE, DEFAULT_FILE);
		completeWays = getBooleanArgument(taskConfig, ARG_COMPLETE_WAYS, DEFAULT_COMPLETE_WAYS);
		completeRelations = getBooleanArgument(taskConfig, ARG_COMPLETE_RELATIONS, DEFAULT_COMPLETE_RELATIONS);
		
		// Create a file object from the file name provided.
		file = new File(fileName);
		
		return new SinkSourceManager(
			taskConfig.getId(),
			new PolygonFilter(idTrackerType, file, completeWays, completeRelations),
			taskConfig.getPipeArgs()
		);
	}
}
