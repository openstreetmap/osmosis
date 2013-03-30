// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.store;


/**
 * Provides object reading functionality where only a single class type will be
 * loaded.
 * 
 * @author Brett Henderson
 */
public class SingleClassObjectReader extends BaseObjectReader {
	
	private Class<?> storeableType;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param storeReader
	 *            The store writer to read all serialised data from.
	 * @param storeClassRegister
	 *            The register for class to identifier mappings.
	 * @param storeableType
	 *            The type of class to be stored.
	 */
	protected SingleClassObjectReader(
			StoreReader storeReader, StoreClassRegister storeClassRegister, Class<?> storeableType) {
		super(storeReader, storeClassRegister);
		
		this.storeableType = storeableType;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Class<?> readClassFromIdentifier(StoreReader sr, StoreClassRegister scr) {
		// This implementation only stores a single type so no data read is
		// required.
		return storeableType;
	}
}
