// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.apidb.v0_6.impl;

import org.openstreetmap.osmosis.core.container.v0_6.ChangeContainer;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainerFactory;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;
import org.openstreetmap.osmosis.core.task.common.ChangeAction;


/**
 * Creates change records based on the data provided by an underlying entity history iterator.
 * 
 * @param <T>
 *            The type of entity provided by this iterator.
 */
public class ChangeReader<T extends Entity> implements ReleasableIterator<ChangeContainer> {

	private ReleasableIterator<EntityHistory<T>> source;
	private EntityContainerFactory<T> containerFactory;


	/**
	 * Creates a new instance.
	 * 
	 * @param source
	 *            The entity history source.
	 * @param containerFactory
	 *            The factory for wrapping entity objects into containers.
	 */
	public ChangeReader(ReleasableIterator<EntityHistory<T>> source, EntityContainerFactory<T> containerFactory) {
		this.source = source;
		this.containerFactory = containerFactory;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasNext() {
		return source.hasNext();
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public ChangeContainer next() {
		EntityHistory<T> entityHistory;
		T entity;
		EntityContainer entityContainer;
		boolean createdPreviously;
		
		// Get the entity from the underlying source.
		entityHistory = source.next();
		entity = entityHistory.getEntity();
		
		// Wrap the entity in a container.
		entityContainer = containerFactory.createContainer(entity);
		
		// This is only a create if the version is 1.
		createdPreviously = (entityHistory.getEntity().getVersion() > 1);
		
		// The entity has been modified if it is visible and was created previously.
		// It is a create if it is visible and was NOT created previously.
		// It is a delete if it is NOT visible and was created previously.
		// No action if it is NOT visible and was NOT created previously.
		if (entityHistory.isVisible() && createdPreviously) {
			return new ChangeContainer(entityContainer, ChangeAction.Modify);
		} else if (entityHistory.isVisible() && !createdPreviously) {
			return new ChangeContainer(entityContainer, ChangeAction.Create);
		} else if (!entityHistory.isVisible() && createdPreviously) {
			return new ChangeContainer(entityContainer, ChangeAction.Delete);
		} else {
			// This is an unusual case in that an initial version has been marked as not visible.
			// The production database contains many examples of this, presumably due to the original
			// TIGER import not being deleted properly.
			return new ChangeContainer(entityContainer, ChangeAction.Delete);
		}
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
		source.release();
	}
}
