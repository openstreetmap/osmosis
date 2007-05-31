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
	
	/**
	 * Creates a new instance and adds the class to the global register.
	 * 
	 * @param taskType
	 *            The name to register the type against.
	 */
	public ChangeApplierFactory(String taskType) {
		super(taskType);
	}
	
	
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
}
