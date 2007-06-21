package com.bretth.osmosis.sort.impl;

import com.bretth.osmosis.OsmosisRuntimeException;


/**
 * Provides an iterator which returns no data. This is returned by iterator
 * methods that have no data to be returned.
 * 
 * @author Brett Henderson
 * @param <DataType>
 *            The type of data to be returned by the iterator.
 */
public class EmptyIterator<DataType> implements ReleasableIterator<DataType> {
	
	/**
	 * {@inheritDoc}
	 */
	public boolean hasNext() {
		// No data can be returned.
		return false;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public DataType next() {
		throw new OsmosisRuntimeException("This iterator contains no data.");
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
		// Nothing to do.
	}
}
