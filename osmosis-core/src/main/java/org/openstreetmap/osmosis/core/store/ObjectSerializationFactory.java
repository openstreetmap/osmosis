// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.store;


/**
 * Implementations of this factory provide methods for creating object readers
 * and writers for storing and loading objects from data stores.
 * 
 * @author Brett Henderson
 */
public interface ObjectSerializationFactory {

	/**
	 * Creates a new object reader.
	 * 
	 * @param storeReader
	 *            The store writer to write all serialised data to.
	 * @param storeClassRegister
	 *            The register for class to identifier mappings.
	 * @return The newly created object reader.
	 */
	ObjectReader createObjectReader(StoreReader storeReader, StoreClassRegister storeClassRegister);
	
	
	/**
	 * Creates a new object writer.
	 * 
	 * @param storeWriter
	 *            The store writer to write all serialised data to.
	 * @param storeClassRegister
	 *            The register for class to identifier mappings.
	 * @return The newly created object writer.
	 */
	ObjectWriter createObjectWriter(StoreWriter storeWriter, StoreClassRegister storeClassRegister);
}
