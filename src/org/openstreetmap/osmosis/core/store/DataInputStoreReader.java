// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.store;

import java.io.DataInput;
import java.io.EOFException;
import java.io.IOException;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;


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
	 * {@inheritDoc}
	 */
	@Override
	public boolean readBoolean() {
		try {
			return input.readBoolean();
		} catch (EOFException e) {
			throw new EndOfStoreException(
					"End of stream was reached while attempting to read a boolean from the store.", e);
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to read a boolean from the store.", e);
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public byte readByte() {
		try {
			return input.readByte();
		} catch (EOFException e) {
			throw new EndOfStoreException(
					"End of stream was reached while attempting to read a byte from the store.", e);
		} catch (IOException e) {
			throw new OsmosisRuntimeException(
					"Unable to read a byte from the store.", e);
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public char readCharacter() {
		try {
			return input.readChar();
		} catch (EOFException e) {
			throw new EndOfStoreException(
					"End of stream was reached while attempting to read a character from the store.", e);
		} catch (IOException e) {
			throw new OsmosisRuntimeException(
					"Unable to read a character from the store.", e);
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int readInteger() {
		try {
			return input.readInt();
		} catch (EOFException e) {
			throw new EndOfStoreException(
					"End of stream was reached while attempting to read an integer from the store.", e);
		} catch (IOException e) {
			throw new OsmosisRuntimeException(
					"Unable to read an integer from the store.", e);
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public long readLong() {
		try {
			return input.readLong();
		} catch (EOFException e) {
			throw new EndOfStoreException(
					"End of stream was reached while attempting to read a long from the store.", e);
		} catch (IOException e) {
			throw new OsmosisRuntimeException(
					"Unable to read a long from the store.", e);
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public double readDouble() {
		try {
			return input.readDouble();
		} catch (EOFException e) {
			throw new EndOfStoreException(
					"End of stream was reached while attempting to read a double from the store.", e);
		} catch (IOException e) {
			throw new OsmosisRuntimeException(
					"Unable to read a double from the store.", e);
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String readString() {
		try {
			return input.readUTF();
		} catch (EOFException e) {
			throw new EndOfStoreException(
					"End of stream was reached while attempting to read a String from the store.", e);
		} catch (IOException e) {
			throw new OsmosisRuntimeException(
					"Unable to read a String from the store.", e);
		}
	}
}
