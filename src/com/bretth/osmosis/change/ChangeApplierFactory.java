package com.bretth.osmosis.change;

import java.util.Map;

import com.bretth.osmosis.pipeline.MultiSinkMultiChangeSinkRunnableSourceManager;
import com.bretth.osmosis.pipeline.TaskManager;
import com.bretth.osmosis.pipeline.TaskManagerFactory;


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
	protected TaskManager createTaskManagerImpl(String taskId, Map<String, String> taskArgs, Map<String, String> pipeArgs) {
		return new MultiSinkMultiChangeSinkRunnableSourceManager(
			taskId,
			new ChangeApplier(10),
			pipeArgs
		);
	}
}
