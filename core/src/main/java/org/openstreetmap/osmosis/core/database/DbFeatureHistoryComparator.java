// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.database;

import java.util.Comparator;

import org.openstreetmap.osmosis.core.store.Storeable;


/**
 * A comparator for sorting database feature objects by entity id, then entity version.
 * 
 * @param <T>
 *            The encapsulated feature type.
 */
public class DbFeatureHistoryComparator<T extends Storeable> implements Comparator<DbFeatureHistory<DbFeature<T>>> {
	
	private DbFeatureComparator<T> featureComparator = new DbFeatureComparator<T>();
	
	
	/**
	 * {@inheritDoc}
	 */
	public int compare(DbFeatureHistory<DbFeature<T>> o1, DbFeatureHistory<DbFeature<T>> o2) {
		int parentComparison;
		
		parentComparison = featureComparator.compare(o1.getFeature(), o2.getFeature());
		if (parentComparison != 0) {
			return parentComparison;
		}
		
		return o1.getVersion() - o2.getVersion();
	}
}
