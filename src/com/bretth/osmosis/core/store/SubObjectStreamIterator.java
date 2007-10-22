package com.bretth.osmosis.core.store;

import java.io.DataInputStream;


/**
 * This class reads objects from an ObjectInputStream until the end of stream is
 * reached or a maximum number of objects is reached.
 * 
 * @param <T>
 *            The type of data to be returned by the iterator.
 * @author Brett Henderson
 */
public class SubObjectStreamIterator<T extends Storeable> extends ObjectStreamIterator<T> {
	private long maxObjectCount;
	private long objectCount;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param inStream
	 *            The stream to read objects from.
	 * @param maxObjectCount
	 *            The maximum number of objects to read.
	 * @param storeClassRegister
	 *            The register defining the classes in the stream and their
	 *            identifiers.
	 */
	public SubObjectStreamIterator(DataInputStream inStream, StoreClassRegister storeClassRegister, long maxObjectCount) {
		super(inStream, storeClassRegister);
		
		this.maxObjectCount = maxObjectCount;
		
		objectCount = 0;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasNext() {
		if (objectCount >= maxObjectCount) {
			return false;
		}
		
		return super.hasNext();
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public T next() {
		T result;
		
		result = super.next();
		objectCount++;
		
		return result;
	}
}
