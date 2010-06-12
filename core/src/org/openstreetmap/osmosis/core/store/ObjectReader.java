// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.store;


/**
 * Implementations provide functionality to deserialise a Storeable
 * implementation from a store.
 * 
 * @author Brett Henderson
 */
public interface ObjectReader {
	
	/**
	 * Reads an object from storage.
	 * 
	 * @return The re-instantiated object.
	 */
	Storeable readObject();
}
