// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.apidb.v0_6.impl;

import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

import org.openstreetmap.osmosis.core.container.v0_6.ChangeContainer;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;
import org.openstreetmap.osmosis.core.store.PeekableIterator;
import org.openstreetmap.osmosis.core.task.common.ChangeAction;


/**
 * Produces a snapshot at a point in time from a complete history stream.
 * 
 * @author Brett Henderson
 */
public class EntitySnapshotReader implements ReleasableIterator<EntityContainer> {
	
	private PeekableIterator<List<ChangeContainer>> sourceIterator;
	private Date snapshotInstant;
	private EntityContainer nextValue;
	private boolean nextValueLoaded;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param sourceIterator
	 *            An iterator containing the full history for entities.
	 * @param snapshotInstant
	 *            The state of the entity at this point in time will be dumped.
	 *            This ensures a consistent snapshot.
	 */
	public EntitySnapshotReader(
			ReleasableIterator<ChangeContainer> sourceIterator, Date snapshotInstant) {
		this.sourceIterator = new PeekableIterator<List<ChangeContainer>>(new EntityHistoryListReader(sourceIterator));
		this.snapshotInstant = snapshotInstant;
		
		nextValueLoaded = false;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public boolean hasNext() {
		while (!nextValueLoaded && sourceIterator.hasNext()) {
			List<ChangeContainer> changeList;
			ChangeContainer changeContainer;
			
			// Get the next change list from the underlying stream.
			changeList = sourceIterator.next();
			
			// Loop until all history values for the current element are exhausted and get the
			// latest version of the entity that fits within the snapshot timestamp.
			changeContainer = null;
			for (ChangeContainer tmpChangeContainer : changeList) {
				
				// We're only interested in elements prior or equal to the snapshot point.
				if (tmpChangeContainer.getEntityContainer().getEntity()
						.getTimestamp().compareTo(snapshotInstant) <= 0) {
					// Replace the current change container with the later version.
					changeContainer = tmpChangeContainer;
				}
			}
			
			// We are not interested in items created after the snapshot timestamp (ie. null) or deleted items.
			if (changeContainer != null && !ChangeAction.Delete.equals(changeContainer.getAction())) {
				nextValue = changeContainer.getEntityContainer();
				nextValueLoaded = true;
			}
		}
		
		return nextValueLoaded;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public EntityContainer next() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
		
		nextValueLoaded = false;
		
		return nextValue;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void remove() {
		throw new UnsupportedOperationException();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void release() {
		sourceIterator.release();
	}
}
