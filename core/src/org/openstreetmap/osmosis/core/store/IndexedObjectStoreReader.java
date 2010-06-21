// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.store;

import org.openstreetmap.osmosis.core.lifecycle.Releasable;


/**
 * Provides read-only access to an indexed object store. Each thread accessing
 * the object store must create its own reader. The reader maintains all
 * references to heavyweight resources such as file handles used to access the
 * store eliminating the need for objects such as object iterators to be cleaned
 * up explicitly.
 * 
 * @param <T>
 *            The object type being stored.
 * @author Brett Henderson
 */
public class IndexedObjectStoreReader<T> implements Releasable {
	private RandomAccessObjectStoreReader<T> objectStoreReader;
	private IndexStoreReader<Long, LongLongIndexElement> indexStoreReader;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param objectStoreReader
	 *            Provides access to the object data.
	 * @param indexStoreReader
	 *            Provides access to the index data.
	 */
	public IndexedObjectStoreReader(
			RandomAccessObjectStoreReader<T> objectStoreReader,
			IndexStoreReader<Long, LongLongIndexElement> indexStoreReader) {
		this.objectStoreReader = objectStoreReader;
		this.indexStoreReader = indexStoreReader;
	}
	
	
	/**
	 * Returns the object identified by id.
	 * 
	 * @param id
	 *            The identifier for the object to be retrieved.
	 * @return The requested object.
	 */
	public T get(long id) {
		long objectOffset;
		T data;
		
		// Get the object offset from the index store.
		objectOffset = indexStoreReader.get(id).getValue();
		
		// Read the object from the object store.
		data = objectStoreReader.get(objectOffset);
		
		return data;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void release() {
		objectStoreReader.release();
		indexStoreReader.release();
	}
}
