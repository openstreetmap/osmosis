// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.apidb.v0_6.impl;

import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;
import org.openstreetmap.osmosis.core.store.PeekableIterator;
import org.openstreetmap.osmosis.core.store.Storeable;


/**
 * Populates entities with their features using a sorted data source.
 * 
 * @param <Te>
 *            The type of entity to be populated.
 * @param <Tf>
 *            The type of feature to be added.
 */
public class FeatureHistoryPopulator<Te extends Entity, Tf extends Storeable> implements FeaturePopulator<Te> {
	
	private PeekableIterator<DbFeatureHistory<DbFeature<Tf>>> source;
	private FeatureCollectionLoader<Te, Tf> featureLoader;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param source
	 *            The feature source.
	 * @param featureLoader
	 *            Provides access to the feature collection within the entity.
	 */
	public FeatureHistoryPopulator(ReleasableIterator<DbFeatureHistory<DbFeature<Tf>>> source,
			FeatureCollectionLoader<Te, Tf> featureLoader) {
		this.source = new PeekableIterator<DbFeatureHistory<DbFeature<Tf>>>(source);
		this.featureLoader = featureLoader;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void populateFeatures(Te entity) {
		// Add all applicable tags to the entity.
		while (source.hasNext()
				&& source.peekNext().getDbFeature().getEntityId() == entity.getId()
				&& source.peekNext().getVersion() == entity.getVersion()) {
			featureLoader.getFeatureCollection(entity).add(source.next().getDbFeature().getFeature());
		}
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void release() {
		source.release();
	}
}
