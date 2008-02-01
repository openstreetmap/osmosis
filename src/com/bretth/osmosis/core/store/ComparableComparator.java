// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.store;

import java.util.Comparator;


/**
 * A simple utility class for providing comparator functionality for class types
 * that are directly comparable.
 * 
 * @param <T>
 *            The class type to be compared.
 * @author Brett Henderson
 */
public class ComparableComparator<T extends Comparable<T>> implements Comparator<T> {
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compare(T o1, T o2) {
		return o1.compareTo(o2);
	}
}
