// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.dataset.v0_6.impl;

import org.openstreetmap.osmosis.core.container.v0_6.EntityManager;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;
import org.openstreetmap.osmosis.core.store.NoSuchIndexElementException;
import org.openstreetmap.osmosis.core.store.ReleasableAdaptorForIterator;


/**
 * Provides access to ways within a dataset store.
 * 
 * @author Brett Henderson
 */
public class WayManager implements EntityManager<Way> {
	
	private WayStorageContainer storageContainer;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param storageContainer
	 *            The storage container containing the entities.
	 */
	public WayManager(WayStorageContainer storageContainer) {
		this.storageContainer = storageContainer;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addEntity(Way entity) {
		throw new UnsupportedOperationException();
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean exists(long id) {
		// Check if the node id exists in the index.
		try {
			storageContainer.getWayObjectOffsetIndexReader().get(id);
			
			return true;
			
		} catch (NoSuchIndexElementException e) {
			return false;
		}
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Way getEntity(long id) {
		return storageContainer.getWayObjectReader().get(
				storageContainer.getWayObjectOffsetIndexReader().get(id).getValue()
		);
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReleasableIterator<Way> iterate() {
		return new ReleasableAdaptorForIterator<Way>(
				storageContainer.getWayObjectReader().iterate());
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void modifyEntity(Way entity) {
		throw new UnsupportedOperationException();
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeEntity(long entityId) {
		throw new UnsupportedOperationException();
	}
}
