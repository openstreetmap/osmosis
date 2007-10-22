package com.bretth.osmosis.core.store;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;

import com.bretth.osmosis.core.OsmosisRuntimeException;


/**
 * This class is used by Storeable classes to load their state.
 * 
 * @author Brett Henderson
 */
public class StoreReader {
	private DataInputStream inputStream;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param inputStream
	 *            The stream to read data from.
	 */
	public StoreReader(DataInputStream inputStream) {
		this.inputStream = inputStream;
	}
	
	
	/**
	 * Reads a boolean from storage.
	 * 
	 * @return The loaded value.
	 */
	public boolean readBoolean() {
		try {
			return inputStream.readBoolean();
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
			return inputStream.readByte();
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
			return inputStream.readInt();
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
			return inputStream.readLong();
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
			return inputStream.readDouble();
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
			return inputStream.readUTF();
		} catch (EOFException e) {
			throw new EndOfStoreException("End of stream was reached while attempting to read a String from the store.", e);
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to read a String from the store.", e);
		}
	}
}
