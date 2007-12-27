package com.bretth.osmosis.core.store;

import java.io.DataInputStream;
import java.util.NoSuchElementException;


/**
 * This class reads objects from an ObjectInputStream until the end of stream is
 * reached.
 * 
 * @param <T>
 *            The type of data to be returned by the iterator.
 * @author Brett Henderson
 */
public class ObjectStreamIterator<T extends Storeable> implements ReleasableIterator<T> {
	
	private DataInputStream inStream;
	private GenericObjectReader objectReader;
	private T nextElement;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param inStream
	 *            The stream to read objects from.
	 * @param storeClassRegister
	 *            The register defining the classes in the stream and their
	 *            identifiers.
	 */
	public ObjectStreamIterator(DataInputStream inStream, StoreClassRegister storeClassRegister) {
		this.inStream = inStream;
		
		objectReader = new GenericObjectReader(new StoreReader(inStream), storeClassRegister);
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

	
	/**
	 * {@inheritDoc}
	 */
	public void release() {
		if (inStream != null) {
			try {
				inStream.close();
			} catch (Exception e) {
				// Do nothing.
			}
			
			inStream = null;
		}
	}
}
