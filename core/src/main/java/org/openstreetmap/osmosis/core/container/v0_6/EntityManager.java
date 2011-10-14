// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.container.v0_6;

import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;


/**
 * Defines the dataset methods available for manipulating entities.
 * 
 * @author Brett Henderson
 * @param <T>
 *            The entity type to be supported.
 */
public interface EntityManager<T extends Entity> {
	
	/**
	 * Retrieves an entity by its identifier.
	 * 
	 * @param id
	 *            The id of the entity.
	 * @return The entity.
	 */
	T getEntity(long id);
	
	
	/**
	 * Returns an iterator providing access to all entities in the database.
	 * 
	 * @return The entity iterator.
	 */
	ReleasableIterator<T> iterate();
	
	
	/**
	 * Indicates if the specified entity exists in the database.
	 * 
	 * @param id
	 *            The id of the entity.
	 * @return True if the entity exists, false otherwise.
	 */
	boolean exists(long id);
	
	
	/**
	 * Adds the specified entity to the database.
	 * 
	 * @param entity
	 *            The entity to add.
	 */
	void addEntity(T entity);
	
	
	/**
	 * Updates the specified entity details in the database.
	 * 
	 * @param entity
	 *            The entity to update.
	 */
	void modifyEntity(T entity);
	
	
	/**
	 * Removes the specified entity from the database.
	 * 
	 * @param entityId
	 *            The id of the entity to remove.
	 */
	void removeEntity(long entityId);
}
