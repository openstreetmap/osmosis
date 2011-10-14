// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.apidb.v0_6.impl;

import java.util.List;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.database.DbFeature;
import org.openstreetmap.osmosis.core.database.DbFeatureHistory;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
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
public class EntityHistoryReader<T extends Entity> implements ReleasableIterator<EntityHistory<T>> {

	private ReleasableContainer releasableContainer;
	private ReleasableIterator<EntityHistory<T>> entityIterator;
	private FeatureHistoryPopulator<T, Tag, ?> tagPopulator;
	private List<FeatureHistoryPopulator<T, ?, ?>> featurePopulators;
	private EntityHistory<T> nextValue;


	/**
	 * Creates a new instance.
	 * 
	 * @param entityIterator
	 *            The entity source.
	 * @param tagIterator
	 *            The tag source.
	 * @param featurePopulators
	 *            Populators to add entity specific features to the generated entities.
	 */
	public EntityHistoryReader(ReleasableIterator<EntityHistory<T>> entityIterator,
			ReleasableIterator<DbFeatureHistory<DbFeature<Tag>>> tagIterator,
			List<FeatureHistoryPopulator<T, ?, ?>> featurePopulators) {
		
		releasableContainer = new ReleasableContainer();
		
		this.entityIterator = releasableContainer.add(entityIterator);
		tagPopulator = releasableContainer.add(new FeatureHistoryPopulator<T, Tag, DbFeature<Tag>>(tagIterator,
				new TagCollectionLoader<T>()));
		for (FeatureHistoryPopulator<T, ?, ?> featurePopulator : featurePopulators) {
			releasableContainer.add(featurePopulator);
		}
		this.featurePopulators = featurePopulators;
	}
	
	
	/**
	 * Consolidates the output of all history readers so that entities are fully
	 * populated.
	 * 
	 * @return An entity history record where the entity is fully populated.
	 */
	private EntityHistory<T> readNextEntityHistory() {
		EntityHistory<T> entityHistory;
		T entity;
		
		entityHistory = entityIterator.next();
		entity = entityHistory.getEntity();
		
		// Add all applicable tags to the entity.
		tagPopulator.populateFeatures(entity);
		
		// Add entity type specific features to the entity.
		for (FeatureHistoryPopulator<T, ?, ?> populator : featurePopulators) {
			populator.populateFeatures(entity);
		}
		
		return entityHistory;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasNext() {
		while (nextValue == null && entityIterator.hasNext()) {
			nextValue = readNextEntityHistory();
		}
		
		return (nextValue != null);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public EntityHistory<T> next() {
		EntityHistory<T> result;
		
		if (!hasNext()) {
			throw new OsmosisRuntimeException("No records are available, call hasNext first.");
		}
		
		result = nextValue;
		nextValue = null;
		
		return result;
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
		releasableContainer.release();
	}
}
