// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.database;

import java.util.Comparator;

import org.openstreetmap.osmosis.core.store.Storeable;

/**
 * A comparator for sorting database feature objects by entity id, then entity version, then feature sequence.
 * 
 * @param <T>
 *            The encapsulated feature type.
 */
public class DbOrderedFeatureHistoryComparator<T extends Storeable> implements
		Comparator<DbFeatureHistory<DbOrderedFeature<T>>> {
	
	private DbFeatureComparator<T> featureComparator = new DbFeatureComparator<T>();
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compare(DbFeatureHistory<DbOrderedFeature<T>> o1,
			DbFeatureHistory<DbOrderedFeature<T>> o2) {
		int parentComparison;
		int versionDelta;
		
		parentComparison = featureComparator.compare(o1.getFeature(), o2.getFeature());
		if (parentComparison != 0) {
			return parentComparison;
		}
		
		versionDelta = o1.getVersion() - o2.getVersion();
		if (versionDelta != 0) {
			return versionDelta;
		}
		
		return o1.getFeature().getSequenceId() - o2.getFeature().getSequenceId();
	}
}
