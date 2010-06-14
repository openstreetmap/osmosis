// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.apidb.v0_6.impl;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.osmosis.core.container.v0_6.ChangeContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;
import org.openstreetmap.osmosis.core.store.PeekableIterator;


/**
 * Reads a history stream and groups all changes for a single entity into lists.
 */
public class EntityHistoryListReader implements ReleasableIterator<List<ChangeContainer>> {
	private PeekableIterator<ChangeContainer> sourceIterator;


	/**
	 * Creates a new instance.
	 * 
	 * @param sourceIterator
	 *            An iterator containing full entity history ordered by type, identifier and
	 *            version.
	 */
	public EntityHistoryListReader(ReleasableIterator<ChangeContainer> sourceIterator) {
		this.sourceIterator = new PeekableIterator<ChangeContainer>(sourceIterator);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasNext() {
		return sourceIterator.hasNext();
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ChangeContainer> next() {
		List<ChangeContainer> changeList;
		Entity peekEntity;
		long currentId;
		EntityType currentEntityType;
		
		// Get the next change from the underlying stream.
		peekEntity = sourceIterator.peekNext().getEntityContainer().getEntity();
		currentId = peekEntity.getId();
		currentEntityType = peekEntity.getType();
		
		// Loop until all history values for the current element are exhausted.
		changeList = new ArrayList<ChangeContainer>();
		while (sourceIterator.hasNext()) {
			ChangeContainer tmpChangeContainer = sourceIterator.peekNext();
			
			// Break out of the loop when we reach the next entity in the stream.
			if (currentId != tmpChangeContainer.getEntityContainer().getEntity().getId()
				|| !currentEntityType.equals(tmpChangeContainer.getEntityContainer().getEntity().getType())) {
				break;
			}
			
			// We want the value that we have already peeked from the iterator, so remove it from the iterator.
			sourceIterator.next();
			
			// Add the change to the result list.
			changeList.add(tmpChangeContainer);
		}
		
		return changeList;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void release() {
		sourceIterator.release();
	}
}
