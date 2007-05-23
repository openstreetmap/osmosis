package com.bretth.osm.conduit.change;

import java.util.Map;

import com.bretth.osm.conduit.pipeline.SinkChangeSinkSourceManager;
import com.bretth.osm.conduit.pipeline.TaskManager;
import com.bretth.osm.conduit.pipeline.TaskManagerFactory;


/**
 * The task manager factory for a change applier.
 * 
 * @author Brett Henderson
 */
public class ChangeApplierFactory extends TaskManagerFactory {
	private static final String TASK_TYPE = "apply-change";
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(String taskId, Map<String, String> taskArgs, Map<String, String> pipeArgs) {
		return new SinkChangeSinkSourceManager(
			taskId,
			new ChangeApplier(),
			pipeArgs
		);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getTaskType() {
		return TASK_TYPE;
	}
}
