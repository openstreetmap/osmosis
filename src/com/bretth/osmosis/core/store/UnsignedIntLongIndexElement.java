package com.bretth.osmosis.core.store;



/**
 * A single index element for a int-long index where the int is to be treated unsigned.
 * 
 * @author Brett Henderson
 */
public class UnsignedIntLongIndexElement implements IndexElement {
	
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
	public UnsignedIntLongIndexElement(int id, long value) {
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
	public UnsignedIntLongIndexElement(StoreReader sr, StoreClassRegister scr) {
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
	public long getIndexId() {
		// This will cause the sign of the identifier to be ignored resulting in
		// unsigned ordering of index values.
		return id & 0xFFFFFFFFl;
	}
	
	
	/**
	 * Returns the id of this index element.
	 * 
	 * @return The index id.
	 */
	public int getId() {
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
