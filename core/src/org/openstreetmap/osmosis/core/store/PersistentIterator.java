// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.store;

import java.util.NoSuchElementException;

import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;


/**
 * Wraps an underlying iterator implementation so that all data is read from the
 * underlying reader and written to a temporary file before being returned. This
 * allows underlying resources such as database connections to be closed sooner
 * than otherwise possible. This object will own the wrapped source iterator and
 * release it along with all other resources.
 * 
 * @author Brett Henderson
 * @param <T>
 *            The type of entity to retrieved.
 */
public class PersistentIterator<T extends Storeable> implements ReleasableIterator<T> {
	
	private ReleasableIterator<T> sourceIterator;
	private SimpleObjectStore<T> store;
	private ReleasableIterator<T> storeIterator;
	private boolean initialized;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param serializationFactory
	 *            The factory defining the object serialisation implementation.
	 * @param sourceIterator
	 *            The source of data.
	 * @param storageFilePrefix
	 *            The prefix of the storage file.
	 * @param useCompression
	 *            If true, the storage file will be compressed.
	 */
	public PersistentIterator(
			ObjectSerializationFactory serializationFactory,
			ReleasableIterator<T> sourceIterator,
			String storageFilePrefix,
			boolean useCompression) {
		this.sourceIterator = sourceIterator;
		
		store = new SimpleObjectStore<T>(serializationFactory, storageFilePrefix, useCompression);
		
		initialized = false;
	}
	
	
	private void initialize() {
		if (!initialized) {
			while (sourceIterator.hasNext()) {
				store.add(sourceIterator.next());
			}
			sourceIterator.release();
			sourceIterator = null;
			
			storeIterator = store.iterate();
			
			initialized = true;
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public boolean hasNext() {
		initialize();
		
		return storeIterator.hasNext();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public T next() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
		
		return storeIterator.next();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void release() {
		if (storeIterator != null) {
			storeIterator.release();
			storeIterator = null;
		}
		
		store.release();
		
		if (sourceIterator != null) {
			sourceIterator.release();
			sourceIterator = null;
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
