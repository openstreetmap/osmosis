// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.sort.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.openstreetmap.osmosis.core.lifecycle.Releasable;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;
import org.openstreetmap.osmosis.core.store.ChunkedObjectStore;
import org.openstreetmap.osmosis.core.store.ObjectSerializationFactory;
import org.openstreetmap.osmosis.core.store.PersistentIterator;
import org.openstreetmap.osmosis.core.store.Storeable;


/**
 * Allows a large number of objects to be sorted by writing them all to disk
 * then sorting using a merge sort algorithm.
 * 
 * @param <T>
 *            The object type to be sorted.
 * @author Brett Henderson
 */
public class FileBasedSort<T extends Storeable> implements Releasable {
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
	

	private ObjectSerializationFactory serializationFactory;
	private Comparator<T> comparator;
	private ChunkedObjectStore<T> chunkedEntityStore;
	private List<T> addBuffer;
	private boolean useCompression;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param serializationFactory
	 *            The factory defining the object serialisation implementation.
	 * @param comparator
	 *            The comparator to be used for sorting the results.
	 * @param useCompression
	 *            If true, the storage files will be compressed.
	 */
	public FileBasedSort(
			ObjectSerializationFactory serializationFactory, Comparator<T> comparator, boolean useCompression) {
		this.serializationFactory = serializationFactory;
		this.comparator = comparator;
		this.useCompression = useCompression;
		
		chunkedEntityStore = new ChunkedObjectStore<T>(serializationFactory, "emta", "idx", useCompression);
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
				chunkedEntityStore.add(entity);
			}
			
			addBuffer.clear();
			
			// Close the chunk in the underlying data store so that it can be
			// read separately.
			chunkedEntityStore.closeChunk();
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
	private ReleasableIterator<T> iteratePersisted(int nestLevel, long beginChunkIndex, long chunkCount) {
		ReleasableIterator<T> persistentIterator;
		
		// Create a persistent iterator based on the requested underlying chunk
		// iterator.
		persistentIterator = new PersistentIterator<T>(
			serializationFactory,
			iterate(nestLevel, beginChunkIndex, chunkCount),
			"emtb",
			useCompression
		);
		
		// Prime the persistent iterator so that all underlying iterator data is
		// written to file.
		try {
			ReleasableIterator<T> result;
			
			result = persistentIterator;
			
			// This will cause all data to be read from the underlying iterator
			// into the persistent store.
			persistentIterator.hasNext();
			
			persistentIterator = null;
			
			return result;
			
		} finally {
			// This will release the persistent iterator and its underlying
			// source iterator if the persistence operations failed.
			if (persistentIterator != null) {
				persistentIterator.release();
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
						chunkedEntityStore.iterate(beginChunkIndex + i)
					);
				}
				
			} else {
				long maxChunkIndex;
				long subChunkCount;
				
				/*
				 * The current chunk count must be divided by
				 * MAX_MERGE_SOURCE_COUNT and we must recurse for each of those
				 * sub chunk counts. Where the result isn't exact, we round up
				 * to the nearest multiple of MAX_MERGE_SOURCE_COUNT to ensure
				 * we don't end up with more than MAX_MERGE_SOURCE_COUNT
				 * sources.
				 */
				subChunkCount = chunkCount / MAX_MERGE_SOURCE_COUNT;
				subChunkCount += chunkCount % MAX_MERGE_SOURCE_COUNT;
				
				// We can never pass beyond the chunk boundaries specified for
				// this function.
				maxChunkIndex = beginChunkIndex + chunkCount;
				
				for (
						long subFirstChunk = beginChunkIndex;
						subFirstChunk < maxChunkIndex;
						subFirstChunk += subChunkCount) {
					
					// The chunk count passed to the nested function should not
					// make the nested function exceed this function's boundaries.
					if (subFirstChunk + subChunkCount > maxChunkIndex) {
						subChunkCount = maxChunkIndex - subFirstChunk;
					}
					
					/*
					 * Either call the persistent or standard version of the
					 * recursive iterate based on whether this nesting level
					 * requires persistence. If we only have one chunk left at
					 * this point we make an exception and skip persistence
					 * because it will only result in a single file being opened
					 * anyway.
					 */
					if (((nestLevel + 1) % MAX_MEMORY_SORT_DEPTH) == 0 && subChunkCount > 1) {
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
		
		return iterate(0, 0, chunkedEntityStore.getChunkCount());
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void release() {
		chunkedEntityStore.release();
	}
}
