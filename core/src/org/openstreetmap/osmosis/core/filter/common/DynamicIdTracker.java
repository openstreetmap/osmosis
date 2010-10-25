// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.filter.common;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;


/**
 * Implements the IdTracker interface using a combination of BitSet and ListId trackers. It breaks
 * the overall address space into segments and automatically switches between implementations for each
 * chunk depending on which is more efficient.
 * 
 * @author Brett Henderson
 */
public class DynamicIdTracker implements IdTracker {
	/**
	 * Defines the number of ids managed by a single segment.
	 */
	/* package */ static final int SEGMENT_SIZE = 1024;
	
	private List<DynamicIdTrackerSegment> segments;
	
	
	/**
	 * Creates a new instance.
	 */
	public DynamicIdTracker() {
		segments = new ArrayList<DynamicIdTrackerSegment>();
	}
	
	
	private int calculateOffset(long id) {
		int offset;
		
		// A long modulo an integer is an integer.
		offset = (int) (id % SEGMENT_SIZE);
		
		// If the number is negative, we need to shift the number relative to the base of the
		// segment.
		if (offset < 0) {
			offset = SEGMENT_SIZE + offset;
		}
		
		return offset;
	}
	
	
	private DynamicIdTrackerSegment getSegment(long base, boolean createIfMissing) {
		int intervalBegin;
		int intervalEnd;
		DynamicIdTrackerSegment segment;
		
		segment = null;
		
		// Perform a binary search splitting the list in half each time until
		// the requested id is confirmed as existing or not.
		intervalBegin = 0;
		intervalEnd = segments.size();
		for (boolean searchComplete = false; !searchComplete;) {
			int intervalSize;
			long currentBase;
			DynamicIdTrackerSegment currentSegment;
			
			// Calculate the interval size.
			intervalSize = intervalEnd - intervalBegin;
			
			// If no elements exist, we have no search to perform. If elements do exist then divide
			// and conquer while the size is large, then commence linear search once the size is
			// small.
			if (intervalSize == 0) {
				if (createIfMissing) {
					segment = new DynamicIdTrackerSegment(base);
					segments.add(intervalBegin, segment);
				}
				
				searchComplete = true;
				
			} else if (intervalSize >= 2) {
				int intervalMid;
				
				// Split the interval in two.
				intervalMid = intervalSize / 2 + intervalBegin;
				
				// Check whether the midpoint id is above or below the id
				// required.
				currentSegment = segments.get(intervalMid);
				currentBase = currentSegment.getBase();
				if (currentBase == base) {
					segment = currentSegment;
					searchComplete = true;
				} else if (currentBase < base) {
					intervalBegin = intervalMid + 1;
				} else {
					intervalEnd = intervalMid;
				}
				
			} else {
				// Iterate through the entire interval.
				for (; intervalBegin < intervalEnd; intervalBegin++) {
					
					// Check if the current offset contains the id required.
					currentSegment = segments.get(intervalBegin);
					currentBase = currentSegment.getBase();
					
					if (currentBase == base) {
						segment = currentSegment;
						break;
						
					} else if (currentBase > base) {
						// The requested segment should exist prior to the current segment.
						if (createIfMissing) {
							segment = new DynamicIdTrackerSegment(base);
							segments.add(intervalBegin, segment);
						}
						break;
					}
				}
				
				// The requested base is at the end of this interval where intervalBegin is currently pointing.
				if (segment == null && createIfMissing) {
					segment = new DynamicIdTrackerSegment(base);
					segments.add(intervalBegin, segment);
				}
				
				searchComplete = true;
			}
		}
		
		return segment;
	}
	

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean get(long id) {
		int offset;
		long base;
		DynamicIdTrackerSegment segment;
		
		offset = calculateOffset(id);
		base = id - offset;
		segment = getSegment(base, false);
		
		if (segment != null) {
			return segment.get(offset);
		} else {
			return false;
		}
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void set(long id) {
		int offset;
		long base;
		DynamicIdTrackerSegment segment;
		
		offset = calculateOffset(id);
		base = id - offset;
		segment = getSegment(base, true);
		
		segment.set(offset);
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
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<Long> iterator() {
		return new SegmentIdIterator(segments.iterator());
	}
	
	
	private static class SegmentIdIterator implements Iterator<Long> {
		private Iterator<DynamicIdTrackerSegment> segments;
		private Iterator<Long> currentSegmentIds;
		private long currentSegmentBase;
		
		
		/**
		 * Creates a new instance.
		 * 
		 * @param segments
		 *            The segments to iterate over.
		 */
		public SegmentIdIterator(Iterator<DynamicIdTrackerSegment> segments) {
			this.segments = segments;
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean hasNext() {
			for (;;) {
				if (currentSegmentIds == null) {
					if (segments.hasNext()) {
						DynamicIdTrackerSegment segment = segments.next();
						
						currentSegmentIds = segment.iterator();
						currentSegmentBase = segment.getBase();
					} else {
						return false;
					}
				}
				
				if (currentSegmentIds.hasNext()) {
					return true;
				} else {
					currentSegmentIds = null;
				}
			}
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		public Long next() {
			if (hasNext()) {
				return currentSegmentIds.next() + currentSegmentBase;
				
			} else {
				throw new NoSuchElementException();
			}
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
