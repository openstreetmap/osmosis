// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.sort.v0_6;

import java.util.Comparator;

import org.openstreetmap.osmosis.core.container.v0_6.ChangeContainer;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;


/**
 * Allows an entity comparator to be used for making change comparisons. It extracts the two
 * entities from the changes and compares them using the underlying entity comparator.
 */
public class ChangeAsEntityComparator implements Comparator<ChangeContainer> {

	private Comparator<EntityContainer> entityComparator;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param entityComparator
	 *            The entity comparator to use for comparisons.
	 */
	public ChangeAsEntityComparator(Comparator<EntityContainer> entityComparator) {
		this.entityComparator = entityComparator;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compare(ChangeContainer o1, ChangeContainer o2) {
		return entityComparator.compare(o1.getEntityContainer(), o2.getEntityContainer());
	}
}
