// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.container.v0_6;

/**
 * Wraps entity objects in containers suitable for the entity type.
 * 
 * @param <T>
 *            The type of entity.
 */
public interface EntityContainerFactory<T> {
	/**
	 * Wraps the entity in a container.
	 * 
	 * @param entity
	 *            The entity to be wrapped.
	 * @return The entity container.
	 */
	EntityContainer createContainer(T entity);
}
