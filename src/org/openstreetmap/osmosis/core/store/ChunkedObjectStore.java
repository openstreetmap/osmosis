// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.store;

import org.openstreetmap.osmosis.core.lifecycle.Completable;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;


/**
 * Adds indexed chunking capabilities to a basic object store allowing groups of
 * objects to be written and retrieved later by their chunk index. The number of
 * objects and the size of the index is limited only by disk space.
 * <p>
 * This store is only suitable for single-threaded use because it does not
 * provide per-thread readers.
 * 
 * @param <T>
 *            The class type to be stored.
 * @author Brett Henderson
 */
public class ChunkedObjectStore<T extends Storeable> implements Completable {
	/**
	 * Stores all the objects written to this store.
	 */
	private SegmentedObjectStore<T> objectStore;
	
	/**
	 * Maintains both the file positions of each chunk and the number of objects
	 * within each chunk. The file position is written when a new chunk is
	 * started, and the object count is written when a chunk is completed.
	 */
	private IndexStore<Long, LongLongIndexElement> indexStore;
	private IndexStoreReader<Long, LongLongIndexElement> indexStoreReader;
	private long chunkCount;
	private boolean chunkInProgress;
	private long newChunkFilePosition;
	private long chunkObjectCount;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param serializationFactory
	 *            The factory defining the object serialisation implementation.
	 * @param storageFilePrefix
	 *            The prefix of the storage file name.
	 * @param indexFilePrefix
	 *            The prefix of the index file name.
	 * @param useCompression
	 *            If true, the storage file will be compressed.
	 */
	public ChunkedObjectStore(
			ObjectSerializationFactory serializationFactory,
			String storageFilePrefix,
			String indexFilePrefix,
			boolean useCompression) {
		objectStore = new SegmentedObjectStore<T>(serializationFactory, storageFilePrefix, useCompression);
		
		indexStore = new IndexStore<Long, LongLongIndexElement>(
			LongLongIndexElement.class,
			new ComparableComparator<Long>(),
			indexFilePrefix
		);
		
		chunkCount = 0;
		chunkInProgress = false;
		newChunkFilePosition = 0;
		chunkObjectCount = 0;
	}
	
	
	/**
	 * Adds the specified object to the store.
	 * 
	 * @param data
	 *            The object to be added.
	 */
	public void add(T data) {
		objectStore.add(data);
		chunkObjectCount++;
		
		if (!chunkInProgress) {
			// Write the file index of the new chunk.
			indexStore.write(new LongLongIndexElement((chunkCount * 2), newChunkFilePosition));
			
			chunkInProgress = true;
		}
	}
	
	
	/**
	 * Stops the current writing operation and begins a new one saving the
	 * current position against a new interval index.
	 */
	public void closeChunk() {
		// We can only close a chunk if one is in progress.
		if (chunkInProgress) {
			// Create an interval in the underlying object store and note the
			// current position.
			newChunkFilePosition = objectStore.closeChunk();
			
			// Flag that no chunk is in progress so that the next add will store
			// the start file index.
			chunkInProgress = false;
			
			// Write then reset the object count of the current chunk.
			indexStore.write(new LongLongIndexElement((chunkCount * 2) + 1, chunkObjectCount));
			chunkObjectCount = 0;
			
			// Increment the chunk count.
			chunkCount++;
		}
	}
	
	
	/**
	 * Returns the number of chunks managed by this store. This count will
	 * include the in progress chunk if one exists.
	 * 
	 * @return The number of chunks.
	 */
	public long getChunkCount() {
		// If a chunk is in progress, the chunk count won't have been updated yet.
		if (chunkInProgress) {
			return chunkCount + 1;
		} else {
			return chunkCount;
		}
	}
	
	
	/**
	 * Provides access to the contents of this store.
	 * 
	 * @param chunk
	 *            The chunk to read objects from.
	 * @return An iterator providing access to contents of the store.
	 */
	public ReleasableIterator<T> iterate(long chunk) {
		complete();
		
		if (indexStoreReader == null) {
			indexStoreReader = indexStore.createReader();
		}
		
		// Retrieve the file position and number of objects for the specified
		// chunk and iterate.
		return objectStore.iterate(
			indexStoreReader.get(chunk * 2).getValue(),
			indexStoreReader.get(chunk * 2 + 1).getValue()
		);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void complete() {
		// Any outstanding chunks must be closed before we can complete.
		closeChunk();
		
		indexStore.complete();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void release() {
		objectStore.release();
		if (indexStoreReader != null) {
			indexStoreReader.release();
			indexStoreReader = null;
		}
		indexStore.release();
	}
}
