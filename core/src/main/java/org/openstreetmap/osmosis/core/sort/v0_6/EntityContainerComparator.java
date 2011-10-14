// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.sort.v0_6;

import java.util.Comparator;

import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;


/**
 * An entity container comparator utilising an inner entity comparator. This allows entity
 * containers to be directly compared instead of having to extract the entities first.
 * 
 * @author Brett Henderson
 */
public class EntityContainerComparator implements Comparator<EntityContainer> {

	private Comparator<Entity> entityComparator;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param entityComparator
	 *            The comparator to use for comparing the contained entities.
	 */
	public EntityContainerComparator(Comparator<Entity> entityComparator) {
		this.entityComparator = entityComparator;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compare(EntityContainer o1, EntityContainer o2) {
		return entityComparator.compare(o1.getEntity(), o2.getEntity());
	}

}
