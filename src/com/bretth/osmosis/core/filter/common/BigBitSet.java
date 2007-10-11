package com.bretth.osmosis.core.filter.common;

import java.util.BitSet;

import com.bretth.osmosis.core.OsmosisRuntimeException;


/**
 * Provides similar BitSet functionality to the JDK BitSet, but allows long and
 * negative arguments. The current implementation only supports 31 bit numbers,
 * but will be enhanced if and when required.
 * 
 * @author Brett Henderson
 */
public class BigBitSet {
	private BitSet positiveSet;
	private BitSet negativeSet;
	
	
	/**
	 * Creates a new instance.
	 */
	public BigBitSet() {
		positiveSet = new BitSet();
		negativeSet = new BitSet();
	}
	
	
	/**
	 * Updates the value of the specified bit.
	 * 
	 * @param bit
	 *            The bit to be updated.
	 * @param value
	 *            The new bit value.
	 */
	public void set(long bit, boolean value) {
		BitSet activeSet;
		long absoluteBit;
		int activeBit;
		
		if (bit >= 0) {
			activeSet = positiveSet;
			absoluteBit = bit;
		} else {
			activeSet = negativeSet;
			absoluteBit = bit * -1;
		}
		
		// Verify that the bit can be safely cast to an integer.
		if (absoluteBit > Integer.MAX_VALUE) {
			throw new OsmosisRuntimeException("Requested bit value " + bit + " exceeds the maximum supported bit size of " + Integer.MAX_VALUE + ".");
		}
		
		activeBit = (int) absoluteBit;
		
		activeSet.set(activeBit, value);
	}
	
	
	/**
	 * Sets the specified bit to true.
	 * 
	 * @param bit
	 *            The bit to be updated.
	 */
	public void set(long bit) {
		set(bit, true);
	}
	
	
	/**
	 * Returns the specified bit value.
	 * 
	 * @param bit
	 *            The bit to be returned.
	 * @return True if the bit is set.
	 */
	public boolean get(long bit) {
		BitSet activeSet;
		long absoluteBit;
		int activeBit;
		
		if (bit >= 0) {
			activeSet = positiveSet;
			absoluteBit = bit;
		} else {
			activeSet = negativeSet;
			absoluteBit = bit * -1;
		}
		
		// Verify that the bit can be safely cast to an integer.
		if (absoluteBit > Integer.MAX_VALUE) {
			throw new OsmosisRuntimeException("Requested bit value " + bit + " exceeds the maximum supported bit size of " + Integer.MAX_VALUE + ".");
		}
		
		activeBit = (int) absoluteBit;
		
		return activeSet.get(activeBit);
	}
}
