// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.store;

import java.util.Iterator;
import java.util.NoSuchElementException;


/**
 * Provides functionality common to all object iterators.
 * 
 * @param <T>
 *            The type of data to be returned by the iterator.
 * @author Brett Henderson
 */
public class ObjectDataInputIterator<T> implements Iterator<T> {
	
	private ObjectReader objectReader;
	private T nextElement;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param objectReader
	 *            The reader containing the objects to be deserialized.
	 */
	public ObjectDataInputIterator(ObjectReader objectReader) {
		this.objectReader = objectReader;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public boolean hasNext() {
		if (nextElement != null) {
			return true;
		}
		
		try {
			nextElement = (T) objectReader.readObject();
			
		} catch (EndOfStoreException e) {
			return false;
		}
		
		return true;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public T next() {
		if (hasNext()) {
			T result;
			
			result = nextElement;
			nextElement = null;
			
			return result;
			
		} else {
			throw new NoSuchElementException();
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
