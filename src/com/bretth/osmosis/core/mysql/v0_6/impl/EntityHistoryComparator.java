// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.mysql.v0_6.impl;

import java.util.Comparator;

import com.bretth.osmosis.core.domain.v0_6.Entity;


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
