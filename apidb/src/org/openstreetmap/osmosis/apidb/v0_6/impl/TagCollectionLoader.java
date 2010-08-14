// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.apidb.v0_6.impl;

import java.util.Collection;

import org.openstreetmap.osmosis.core.database.FeatureCollectionLoader;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;


/**
 * Loads tags from entities.
 * 
 * @param <T>
 *            The type of entity.
 */
public class TagCollectionLoader<T extends Entity> implements FeatureCollectionLoader<T, Tag> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<Tag> getFeatureCollection(T entity) {
		return entity.getTags();
	}
}
