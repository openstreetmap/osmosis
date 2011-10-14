// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.bound.v0_6;

import org.openstreetmap.osmosis.core.OsmosisConstants;
import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManager;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.pipeline.v0_6.SinkSourceManager;


/**
 * The task manager factory for a bounding box computer.
 * 
 * @author Igor Podolskiy
 */
public class BoundComputerFactory extends TaskManagerFactory {

	private static final String ARG_ORIGIN = "origin";
	private static final String DEFAULT_ORIGIN = "Osmosis/" + OsmosisConstants.VERSION;

	
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		
		String origin = getStringArgument(taskConfig, ARG_ORIGIN, DEFAULT_ORIGIN);
		
		return new SinkSourceManager(taskConfig.getId(), 
				new BoundComputer(origin), 
				taskConfig.getPipeArgs());
	}

}
