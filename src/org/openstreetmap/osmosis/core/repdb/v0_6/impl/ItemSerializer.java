// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.repdb.v0_6.impl;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import org.openstreetmap.osmosis.core.container.v0_6.ChangeContainer;
import org.openstreetmap.osmosis.core.store.DataOutputStoreWriter;
import org.openstreetmap.osmosis.core.store.StoreClassRegister;
import org.openstreetmap.osmosis.core.store.StoreWriter;


/**
 * Provides serialisation of change items.
 */
public class ItemSerializer {
	private ByteArrayOutputStream buffer;
	private StoreWriter storeWriter;
	private StoreClassRegister storeClassRegister;
	
	
	/**
	 * Creates a new instance.
	 */
	public ItemSerializer() {
		buffer = new ByteArrayOutputStream();
		storeWriter = new DataOutputStoreWriter(new DataOutputStream(buffer));
		storeClassRegister = new ChangeContainerStoreClassRegister();
	}
	
	
	/**
	 * Serialises a change container into byte form.
	 * 
	 * @param change
	 *            The change to be serialized.
	 * @return The raw byte data.
	 */
	public byte[] serialize(ChangeContainer change) {
		byte[] data;
		
		change.store(storeWriter, storeClassRegister);
		
		data = buffer.toByteArray();
		buffer.reset();
		
		return data;
	}
}
