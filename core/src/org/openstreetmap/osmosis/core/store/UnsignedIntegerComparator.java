// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.store;

import java.util.Comparator;


/**
 * Compares two Integers as unsigned values.
 * 
 * @author Brett Henderson
 */
public class UnsignedIntegerComparator implements Comparator<Integer> {
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compare(Integer o1, Integer o2) {
		long value1;
		long value2;
		long comparison;
		
		// Convert the two integers to longs using an unsigned conversion and
		// perform the comparison on those.
		value1 = o1.intValue() & 0xFFFFFFFFL;
		value2 = o2.intValue() & 0xFFFFFFFFL;
		
		comparison = value1 - value2;
		
		if (comparison == 0) {
			return 0;
		} else if (comparison > 0) {
			return 1;
		} else {
			return -1;
		}
	}
}
