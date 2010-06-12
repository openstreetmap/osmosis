// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.store;


/**
 * Provides functionality common to all object writer implementations.
 * 
 * @author Brett Henderson
 */
public abstract class BaseObjectWriter implements ObjectWriter {
	
	private StoreWriter storeWriter;
	private StoreClassRegister storeClassRegister;
	private StoreableConstructorCache constructorCache;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param storeWriter
	 *            The store writer to write all serialised data to.
	 * @param storeClassRegister
	 *            The register for class to identifier mappings.
	 */
	protected BaseObjectWriter(StoreWriter storeWriter, StoreClassRegister storeClassRegister) {
		this.storeWriter = storeWriter;
		this.storeClassRegister = storeClassRegister;
		
		constructorCache = new StoreableConstructorCache();
	}
	
	
	/**
	 * Writes the class identifier to the underlying data stream to allow it to
	 * be identified when reading in again.
	 * 
	 * @param sw
	 *            The store writer to write all serialised data to.
	 * @param scr
	 *            The register for class to identifier mappings.
	 * @param clazz
	 *            The class to be written.
	 */
	protected abstract void writeClassIdentifier(StoreWriter sw, StoreClassRegister scr, Class<?> clazz);
	
	
	/**
	 * Writes an object to storage in a way that allows its type to be
	 * automatically determined when read back in.
	 * 
	 * @param value
	 *            The object to be written.
	 */
	public void writeObject(Storeable value) {
		Class<?> clazz;
		
		clazz = value.getClass();
		
		// Verify that the class has the appropriate constructor for de-serialization.
		constructorCache.getStoreableConstructor(clazz);
		
		writeClassIdentifier(storeWriter, storeClassRegister, clazz);
		value.store(storeWriter, storeClassRegister);
	}
}
