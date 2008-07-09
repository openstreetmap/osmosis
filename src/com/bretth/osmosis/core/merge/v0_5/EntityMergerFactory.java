// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.merge.v0_5;

import java.util.HashMap;
import java.util.Map;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.merge.common.ConflictResolutionMethod;
import com.bretth.osmosis.core.pipeline.common.TaskConfiguration;
import com.bretth.osmosis.core.pipeline.common.TaskManager;
import com.bretth.osmosis.core.pipeline.common.TaskManagerFactory;
import com.bretth.osmosis.core.pipeline.v0_5.MultiSinkRunnableSourceManager;


/**
 * The task manager factory for an entity merger.
 * 
 * @author Brett Henderson
 */
public class EntityMergerFactory extends TaskManagerFactory {
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
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		String conflictResolutionMethod;
		
		conflictResolutionMethod = getStringArgument(taskConfig, ARG_CONFLICT_RESOLUTION_METHOD, DEFAULT_CONFLICT_RESOLUTION_METHOD);
		
		if (!conflictResolutionMethodMap.containsKey(conflictResolutionMethod)) {
			throw new OsmosisRuntimeException(
					"Argument " + ARG_CONFLICT_RESOLUTION_METHOD + " for task " + taskConfig.getId() +
					" has value \"" + conflictResolutionMethod + "\" which is unrecognised.");
		}
		
		return new MultiSinkRunnableSourceManager(
			taskConfig.getId(),
			new EntityMerger(conflictResolutionMethodMap.get(conflictResolutionMethod), 10),
			taskConfig.getPipeArgs()
		);
	}
}
