package com.bretth.osm.conduit.sort.impl;

import java.io.ObjectInputStream;


/**
 * This class reads objects from an ObjectInputStream until the end of stream is
 * reached or a maximum number of objects is reached.
 * 
 * @param <DataType>
 *            The type of data to be returned by the iterator.
 * @author Brett Henderson
 */
public class SubObjectStreamIterator<DataType> extends ObjectStreamIterator<DataType> {
	private long maxObjectCount;
	private long objectCount;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param inStream
	 *            The stream to read objects from.
	 * @param maxObjectCount
	 *            The maximum number of objects to read.
	 */
	public SubObjectStreamIterator(ObjectInputStream inStream, long maxObjectCount) {
		super(inStream);
		
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
}
