package com.bretth.osm.conduit.sort.impl;


/**
 * Adds indexing capabilities to a basic object store allowing objects to be
 * retrieved by their index. The number of objects and the size of the index is
 * limited only by disk space.
 * 
 * @param <DataType>
 *            The object type to be sorted.
 * @author Brett Henderson
 */
public class IndexedObjectStore<DataType> implements Releasable {
	private ObjectStore<DataType> objectStore;
	private IndexStore indexStore;
	private long storedObjectCount;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param storageFilePrefix
	 *            The prefix of the storage file name.
	 * @param indexFilePrefix
	 *            The prefix of the index file name.
	 */
	public IndexedObjectStore(String storageFilePrefix, String indexFilePrefix) {
		objectStore = new ObjectStore<DataType>(storageFilePrefix);
		indexStore = new IndexStore(indexFilePrefix);
		
		storedObjectCount = 0;
	}
	
	
	/**
	 * The number of objects contained in this store.
	 * 
	 * @return The number of objects.
	 */
	public long getStoredObjectCount() {
		return storedObjectCount;
	}
	
	
	/**
	 * Adds the specified object to the store.
	 * 
	 * @param data
	 *            The object to be added.
	 */
	public void add(DataType data) {
		indexStore.write(storedObjectCount++, objectStore.getFileSize());
		objectStore.add(data);
	}
	
	
	/**
	 * Provides access to the contents of this store.
	 * 
	 * @param beginObjectIndex
	 *            The object to begin reading from.
	 * @param objectCount
	 *            The number of objects to be read.
	 * @return An iterator providing access to contents of the store.
	 */
	public ReleasableIterator<DataType> iterate(long beginObjectIndex, long objectCount) {
		return objectStore.iterate(
			indexStore.read(beginObjectIndex),
			objectCount
		);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void release() {
		objectStore.release();
		indexStore.release();
	}
}
