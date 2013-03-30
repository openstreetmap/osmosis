// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.filter.common;

import java.util.Iterator;


/**
 * Stores the data for a single id range within the dynamic id tracker. It selects between the most
 * appropriate id tracker implementation depending on id density.
 * 
 * @author Brett Henderson
 */
public class DynamicIdTrackerSegment implements Comparable<DynamicIdTrackerSegment>, Iterable<Long> {
	private static final int BITSET_THRESHOLD = DynamicIdTracker.SEGMENT_SIZE / 32;
	
	private long base;
	private char setCount;
	private IdTracker idTracker;
	private boolean bitsetEnabled;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param base
	 *            The minimum value represented by this segment.
	 */
	public DynamicIdTrackerSegment(long base) {
		this.base = base;
		
		idTracker = new ListIdTracker();
		bitsetEnabled = false;
	}
	
	
	/**
	 * Gets the base.
	 * 
	 * @return The base.
	 */
	public long getBase() {
		return base;
	}
	
	
	/**
	 * Gets the number of set ids in this segment.
	 * 
	 * @return The set count.
	 */
	public long getSetCount() {
		return setCount;
	}
	

	/**
	 * Checks whether the specified id is active.
	 * 
	 * @param id
	 *            The identifier to be checked.
	 * @return True if the identifier is active, false otherwise.
	 */
	public boolean get(long id) {
		return idTracker.get(id);
	}

	
	/**
	 * Marks the specified id as active.
	 * 
	 * @param id
	 *            The identifier to be flagged.
	 */
	public void set(long id) {
		if (!idTracker.get(id)) {
			idTracker.set(id);
			setCount++;
			
			// If the count threshold has been exceeded, switch to a bitset implementation.
			if (!bitsetEnabled && setCount > BITSET_THRESHOLD) {
				IdTracker bitsetIdTracker;
				
				bitsetIdTracker = new BitSetIdTracker();
				bitsetIdTracker.setAll(idTracker);
				
				idTracker = bitsetIdTracker;
				bitsetEnabled = true;
			}
		}
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compareTo(DynamicIdTrackerSegment o) {
		long result;
		
		result = base - o.base;
		
		if (result == 0) {
			return 0;
		} else if (result > 0) {
			return 1;
		} else {
			return -1;
		}
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<Long> iterator() {
		return idTracker.iterator();
	}
}
