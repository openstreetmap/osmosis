package com.bretth.osmosis.core.filter.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.bretth.osmosis.core.OsmosisRuntimeException;


/**
 * Implements the IdTracker interface using a list of ids. The current
 * implementation only supports 31 bit numbers, but will be enhanced if and when
 * required.
 * 
 * @author Brett Henderson
 */
public class ListIdTracker implements IdTracker {
	private List<Integer> idList;
	private boolean sorted;
	
	
	/**
	 * Creates a new instance.
	 */
	public ListIdTracker() {
		idList = new ArrayList<Integer>();
		sorted = false;
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
		if (value < Integer.MIN_VALUE) {
			throw new OsmosisRuntimeException("Requested value " + value + " exceeds the minimum supported size of " + Integer.MIN_VALUE + ".");
		}
		
		return (int) value;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void set(long id) {
		int integerId;
		
		integerId = longToInt(id);
		
		idList.add(Integer.valueOf(integerId));
		
		sorted = false;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public boolean get(long id) {
		int integerId;
		int intervalBegin;
		int intervalEnd;
		boolean idFound;
		
		integerId = longToInt(id);
		
		if (!sorted) {
			Collections.sort(idList);
			
			sorted = true;
		}
		
		// Perform a binary search splitting the list in half each time until
		// the requested id is confirmed as existing or not.
		intervalBegin = 0;
		intervalEnd = idList.size();
		idFound = false;
		for (boolean searchComplete = false; !searchComplete; ) {
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
				currentId = idList.get(intervalMid).intValue();
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
					currentId = idList.get(currentOffset).intValue();
					
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
}
