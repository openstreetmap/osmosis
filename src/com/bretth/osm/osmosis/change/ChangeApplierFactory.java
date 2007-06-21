package com.bretth.osm.osmosis.change;

import java.util.Map;

import com.bretth.osm.osmosis.pipeline.MultiSinkMultiChangeSinkRunnableSourceManager;
import com.bretth.osm.osmosis.pipeline.TaskManager;
import com.bretth.osm.osmosis.pipeline.TaskManagerFactory;


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
