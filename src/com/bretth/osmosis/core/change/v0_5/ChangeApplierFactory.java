package com.bretth.osmosis.core.change.v0_5;

import com.bretth.osmosis.core.cli.TaskConfiguration;
import com.bretth.osmosis.core.pipeline.common.TaskManager;
import com.bretth.osmosis.core.pipeline.common.TaskManagerFactory;
import com.bretth.osmosis.core.pipeline.v0_5.MultiSinkMultiChangeSinkRunnableSourceManager;


/**
 * The task manager factory for a change applier.
 * 
 * @author Brett Henderson
 */
public class ChangeApplierFactory extends TaskManagerFactory {
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		return new MultiSinkMultiChangeSinkRunnableSourceManager(
			taskConfig.getId(),
			new ChangeApplier(10),
			taskConfig.getPipeArgs()
		);
	}
}
