// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.bdb.common;

import com.bretth.osmosis.core.store.StoreWriter;
import com.sleepycat.bind.tuple.TupleOutput;


/**
 * Allows persisted output to be written to a TupleOutput implementation.
 * 
 * @author Brett Henderson
 */
/* package */ class TupleOutputStoreWriter implements StoreWriter {
	
	/**
	 * The destination to write the data to. This is public because it is
	 * updated every time an object is written.
	 */
	public TupleOutput output;
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeBoolean(boolean value) {
		output.writeBoolean(value);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeByte(byte value) {
		output.writeByte(value);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeCharacter(char value) {
		output.writeChar(value);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeDouble(double value) {
		output.writeDouble(value);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeInteger(int value) {
		output.writeInt(value);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeLong(long value) {
		output.writeLong(value);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeString(String value) {
		output.writeString(value);
	}
}
