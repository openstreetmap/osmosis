// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.filter.common;

import java.util.BitSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.bretth.osmosis.core.OsmosisRuntimeException;


/**
 * Implements the IdTracker interface using an internal JDK BitSet. The current
 * implementation only supports 31 bit numbers, but will be enhanced if and when
 * required.
 * 
 * @author Brett Henderson
 */
public class BitSetIdTracker implements IdTracker {
	/* package */ BitSet positiveSet;
	/* package */ ListIdTracker negativeSet;
	
	
	/**
	 * Creates a new instance.
	 */
	public BitSetIdTracker() {
		positiveSet = new BitSet();
		negativeSet = new ListIdTracker();
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
		int intId;
		
		intId = longToInt(id);
		
		if (intId >= 0) {
			positiveSet.set(intId);
		} else {
			negativeSet.set(intId);
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public boolean get(long id) {
		int intId;
		
		intId = longToInt(id);
		
		if (intId >= 0) {
			return positiveSet.get(intId);
		} else {
			return negativeSet.get(intId);
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<Long> iterator() {
		return new IdIterator();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setAll(IdTracker idTracker) {
		for (Long id : idTracker) {
			set(id);
		}
	}
	
	
	/**
	 * The iterator implementation for providing access to the list of ids.
	 * 
	 * @author Brett Henderson
	 */
	private class IdIterator implements Iterator<Long> {
		
		/**
		 * Tracks whether we're currently reading positive or negative bitsets.
		 */
		private boolean readingPositive;
		private long nextId;
		private boolean nextIdAvailable;
		private Iterator<Long> negativeIterator;
		/**
		 * The current bit offset in the positive bitset.
		 */
		private int positiveOffset;
		
		
		/**
		 * Creates a new instance.
		 */
		public IdIterator() {
			readingPositive = false;
			nextIdAvailable = false;
			
			positiveOffset = 0;
		}
		
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean hasNext() {
			if (!nextIdAvailable) {
				if (!readingPositive) {
					// Create a negative set iterator if one doesn't already exist.
					if (negativeIterator == null) {
						negativeIterator = negativeSet.iterator();
					}
					
					// Get data from the negative iterator if available, if not
					// available switch to positive reading.
					if (negativeIterator.hasNext()) {
						nextId = negativeIterator.next();
						nextIdAvailable = true;
					} else {
						negativeIterator = null;
						readingPositive = true;
					}
				}
				
				if (readingPositive) {
					int nextBitOffset;
					
					nextBitOffset = positiveSet.nextSetBit(positiveOffset);
					
					if (nextBitOffset >= 0) {
						nextId = nextBitOffset;
						nextIdAvailable = true;
						positiveOffset = nextBitOffset + 1;
					}
				}
			}
			
			return nextIdAvailable;
		}
		
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public Long next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			
			nextIdAvailable = false;
			
			return (long) nextId;
		}
		
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}
