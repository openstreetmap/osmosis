package com.bretth.osmosis.sort.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * Allows a large number of objects to be sorted by writing them all to disk
 * then sorting using a merge sort algorithm.
 * 
 * @param <T>
 *            The object type to be sorted.
 * @author Brett Henderson
 */
public class FileBasedSort<T> implements Releasable {
	/**
	 * The maximum number of entities to perform memory-based sorting on,
	 * amounts larger than this will be split into chunks of this size, the
	 * chunks sorted in memory before writing to file, and all the results
	 * merged using the merge sort algorithm.
	 */
	private static final int MAX_MEMORY_SORT_COUNT = 16384;
	
	/**
	 * The maximum number of sources to merge together at a single level of the
	 * merge sort hierarchy. Must be 2 or higher. A standard merge sort is 2.
	 */
	private static final int MAX_MERGE_SOURCE_COUNT = 2;
	
	
	/**
	 * The number of levels in the merge sort hierarchy to perform in memory
	 * before persisting to a file. By persisting to file at regular hierarchy
	 * levels, the number of file handles is minimised. File handle count is
	 * likely to be an issue before memory usage due to the small number of
	 * in-flight objects at any point in time.
	 * <p>
	 * The number of file handles will be MAX_MERGE_SOURCE_COUNT raised to the
	 * power of MAX_MEMORY_SORT_DEPTH.
	 */
	private static final int MAX_MEMORY_SORT_DEPTH = 8;
	
	
	private Comparator<T> comparator;
	private ChunkedObjectStore<T> indexedEntityStore;
	private List<T> addBuffer;
	private boolean useCompression;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param comparator
	 *            The comparator to be used for sorting the results.
	 * @param useCompression
	 *            If true, the storage files will be compressed.
	 */
	public FileBasedSort(Comparator<T> comparator, boolean useCompression) {
		this.comparator = comparator;
		this.useCompression = useCompression;
		
		indexedEntityStore = new ChunkedObjectStore<T>("emta", "idx", useCompression);
		addBuffer = new ArrayList<T>(MAX_MEMORY_SORT_COUNT);
	}
	
	
	/**
	 * Sorts the data currently in the add buffer, writes it to the object
	 * store, and clears the buffer.
	 */
	private void flushAddBuffer() {
		if (addBuffer.size() >= 0) {
			// Sort the chunk prior to writing.
			Collections.sort(addBuffer, comparator);
			
			// Write all entities in the buffer to entity storage.
			for (T entity : addBuffer) {
				indexedEntityStore.add(entity);
			}
			
			addBuffer.clear();
			
			// Close the chunk in the underlying data store so that it can be
			// read separately.
			indexedEntityStore.closeChunk();
		}
	}
	
	
	/**
	 * Adds a new object to be sorted.
	 * 
	 * @param value
	 *            The data object.
	 */
	public void add(T value) {
		// Add the new data entity to the add buffer.
		addBuffer.add(value);
		
		// If the add buffer is full, it must be sorted and written to entity
		// storage.
		if (addBuffer.size() >= MAX_MEMORY_SORT_COUNT) {
			flushAddBuffer();
		}
	}
	
	
	/**
	 * This is a wrapper method around the iterate method with the same argument
	 * list that persists the sort results prior to returning. This forces all
	 * sorting by nested recursive method calls to be performed allowing all
	 * associated memory can be freed.
	 * 
	 * @param nestLevel
	 *            The current recursive nesting level of the merge sort
	 *            operation.
	 * @param beginChunkIndex
	 *            The initial chunk to begin sorting from.
	 * @param chunkCount
	 *            The number of chunks to sort.
	 * @return An iterator providing access to the sort result.
	 */
	@SuppressWarnings("null")
	private ReleasableIterator<T> iteratePersisted(int nestLevel, long beginChunkIndex, long chunkCount) {
		ReleasableIterator<T> sourceIterator = null;
		ObjectStore<T> objectStorage = null;
		ReleasableIterator<T> storageIterator = null;
		
		try {
			ReleasableIterator<T> resultIterator;
			
			// Open the underlying source iterator.
			sourceIterator = iterate(nestLevel, beginChunkIndex, chunkCount);
			
			// Create a persistent object store.
			objectStorage = new ObjectStore<T>("emtb", useCompression);
			
			// Write the source data to disk.
			while (sourceIterator.hasNext()) {
				objectStorage.add(sourceIterator.next());
			}
			
			// Release the source iterator.
			sourceIterator.release();
			sourceIterator = null;
			
			// Open a new iterator on the persisted data.
			storageIterator = objectStorage.iterate();
			
			// Create a result iterator which will close the object storage when
			// the iterator is released.
			resultIterator = new StoreReleasingIterator<T>(storageIterator, objectStorage);
			
			// Clear the references to the object storage and storage iterator
			// so they won't be released on method exit.
			objectStorage = null; 
			storageIterator = null;
			
			return resultIterator;
			
		} finally {
			if (sourceIterator != null) {
				sourceIterator.release();
			}
			if (objectStorage != null) {
				objectStorage.release();
			}
			if (storageIterator != null) {
				storageIterator.release();
			}
		}
	}
	
	
	/**
	 * Sorts the specified sub-section of the overall storage contents. This
	 * result list is not backed by a file and should be persisted prior to
	 * being incorporated into a higher level merge operation.
	 * 
	 * @param nestLevel
	 *            The current recursive nesting level of the merge sort
	 *            operation.
	 * @param beginChunkIndex
	 *            The initial chunk to begin sorting from.
	 * @param chunkCount
	 *            The number of chunks to sort.
	 * @return An iterator providing access to the sort result.
	 */
	private ReleasableIterator<T> iterate(int nestLevel, long beginChunkIndex, long chunkCount) {
		List<ReleasableIterator<T>> sources;
		
		sources = new ArrayList<ReleasableIterator<T>>();
		
		try {
			MergingIterator<T> mergingIterator;
			
			// If we are down to a small number of entities, we retrieve each source from file.
			// Otherwise we recurse and split the number of entities down into smaller chunks.
			if (chunkCount <= MAX_MERGE_SOURCE_COUNT) {
				for (int i = 0; i < chunkCount; i++) {
					sources.add(
						indexedEntityStore.iterate(beginChunkIndex + i)
					);
				}
				
			} else {
				long maxChunkIndex;
				long subChunkCount;
				
				// The current chunk count must be divided by
				// MAX_FILE_SORT_COUNT and we must recurse for each
				// of those sub chunk counts.
				subChunkCount = chunkCount / MAX_MERGE_SOURCE_COUNT;
				
				// If the sub chunk count is now less than MAX_FILE_SORT_COUNT,
				// increase it to that value to minimise the number of files
				// created.
				if (subChunkCount < MAX_MERGE_SOURCE_COUNT) {
					subChunkCount = MAX_MERGE_SOURCE_COUNT;
				}
				
				// We can never pass beyond the chunk boundaries specified for
				// this function.
				maxChunkIndex = beginChunkIndex + chunkCount;
				
				for (long subFirstChunk = beginChunkIndex; subFirstChunk < maxChunkIndex; subFirstChunk += subChunkCount) {
					// The chunk count passed to the nested function should not
					// make the nested function exceed this function's boundaries.
					if (subFirstChunk + subChunkCount > maxChunkIndex) {
						subChunkCount = maxChunkIndex - subFirstChunk;
					}
					
					// Either call the persistent or standard version of the recursive iterate based on whether this nesting level requires persistence.
					if (((nestLevel + 1) % MAX_MEMORY_SORT_DEPTH) == 0) {
						sources.add(
							iteratePersisted(nestLevel + 1, subFirstChunk, subChunkCount)
						);
					} else {
						sources.add(
							iterate(nestLevel + 1, subFirstChunk, subChunkCount)
						);
					}
				}
			}
			
			// Create a merging iterator to merge all of the sources.
			mergingIterator = new MergingIterator<T>(sources, comparator);
			
			// The merging iterator owns the sources now, so we clear our copy
			// of them to prevent them being released on method exit.
			sources.clear();
			
			return mergingIterator;
			
		} finally {
			for (ReleasableIterator<T> source : sources) {
				source.release();
			}
		}
	}
	
	
	/**
	 * Sorts and returns the contents of the sorter.
	 * 
	 * @return An iterator providing access to the sorted entities.
	 */
	public ReleasableIterator<T> iterate() {
		flushAddBuffer();
		
		return iterate(0, 0, indexedEntityStore.getChunkCount());
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void release() {
		indexedEntityStore.release();
	}
}
