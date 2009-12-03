// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.sort.v0_6;

import java.util.Comparator;

import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;


/**
 * Compares two entities and sorts them by their version.
 * 
 * @author Brett Henderson
 */
public class EntityByVersionComparator implements Comparator<EntityContainer> {
	
	/**
	 * {@inheritDoc}
	 */
	public int compare(EntityContainer o1, EntityContainer o2) {
		long verDiff;
		
		// Compare the version.
		verDiff = o1.getEntity().getVersion() - o2.getEntity().getVersion();
		if (verDiff > 0) {
			return 1;
		} else if (verDiff < 0) {
			return -1;
		} else {
			return 0;
		}
	}
}
