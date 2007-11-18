package com.bretth.osmosis.core.misc.v0_5;

import com.bretth.osmosis.core.cli.TaskConfiguration;
import com.bretth.osmosis.core.pipeline.common.TaskManager;
import com.bretth.osmosis.core.pipeline.common.TaskManagerFactory;
import com.bretth.osmosis.core.pipeline.v0_5.ChangeSinkManager;


/**
 * The task manager factory for a null change writer.
 * 
 * @author Brett Henderson
 */
public class NullChangeWriterFactory extends TaskManagerFactory {
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		return new ChangeSinkManager(taskConfig.getId(), new NullChangeWriter(), taskConfig.getPipeArgs());
	}
}
