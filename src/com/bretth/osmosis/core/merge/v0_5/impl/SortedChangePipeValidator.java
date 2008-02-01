// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.merge.v0_5.impl;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.container.v0_5.ChangeContainer;
import com.bretth.osmosis.core.sort.v0_5.EntityByTypeThenIdComparator;
import com.bretth.osmosis.core.task.v0_5.ChangeSink;
import com.bretth.osmosis.core.task.v0_5.ChangeSinkChangeSource;


/**
 * Validates that change data in a pipeline is sorted by entity type then id. It
 * accepts input data from a Source and passes all data to a downstream Sink.
 * 
 * @author Brett Henderson
 */
public class SortedChangePipeValidator implements ChangeSinkChangeSource {
	private ChangeSink changeSink;
	private EntityByTypeThenIdComparator comparator;
	private ChangeContainer previousChangeContainer;
	
	
	/**
	 * Creates a new instance.
	 */
	public SortedChangePipeValidator() {
		comparator = new EntityByTypeThenIdComparator();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void complete() {
		changeSink.complete();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(ChangeContainer changeContainer) {
		// If this is not the first entity in the pipeline, make sure this
		// entity is greater than the previous.
		if (previousChangeContainer != null) {
			if (comparator.compare(previousChangeContainer.getEntityContainer(), changeContainer.getEntityContainer()) >= 0) {
				throw new OsmosisRuntimeException(
					"Pipeline entities are not sorted, previous entity type=" + previousChangeContainer.getEntityContainer().getEntity().getType() + ", id=" + previousChangeContainer.getEntityContainer().getEntity().getId()
					+ " current entity type=" + changeContainer.getEntityContainer().getEntity().getType() + ", id=" + changeContainer.getEntityContainer().getEntity().getId() + "."
				);
			}
		}
		
		changeSink.process(changeContainer);
		
		previousChangeContainer = changeContainer;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void release() {
		changeSink.release();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void setChangeSink(ChangeSink changeSink) {
		this.changeSink = changeSink;
	}
}
