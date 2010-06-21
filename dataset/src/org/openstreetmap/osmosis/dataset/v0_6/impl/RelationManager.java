// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.dataset.v0_6.impl;

import org.openstreetmap.osmosis.core.container.v0_6.EntityManager;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;
import org.openstreetmap.osmosis.core.store.NoSuchIndexElementException;
import org.openstreetmap.osmosis.core.store.ReleasableAdaptorForIterator;


/**
 * Provides access to relations within a dataset store.
 * 
 * @author Brett Henderson
 */
public class RelationManager implements EntityManager<Relation> {
	
	private RelationStorageContainer storageContainer;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param storageContainer
	 *            The storage container containing the entities.
	 */
	public RelationManager(RelationStorageContainer storageContainer) {
		this.storageContainer = storageContainer;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addEntity(Relation entity) {
		throw new UnsupportedOperationException();
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean exists(long id) {
		// Check if the node id exists in the index.
		try {
			storageContainer.getRelationObjectOffsetIndexReader().get(id);
			
			return true;
			
		} catch (NoSuchIndexElementException e) {
			return false;
		}
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Relation getEntity(long id) {
		return storageContainer.getRelationObjectReader().get(
				storageContainer.getRelationObjectOffsetIndexReader().get(id).getValue()
		);
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReleasableIterator<Relation> iterate() {
		return new ReleasableAdaptorForIterator<Relation>(
				storageContainer.getRelationObjectReader().iterate());
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void modifyEntity(Relation entity) {
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
