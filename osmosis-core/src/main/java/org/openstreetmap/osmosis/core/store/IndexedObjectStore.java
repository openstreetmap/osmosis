// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.store;

import java.io.File;

import org.openstreetmap.osmosis.core.lifecycle.Completable;


/**
 * Provides a store for objects that can be located by a long identifier.
 * 
 * @param <T>
 *            The object type to be stored.
 * @author Brett Henderson
 */
public class IndexedObjectStore<T extends Storeable> implements Completable {
	private RandomAccessObjectStore<T> objectStore;
	private IndexStore<Long, LongLongIndexElement> indexStore;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param serializationFactory
	 *            The factory defining the object serialisation implementation.
	 * @param tmpFilePrefix
	 *            The prefix of the storage file.
	 */
	public IndexedObjectStore(ObjectSerializationFactory serializationFactory, String tmpFilePrefix) {
		objectStore = new RandomAccessObjectStore<T>(serializationFactory, tmpFilePrefix + "d");
		
		indexStore = new IndexStore<Long, LongLongIndexElement>(
			LongLongIndexElement.class,
			new ComparableComparator<Long>(), 
			tmpFilePrefix + "i"
		);
	}
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param serializationFactory
	 *            The factory defining the object serialisation implementation.
	 * @param objectStorageFile
	 *            The storage file to use for objects.
	 * @param indexStorageFile
	 *            The storage file to use for the index.
	 */
	public IndexedObjectStore(
			ObjectSerializationFactory serializationFactory, File objectStorageFile, File indexStorageFile) {
		objectStore = new RandomAccessObjectStore<T>(serializationFactory, objectStorageFile);
		indexStore = new IndexStore<Long, LongLongIndexElement>(
			LongLongIndexElement.class,
			new ComparableComparator<Long>(),
			indexStorageFile
		);
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
		
		// Write the object to the object store.
		objectOffset = objectStore.add(data);
		
		// Write the object offset keyed by the id to the index store.
		indexStore.write(new LongLongIndexElement(id, objectOffset));
	}
	
	
	/**
	 * Creates a new reader capable of accessing the contents of this store. The
	 * reader must be explicitly released when no longer required. Readers must
	 * be released prior to this store.
	 * 
	 * @return A store reader.
	 */
	public IndexedObjectStoreReader<T> createReader() {
		RandomAccessObjectStoreReader<T> objectStoreReader = null;
		
		objectStoreReader = objectStore.createReader();
		
		try {
			IndexStoreReader<Long, LongLongIndexElement> indexStoreReader;
			IndexedObjectStoreReader<T> reader;
			
			indexStoreReader = indexStore.createReader();
			
			reader = new IndexedObjectStoreReader<T>(objectStoreReader, indexStoreReader);
			
			objectStoreReader = null;
			
			return reader;
			
		} finally {
			if (objectStoreReader != null) {
				objectStoreReader.release();
			}
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void complete() {
		objectStore.complete();
		indexStore.complete();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void release() {
		objectStore.release();
		indexStore.release();
	}
}
