// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.store;

/**
 * Implementations provide functionality to serialise a Storeable implementation
 * to a store.
 * 
 * @author Brett Henderson
 */
public interface ObjectWriter {
	
	/**
	 * Writes an object to storage.
	 * 
	 * @param value
	 *            The object to be written.
	 */
	void writeObject(Storeable value);
}
