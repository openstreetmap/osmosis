// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.sort.v0_6;

import java.util.Comparator;

import org.openstreetmap.osmosis.core.domain.v0_6.Entity;


/**
 * Compares two entities and sorts them by by their type (Nodes, then Segments, then Ways).
 * 
 * @author Brett Henderson
 */
public class EntityByTypeComparator implements Comparator<Entity> {
	
	/**
	 * {@inheritDoc}
	 */
	public int compare(Entity o1, Entity o2) {
		// Perform a type comparison.
		return o1.getType().compareTo(o2.getType());
	}
}
