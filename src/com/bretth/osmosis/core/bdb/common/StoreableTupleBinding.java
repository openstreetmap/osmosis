// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.bdb.common;

import com.bretth.osmosis.core.store.Storeable;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;


/**
 * Provides a Berkeley DB tuple binding for all storeable class implementations.
 * 
 * @param <T>
 *            The object type to be stored.
 * @author Brett Henderson
 */
public class StoreableTupleBinding<T extends Storeable> extends TupleBinding {
	
	private TupleOutputStoreWriter writer;
	
	
	/**
	 * Creates a new instance.
	 */
	public StoreableTupleBinding() {
		writer = new TupleOutputStoreWriter();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public T entryToObject(TupleInput input) {
		throw new UnsupportedOperationException("This must be fixed ...");
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void objectToEntry(Object object, TupleOutput output) {
		writer.output = output;
		
		((Storeable) object).store(writer, null);
	}
}
