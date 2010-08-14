// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.sort.v0_6;

import java.util.Comparator;

import org.openstreetmap.osmosis.core.domain.v0_6.Entity;


/**
 * Compares two entities and sorts them by their version.
 * 
 * @author Brett Henderson
 */
public class EntityByVersionComparator implements Comparator<Entity> {
	
	/**
	 * {@inheritDoc}
	 */
	public int compare(Entity o1, Entity o2) {
		long verDiff;
		
		// Compare the version.
		verDiff = o1.getVersion() - o2.getVersion();
		if (verDiff > 0) {
			return 1;
		} else if (verDiff < 0) {
			return -1;
		} else {
			return 0;
		}
	}
}
