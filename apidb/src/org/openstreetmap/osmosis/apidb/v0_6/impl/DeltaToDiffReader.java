// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.apidb.v0_6.impl;

import java.util.List;
import java.util.NoSuchElementException;

import org.openstreetmap.osmosis.core.container.v0_6.ChangeContainer;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;
import org.openstreetmap.osmosis.core.store.PeekableIterator;
import org.openstreetmap.osmosis.core.task.common.ChangeAction;


/**
 * Takes an underlying full history delta stream and converts it into a diff stream. The difference
 * between the two is that a delta stream may contain multiple changes for a single entity, a diff
 * stream will contain a single change for the entity to get from the beginning to end point.
 */
public class DeltaToDiffReader implements ReleasableIterator<ChangeContainer> {
	
	private PeekableIterator<List<ChangeContainer>> sourceIterator;
	private ChangeContainer nextValue;
	private boolean nextValueLoaded;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param sourceIterator
	 *            An iterator containing the full history for entities.
	 */
	public DeltaToDiffReader(
			ReleasableIterator<ChangeContainer> sourceIterator) {
		this.sourceIterator = new PeekableIterator<List<ChangeContainer>>(new EntityHistoryListReader(sourceIterator));
		
		nextValueLoaded = false;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public boolean hasNext() {
		while (!nextValueLoaded && sourceIterator.hasNext()) {
			List<ChangeContainer> changeList;
			ChangeContainer changeContainer;
			boolean createdPreviously;
			
			// Get the next change list from the underlying stream.
			changeList = sourceIterator.next();
			
			// Check the first node, if it has a version greater than 1 the node
			// existed prior to the interval beginning and therefore cannot be a
			// create.
			createdPreviously = (changeList.get(0).getEntityContainer().getEntity().getVersion() > 1);
			
			// Get the most current change.
			changeContainer = changeList.get(changeList.size() - 1);
			
			// The entity has been modified if it is a create/modify and was created previously.
			// It is a create if it is create/modify and was NOT created previously.
			// It is a delete if it is a delete and was created previously.
			// No action if it is a delete and was NOT created previously.
			if (!ChangeAction.Delete.equals(changeContainer.getAction()) && createdPreviously) {
				nextValue = new ChangeContainer(changeContainer.getEntityContainer(), ChangeAction.Modify);
				nextValueLoaded = true;
			} else if (!ChangeAction.Delete.equals(changeContainer.getAction()) && !createdPreviously) {
				nextValue = new ChangeContainer(changeContainer.getEntityContainer(), ChangeAction.Create);
				nextValueLoaded = true;
			} else if (ChangeAction.Delete.equals(changeContainer.getAction()) && createdPreviously) {
				nextValue = new ChangeContainer(changeContainer.getEntityContainer(), ChangeAction.Delete);
				nextValueLoaded = true;
			}
		}
		
		return nextValueLoaded;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public ChangeContainer next() {
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
