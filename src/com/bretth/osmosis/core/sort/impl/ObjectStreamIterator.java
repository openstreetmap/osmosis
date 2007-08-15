package com.bretth.osmosis.core.sort.impl;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.NoSuchElementException;

import com.bretth.osmosis.core.OsmosisRuntimeException;


/**
 * This class reads objects from an ObjectInputStream until the end of stream is
 * reached.
 * 
 * @param <T>
 *            The type of data to be returned by the iterator.
 * @author Brett Henderson
 */
public class ObjectStreamIterator<T> implements ReleasableIterator<T> {
	
	private ObjectInputStream inStream;
	private T nextElement;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param inStream
	 *            The stream to read objects from.
	 */
	public ObjectStreamIterator(ObjectInputStream inStream) {
		this.inStream = inStream;
	}


	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public boolean hasNext() {
		try {
			if (nextElement != null) {
				return true;
			}
			
			try {
				nextElement = (T) inStream.readObject();
			} catch (EOFException e) {
				return false;
			}
			
			return true;
			
		} catch (ClassNotFoundException e) {
			throw new OsmosisRuntimeException("Unable to read object from object stream.", e);
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to read from object stream.", e);
		}
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
