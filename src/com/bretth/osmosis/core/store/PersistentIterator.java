package com.bretth.osmosis.core.store;

import java.util.NoSuchElementException;



/**
 * Wraps an underlying entity reader implementation so that all data is read
 * from the underlying reader and written to a temporary file before being
 * returned. This allows the underlying database connection to be closed sooner
 * than otherwise possible. This object will own the wrapped source iterator and
 * release it along with all other resources.
 * 
 * @author Brett Henderson
 * @param <T>
 *            The type of entity to retrieved.
 */
public class PersistentIterator<T> implements ReleasableIterator<T>{
	
	private ReleasableIterator<T> sourceIterator;
	private SimpleObjectStore<T> store;
	private ReleasableIterator<T> storeIterator;
	private boolean initialized;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param sourceIterator
	 *            The source of data.
	 * @param storageFilePrefix
	 *            The prefix of the storage file.
	 * @param useCompression
	 *            If true, the storage file will be compressed.
	 */
	public PersistentIterator(ReleasableIterator<T> sourceIterator, String storageFilePrefix, boolean useCompression) {
		this.sourceIterator = sourceIterator;
		
		store = new SimpleObjectStore<T>(storageFilePrefix, useCompression);
		
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
