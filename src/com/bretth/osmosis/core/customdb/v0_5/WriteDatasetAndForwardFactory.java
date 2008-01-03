package com.bretth.osmosis.core.customdb.v0_5;

import com.bretth.osmosis.core.cli.TaskConfiguration;
import com.bretth.osmosis.core.pipeline.common.TaskManager;
import com.bretth.osmosis.core.pipeline.common.TaskManagerFactory;
import com.bretth.osmosis.core.pipeline.v0_5.SinkDatasetSourceManager;


/**
 * The task manager factory for a bounding box filter.
 * 
 * @author Brett Henderson
 */
public class WriteDatasetAndForwardFactory extends TaskManagerFactory {
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		return new SinkDatasetSourceManager(
			taskConfig.getId(),
			new WriteDatasetAndForward(),
			taskConfig.getPipeArgs()
		);
	}
}
