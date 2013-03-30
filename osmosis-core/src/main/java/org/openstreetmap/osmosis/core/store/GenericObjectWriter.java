// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.store;


/**
 * Provides functionality to serialise a Storeable implementation to a store.
 * This implementation supports the storing of any Storeable object.
 * 
 * @author Brett Henderson
 */
public class GenericObjectWriter extends BaseObjectWriter {
	
	/**
	 * Creates a new instance.
	 * 
	 * @param storeWriter
	 *            The store writer to write all serialised data to.
	 * @param storeClassRegister
	 *            The register for class to identifier mappings.
	 */
	public GenericObjectWriter(StoreWriter storeWriter, StoreClassRegister storeClassRegister) {
		super(storeWriter, storeClassRegister);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void writeClassIdentifier(StoreWriter sw, StoreClassRegister scr, Class<?> clazz) {
		scr.storeIdentifierForClass(sw, clazz);
	}
}
