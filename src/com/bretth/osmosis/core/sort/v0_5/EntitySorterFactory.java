package com.bretth.osmosis.core.sort.v0_5;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import com.bretth.osmosis.core.cli.TaskConfiguration;
import com.bretth.osmosis.core.container.v0_5.EntityContainer;
import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.pipeline.common.TaskManager;
import com.bretth.osmosis.core.pipeline.common.TaskManagerFactory;
import com.bretth.osmosis.core.pipeline.v0_5.SinkSourceManager;


/**
 * The task manager factory for an entity sorter.
 * 
 * @author Brett Henderson
 */
public class EntitySorterFactory extends TaskManagerFactory {
	private static final String ARG_COMPARATOR_TYPE = "type";
	
	private Map<String, Comparator<EntityContainer>> comparatorMap;
	private String defaultComparatorType;
	
	
	/**
	 * Creates a new instance.
	 */
	public EntitySorterFactory() {
		comparatorMap = new HashMap<String, Comparator<EntityContainer>>();
	}
	
	
	/**
	 * Registers a new comparator.
	 * 
	 * @param comparatorType
	 *            The name of the comparator.
	 * @param comparator
	 *            The comparator.
	 * @param setAsDefault
	 *            If true, this will be set to be the default comparator if no
	 *            comparator is specified.
	 */
	public void registerComparator(String comparatorType, Comparator<EntityContainer> comparator, boolean setAsDefault) {
		if (comparatorMap.containsKey(comparatorType)) {
			throw new OsmosisRuntimeException("Comparator type \"" + comparatorType + "\" already exists.");
		}
		
		if (setAsDefault) {
			defaultComparatorType = comparatorType;
		}
		
		comparatorMap.put(comparatorType, comparator);
	}
	
	
	/**
	 * Retrieves the comparator identified by the specified type.
	 * 
	 * @param comparatorType
	 *            The comparator to be retrieved.
	 * @return The comparator.
	 */
	private Comparator<EntityContainer> getComparator(String comparatorType) {
		if (!comparatorMap.containsKey(comparatorType)) {
			throw new OsmosisRuntimeException("Comparator type " + comparatorType
					+ " doesn't exist.");
		}
		
		return comparatorMap.get(comparatorType);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		Comparator<EntityContainer> comparator;
		
		// Get the comparator.
		comparator = getComparator(
			getStringArgument(
				taskConfig,
				ARG_COMPARATOR_TYPE,
				getDefaultStringArgument(taskConfig, defaultComparatorType)
			)
		);
		
		return new SinkSourceManager(
			taskConfig.getId(),
			new EntitySorter(comparator),
			taskConfig.getPipeArgs()
		);
	}
}
