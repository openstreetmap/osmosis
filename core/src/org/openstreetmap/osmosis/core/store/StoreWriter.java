// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.store;


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
	void writeBoolean(boolean value);
	
	
	/**
	 * Writes a byte to storage.
	 * 
	 * @param value
	 *            The value to be written.
	 */
	void writeByte(byte value);
	
	
	/**
	 * Writes a character to storage.
	 * 
	 * @param value
	 *            The value to be written.
	 */
	void writeCharacter(char value);
	
	
	/**
	 * Writes an integer to storage.
	 * 
	 * @param value
	 *            The value to be written.
	 */
	void writeInteger(int value);
	
	
	/**
	 * Writes a long to storage.
	 * 
	 * @param value
	 *            The value to be written.
	 */
	void writeLong(long value);
	
	
	/**
	 * Writes a double to storage.
	 * 
	 * @param value
	 *            The value to be written.
	 */
	void writeDouble(double value);
	
	
	/**
	 * Writes a String to storage.
	 * 
	 * @param value
	 *            The value to be written.
	 */
	void writeString(String value);
}
