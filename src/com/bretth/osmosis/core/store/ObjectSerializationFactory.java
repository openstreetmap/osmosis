package com.bretth.osmosis.core.store;


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
	public ObjectReader createObjectReader(StoreReader storeReader, StoreClassRegister storeClassRegister);
	
	
	/**
	 * Creates a new object writer.
	 * 
	 * @param storeWriter
	 *            The store writer to write all serialised data to.
	 * @param storeClassRegister
	 *            The register for class to identifier mappings.
	 * @return The newly created object writer.
	 */
	public ObjectWriter createObjectWriter(StoreWriter storeWriter, StoreClassRegister storeClassRegister);
}
