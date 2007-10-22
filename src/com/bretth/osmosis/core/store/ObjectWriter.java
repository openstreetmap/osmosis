package com.bretth.osmosis.core.store;


/**
 * Provides functionality to serialise a Storeable implementation to a store.
 * 
 * @author Brett Henderson
 */
public class ObjectWriter {
	private StoreWriter storeWriter;
	private StoreClassRegister storeClassRegister;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param storeWriter
	 *            The store writer to write all serialised data to.
	 * @param storeClassRegister
	 *            The register for class to identifier mappings.
	 */
	public ObjectWriter(StoreWriter storeWriter, StoreClassRegister storeClassRegister) {
		this.storeWriter = storeWriter;
		this.storeClassRegister = storeClassRegister;
	}
	
	
	/**
	 * Writes an object to storage in a way that allows its type to be
	 * automatically determined when read back in.
	 * 
	 * @param value
	 *            The object to be written.
	 */
	public void writeObject(Storeable value) {
		storeWriter.writeByte(
			storeClassRegister.getIdentifierForClass(value.getClass())
		);
		
		value.store(storeWriter, storeClassRegister);
	}
}
