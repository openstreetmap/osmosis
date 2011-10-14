// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.sort.v0_6;

import java.util.Comparator;

import org.openstreetmap.osmosis.core.domain.v0_6.Entity;


/**
 * Allows a comparator for a specific entity type to be created.  This is necessary in some generic classes.
 * 
 * @author Brett Henderson
 *
 * @param <T>
 *            The entity type to be supported.
 */
public class EntitySubClassComparator<T extends Entity> implements Comparator<T> {

	private Comparator<Entity> comparator;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param comparator
	 *            The underlying entity comparator.
	 */
	public EntitySubClassComparator(Comparator<Entity> comparator) {
		this.comparator = comparator;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compare(T o1, T o2) {
		return comparator.compare(o1, o2);
	}

}
