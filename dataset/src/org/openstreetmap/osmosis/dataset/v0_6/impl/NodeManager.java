// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.dataset.v0_6.impl;

import org.openstreetmap.osmosis.core.container.v0_6.EntityManager;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;
import org.openstreetmap.osmosis.core.store.NoSuchIndexElementException;
import org.openstreetmap.osmosis.core.store.ReleasableAdaptorForIterator;


/**
 * Provides access to nodes within a dataset store.
 * 
 * @author Brett Henderson
 */
public class NodeManager implements EntityManager<Node> {
	
	private NodeStorageContainer storageContainer;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param storageContainer
	 *            The storage container containing the entities.
	 */
	public NodeManager(NodeStorageContainer storageContainer) {
		this.storageContainer = storageContainer;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addEntity(Node entity) {
		throw new UnsupportedOperationException();
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean exists(long id) {
		// Check if the node id exists in the index.
		try {
			storageContainer.getNodeObjectOffsetIndexReader().get(id);
			
			return true;
			
		} catch (NoSuchIndexElementException e) {
			return false;
		}
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Node getEntity(long id) {
		return storageContainer.getNodeObjectReader().get(
				storageContainer.getNodeObjectOffsetIndexReader().get(id).getValue()
		);
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReleasableIterator<Node> iterate() {
		return new ReleasableAdaptorForIterator<Node>(
				storageContainer.getNodeObjectReader().iterate());
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void modifyEntity(Node entity) {
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
