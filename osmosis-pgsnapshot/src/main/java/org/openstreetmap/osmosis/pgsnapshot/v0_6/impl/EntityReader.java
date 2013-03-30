// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsnapshot.v0_6.impl;

import java.util.List;
import java.util.NoSuchElementException;

import org.openstreetmap.osmosis.core.database.FeaturePopulator;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableContainer;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;


/**
 * Provides a single iterator based on data provided by underlying iterators from each of the
 * underlying entity and feature iterators. Each underlying iterator provides one component of the
 * overall entity.
 * 
 * @param <T>
 *            The type of entity provided by this iterator.
 */
public class EntityReader<T extends Entity> implements ReleasableIterator<T> {
	
	private boolean nextValueLoaded;
	private ReleasableContainer releasableContainer;
	private ReleasableIterator<T> entityIterator;
	private List<FeaturePopulator<T>> featurePopulators;
	private T nextValue;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param entityIterator
	 *            The entity source.
	 * @param featurePopulators
	 *            Populators to add entity specific features to the generated entities.
	 */
	public EntityReader(ReleasableIterator<T> entityIterator, List<FeaturePopulator<T>> featurePopulators) {
		releasableContainer = new ReleasableContainer();
		
		this.entityIterator = releasableContainer.add(entityIterator);
		for (FeaturePopulator<T> featurePopulator : featurePopulators) {
			releasableContainer.add(featurePopulator);
		}
		this.featurePopulators = featurePopulators;
	}
	
	
	/**
	 * Consolidates the output of all readers so that entities are fully
	 * populated.
	 * 
	 * @return An entity record where the entity is fully populated.
	 */
	private T readNextEntity() {
		T entity;
		
		entity = entityIterator.next();
		
		// Add entity type specific features to the entity.
		for (FeaturePopulator<T> populator : featurePopulators) {
			populator.populateFeatures(entity);
		}
		
		return entity;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public boolean hasNext() {
		if (!nextValueLoaded && entityIterator.hasNext()) {
			nextValue = readNextEntity();
			nextValueLoaded = true;
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
		releasableContainer.release();
	}
}
