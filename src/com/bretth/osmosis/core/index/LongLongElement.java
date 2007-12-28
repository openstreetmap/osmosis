package com.bretth.osmosis.core.index;

import com.bretth.osmosis.core.store.StoreClassRegister;
import com.bretth.osmosis.core.store.StoreReader;
import com.bretth.osmosis.core.store.StoreWriter;


/**
 * A single index element for a long-long index.
 * 
 * @author Brett Henderson
 */
public class LongLongElement implements IndexElement {
	
	/**
	 * The value identifier.
	 */
	private long id;
	
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
	public LongLongElement(long id, long value) {
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
	public LongLongElement(StoreReader sr, StoreClassRegister scr) {
		this(sr.readLong(), sr.readLong());
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void store(StoreWriter writer, StoreClassRegister storeClassRegister) {
		writer.writeLong(id);
		writer.writeLong(value);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getIndexId() {
		return id;
	}
	
	
	/**
	 * Returns the id of this index element.
	 * 
	 * @return The index id.
	 */
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
