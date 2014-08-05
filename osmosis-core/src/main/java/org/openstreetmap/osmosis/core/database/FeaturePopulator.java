// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.database;

import org.openstreetmap.osmosis.core.lifecycle.Releasable;


/**
 * Populates an entity with its features.
 * 
 * @param <T>  The kind of entity to be populated
 */
public interface FeaturePopulator<T> extends Releasable {
	
	/**
	 * Populates the specified entity.
	 * 
	 * @param entity
	 *            The entity to be populated.
	 */
	void populateFeatures(T entity);
}
