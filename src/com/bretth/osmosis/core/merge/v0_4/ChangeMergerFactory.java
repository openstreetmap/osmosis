package com.bretth.osmosis.core.merge.v0_4;

import java.util.HashMap;
import java.util.Map;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.merge.common.ConflictResolutionMethod;
import com.bretth.osmosis.core.pipeline.common.TaskManager;
import com.bretth.osmosis.core.pipeline.common.TaskManagerFactory;
import com.bretth.osmosis.core.pipeline.v0_4.MultiChangeSinkRunnableChangeSourceManager;


/**
 * The task manager factory for a change merger.
 * 
 * @author Brett Henderson
 */
public class ChangeMergerFactory extends TaskManagerFactory {
	private static final String ARG_CONFLICT_RESOLUTION_METHOD = "conflictResolutionMethod";
	private static final String DEFAULT_CONFLICT_RESOLUTION_METHOD = "timestamp";
	private static final String ALTERNATIVE_CONFLICT_RESOLUTION_METHOD_1 = "lastSource";
	private static final Map<String, ConflictResolutionMethod> conflictResolutionMethodMap = new HashMap<String, ConflictResolutionMethod>();
	
	static {
		conflictResolutionMethodMap.put(DEFAULT_CONFLICT_RESOLUTION_METHOD, ConflictResolutionMethod.Timestamp);
		conflictResolutionMethodMap.put(ALTERNATIVE_CONFLICT_RESOLUTION_METHOD_1, ConflictResolutionMethod.LatestSource);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(String taskId, Map<String, String> taskArgs, Map<String, String> pipeArgs) {
		String conflictResolutionMethod;
		
		conflictResolutionMethod = getStringArgument(taskId, taskArgs, ARG_CONFLICT_RESOLUTION_METHOD, DEFAULT_CONFLICT_RESOLUTION_METHOD);
		
		if (!conflictResolutionMethodMap.containsKey(conflictResolutionMethod)) {
			throw new OsmosisRuntimeException(
					"Argument " + ARG_CONFLICT_RESOLUTION_METHOD + " for task " + taskId +
					" has value \"" + conflictResolutionMethod + "\" which is unrecognised.");
		}
		
		return new MultiChangeSinkRunnableChangeSourceManager(
			taskId,
			new ChangeMerger(conflictResolutionMethodMap.get(conflictResolutionMethod), 10),
			pipeArgs
		);
	}
}
