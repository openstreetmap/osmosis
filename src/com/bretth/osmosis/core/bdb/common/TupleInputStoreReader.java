// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.bdb.common;

import com.bretth.osmosis.core.store.StoreReader;
import com.sleepycat.bind.tuple.TupleInput;


/**
 * Allows persisted output to be read from a TupleInput implementation.
 * 
 * @author Brett Henderson
 */
public class TupleInputStoreReader implements StoreReader {
	
	/**
	 * The destination to read the data from. This is public because it is
	 * updated every time an object is read.
	 */
	public TupleInput input;
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean readBoolean() {
		return input.readBoolean();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public byte readByte() {
		return input.readByte();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public double readDouble() {
		return input.readDouble();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int readInteger() {
		return input.readInt();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public long readLong() {
		return input.readLong();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String readString() {
		return input.readString();
	}
}
