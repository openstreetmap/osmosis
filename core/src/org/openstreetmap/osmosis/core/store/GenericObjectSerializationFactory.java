// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.store;


/**
 * An object reader and writer factory providing generic object serialisation
 * capabilities capable of storing and loading any Storeable class
 * implementations.
 * 
 * @author Brett Henderson
 */
public class GenericObjectSerializationFactory implements ObjectSerializationFactory {
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public ObjectReader createObjectReader(StoreReader storeReader, StoreClassRegister storeClassRegister) {
		return new GenericObjectReader(storeReader, storeClassRegister);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public ObjectWriter createObjectWriter(StoreWriter storeWriter, StoreClassRegister storeClassRegister) {
		return new GenericObjectWriter(storeWriter, storeClassRegister);
	}
}
