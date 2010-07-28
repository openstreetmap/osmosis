// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.filter.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.openstreetmap.osmosis.core.util.LongAsInt;


/**
 * Implements the IdTracker interface using a list of ids. The current
 * implementation only supports 31 bit numbers, but will be enhanced if and when
 * required.
 * 
 * @author Brett Henderson
 */
public class ListIdTracker implements IdTracker {
	/**
	 * The internal list size is multiplied by this factor when more space is
	 * required.
	 */
	private static final double LIST_SIZE_EXTENSION_FACTOR = 1.5;
	
	
	/**
	 * This is the main list of id values. It is allocated in chunks and the
	 * maximum valid offset is defined by idOffset.
	 */
	/* package */ int[] idList;
	/**
	 * Flags where the maximum written id offset occurs in the list. If new
	 * values are added and the list is full, new space must be allocated.
	 */
	/* package */ int idOffset;
	private int maxIdAdded;
	private boolean sorted;
	
	
	/**
	 * Creates a new instance.
	 */
	public ListIdTracker() {
		// This is being initialised with a very small array size because this class is now also
		// used within the DynamicIdTracker which relies on the ListIdTracker being efficient for
		// small amounts of data.
		// When storing large amounts of data, the overhead of extending the array more often in the
		// early stages is relatively small compared to the overall processing. For small amounts of
		// data, the size efficiency is most important.
		idList = new int[1];
		idOffset = 0;
		maxIdAdded = Integer.MIN_VALUE;
		sorted = true;
	}
	
	
	/**
	 * Increases the size of the id list to make space for new ids.
	 */
	private void extendIdList() {
		int[] newIdList;
		int newListLength;
		
		newListLength = (int) (idList.length * LIST_SIZE_EXTENSION_FACTOR);
		if (newListLength == idList.length) {
			newListLength++;
		}
		
		newIdList = new int[newListLength];
		
		System.arraycopy(idList, 0, newIdList, 0, idList.length);
		
		idList = newIdList;
	}
	
	
	/**
	 * If the list is unsorted, this method will re-order the contents.
	 */
	private void ensureListIsSorted() {
		if (!sorted) {
			List<Integer> tmpList;
			int newIdOffset;
			
			tmpList = new ArrayList<Integer>(idOffset);
			
			for (int i = 0; i < idOffset; i++) {
				tmpList.add(Integer.valueOf(idList[i]));
			}
			
			Collections.sort(tmpList);
			
			newIdOffset = 0;
			for (int i = 0; i < idOffset; i++) {
				int nextValue;
				
				nextValue = tmpList.get(i).intValue();
				
				if (newIdOffset <= 0 || nextValue > idList[newIdOffset - 1]) {
					idList[newIdOffset++] = nextValue;
				}
			}
			idOffset = newIdOffset;
			
			sorted = true;
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void set(long id) {
		int integerId;
		
		integerId = LongAsInt.longToInt(id);
		
		// Increase the id list size if it is full.
		if (idOffset >= idList.length) {
			extendIdList();
		}
		
		idList[idOffset++] = integerId;
		
		// If ids are added out of order, the list will have to be sorted before
		// it can be searched using a binary search algorithm.
		if (integerId < maxIdAdded) {
			sorted = false;
		} else {
			maxIdAdded = integerId;
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public boolean get(long id) {
		int integerId;
		int intervalBegin;
		int intervalEnd;
		boolean idFound;
		
		integerId = LongAsInt.longToInt(id);
		
		// If the list is not sorted, it must be sorted prior to a search being
		// performed.
		ensureListIsSorted();
		
		// Perform a binary search splitting the list in half each time until
		// the requested id is confirmed as existing or not.
		intervalBegin = 0;
		intervalEnd = idOffset;
		idFound = false;
		for (boolean searchComplete = false; !searchComplete;) {
			int intervalSize;
			
			// Calculate the interval size.
			intervalSize = intervalEnd - intervalBegin;
			
			// Divide and conquer if the size is large, otherwise commence
			// linear search.
			if (intervalSize >= 2) {
				int intervalMid;
				int currentId;
				
				// Split the interval in two.
				intervalMid = intervalSize / 2 + intervalBegin;
				
				// Check whether the midpoint id is above or below the id
				// required.
				currentId = idList[intervalMid];
				if (currentId == integerId) {
					idFound = true;
					searchComplete = true;
				} else if (currentId < integerId) {
					intervalBegin = intervalMid + 1;
				} else {
					intervalEnd = intervalMid;
				}
				
			} else {
				// Iterate through the entire interval.
				for (int currentOffset = intervalBegin; currentOffset < intervalEnd; currentOffset++) {
					int currentId;
					
					// Check if the current offset contains the id required.
					currentId = idList[currentOffset];
					
					if (currentId == integerId) {
						idFound = true;
						break;
					}
				}
				
				searchComplete = true;
			}
		}
		
		return idFound;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<Long> iterator() {
		// If the list is not sorted, it must be sorted prior to data being
		// returned.
		ensureListIsSorted();
		
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
		
		private int iteratorOffset;
		
		
		/**
		 * Creates a new instance.
		 */
		public IdIterator() {
			iteratorOffset = 0;
		}
		
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean hasNext() {
			return (iteratorOffset < idOffset);
		}
		
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public Long next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			
			return (long) idList[iteratorOffset++];
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
