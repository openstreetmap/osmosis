// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.store;

import java.io.DataOutput;
import java.io.IOException;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;


/**
 * Allows persisted output to be written to a DataOutput implementation.
 * 
 * @author Brett Henderson
 */
public class DataOutputStoreWriter implements StoreWriter {
	private DataOutput output;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param output
	 *            The destination to write the data to.
	 */
	public DataOutputStoreWriter(DataOutput output) {
		this.output = output;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void writeBoolean(boolean value) {
		try {
			output.writeBoolean(value);
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to write boolean " + value + " to the store.", e);
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void writeByte(byte value) {
		try {
			output.writeByte(value);
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to write byte " + value + " to the store.", e);
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeCharacter(char value) {
		try {
			output.writeChar(value);
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to write character " + ((int) value) + " to the store.", e);
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void writeInteger(int value) {
		try {
			output.writeInt(value);
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to write integer " + value + " to the store.", e);
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void writeLong(long value) {
		try {
			output.writeLong(value);
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to write long " + value + " to the store.", e);
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void writeDouble(double value) {
		try {
			output.writeDouble(value);
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to write double " + value + " to the store.", e);
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void writeString(String value) {
		try {
			output.writeUTF(value);
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to write String (" + value + ") to the store.", e);
		}
	}
}
