// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.merge.v0_6.impl;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.sort.v0_6.EntityByTypeThenIdComparator;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.core.task.v0_6.SinkSource;


/**
 * Validates that entity data in a pipeline is sorted by entity type then id. It
 * accepts input data from a Source and passes all data to a downstream Sink.
 * 
 * @author Brett Henderson
 */
public class SortedEntityPipeValidator implements SinkSource {
	private Sink sink;
	private EntityByTypeThenIdComparator comparator;
	private EntityContainer previousEntityContainer;
	
	
	/**
	 * Creates a new instance.
	 */
	public SortedEntityPipeValidator() {
		comparator = new EntityByTypeThenIdComparator();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void complete() {
		sink.complete();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(EntityContainer entityContainer) {
		// If this is not the first entity in the pipeline, make sure this
		// entity is greater than the previous.
		if (previousEntityContainer != null) {
			if (comparator.compare(previousEntityContainer,	entityContainer) >= 0) {
				throw new OsmosisRuntimeException(
					"Pipeline entities are not sorted, previous entity type=" + previousEntityContainer.getEntity().getType() + ", id=" + previousEntityContainer.getEntity().getId()
					+ " current entity type=" + entityContainer.getEntity().getType() + ", id=" + entityContainer.getEntity().getId() + "."
				);
			}
		}
		
		sink.process(entityContainer);
		
		previousEntityContainer = entityContainer;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void release() {
		sink.release();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void setSink(Sink sink) {
		this.sink = sink;
	}
}
