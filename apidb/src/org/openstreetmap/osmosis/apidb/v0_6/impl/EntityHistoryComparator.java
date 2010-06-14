// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.apidb.v0_6.impl;

import java.util.Comparator;

import org.openstreetmap.osmosis.core.domain.v0_6.Entity;


/**
 * A comparator for sorting entity history objects by entity id then entity version.
 * 
 * @param <T>
 *            The type of entity being sorted.
 */
public class EntityHistoryComparator<T extends Entity> implements Comparator<EntityHistory<T>> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compare(EntityHistory<T> o1, EntityHistory<T> o2) {
		long idDelta;
		int versionDelta;
		
		idDelta = o1.getEntity().getId() - o2.getEntity().getId();
		
		if (idDelta < 0) {
			return -1;
		} else if (idDelta > 0) {
			return 1;
		}
		
		versionDelta = o1.getEntity().getVersion() - o2.getEntity().getVersion();
		
		if (versionDelta < 0) {
			return -1;
		} else if (versionDelta > 0) {
			return 1;
		}
		
		return 0;
	}
}
