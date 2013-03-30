// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.sort.v0_6;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.openstreetmap.osmosis.core.domain.v0_6.Entity;


/**
 * Orders entities first by their type (bound, node, way and relation), then their identifer.
 * 
 * @author Brett Henderson
 */
public class EntityByTypeThenIdComparator implements Comparator<Entity> {
	private Comparator<Entity> comparator;


	/**
	 * Creates a new instance.
	 */
	public EntityByTypeThenIdComparator() {
		List<Comparator<Entity>> entityComparators;

		// Build the sequence of entity comparisons.
		entityComparators = new ArrayList<Comparator<Entity>>();
		entityComparators.add(new EntityByTypeComparator());
		entityComparators.add(new EntityByIdComparator());

		// Combine all entity comparisons into a single logical comparison.
		comparator = new StackableComparator<Entity>(entityComparators);
	}


	/**
	 * {@inheritDoc}
	 */
	public int compare(Entity o1, Entity o2) {
		return comparator.compare(o1, o2);
	}
}
