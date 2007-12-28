package com.bretth.osmosis.core.index;

import com.bretth.osmosis.core.store.StoreClassRegister;
import com.bretth.osmosis.core.store.StoreReader;
import com.bretth.osmosis.core.store.StoreWriter;


/**
 * A single index element for a int-long index.
 * 
 * @author Brett Henderson
 */
public class IntLongElement implements IndexElement {
	
	/**
	 * The value identifier.
	 */
	private int id;
	
	/**
	 * The data value.
	 */
	private long value;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param id
	 *            The value identifier.
	 * @param value
	 *            The data value.
	 */
	public IntLongElement(int id, long value) {
		this.id = id;
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
	public IntLongElement(StoreReader sr, StoreClassRegister scr) {
		this(sr.readInteger(), sr.readLong());
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void store(StoreWriter writer, StoreClassRegister storeClassRegister) {
		writer.writeInteger(id);
		writer.writeLong(value);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getId() {
		return id;
	}
	
	
	/**
	 * Returns the value of this index element.
	 * 
	 * @return The index value.
	 */
	public long getValue() {
		return value;
	}
}
