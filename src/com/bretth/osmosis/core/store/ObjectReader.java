package com.bretth.osmosis.core.store;


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
