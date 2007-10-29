package com.bretth.osmosis.core.filter.common;

import java.util.BitSet;

import com.bretth.osmosis.core.OsmosisRuntimeException;


/**
 * Implements the IdTracker interface using an internal JDK BitSet. The current
 * implementation only supports 31 bit numbers, but will be enhanced if and when
 * required.
 * 
 * @author Brett Henderson
 */
public class BitSetIdTracker implements IdTracker {
	private BitSet positiveSet;
	private BitSet negativeSet;
	
	
	/**
	 * Creates a new instance.
	 */
	public BitSetIdTracker() {
		positiveSet = new BitSet();
		negativeSet = new BitSet();
	}
	
	
	/**
	 * Converts the specified long to an int and verifies that it is legal.
	 * 
	 * @param value
	 *            The identifier to be converted.
	 * @return The integer representation of the id.
	 */
	private int longToInt(long value) {
		// Verify that the bit can be safely cast to an integer.
		if (value > Integer.MAX_VALUE) {
			throw new OsmosisRuntimeException("Requested value " + value + " exceeds the maximum supported size of " + Integer.MAX_VALUE + ".");
		}
		
		return (int) value;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void set(long id) {
		BitSet activeSet;
		long absoluteId;
		int activeIndex;
		
		if (id >= 0) {
			activeSet = positiveSet;
			absoluteId = id;
		} else {
			activeSet = negativeSet;
			absoluteId = id * -1;
		}
		
		activeIndex = longToInt(absoluteId);
		
		activeSet.set(activeIndex);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public boolean get(long id) {
		BitSet activeSet;
		long absoluteBit;
		int activeBit;
		
		if (id >= 0) {
			activeSet = positiveSet;
			absoluteBit = id;
		} else {
			activeSet = negativeSet;
			absoluteBit = id * -1;
		}
		
		activeBit = longToInt(absoluteBit);
		
		return activeSet.get(activeBit);
	}
}
