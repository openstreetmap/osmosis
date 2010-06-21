// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.apidb.v0_6.impl;

import java.util.NoSuchElementException;

import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainerFactory;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;

/**
 * Wraps a stream of entity history objects into entity containers.  Only visible items will be returned.
 * 
 * @param <T>
 *            The type of entity provided by this iterator.
 */
public class EntityContainerReader<T extends Entity> implements ReleasableIterator<EntityContainer> {

	private ReleasableIterator<EntityHistory<T>> source;
	private EntityContainerFactory<T> containerFactory;
	private EntityContainer nextValue;
	private boolean nextValueLoaded;


	/**
	 * Creates a new instance.
	 * 
	 * @param source
	 *            The entity history source.
	 * @param containerFactory
	 *            The factory for wrapping entity objects into containers.
	 */
	public EntityContainerReader(
			ReleasableIterator<EntityHistory<T>> source, EntityContainerFactory<T> containerFactory) {
		this.source = source;
		this.containerFactory = containerFactory;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasNext() {
		while (!nextValueLoaded && source.hasNext()) {
			T entity;
			
			// Get the entity from the underlying source.
			entity = source.next().getEntity();
			
			// Wrap the entity in a container.
			nextValue = containerFactory.createContainer(entity);
			
			nextValueLoaded = true;
		}
		
		return nextValueLoaded;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
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
