package com.bretth.osm.conduit.sort;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import com.bretth.osm.conduit.ConduitRuntimeException;
import com.bretth.osm.conduit.data.OsmElement;
import com.bretth.osm.conduit.pipeline.SinkSourceManager;
import com.bretth.osm.conduit.pipeline.TaskManager;
import com.bretth.osm.conduit.pipeline.TaskManagerFactory;


/**
 * The task manager factory for an element sorter.
 * 
 * @author Brett Henderson
 */
public class ElementSorterFactory extends TaskManagerFactory {
	private static final String ARG_COMPARATOR_TYPE = "type";
	private static final String DEFAULT_COMPARATOR_TYPE = "TypeThenId";
	
	private Map<String, Comparator<OsmElement>> comparatorMap;
	
	
	/**
	 * Creates a new instance.
	 */
	public ElementSorterFactory() {
		comparatorMap = new HashMap<String, Comparator<OsmElement>>();
	}
	
	
	/**
	 * Registers a new comparator.
	 * 
	 * @param comparatorType
	 *            The name of the comparator.
	 * @param comparator
	 *            The comparator.
	 */
	public void registerComparator(String comparatorType, Comparator<OsmElement> comparator) {
		if (comparatorMap.containsKey(comparatorType)) {
			throw new ConduitRuntimeException("Comparator type \"" + comparatorType + "\" already exists.");
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
	private Comparator<OsmElement> getComparator(String comparatorType) {
		if (!comparatorMap.containsKey(comparatorType)) {
			throw new ConduitRuntimeException("Comparator type " + comparatorType
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
		Comparator<OsmElement> comparator;
		
		// Get the comparator.
		comparator = getComparator(
			getStringArgument(taskArgs, ARG_COMPARATOR_TYPE, DEFAULT_COMPARATOR_TYPE)
		);
		
		return new SinkSourceManager(
			taskId,
			new ElementSorter(comparator),
			pipeArgs
		);
	}
}
