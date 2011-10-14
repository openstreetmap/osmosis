// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.database;

import java.util.Comparator;

import org.openstreetmap.osmosis.core.store.Storeable;


/**
 * A comparator for sorting database feature objects by entity id.
 * 
 * @author Brett Henderson
 * @param <T>
 *            The encapsulated feature type.
 */
public class DbFeatureComparator<T extends Storeable> implements Comparator<DbFeature<T>> {
	
	/**
	 * {@inheritDoc}
	 */
	public int compare(DbFeature<T> o1, DbFeature<T> o2) {
		long idDelta;
		
		idDelta = o1.getEntityId() - o2.getEntityId();
		
		if (idDelta < 0) {
			return -1;
		} else if (idDelta > 0) {
			return 1;
		}
		
		return 0;
	}
}
