package com.bretth.osmosis.core.index;

import com.bretth.osmosis.core.store.StoreReader;
import com.bretth.osmosis.core.store.StoreWriter;


/**
 * Defines methods for storing and loading index elements.
 * 
 * @param <T>
 *            The index element type to be stored.
 * @author Brett Henderson
 */
public interface IndexElementFactory<T> {
	
	/**
	 * Writes the element to the data store.
	 * 
	 * @param sw
	 *            The serialisation store writer.
	 * @param element
	 *            The element to be stored.
	 */
	void storeElement(StoreWriter sw, T element);
	
	
	/**
	 * Reads an element from the data store.
	 * 
	 * @param sr
	 *            The serialisation store reader.
	 * @return The element read from the store.
	 * @throws NoMoreObjectsInStoreException
	 *             when the end of the store is reached.
	 */
	T loadElement(StoreReader sr);
}
