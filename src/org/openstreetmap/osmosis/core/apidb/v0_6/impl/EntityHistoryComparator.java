// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.apidb.v0_6.impl;

import java.util.Comparator;

import org.openstreetmap.osmosis.core.domain.v0_6.Entity;


/**
 * A comparator for ordering entity history records by version.
 * 
 * @author Brett Henderson
 * @param <T>
 *            The data type to be compared.
 */
public class EntityHistoryComparator<T extends Entity> implements Comparator<EntityHistory<T>> {
	
	/**
	 * {@inheritDoc}
	 */
	public int compare(EntityHistory<T> o1, EntityHistory<T> o2) {
		return o1.getEntity().getVersion() - o2.getEntity().getVersion();
	}
}
