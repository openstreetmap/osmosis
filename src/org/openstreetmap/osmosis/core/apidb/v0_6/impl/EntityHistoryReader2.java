// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.apidb.v0_6.impl;

import java.util.List;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.container.v0_6.ChangeContainer;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainerFactory;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableContainer;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;
import org.openstreetmap.osmosis.core.task.common.ChangeAction;


/**
 * Provides a single iterator based on data provided by underlying iterators from each of the
 * underlying entity and feature iterators. Each underlying iterator provides one component of the
 * overall entity.
 * 
 * @param <T>
 *            The type of entity provided by this iterator.
 */
public class EntityHistoryReader2<T extends Entity> implements ReleasableIterator<ChangeContainer> {

	private ReleasableContainer releasableContainer;
	private ReleasableIterator<EntityHistory<T>> entityIterator;
	private FeatureHistoryPopulator<T, Tag> tagPopulator;
	private List<FeatureHistoryPopulator<T, ?>> featurePopulators;
	private ChangeContainer nextValue;
	private EntityContainerFactory<T> containerFactory;


	/**
	 * Creates a new instance.
	 * 
	 * @param entityIterator
	 *            The entity source.
	 * @param tagIterator
	 *            The tag source.
	 * @param featurePopulators
	 *            Populators to add entity specific features to the generated entities.
	 * @param containerFactory
	 *            The factory for wrapping entity objects into containers.
	 */
	public EntityHistoryReader2(ReleasableIterator<EntityHistory<T>> entityIterator,
			ReleasableIterator<DbFeatureHistory<DbFeature<Tag>>> tagIterator,
			List<FeatureHistoryPopulator<T, ?>> featurePopulators,
			EntityContainerFactory<T> containerFactory) {
		
		releasableContainer = new ReleasableContainer();
		
		this.entityIterator = releasableContainer.add(entityIterator);
		tagPopulator = releasableContainer.add(new FeatureHistoryPopulator<T, Tag>(tagIterator,
				new TagCollectionLoader<T>()));
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
		for (FeatureHistoryPopulator<T, ?> populator : featurePopulators) {
			populator.populateFeatures(entity);
		}
		
		return entityHistory;
	}
	
	
	/**
	 * Reads the history of the next entity and builds a change object.
	 */
	private ChangeContainer readChange() {
		boolean createdPreviously;
		EntityHistory<T> mostRecentHistory;
		T entity;
		EntityContainer entityContainer;
		
		// Check the first entity, if it has a version greater than 1 the entity
		// existed prior to the interval beginning and therefore cannot be a
		// create.
		mostRecentHistory = readNextEntityHistory();
		entity = mostRecentHistory.getEntity();
		createdPreviously = (entity.getVersion() > 1);
		
		// The entity in the result must be wrapped in a container.
		entityContainer = containerFactory.createContainer(entity);
		
		// The entity has been modified if it is visible and was created previously.
		// It is a create if it is visible and was NOT created previously.
		// It is a delete if it is NOT visible and was created previously.
		// No action if it is NOT visible and was NOT created previously.
		if (mostRecentHistory.isVisible() && createdPreviously) {
			return new ChangeContainer(entityContainer, ChangeAction.Modify);
		} else if (mostRecentHistory.isVisible() && !createdPreviously) {
			return new ChangeContainer(entityContainer, ChangeAction.Create);
		} else if (!mostRecentHistory.isVisible() && createdPreviously) {
			return new ChangeContainer(entityContainer, ChangeAction.Delete);
		} else {
			return null;
		}
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasNext() {
		while (nextValue == null && entityIterator.hasNext()) {
			nextValue = readChange();
		}
		
		return (nextValue != null);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public ChangeContainer next() {
		ChangeContainer result;
		
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
