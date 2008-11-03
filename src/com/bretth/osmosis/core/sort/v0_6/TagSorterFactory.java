// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.sort.v0_6;

import com.bretth.osmosis.core.pipeline.common.TaskConfiguration;
import com.bretth.osmosis.core.pipeline.common.TaskManager;
import com.bretth.osmosis.core.pipeline.common.TaskManagerFactory;
import com.bretth.osmosis.core.pipeline.v0_6.SinkSourceManager;


/**
 * The task manager factory for a tag sorter.
 * 
 * @author Brett Henderson
 */
public class TagSorterFactory extends TaskManagerFactory {
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		
		return new SinkSourceManager(
			taskConfig.getId(),
			new TagSorter(),
			taskConfig.getPipeArgs()
		);
	}
}
