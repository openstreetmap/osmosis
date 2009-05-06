// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.store;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;


/**
 * Provides object writing functionality where only a single class type will be
 * stored.
 * 
 * @author Brett Henderson
 */
public class SingleClassObjectWriter extends BaseObjectWriter {
	
	private Class<?> storeableType;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param storeWriter
	 *            The store writer to write all serialised data to.
	 * @param storeClassRegister
	 *            The register for class to identifier mappings.
	 * @param storeableType
	 *            The type of class to be stored.
	 */
	protected SingleClassObjectWriter(
			StoreWriter storeWriter, StoreClassRegister storeClassRegister, Class<?> storeableType) {
		super(storeWriter, storeClassRegister);
		
		this.storeableType = storeableType;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void writeClassIdentifier(StoreWriter sw, StoreClassRegister scr, Class<?> clazz) {
		// We don't need to write anything, we just need to verify that the
		// class is of the correct type.
		if (!storeableType.equals(clazz)) {
			throw new OsmosisRuntimeException(
					"Received class " + clazz.getName() + ", expected class " + storeableType.getName() + ".");
		}
	}
}
