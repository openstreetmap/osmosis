// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.store;

import com.bretth.osmosis.core.OsmosisRuntimeException;


/**
 * This class allows an underlying store to be released along with the iterator
 * accessing it. As such it acts as an iterator delegating all calls to a
 * provided iterator, but during release it also releases the data store.
 * 
 * @param <DataType>
 *            The type of data to be returned by the iterator.
 * @author Brett Henderson
 */
public class StoreReleasingIterator<DataType> implements ReleasableIterator<DataType> {
	private Releasable store;
	private ReleasableIterator<DataType> iterator;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param iterator
	 *            The underlying iterator to read from.
	 * @param store
	 *            The store to be released after use.
	 */
	public StoreReleasingIterator(ReleasableIterator<DataType> iterator, Releasable store) {
		this.iterator = iterator;
		this.store = store;
	}


	/**
	 * {@inheritDoc}
	 */
	public boolean hasNext() {
		if (iterator == null) {
			throw new OsmosisRuntimeException("Iterator has been released.");
		}
		
		return iterator.hasNext();
	}


	/**
	 * {@inheritDoc}
	 */
	public DataType next() {
		if (iterator == null) {
			throw new OsmosisRuntimeException("Iterator has been released.");
		}
		
		return iterator.next();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void remove() {
		if (iterator == null) {
			throw new OsmosisRuntimeException("Iterator has been released.");
		}
		
		iterator.remove();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void release() {
		if (iterator != null) {
			iterator.release();
			
			iterator = null;
		}
		
		if (store != null) {
			store.release();
			
			store = null;
		}
	}
}
