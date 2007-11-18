package com.bretth.osmosis.core.change.v0_5;

import com.bretth.osmosis.core.cli.TaskConfiguration;
import com.bretth.osmosis.core.pipeline.common.TaskManager;
import com.bretth.osmosis.core.pipeline.common.TaskManagerFactory;
import com.bretth.osmosis.core.pipeline.v0_5.MultiSinkRunnableChangeSourceManager;


/**
 * The task manager factory for a change deriver.
 * 
 * @author Brett Henderson
 */
public class ChangeDeriverFactory extends TaskManagerFactory {
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		return new MultiSinkRunnableChangeSourceManager(
			taskConfig.getId(),
			new ChangeDeriver(10),
			taskConfig.getPipeArgs()
		);
	}
}
