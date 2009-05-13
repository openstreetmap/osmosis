// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.repdb.v0_6.impl;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import org.openstreetmap.osmosis.core.container.v0_6.ChangeContainer;
import org.openstreetmap.osmosis.core.store.DataInputStoreReader;
import org.openstreetmap.osmosis.core.store.StoreClassRegister;
import org.openstreetmap.osmosis.core.store.StoreReader;


/**
 * Provides deserialisation of change items.
 */
public class ItemDeserializer {
	private StoreClassRegister storeClassRegister;
	
	
	/**
	 * Creates a new instance.
	 */
	public ItemDeserializer() {
		storeClassRegister = new ChangeContainerStoreClassRegister();
	}
	
	
	/**
	 * De-serializes a change container from byte form.
	 * 
	 * @param data
	 *            The data to be de-serialized.
	 * @return The change container.
	 */
	public ChangeContainer deserialize(byte[] data) {
		ByteArrayInputStream bufferStream;
		StoreReader storeReader;
		ChangeContainer change;
		
		bufferStream = new ByteArrayInputStream(data);
		storeReader = new DataInputStoreReader(new DataInputStream(bufferStream));
		
		change = new ChangeContainer(storeReader, storeClassRegister);
		
		return change;
	}
}
