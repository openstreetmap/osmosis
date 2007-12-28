package com.bretth.osmosis.core.store;

import com.bretth.osmosis.core.OsmosisRuntimeException;


/**
 * Provides read-only access to an index store. Each thread accessing the object
 * store must create its own reader. The reader maintains all references to
 * heavyweight resources such as file handles used to access the store
 * eliminating the need for objects such as object iterators to be cleaned up
 * explicitly.
 * 
 * @param <T>
 *            The object type being stored.
 * @author Brett Henderson
 */
public class IndexStoreReader<T extends IndexElement> implements Releasable {
	private RandomAccessObjectStoreReader<T> indexStoreReader;
	private long elementCount;
	private long elementSize;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param indexStoreReader
	 *            Provides access to the index data.
	 * @param elementCount
	 *            The number of elements in the index.
	 * @param elementSize
	 *            The size of each element within the index.
	 */
	public IndexStoreReader(RandomAccessObjectStoreReader<T> indexStoreReader, long elementCount, long elementSize) {
		this.indexStoreReader = indexStoreReader;
		this.elementCount = elementCount;
		this.elementSize = elementSize;
	}
	
	
	/**
	 * Returns the index element identified by id.
	 * 
	 * @param id
	 *            The identifier for the index element to be retrieved.
	 * @return The requested object.
	 */
	public T get(long id) {
		long intervalBegin;
		long intervalEnd;
		T element = null;
		
		// Perform a binary search within the index.
		intervalBegin = 0;
		intervalEnd = elementCount;
		for (boolean offsetFound = false; !offsetFound; ) {
			long intervalSize;
			
			// Calculate the interval size.
			intervalSize = intervalEnd - intervalBegin;
			
			// Divide and conquer if the size is large, otherwise commence
			// linear search.
			if (intervalSize >= 2) {
				long intervalMid;
				long currentId;
				
				// Split the interval in two.
				intervalMid = intervalSize / 2 + intervalBegin;
				
				// Check whether the midpoint id is above or below the id
				// required.
				element = indexStoreReader.get(intervalMid * elementSize);
				currentId = element.getIndexId();
				
				if (currentId == id) {
					intervalBegin = intervalMid;
					offsetFound = true;
				} else if (currentId < id) {
					intervalBegin = intervalMid + 1;
				} else {
					intervalEnd = intervalMid;
				}
				
			} else {
				// Iterate through the entire interval.
				for (long currentOffset = intervalBegin; currentOffset < intervalEnd; currentOffset++) {
					long currentId;
					
					// Check if the current offset contains the id required.
					element = indexStoreReader.get(currentOffset * elementSize);
					currentId = element.getIndexId();
					
					if (currentId == id) {
						intervalBegin = currentOffset;
						offsetFound = true;
						break;
					}
				}
				
				if (!offsetFound) {
					throw new OsmosisRuntimeException("Requested id " + id + " does not exist.");
				}
			}
		}
		
		return element;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void release() {
		indexStoreReader.release();
	}
}
