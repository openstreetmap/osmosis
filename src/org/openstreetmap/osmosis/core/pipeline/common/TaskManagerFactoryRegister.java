// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.pipeline.common;

import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;


/**
 * Maintains the set of task manager factories each identified by one or more
 * unique names. This allows a pipeline to be configured dynamically such as
 * from a user provided command line.
 * 
 * @author Brett Henderson
 */
public class TaskManagerFactoryRegister {

	/**
	 * The global register of task manager factories, keyed by a unique
	 * identifier.
	 */
	private Map<String, TaskManagerFactory> factoryMap;
	
	
	/**
	 * Creates a new instance.
	 */
	public TaskManagerFactoryRegister() {
		factoryMap = new HashMap<String, TaskManagerFactory>();
	}
	
	
	/**
	 * Registers a new factory.
	 * 
	 * @param taskType
	 *            The name the factory is identified by.
	 * @param factory
	 *            The factory to be registered.
	 */
	public void register(String taskType, TaskManagerFactory factory) {
		if (factoryMap.containsKey(taskType)) {
			throw new OsmosisRuntimeException("Task type \"" + taskType + "\" already exists.");
		}
		
		factoryMap.put(taskType, factory);
	}
	
	
	/**
	 * Get a task manager factory from the register.
	 * 
	 * @param taskType
	 *            The type of task requiring a factory.
	 * @return The factory instance.
	 */
	public TaskManagerFactory getInstance(String taskType) {
		if (!factoryMap.containsKey(taskType)) {
			throw new OsmosisRuntimeException("Task type " + taskType
					+ " doesn't exist.");
		}
		
		return factoryMap.get(taskType);
	}
}
