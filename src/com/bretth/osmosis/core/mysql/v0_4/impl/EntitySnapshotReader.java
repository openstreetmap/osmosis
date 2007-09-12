package com.bretth.osmosis.core.mysql.v0_4.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

import com.bretth.osmosis.core.domain.v0_4.Entity;
import com.bretth.osmosis.core.mysql.common.EntityHistory;
import com.bretth.osmosis.core.store.PeekableIterator;
import com.bretth.osmosis.core.store.ReleasableIterator;


/**
 * Reads a complete snapshot of an entity type based on the complete history of
 * an entity type.
 * 
 * @author Brett Henderson
 * @param <T>
 *            The data type to be read.
 */
public class EntitySnapshotReader<T extends Entity> implements ReleasableIterator<T> {
	
	private PeekableIterator<EntityHistory<T>> entityIterator;
	private Date snapshotInstant;
	private Comparator<EntityHistory<T>> resultOrdering;
	private T nextValue;
	private boolean nextValueLoaded;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param entityIterator
	 *            An iterator containing the full history for an entity type
	 *            ordered by identifier.
	 * @param snapshotInstant
	 *            The state of the entity at this point in time will be dumped.
	 *            This ensures a consistent snapshot.
	 * @param resultOrdering
	 *            This provides a way of sorting the history records for a
	 *            single entity, it may be null if no sorting is required.
	 */
	public EntitySnapshotReader(PeekableIterator<EntityHistory<T>> entityIterator, Date snapshotInstant, Comparator<EntityHistory<T>> resultOrdering) {
		this.entityIterator = entityIterator;
		this.snapshotInstant = snapshotInstant;
		this.resultOrdering = resultOrdering;
		
		nextValueLoaded = false;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public boolean hasNext() {
		while (!nextValueLoaded && entityIterator.hasNext()) {
			List<EntityHistory<T>> entityHistoryList;
			long currentId;
			
			entityHistoryList = new ArrayList<EntityHistory<T>>();
			
			// Determine the id of the next set of history elements.
			currentId = entityIterator.peekNext().getEntity().getId();
			
			// Loop until all history values for the current element are exhausted.
			while (entityIterator.hasNext() && currentId == entityIterator.peekNext().getEntity().getId()) {
				EntityHistory<T> entityHistory;
				
				entityHistory = entityIterator.next();
				
				// We're only interested in elements prior or equal to the snapshot point.
				if (entityHistory.getEntity().getTimestamp().compareTo(snapshotInstant) <= 0) {
					entityHistoryList.add(entityHistory);
				}
			}
			
			if (resultOrdering != null) {
				Collections.sort(entityHistoryList, resultOrdering);
			}
			
			// If we have elements in the list, the last one is the one required
			// for the snapshot.  We only consider it if it is visible.
			if (entityHistoryList.size() > 0) {
				EntityHistory<T> entityHistory;
				
				entityHistory = entityHistoryList.get(entityHistoryList.size() - 1);
				
				if (entityHistory.isVisible()) {
					nextValue = entityHistory.getEntity();
					nextValueLoaded = true;
				}
			}
		}
		
		return nextValueLoaded;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public T next() {
		T result;
		
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
		
		result = nextValue;
		nextValueLoaded = false;
		
		return result;
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
		entityIterator.release();
	}
}
