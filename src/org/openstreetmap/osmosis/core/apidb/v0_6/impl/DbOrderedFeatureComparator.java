// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.apidb.v0_6.impl;

import java.util.Comparator;

import org.openstreetmap.osmosis.core.store.Storeable;


/**
 * Compares way nodes to allow them to be sorted by way id then sequence
 * number.
 * 
 * @author Brett Henderson
 * @param <T>
 *            The encapsulated feature type.
 */
public class DbOrderedFeatureComparator<T extends Storeable> implements Comparator<DbOrderedFeature<T>> {
	
	/**
	 * {@inheritDoc}
	 */
	public int compare(DbOrderedFeature<T> o1, DbOrderedFeature<T> o2) {
		long way1Id;
		long way2Id;
		
		way1Id = o1.getEntityId();
		way2Id = o2.getEntityId();
		if (way1Id != way2Id) {
			if (way1Id < way2Id) {
				return -1;
			} else {
				return 1;
			}
		}
		
		return o1.getSequenceId() - o2.getSequenceId();
	}
}
