// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.sort.common;

import org.openstreetmap.osmosis.core.store.StoreClassRegister;
import org.openstreetmap.osmosis.core.store.StoreReader;
import org.openstreetmap.osmosis.core.store.StoreWriter;
import org.openstreetmap.osmosis.core.store.Storeable;


/**
 * A simple storeable class for testing {@link FileBasedSort}.
 */
public class SampleStoreable implements Storeable {

	private int value;


	/**
	 * Constructs a new instance.
	 * 
	 * @param value
	 *            See {@link #getValue()}.
	 */
	public SampleStoreable(int value) {
		this.value = value;
	}


	/**
	 * Creates a new instance.
	 * 
	 * @param sr
	 *            The store to read state from.
	 * @param scr
	 *            Maintains the mapping between classes and their identifiers
	 *            within the store.
	 */
	public SampleStoreable(StoreReader sr, StoreClassRegister scr) {
		this.value = sr.readInteger();
	}


	@Override
	public void store(StoreWriter writer, StoreClassRegister storeClassRegister) {
		writer.writeInteger(value);
	}


	/**
	 * The value used for sorting.
	 * 
	 * @return The sort value.
	 */
	public int getValue() {
		return value;
	}
}
