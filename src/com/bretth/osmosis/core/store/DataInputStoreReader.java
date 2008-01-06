// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.store;

import java.io.DataInput;
import java.io.EOFException;
import java.io.IOException;

import com.bretth.osmosis.core.OsmosisRuntimeException;


/**
 * Allows persisted input to be read from a DataInput implementation.
 * 
 * @author Brett Henderson
 */
public class DataInputStoreReader implements StoreReader {
	private DataInput input;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param input
	 *            The data input to read data from.
	 */
	public DataInputStoreReader(DataInput input) {
		this.input = input;
	}
	
	
	/**
	 * Reads a boolean from storage.
	 * 
	 * @return The loaded value.
	 */
	public boolean readBoolean() {
		try {
			return input.readBoolean();
		} catch (EOFException e) {
			throw new EndOfStoreException("End of stream was reached while attempting to read a boolean from the store.", e);
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to read a boolean from the store.", e);
		}
	}
	
	
	/**
	 * Reads a byte from storage.
	 * 
	 * @return The loaded value.
	 */
	public byte readByte() {
		try {
			return input.readByte();
		} catch (EOFException e) {
			throw new EndOfStoreException("End of stream was reached while attempting to read a byte from the store.", e);
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to read a byte from the store.", e);
		}
	}
	
	
	/**
	 * Reads an integer from storage.
	 * 
	 * @return The loaded value.
	 */
	public int readInteger() {
		try {
			return input.readInt();
		} catch (EOFException e) {
			throw new EndOfStoreException("End of stream was reached while attempting to read an integer from the store.", e);
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to read an integer from the store.", e);
		}
	}
	
	
	/**
	 * Reads a long from storage.
	 * 
	 * @return The loaded value.
	 */
	public long readLong() {
		try {
			return input.readLong();
		} catch (EOFException e) {
			throw new EndOfStoreException("End of stream was reached while attempting to read a long from the store.", e);
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to read a long from the store.", e);
		}
	}
	
	
	/**
	 * Reads a double from storage.
	 * 
	 * @return The loaded value.
	 */
	public double readDouble() {
		try {
			return input.readDouble();
		} catch (EOFException e) {
			throw new EndOfStoreException("End of stream was reached while attempting to read a double from the store.", e);
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to read a double from the store.", e);
		}
	}
	
	
	/**
	 * Reads a String from storage.
	 * 
	 * @return The loaded value.
	 */
	public String readString() {
		try {
			return input.readUTF();
		} catch (EOFException e) {
			throw new EndOfStoreException("End of stream was reached while attempting to read a String from the store.", e);
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to read a String from the store.", e);
		}
	}
}
