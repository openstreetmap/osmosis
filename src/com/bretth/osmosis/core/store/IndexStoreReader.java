package com.bretth.osmosis.core.store;

import java.util.Comparator;

import com.bretth.osmosis.core.OsmosisRuntimeException;


/**
 * Provides read-only access to an index store. Each thread accessing the object
 * store must create its own reader. The reader maintains all references to
 * heavyweight resources such as file handles used to access the store
 * eliminating the need for objects such as object iterators to be cleaned up
 * explicitly.
 * 
 * @param <K>
 *            The index key type.
 * @param <T>
 *            The object type being stored.
 * @author Brett Henderson
 */
public class IndexStoreReader<K, T extends IndexElement<K>> implements Releasable {
	private RandomAccessObjectStoreReader<T> indexStoreReader;
	private Comparator<K> ordering;
	private long elementCount;
	private long elementSize;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param indexStoreReader
	 *            Provides access to the index data.
	 * @param ordering
	 *            A comparator that sorts index elements desired index key
	 *            ordering.
	 * @param elementCount
	 *            The number of elements in the index.
	 * @param elementSize
	 *            The size of each element within the index.
	 */
	public IndexStoreReader(RandomAccessObjectStoreReader<T> indexStoreReader, Comparator<K> ordering, long elementCount, long elementSize) {
		this.indexStoreReader = indexStoreReader;
		this.ordering = ordering;
		this.elementCount = elementCount;
		this.elementSize = elementSize;
	}
	
	
	/**
	 * Returns the index element identified by id.
	 * 
	 * @param key
	 *            The identifier for the index element to be retrieved.
	 * @return The requested object.
	 */
	public T get(K key) {
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
				K currentKey;
				int comparison;
				
				// Split the interval in two.
				intervalMid = intervalSize / 2 + intervalBegin;
				
				// Check whether the midpoint id is above or below the id
				// required.
				element = indexStoreReader.get(intervalMid * elementSize);
				currentKey = element.getKey();
				
				// Compare the current key for equality with the desired key.
				comparison = ordering.compare(currentKey, key);
				
				if (comparison == 0) {
					intervalBegin = intervalMid;
					offsetFound = true;
				} else if (comparison < 0) {
					intervalBegin = intervalMid + 1;
				} else {
					intervalEnd = intervalMid;
				}
				
			} else {
				// Iterate through the entire interval.
				for (long currentOffset = intervalBegin; currentOffset < intervalEnd; currentOffset++) {
					K currentKey;
					
					// Check if the current offset contains the key required.
					element = indexStoreReader.get(currentOffset * elementSize);
					currentKey = element.getKey();
					
					if (ordering.compare(currentKey, key) == 0) {
						intervalBegin = currentOffset;
						offsetFound = true;
						break;
					}
				}
				
				if (!offsetFound) {
					throw new OsmosisRuntimeException("Requested key " + key + " does not exist.");
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
