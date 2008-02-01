// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.store;



/**
 * Implementations of this interface are used by Storeable classes to load their
 * state.
 * 
 * @author Brett Henderson
 */
public interface StoreReader {
	
	/**
	 * Reads a boolean from storage.
	 * 
	 * @return The loaded value.
	 */
	public boolean readBoolean();
	
	
	/**
	 * Reads a byte from storage.
	 * 
	 * @return The loaded value.
	 */
	public byte readByte();
	
	
	/**
	 * Reads a character from storage.
	 * 
	 * @return The loaded value.
	 */
	public char readCharacter();
	
	
	/**
	 * Reads an integer from storage.
	 * 
	 * @return The loaded value.
	 */
	public int readInteger();
	
	
	/**
	 * Reads a long from storage.
	 * 
	 * @return The loaded value.
	 */
	public long readLong();
	
	
	/**
	 * Reads a double from storage.
	 * 
	 * @return The loaded value.
	 */
	public double readDouble();
	
	
	/**
	 * Reads a String from storage.
	 * 
	 * @return The loaded value.
	 */
	public String readString();
}
