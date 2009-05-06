// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.store;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.openstreetmap.osmosis.core.lifecycle.Releasable;


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
	private boolean elementDetailsInitialized;
	private long elementCount;
	private long elementSize;
	private long binarySearchElementCount;
	private int binarySearchDepth;
	private List<ComparisonElement<K>> binarySearchCache;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param indexStoreReader
	 *            Provides access to the index data.
	 * @param ordering
	 *            A comparator that sorts index elements desired index key
	 *            ordering.
	 */
	public IndexStoreReader(RandomAccessObjectStoreReader<T> indexStoreReader, Comparator<K> ordering) {
		this.indexStoreReader = indexStoreReader;
		this.ordering = ordering;
		
		elementDetailsInitialized = false;
	}
	
	
	/**
	 * Initialises the element count and element size required for performing
	 * binary searches within the index.
	 */
	private void initializeElementDetails() {
		long dataLength;
		
		dataLength = indexStoreReader.length();
		
		if (dataLength <= 0) {
			elementCount = 0;
			elementSize = 0;
		} else {
			indexStoreReader.get(0);
			elementSize = indexStoreReader.position();
			elementCount = dataLength / elementSize;
		}
		
		// Determine how many levels of a binary tree must be traversed to reach a result.
		binarySearchDepth = 0;
		binarySearchElementCount = 1;
		// Must add 1 here because we search from offset -1 to elementCount
		while (binarySearchElementCount < (elementCount + 1)) {
			binarySearchDepth++;
			binarySearchElementCount *= 2;
		}
		
		// Initialise the binary search cache.
		binarySearchCache = new ArrayList<ComparisonElement<K>>(binarySearchDepth);
		for (int i = 0; i < binarySearchDepth; i++) {
			binarySearchCache.add(null);
		}
		
		elementDetailsInitialized = true;
	}
	
	
	/**
	 * Returns the index of the first index element with a key greater than or
	 * equal to the specified key.
	 * 
	 * @param searchKey
	 *            The key to search for.
	 * @return The matching index.
	 */
	private long getKeyIndex(K searchKey) {
		long intervalBegin;
		long intervalEnd;
		int currentSearchDepth;
		boolean useCache;
		boolean higherThanPrevious;
		
		// The element details must be initialised before searching.
		if (!elementDetailsInitialized) {
			initializeElementDetails();
		}
		
		intervalBegin = -1;
		intervalEnd = binarySearchElementCount;
		currentSearchDepth = 0;
		useCache = true;
		higherThanPrevious = true;
		while ((intervalBegin + 1) < intervalEnd) {
			long intervalMid;
			
			// Calculate the mid point of the current interval.
			intervalMid = (intervalBegin + intervalEnd) / 2;
			
			// We can only perform a comparison if the mid point of the current
			// search interval is within the data set.
			if (intervalMid < elementCount) {
				K intervalMidKey = null;
				ComparisonElement<K> searchElement;
				boolean comparisonHigher;
				
				// Attempt to retrieve the key for the mid point from the search
				// cache.
				if (useCache) {
					searchElement = binarySearchCache.get(currentSearchDepth);
					
					if (searchElement != null && searchElement.getIndexOffset() == intervalMid) {
						intervalMidKey = searchElement.getKey();
					} else {
						useCache = false;
					}
				}
				
				// If the value couldn't be retrieved from cache, load it from
				// the underlying dataset.
				if (!useCache) {
					intervalMidKey = indexStoreReader.get(intervalMid * elementSize).getKey();
				}
				
				// Compare the current key for equality with the desired key.
				comparisonHigher = ordering.compare(searchKey, intervalMidKey) > 0;
				
				if (!useCache) {
					binarySearchCache.set(currentSearchDepth, new ComparisonElement<K>(intervalMid, intervalMidKey));
				}
				
				higherThanPrevious = comparisonHigher;
				
			} else {
				higherThanPrevious = false;
			}
			
			// Update binary search attributes based on recent comparison.
			currentSearchDepth++;
			if (higherThanPrevious) {
				intervalBegin = intervalMid;
			} else {
				intervalEnd = intervalMid;
			}
		}
		
		return intervalEnd;
	}
	
	
	/**
	 * Returns the index element identified by id.
	 * 
	 * @param key
	 *            The identifier for the index element to be retrieved.
	 * @return The requested object.
	 */
	public T get(K key) {
		long keyIndex;
		
		// Determine the location of the key within the index.
		keyIndex = getKeyIndex(key);
		
		if (keyIndex < elementCount) {
			T element;
			K locatedKey;
			
			element = indexStoreReader.get(keyIndex * elementSize);
			locatedKey = element.getKey();
			
			if (ordering.compare(key, locatedKey) == 0) {
				return element;
			}
		}
		
		throw new NoSuchIndexElementException("Requested key " + key + " does not exist.");
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
		long keyIndex;
		
		// Determine the location of the begin key within the index.
		keyIndex = getKeyIndex(beginKey);
		
		// Iterate across the range.
		return new IndexRangeIterator<K, T>(
			indexStoreReader.iterate(keyIndex * elementSize),
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
	
	
	/**
	 * Maintains the state associated with a single index search comparison. The
	 * complete path to the previously searched element is maintained using a
	 * list of these elements between searches, improving performance for
	 * searches on keys that are closely located.
	 */
	private static class ComparisonElement<K> {
		private long indexOffset;
		private K key;
		
		
		/**
		 * Creates a new instance.
		 * 
		 * @param indexOffset
		 *            The offset of the current key within the index.
		 * @param key
		 *            The key of the index element compared against.
		 */
		public ComparisonElement(long indexOffset, K key) {
			this.indexOffset = indexOffset;
			this.key = key;
		}
		
		
		/**
		 * Returns the index offset this key is located at.
		 * 
		 * @return The offset of the key within the index.
		 */
		public long getIndexOffset() {
			return indexOffset;
		}
		
		
		/**
		 * Returns the key associated with this comparison.
		 * 
		 * @return The comparison key.
		 */
		public K getKey() {
			return key;
		}
	}
}
