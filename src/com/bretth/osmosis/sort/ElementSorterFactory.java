package com.bretth.osmosis.sort;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import com.bretth.osmosis.OsmosisRuntimeException;
import com.bretth.osmosis.container.ElementContainer;
import com.bretth.osmosis.pipeline.SinkSourceManager;
import com.bretth.osmosis.pipeline.TaskManager;
import com.bretth.osmosis.pipeline.TaskManagerFactory;


/**
 * The task manager factory for an element sorter.
 * 
 * @author Brett Henderson
 */
public class ElementSorterFactory extends TaskManagerFactory {
	private static final String ARG_COMPARATOR_TYPE = "type";
	
	private Map<String, Comparator<ElementContainer>> comparatorMap;
	private String defaultComparatorType;
	
	
	/**
	 * Creates a new instance.
	 */
	public ElementSorterFactory() {
		comparatorMap = new HashMap<String, Comparator<ElementContainer>>();
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
	public void registerComparator(String comparatorType, Comparator<ElementContainer> comparator, boolean setAsDefault) {
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
	private Comparator<ElementContainer> getComparator(String comparatorType) {
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
	protected TaskManager createTaskManagerImpl(String taskId,
			Map<String, String> taskArgs, Map<String, String> pipeArgs) {
		Comparator<ElementContainer> comparator;
		
		// Get the comparator.
		comparator = getComparator(
			getStringArgument(taskArgs, ARG_COMPARATOR_TYPE, defaultComparatorType)
		);
		
		return new SinkSourceManager(
			taskId,
			new ElementSorter(comparator),
			pipeArgs
		);
	}
}
