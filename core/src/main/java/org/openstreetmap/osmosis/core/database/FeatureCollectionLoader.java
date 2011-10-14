// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.database;

import java.util.Collection;


/**
 * Retrieves feature collections from entities. This allows feature collections to be loaded in a
 * generic way without requiring knowledge of the type of feature being dealt with.
 * 
 * @param <Te>
 *            The type of entity.
 * @param <Tf>
 *            The type of feature.
 */
public interface FeatureCollectionLoader<Te, Tf> {

	/**
	 * Gets the feature collection from the entity.
	 * 
	 * @param entity
	 *            The entity containing the collection.
	 * @return The feature collection.
	 */
	Collection<Tf> getFeatureCollection(Te entity);
}
