package com.bretth.osmosis.core.store;

import java.io.DataOutputStream;
import java.io.IOException;

import com.bretth.osmosis.core.OsmosisRuntimeException;


/**
 * This class is used by Storeable classes to persist their state.
 * 
 * @author Brett Henderson
 */
public class StoreWriter {
	private DataOutputStream outputStream;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param outputStream
	 *            The stream to write the data to.
	 * @param storeClassRegister
	 *            The register maintaining the stored class type to id mapping.
	 */
	public StoreWriter(DataOutputStream outputStream) {
		this.outputStream = outputStream;
	}
	
	
	/**
	 * Writes a boolean to storage.
	 * 
	 * @param value
	 *            The value to be written.
	 */
	public void writeBoolean(boolean value) {
		try {
			outputStream.writeBoolean(value);
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to write boolean " + value + " to the store.", e);
		}
	}
	
	
	/**
	 * Writes a byte to storage.
	 * 
	 * @param value
	 *            The value to be written.
	 */
	public void writeByte(byte value) {
		try {
			outputStream.writeByte(value);
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to write byte " + value + " to the store.", e);
		}
	}
	
	
	/**
	 * Writes an integer to storage.
	 * 
	 * @param value
	 *            The value to be written.
	 */
	public void writeInteger(int value) {
		try {
			outputStream.writeInt(value);
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to write integer " + value + " to the store.", e);
		}
	}
	
	
	/**
	 * Writes a long to storage.
	 * 
	 * @param value
	 *            The value to be written.
	 */
	public void writeLong(long value) {
		try {
			outputStream.writeLong(value);
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to write long " + value + " to the store.", e);
		}
	}
	
	
	/**
	 * Writes a double to storage.
	 * 
	 * @param value
	 *            The value to be written.
	 */
	public void writeDouble(double value) {
		try {
			outputStream.writeDouble(value);
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to write double " + value + " to the store.", e);
		}
	}
	
	
	/**
	 * Writes a String to storage.
	 * 
	 * @param value
	 *            The value to be written.
	 */
	public void writeString(String value) {
		try {
			outputStream.writeUTF(value);
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to write String (" + value + ") to the store.", e);
		}
	}
}
