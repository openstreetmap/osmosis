// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.store;

import java.util.Comparator;
import java.util.Iterator;

import com.bretth.osmosis.core.customdb.v0_5.impl.IndexRangeIterator;


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
					throw new NoSuchIndexElementException("Requested key " + key + " does not exist.");
				}
			}
		}
		
		return element;
	}
	
	
	/**
	 * Returns all elements in the range specified by the begin and end keys.
	 * All elements with keys matching and lying between the two keys will be
	 * returned.
	 * 
	 * @param beginKey
	 *            The key marking the beginning of the required index elements.
	 * @param endKey
	 *            The key marking the end of the required index elements. The
	 *            identifier for the index element to be retrieved.
	 * @return An iterator pointing to the requested range.
	 */
	public Iterator<T> getRange(K beginKey, K endKey) {
		long intervalBegin;
		long intervalEnd;
		
		// Perform a binary search within the index for the first element with
		// beginKey.
		intervalBegin = 0;
		intervalEnd = elementCount;
		for (long intervalSize = intervalEnd - intervalBegin; intervalSize > 1; intervalSize = intervalEnd - intervalBegin) {
			long intervalMid;
			T element;
			K currentKey;
			int comparison;
			
			// Split the interval in two.
			intervalMid = intervalSize / 2 + intervalBegin;
			
			// Check whether the midpoint id is above or below the id
			// required.
			element = indexStoreReader.get(intervalMid * elementSize);
			currentKey = element.getKey();
			
			// Compare the current key for equality with the desired key.
			comparison = ordering.compare(currentKey, beginKey);
			
			if (comparison == 0) {
				intervalEnd = intervalMid + 1;
			} else if (comparison < 0) {
				intervalBegin = intervalMid + 1;
			} else {
				intervalEnd = intervalMid;
			}
		}
		
		return new IndexRangeIterator<K, T>(
			indexStoreReader.iterate(intervalBegin * elementSize),
			beginKey,
			endKey,
			ordering
		);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void release() {
		indexStoreReader.release();
	}
}
