package com.bretth.osm.conduit.sort.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * Allows a large number of objects to be sorted by writing them all to disk
 * then sorting using a merge sort algorithm.
 * 
 * @param <DataType>
 *            The object type to be sorted.
 * @author Brett Henderson
 */
public class FileBasedSort<DataType> implements Releasable {
	/**
	 * The maximum number of elements to perform memory-based sorting on,
	 * amounts larger than this will be sorted by merging files.
	 */
	private static final int MAX_MEMORY_SORT_COUNT = 10000;
	
	/**
	 * The maximum number of files to merge at a single time.
	 */
	private static final int MAX_FILE_SORT_COUNT = 50;
	
	
	private Comparator<DataType> comparator;
	private ChunkedObjectStore<DataType> indexedElementStore;
	private List<DataType> addBuffer;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param comparator
	 *            The comparator to be used for sorting the results.
	 */
	public FileBasedSort(Comparator<DataType> comparator) {
		this.comparator = comparator;
		
		indexedElementStore = new ChunkedObjectStore<DataType>("emta", "idx");
		addBuffer = new ArrayList<DataType>(MAX_MEMORY_SORT_COUNT);
	}
	
	
	/**
	 * Sorts the data currently in the add buffer, writes it to the object
	 * store, and clears the buffer.
	 */
	private void flushAddBuffer() {
		if (addBuffer.size() >= 0) {
			// Sort the chunk prior to writing.
			Collections.sort(addBuffer, comparator);
			
			// Write all elements in the buffer to element storage.
			for (DataType element : addBuffer) {
				indexedElementStore.add(element);
			}
			
			addBuffer.clear();
			
			// Close the chunk in the underlying data store so that it can be
			// read separately.
			indexedElementStore.closeChunk();
		}
	}
	
	
	/**
	 * Adds a new object to be sorted.
	 * 
	 * @param value
	 *            The data object.
	 */
	public void add(DataType value) {
		// Add the new data element to the add buffer.
		addBuffer.add(value);
		
		// If the add buffer is full, it must be sorted and written to element
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
	private ReleasableIterator<DataType> iteratePersisted(int nestLevel, long beginChunkIndex, long chunkCount) {
		ReleasableIterator<DataType> sourceIterator = null;
		ObjectStore<DataType> objectStorage = null;
		ReleasableIterator<DataType> storageIterator = null;
		
		try {
			ReleasableIterator<DataType> resultIterator;
			
			// Open the underlying source iterator.
			sourceIterator = iterate(nestLevel, beginChunkIndex, chunkCount);
			
			// Create a persistent object store.
			objectStorage = new ObjectStore<DataType>("emtb");
			
			// Write the source data to disk.
			while (sourceIterator.hasNext()) {
				objectStorage.add(sourceIterator.next());
			}
			
			// Open a new iterator on the persisted data.
			storageIterator = objectStorage.iterate();
			
			// Create a result iterator which will close the object storage when
			// the iterator is released.
			resultIterator = new StoreReleasingIterator<DataType>(storageIterator, objectStorage);
			
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
	private ReleasableIterator<DataType> iterate(int nestLevel, long beginChunkIndex, long chunkCount) {
		List<ReleasableIterator<DataType>> sources;
		
		sources = new ArrayList<ReleasableIterator<DataType>>();
		
		try {
			MergingIterator<DataType> mergingIterator;
			
			// If we are down to a small number of elements, we retrieve each source from file.
			// Otherwise we recurse and split the number of elements down into smaller chunks.
			if (chunkCount <= MAX_FILE_SORT_COUNT) {
				for (int i = 0; i < chunkCount; i++) {
					sources.add(
						indexedElementStore.iterate(beginChunkIndex + i)
					);
				}
				
			} else {
				long maxChunkIndex;
				long subChunkCount;
				
				// The current chunk count must be divided by
				// MAX_FILE_SORT_COUNT and we must recurse for each
				// of those sub chunk counts.
				subChunkCount = chunkCount / MAX_FILE_SORT_COUNT;
				
				// If the sub chunk count is now less than MAX_FILE_SORT_COUNT,
				// increase it to that value to minimise the number of files
				// created.
				if (subChunkCount < MAX_FILE_SORT_COUNT) {
					subChunkCount = MAX_FILE_SORT_COUNT;
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
					
					// Call the persistent version of this function which will
					// break the chunks down to the next level and write the
					// results before returning.
					sources.add(
						iteratePersisted(nestLevel + 1, subFirstChunk, subChunkCount)
					);
				}
			}
			
			// Create a merging iterator to merge all of the sources.
			mergingIterator = new MergingIterator<DataType>(sources, comparator);
			
			// The merging iterator owns the sources now, so we clear our copy
			// of them to prevent them being released on method exit.
			sources.clear();
			
			return mergingIterator;
			
		} finally {
			for (ReleasableIterator<DataType> source : sources) {
				source.release();
			}
		}
	}
	
	
	/**
	 * Sorts and returns the contents of the sorter.
	 * 
	 * @return An iterator providing access to the sorted elements.
	 */
	public ReleasableIterator<DataType> iterate() {
		flushAddBuffer();
		
		return iterate(0, 0, indexedElementStore.getChunkCount());
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void release() {
		indexedElementStore.release();
	}
}
