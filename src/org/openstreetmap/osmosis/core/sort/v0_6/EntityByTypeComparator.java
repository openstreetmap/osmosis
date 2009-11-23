// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.sort.v0_6;

import java.util.Comparator;

import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;


/**
 * Compares two entities and sorts them by by their type (Nodes, then Segments, then Ways).
 * 
 * @author Brett Henderson
 */
public class EntityByTypeComparator implements Comparator<EntityContainer> {
	
	/**
	 * {@inheritDoc}
	 */
	public int compare(EntityContainer o1, EntityContainer o2) {
		// Perform a type comparison.
		return o1.getEntity().getType().compareTo(o2.getEntity().getType());
	}
}
