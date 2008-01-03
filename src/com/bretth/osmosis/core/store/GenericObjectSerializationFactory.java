// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.store;


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
