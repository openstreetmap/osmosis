// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.store;



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
	boolean readBoolean();
	
	
	/**
	 * Reads a byte from storage.
	 * 
	 * @return The loaded value.
	 */
	byte readByte();
	
	
	/**
	 * Reads a character from storage.
	 * 
	 * @return The loaded value.
	 */
	char readCharacter();
	
	
	/**
	 * Reads an integer from storage.
	 * 
	 * @return The loaded value.
	 */
	int readInteger();
	
	
	/**
	 * Reads a long from storage.
	 * 
	 * @return The loaded value.
	 */
	long readLong();
	
	
	/**
	 * Reads a double from storage.
	 * 
	 * @return The loaded value.
	 */
	double readDouble();
	
	
	/**
	 * Reads a String from storage.
	 * 
	 * @return The loaded value.
	 */
	String readString();
}
