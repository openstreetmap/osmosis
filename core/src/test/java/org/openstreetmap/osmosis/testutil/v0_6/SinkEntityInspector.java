// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.testutil.v0_6;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;

/**
 * Mock object for inspecting the resulting entities after passing through a pipeline task.
 * 
 * @author Karl Newman
 */
public class SinkEntityInspector implements Sink {

	private List<EntityContainer> processedEntities;
	
	
	/**
	 * Creates a new instance.
	 */
	public SinkEntityInspector() {
		processedEntities = new LinkedList<EntityContainer>();
	}


	/**
	 * {@inheritDoc}
	 */
	public void initialize(Map<String, Object> metaData) {
		// Nothing to do here
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void complete() {
		// Nothing to do here
	}


	/**
	 * Catch all passed entities and save them for later inspection.
	 * 
	 * @param entityContainer
	 *            The entity to be processed.
	 */
	@Override
	public void process(EntityContainer entityContainer) {
		processedEntities.add(entityContainer);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void release() {
		// Nothing to do here
	}


	/**
	 * Shortcut method if you only care about the most recent EntityContainer.
	 * 
	 * @return the lastEntityContainer
	 */
	public EntityContainer getLastEntityContainer() {
		if (processedEntities.isEmpty()) {
			return null;
		} else {
			return processedEntities.get(processedEntities.size() - 1);
		}
	}


	/**
	 * Retrieve an Iterable of all the processed EntityContainers.
	 * 
	 * @return the processedEntities
	 */
	public Iterable<EntityContainer> getProcessedEntities() {
		return Collections.unmodifiableList(processedEntities);
	}

}
