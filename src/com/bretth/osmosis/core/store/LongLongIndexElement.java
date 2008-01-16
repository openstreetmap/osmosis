// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.store;

import com.bretth.osmosis.core.util.LongAsInt;



/**
 * A single index element for a long-long index.
 * 
 * @author Brett Henderson
 */
public class LongLongIndexElement implements IndexElement<Long> {
	
	/**
	 * The value identifier.
	 */
	private int id;
	
	/**
	 * The data value.
	 */
	private int value;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param id
	 *            The value identifier.
	 * @param value
	 *            The data value.
	 */
	public LongLongIndexElement(long id, long value) {
		this.id = LongAsInt.longToInt(id);
		this.value = LongAsInt.longToInt(value);
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
	public LongLongIndexElement(StoreReader sr, StoreClassRegister scr) {
		this(sr.readInteger(), sr.readInteger());
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void store(StoreWriter writer, StoreClassRegister storeClassRegister) {
		writer.writeInteger(id);
		writer.writeInteger(value);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Long getKey() {
		return (long) id;
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
