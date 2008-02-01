// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.store;


/**
 * Implementations of this interface are used by Storeable classes to persist
 * their state.
 * 
 * @author Brett Henderson
 */
public interface StoreWriter {
	/**
	 * Writes a boolean to storage.
	 * 
	 * @param value
	 *            The value to be written.
	 */
	public void writeBoolean(boolean value);
	
	
	/**
	 * Writes a byte to storage.
	 * 
	 * @param value
	 *            The value to be written.
	 */
	public void writeByte(byte value);
	
	
	/**
	 * Writes a character to storage.
	 * 
	 * @param value
	 *            The value to be written.
	 */
	public void writeCharacter(char value);
	
	
	/**
	 * Writes an integer to storage.
	 * 
	 * @param value
	 *            The value to be written.
	 */
	public void writeInteger(int value);
	
	
	/**
	 * Writes a long to storage.
	 * 
	 * @param value
	 *            The value to be written.
	 */
	public void writeLong(long value);
	
	
	/**
	 * Writes a double to storage.
	 * 
	 * @param value
	 *            The value to be written.
	 */
	public void writeDouble(double value);
	
	
	/**
	 * Writes a String to storage.
	 * 
	 * @param value
	 *            The value to be written.
	 */
	public void writeString(String value);
}
