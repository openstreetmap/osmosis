package com.bretth.osmosis.core.store;

import com.bretth.osmosis.core.OsmosisRuntimeException;


/**
 * Provides a store for objects that can be located by a long identifier. This
 * implementation requires that input object identifiers are sorted and always
 * increase.
 * 
 * @param <T>
 *            The object type to be stored.
 * @author Brett Henderson
 */
public class IndexedObjectStore<T extends Storeable> implements Releasable {
	private IndexStore activeIdIndex;
	private IndexStore storeOffsetIndex;
	private RandomAccessObjectStore<T> objectStore;
	private int objectCount;
	private long previousId;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param serializationFactory
	 *            The factory defining the object serialisation implementation.
	 * @param tmpFilePrefix
	 *            The prefix of the storage file.
	 */
	public IndexedObjectStore(ObjectSerializationFactory serializationFactory, String tmpFilePrefix) {
		activeIdIndex = new IndexStore(tmpFilePrefix + "aii");
		storeOffsetIndex = new IndexStore(tmpFilePrefix + "soi");
		objectStore = new RandomAccessObjectStore<T>(serializationFactory, tmpFilePrefix + "osd");
		
		objectCount = 0;
		previousId = Long.MIN_VALUE;
	}
	
	
	/**
	 * Adds the specified object to the store.
	 * 
	 * @param id
	 *            The identifier allowing the object to be retrieved.
	 * @param data
	 *            The object to be added.
	 */
	public void add(long id, T data) {
		long objectOffset;
		
		// Verify that the data is being added with sorted ids.  Ids must always increase and be unique.
		if (id <= previousId) {
			throw new OsmosisRuntimeException(
				"Ids must be unique and sorted in ascending order, new id " + id +
				" is not greater than previous id of " + previousId);
		}
		previousId = id;
		
		// Write the object to the object store.
		objectOffset = objectStore.add(data);
		
		// Write the file offset to the offset index keyed by the current object count.
		storeOffsetIndex.write(objectCount, objectOffset);
		
		// Write the id to the active id index keyed by the current object count.
		activeIdIndex.write(objectCount, id);
		
		// Update the current object count.
		objectCount++;
	}
	
	
	/**
	 * Returns the object identified by id.
	 * 
	 * @param id
	 *            The identifier for the object to be retrieved.
	 * @return The requested object.
	 */
	public T get(long id) {
		int activeIdOffset;
		int intervalBegin;
		int intervalEnd;
		long objectStoreOffset;
		
		// Find the requested id in the active id index. This will give us the
		// offset of the file offset value within the store offset index.
		intervalBegin = 0;
		intervalEnd = objectCount;
		for (boolean offsetFound = false; !offsetFound; ) {
			int intervalSize;
			
			// Calculate the interval size.
			intervalSize = intervalEnd - intervalBegin;
			
			// Divide and conquer if the size is large, otherwise commence
			// linear search.
			if (intervalSize >= 2) {
				int intervalMid;
				long currentId;
				
				// Split the interval in two.
				intervalMid = intervalSize / 2 + intervalBegin;
				
				// Check whether the midpoint id is above or below the id
				// required.
				currentId = activeIdIndex.read(intervalMid);
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
				for (int currentOffset = intervalBegin; currentOffset < intervalEnd; currentOffset++) {
					long currentId;
					
					// Check if the current offset contains the id required.
					currentId = activeIdIndex.read(currentOffset);
					
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
		
		// The offset of the requested id is the beginning of the interval.
		activeIdOffset = intervalBegin;
		
		// The offset of the requested object within the file can now be loaded.
		objectStoreOffset = storeOffsetIndex.read(activeIdOffset);
		
		// The object can now be read from the store.
		return objectStore.get(objectStoreOffset);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void release() {
		activeIdIndex.release();
		storeOffsetIndex.release();
		objectStore.release();
	}
}
