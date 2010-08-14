// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.sort.v0_6;

import java.util.Comparator;

import org.openstreetmap.osmosis.core.domain.v0_6.Entity;


/**
 * Compares two entities and sorts them by their identifier.
 * 
 * @author Brett Henderson
 */
public class EntityByIdComparator implements Comparator<Entity> {
	
	/**
	 * {@inheritDoc}
	 */
	public int compare(Entity o1, Entity o2) {
		long idDiff;
        
		// Perform an identifier comparison.
		idDiff = o1.getId() - o2.getId();
		if (idDiff > 0) {
			return 1;
		} else if (idDiff < 0) {
			return -1;
		} else {
			return 0;
		}
	}
}
