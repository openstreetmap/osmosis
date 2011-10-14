// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.store;


/**
 * This interface defines the methods supporting custom serialisation. This
 * custom serialisation provides performance improvements over default java
 * serialisation at the expense of having to be supported explicitly by classes.
 * 
 * @author Brett Henderson
 */
public interface Storeable {
	/**
	 * Stores all state to the specified store writer.
	 * 
	 * @param writer
	 *            The writer that persists data to an underlying store.
	 * @param storeClassRegister
	 *            Maintains the mapping between classes and their identifiers
	 *            within the store.
	 */
	void store(StoreWriter writer, StoreClassRegister storeClassRegister);
}
