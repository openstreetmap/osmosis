// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.sort.v0_6;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


/**
 * A comparator implementation that allows multiple comparators to be combined together. It invokes
 * each comparator in order, and returns the first non-zero result. If all comparators return 0, the
 * result is 0.
 * 
 * @param <T>
 *            The type of data to be compared.
 */
public class StackableComparator<T> implements Comparator<T> {

	private List<Comparator<T>> comparators;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param comparators
	 *            The comparators to use for comparisons.
	 */
	public StackableComparator(List<Comparator<T>> comparators) {
		this.comparators = new ArrayList<Comparator<T>>(comparators);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compare(T o1, T o2) {
		// Compare using each comparator in turn. Stop if a comparator detects a difference and
		// return the result.
		for (Comparator<T> comparator : comparators) {
			int result;
			
			result = comparator.compare(o1, o2);
			
			if (result != 0) {
				return result;
			}
		}
		
		return 0;
	}
}
