// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.store;


/**
 * Provides functionality to deserialise a Storeable implementation from a
 * store. This implementation supports the loading of any Storeable object.
 * 
 * @author Brett Henderson
 */
public class GenericObjectReader extends BaseObjectReader {
	
	/**
	 * Creates a new instance.
	 * 
	 * @param storeReader
	 *            The store writer to read all serialised data from.
	 * @param storeClassRegister
	 *            The register for class to identifier mappings.
	 */
	public GenericObjectReader(StoreReader storeReader, StoreClassRegister storeClassRegister) {
		super(storeReader, storeClassRegister);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Class<?> readClassFromIdentifier(StoreReader sr, StoreClassRegister scr) {
		return scr.getClassFromIdentifier(sr);
	}
}
